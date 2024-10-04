package GestoreIOUser;

import Connessione.ClientWriter;
import GestoreIOUser.GestoreUtenti;
import GestoreIOUser.User;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GestoreRegistrazione {

    private final Map<SocketChannel, User> connectedUsers;
    private final ClientWriter clientWriter;
    private final Map<String, Set<SocketChannel>> channels;
    private final Map<String, Map<String, String>> duplicateUsersMap;
    private final Map<String, Integer> tempIdCounters;  // Contatori specifici per ogni nickname

    public GestoreRegistrazione(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter, Map<String, Set<SocketChannel>> channels, Map<String, Map<String, String>> duplicateUsersMap, Map<String, Integer> tempIdCounters) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.channels = channels;
        this.duplicateUsersMap = duplicateUsersMap;
        this.tempIdCounters = tempIdCounters;
    }

    public void handleRegistration(SocketChannel client, JsonObject jsonMessage) throws IOException {
        // Prendi il nick e la password dal jsonMessage
        String nick = jsonMessage.get("nick").getAsString();
        String password = jsonMessage.get("password").getAsString();

        System.out.println("Tentativo di registrazione per utente: " + nick);

        Map<String, User> users = GestoreUtenti.loadUsers();  // Carica gli utenti dal file

        // Trova l'utente corrispondente
        User existingUser = users.values().stream()
                .filter(user -> user.getNick().equals(nick) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        JsonObject response = new JsonObject(); // Oggetto JSON per la risposta

        if (existingUser != null) {
            // Utente esistente con lo stesso nick e password
            System.out.println("Utente già esistente: " + nick);
            response.addProperty("status", "error");
            response.addProperty("message", "User already exists with the same nickname and password.");
            clientWriter.writeToClient(client, response.toString());
            client.close();
        } else {
            // Crea un nuovo utente
            System.out.println("Creazione nuovo utente: " + nick);
            User newUser = new User(nick, password, "active", "user", client);
            assignTempIdIfDuplicate(nick, newUser); // Assegna un ID temporaneo in caso di nickname duplicato
            users.put(newUser.getID(), newUser);
            GestoreUtenti.saveUsers(users);  // Salva il nuovo utente nel file

            connectedUsers.put(client, newUser);
            assignToAvailableChannel(newUser, client); // Assegna l'utente a un canale disponibile

            // Risposta di successo
            response.addProperty("status", "success");
            response.addProperty("message", "Registration successful. Welcome, " + nick);
            clientWriter.writeToClient(client, response.toString());
            System.out.println("Registrazione riuscita per utente: " + nick);
        }
    }

    private void assignToAvailableChannel(User user, SocketChannel client) throws IOException {
        // Ottieni il primo canale creato
        String firstChannel = channels.keySet().iterator().next();

        // Assegna il client al primo canale
        channels.get(firstChannel).add(client);
        user.setChannel(firstChannel);

        // Notifica al client a quale canale è stato assegnato
        JsonObject channelResponse = new JsonObject();
        channelResponse.addProperty("status", "success");
        channelResponse.addProperty("message", "You have been assigned to channel: " + firstChannel);
        clientWriter.writeToClient(client, channelResponse.toString());
    }

    private void assignTempIdIfDuplicate(String nick, User user) {
        if (connectedUsers.values().stream().anyMatch(u -> u.getNick().equals(nick))) {
            // Ottieni o inizializza il contatore per questo nickname
            int currentCounter = tempIdCounters.computeIfAbsent(nick, k -> 1);

            // Assegna il tempId incrementando il contatore
            String tempId = String.format("%05d", currentCounter);
            user.setTempId(tempId);

            // Incrementa il contatore per il prossimo utente con lo stesso nickname
            tempIdCounters.put(nick, currentCounter + 1);

            // Salva il mapping tra l'UUID dell'utente e il tempId
            duplicateUsersMap
                    .computeIfAbsent(nick, k -> new ConcurrentHashMap<>())
                    .put(user.getID(), tempId);
        }
    }
}
