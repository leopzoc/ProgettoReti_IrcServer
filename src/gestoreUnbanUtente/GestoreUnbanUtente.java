package gestoreUnbanUtente;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GestoreUnbanUtente implements IGestoreUnbanUtente{

    private final Map<String, Set<String>> bannedUsersByChannel; // mappa dei bannati per canale
    private final Map<SocketChannel, User> connectedUsers; // utenti connessi
    private final ClientWriter clientWriter; // per inviare risposte ai client

    public GestoreUnbanUtente(Map<String, Set<String>> bannedUsersByChannel, Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter) {
        this.bannedUsersByChannel = bannedUsersByChannel;
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
    }

    // Metodo per gestire l'unban
    @Override
    public void unbanUtente(SocketChannel mittente, String messaggio) {
        try {

            JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();
            String destinatario = jsonMessage.get("message").getAsString();
            String canaleDaSbannare = jsonMessage.get("channel").getAsString();

            // Estrai nickname e tempId dal destinatario
            String[] destinatarioParts = destinatario.split(":");
            String nickDestinatario = destinatarioParts[0];
            String tempId = destinatarioParts.length > 1 ? destinatarioParts[1] : null;

            // Trova l'utente bannato usando il nick e il tempID
            User userDaSbannare = trovaUtente(nickDestinatario, tempId);
            if (userDaSbannare == null) {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("status", "error");
                errorMessage.addProperty("message", "Utente non trovato o non bannato.");
                clientWriter.writeToClient(mittente, errorMessage.toString());
                return;
            }

            // Verifica se l'utente è bannato nel canale specificato
            Set<String> bannedUsersInChannel = bannedUsersByChannel.get(canaleDaSbannare);
            if (bannedUsersInChannel == null || !bannedUsersInChannel.contains(userDaSbannare.getID())) {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("status", "error");
                errorMessage.addProperty("message", "Utente non è stato bannato dal canale.");
                clientWriter.writeToClient(mittente, errorMessage.toString());
                return;
            }

            // Rimuovi l'utente dalla lista dei bannati
            bannedUsersInChannel.remove(userDaSbannare.getID());

            // Se la lista dei bannati per questo canale è vuota, rimuovi la voce dalla mappa
            if (bannedUsersInChannel.isEmpty()) {
                bannedUsersByChannel.remove(canaleDaSbannare);
            }

            // Notifica al mittente che l'utente è stato sbannato con successo
            JsonObject successMessage = new JsonObject();
            successMessage.addProperty("status", "success");
            successMessage.addProperty("message", "Utente sbannato con successo dal canale " + canaleDaSbannare);
            clientWriter.writeToClient(mittente, successMessage.toString());

            // Notifica all'utente sbannato
            JsonObject unbanMessage = new JsonObject();
            unbanMessage.addProperty("status", "unbanned");
            unbanMessage.addProperty("message", "Sei stato sbannato dal canale " + canaleDaSbannare);
            clientWriter.writeToClient(userDaSbannare.getSocketChannel(), unbanMessage.toString());

        } catch (Exception e) {
            System.err.println("Errore durante l'unban dell'utente: " + e.getMessage());
            // Invia un messaggio di errore in formato JSON in caso di eccezione
            JsonObject errorMessage = new JsonObject();
            errorMessage.addProperty("status", "error");
            errorMessage.addProperty("message", "Errore durante l'unban dell'utente.");
            try {
                clientWriter.writeToClient(mittente, errorMessage.toString());
            } catch (IOException ioException) {
                System.err.println("Errore durante l'invio del messaggio di errore: " + ioException.getMessage());
            }
        }
    }

    // Metodo per trovare l'utente bannato
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
}

/*
{
    "command": "unban",
    "message": "leo:00001",
    "channel": "nome_canale"
}
 */



/* dal server


Modifiche principali:
Messaggi di errore in formato JSON:

Se l'utente non viene trovato o non è bannato, viene inviato un messaggio di errore in formato JSON:
json

{
  "status": "error",
  "message": "Utente non trovato o non bannato."
}
Se l'utente non è stato bannato nel canale specificato:
json

{
  "status": "error",
  "message": "Utente non è stato bannato dal canale."
}
Messaggio di successo per l'unban:

Se l'utente viene sbannato con successo, viene inviato un messaggio di conferma in formato JSON al mittente:
json

{
  "status": "success",
  "message": "Utente sbannato con successo dal canale canaleDaSbannare."
}
Notifica all'utente sbannato:

L'utente sbannato riceve una notifica in formato JSON:
json

{
  "status": "unbanned",
  "message": "Sei stato sbannato
 */