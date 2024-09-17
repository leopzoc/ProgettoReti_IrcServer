package GestoreDeiBan;

import GestoreIOUser.User;
import Connessione.ClientWriter;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GestoreBanUtente implements IBanUtenti{

    private final Map<SocketChannel, User> connectedUsers; // Associa SocketChannel a User
    private final ClientWriter clientWriter;
    private final Map<String, Set<SocketChannel>> channels; // Canale -> Lista di socket connessi
    private final Map<String, Set<String>> bannedUsersByChannel; // Canale -> Lista di utenti bannati (per ID)
    private final Map<String, Map<String, String>> duplicateUsersMap; // Mappa nick -> (ID univoco : id temporaneo)

    public GestoreBanUtente(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter,
                            Map<String, Set<SocketChannel>> channels,
                            Map<String, Set<String>> bannedUsersByChannel,
                            Map<String, Map<String, String>> duplicateUsersMap) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.channels = channels;
        this.bannedUsersByChannel = bannedUsersByChannel;
        this.duplicateUsersMap = duplicateUsersMap;
    }

    public void banUtente(SocketChannel mittente, String messaggio) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();
            String destinatario = jsonMessage.get("message").getAsString();
            String canaleDaBannare = jsonMessage.get("channel").getAsString();

            // Ottieni il nome del primo canale (la lobby)
            String lobbyChannel = getLobbyChannel();

            // Non permettere il ban nel canale lobby
            if (canaleDaBannare.equals(lobbyChannel)) {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("status", "error");
                errorMessage.addProperty("message", "Non puoi bannare utenti nella lobby.");
                clientWriter.writeToClient(mittente, errorMessage.toString());
                return;
            }

            // Estrai nickname e tempId dal destinatario
            String[] destinatarioParts = destinatario.split(":");
            String nickDestinatario = destinatarioParts[0];
            String tempId = destinatarioParts.length > 1 ? destinatarioParts[1] : null;

            // Trova l'utente da bannare usando il nick e il tempID
            User userDaBannare = trovaUtente(nickDestinatario, tempId);
            if (userDaBannare == null) {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("status", "error");
                errorMessage.addProperty("message", "Utente non trovato o più utenti con lo stesso nome.");
                clientWriter.writeToClient(mittente, errorMessage.toString());
                return;
            }

            // Aggiungi l'ID dell'utente alla mappa dei bannati per il canale
            bannedUsersByChannel.computeIfAbsent(canaleDaBannare, k -> ConcurrentHashMap.newKeySet())
                    .add(userDaBannare.getID());

            // Rimuovi l'utente dal canale se è connesso
            Set<SocketChannel> utentiNelCanale = channels.get(canaleDaBannare);
            if (utentiNelCanale != null) {
                utentiNelCanale.remove(userDaBannare.getSocketChannel());
            }

            // Sposta l'utente nella lobby
            String lobby = getLobbyChannel();
            channels.get(lobby).add(userDaBannare.getSocketChannel());
            userDaBannare.setChannel(lobby);

            // Notifica l'utente bannato (se connesso) e chi lo ha bannato
            // Notifica l'utente bannato (se connesso) in formato JSON
            JsonObject banMessage = new JsonObject();
            banMessage.addProperty("status", "banned");
            banMessage.addProperty("message", "Sei stato bannato dal canale " + canaleDaBannare + " e sei stato spostato nella lobby.");
            clientWriter.writeToClient(userDaBannare.getSocketChannel(), banMessage.toString());


            // Notifica chi ha eseguito il ban in formato JSON
            JsonObject successMessage = new JsonObject();
            successMessage.addProperty("status", "success");
            successMessage.addProperty("message", "Utente bannato con successo dal canale " + canaleDaBannare);
            clientWriter.writeToClient(mittente, successMessage.toString());
        } catch (Exception e) {
            System.err.println("Errore durante il ban dell'utente: " + e.getMessage());
        }
    }



    // Metodo per trovare l'utente da bannare
    private User trovaUtente(String nick, String tempId) {
        for (User user : connectedUsers.values()) {
            if (tempId == null || tempId.isEmpty()) {
                if (user.getNick().equals(nick)) {
                    return user;
                }
            } else {
                if (user.getNick().equals(nick) && tempId.equals(user.getTempId())) {
                    return user;
                }
            }
        }
        return null;
    }

    // Metodo per ottenere il nome del canale lobby (primo canale nella mappa)
    private String getLobbyChannel() {
        return channels.keySet().iterator().next();
    }
}
/*
{
        "command": "ban",
        "message": "leo:00001",
        "channel": "nome_canale"
        }
        */


/*
Messaggi di ban in formato JSON:

Se l'utente viene bannato, viene inviato un messaggio in formato JSON all'utente bannato:
json

{
  "status": "banned",
  "message": "Sei stato bannato dal canale canaleDaBannare e sei stato spostato nella lobby."
}
Messaggio di successo per chi ha bannato:

Dopo che l'utente è stato bannato, chi ha eseguito il ban riceve un messaggio di conferma in formato JSON:
json

{
  "status": "success",
  "message": "Utente bannato con successo dal canale canaleDaBannare."
}
Messaggi di errore in formato JSON:

Se l'amministratore tenta di bannare qualcuno nella lobby o l'utente non viene trovato, viene inviato un messaggio di errore in formato JSON:
json

{
  "status": "error",
  "message": "Non puoi bannare utenti nella lobby."
}
Se l'utente non viene trovato:
json

{
  "status": "error",
  "message": "Utente non trovato o più utenti con lo stesso nome."
}
Gestione delle eccezioni:

In caso di eccezione, viene inviato un messaggio di errore in formato JSON all'amministratore:
json

{
  "status": "error",
  "message": "Errore durante il ban dell'utente."
}
 */