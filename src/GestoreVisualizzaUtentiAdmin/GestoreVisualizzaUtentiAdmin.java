package GestoreVisualizzaUtentiAdmin;

import Connessione.ClientWriter;
import GestoreIOUser.GestoreUtenti;
import GestoreIOUser.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class GestoreVisualizzaUtentiAdmin {

    private final Map<SocketChannel, User> connectedUsers; // Utenti connessi
    private final ClientWriter clientWriter; // Per inviare risposte al client
    private final String userFilePath; // Percorso del file users.txt

    public GestoreVisualizzaUtentiAdmin(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter, String userFilePath) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.userFilePath = userFilePath;
    }

    public void visualizzaUtenti(SocketChannel mittente) {
        try {
            // Carica tutti gli utenti dal file
            Map<String, User> allUsers = GestoreUtenti.loadUsers();

            JsonArray usersArray = new JsonArray();

            for (User user : allUsers.values()) {
                JsonObject userJson = new JsonObject();
                userJson.addProperty("uuid", user.getID());
                userJson.addProperty("nick", user.getNick());
                userJson.addProperty("status", user.getStatus());

                // Verifica se l'utente è connesso
                boolean isOnline = connectedUsers.values().stream().anyMatch(u -> u.getID().equals(user.getID()));
                userJson.addProperty("connection", isOnline ? "online" : "offline");

                usersArray.add(userJson);
            }

            JsonObject response = new JsonObject();
            response.add("users", usersArray);

            // Invia la lista utenti al client richiedente
            clientWriter.writeToClient(mittente, response.toString());

        } catch (IOException e) {
            System.err.println("Errore durante la visualizzazione degli utenti: " + e.getMessage());

            // Invia un messaggio di errore in formato JSON in caso di eccezione
            JsonObject errorMessage = new JsonObject();
            errorMessage.addProperty("status", "error");
            errorMessage.addProperty("message", "Errore durante la visualizzazione degli utenti.");
            try {
                clientWriter.writeToClient(mittente, errorMessage.toString());
            } catch (IOException ioException) {
                System.err.println("Errore durante l'invio del messaggio di errore: " + ioException.getMessage());
            }
        }
    }
}
/*
messaggio di risposta
"users": [
    {
      "uuid": "user-id",
      "nick": "nickname",
      "status": "active",
      "connection": "online"
    },
    {
      "uuid": "user-id",
      "nick": "nickname",
      "status": "banned",
      "connection": "offline"
    }
  ]
}

Messaggio di errore in formato JSON:

In caso di errore durante la visualizzazione degli utenti, viene inviato un messaggio di errore in formato JSON:
json
{
  "status": "error",
  "message": "Errore durante la visualizzazione degli utenti."
}
 */