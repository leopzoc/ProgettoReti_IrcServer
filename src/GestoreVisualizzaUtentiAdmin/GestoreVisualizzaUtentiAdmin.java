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
        }
    }
}

