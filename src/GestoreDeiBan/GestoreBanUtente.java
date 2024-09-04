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
                clientWriter.writeToClient(mittente, "Non puoi bannare utenti nella lobby.");
                return;
            }

            // Estrai nickname e tempId dal destinatario
            String[] destinatarioParts = destinatario.split(":");
            String nickDestinatario = destinatarioParts[0];
            String tempId = destinatarioParts.length > 1 ? destinatarioParts[1] : null;

            // Trova l'utente da bannare usando il nick e il tempID
            User userDaBannare = trovaUtente(nickDestinatario, tempId);
            if (userDaBannare == null) {
                clientWriter.writeToClient(mittente, "Utente non trovato o più utenti con lo stesso nome.");
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
            clientWriter.writeToClient(userDaBannare.getSocketChannel(), "Sei stato bannato dal canale " + canaleDaBannare + " e sei stato spostato nella lobby.");
            clientWriter.writeToClient(mittente, "Utente bannato con successo dal canale " + canaleDaBannare);

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
