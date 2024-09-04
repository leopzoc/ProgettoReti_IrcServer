package GestoreIOUser;

import Connessione.ClientWriter;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import Connessione.ClientWriter;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GestoreLogin implements IGestoreLogin {

    private final Map<SocketChannel, User> connectedUsers;
    private final ClientWriter clientWriter;
    private final Map<String, Set<SocketChannel>> channels;
    private final Map<String, Map<String, String>> duplicateUsersMap;
    private final Map<String, Integer> tempIdCounters;  // Contatori specifici per ogni nickname


    public GestoreLogin(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter, Map<String, Set<SocketChannel>> channels, Map<String, Map<String, String>> duplicateUsersMap, Map<String, Integer> tempIdCounters) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.channels = channels;
        this.duplicateUsersMap = duplicateUsersMap;
        this.tempIdCounters = tempIdCounters;
    }

    @Override
    public void handleLogin(SocketChannel client, JsonObject jsonMessage) throws IOException {
        String nick = jsonMessage.get("nick").getAsString();
        String password = jsonMessage.get("password").getAsString();

        System.out.println("Tentativo di login per utente: " + nick);

        Map<String, User> users = GestoreUtenti.loadUsers();  // Carica gli utenti dal file
    /*
        User matchingUser = null;
        for (User user : users.values()) {
            if (user.getNick().equals(nick) && user.getPassword().equals(password)) {
                matchingUser = user;
                break;
            }
        }

     */
        // Trova l'utente corrispondente
        User matchingUser = users.values().stream()
                .filter(user -> user.getNick().equals(nick) && user.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (matchingUser != null) {
            System.out.println("Utente trovato: " + nick);

            // Controlla se l'utente è già connesso
            boolean isAlreadyConnected = connectedUsers.values().stream()
                    .anyMatch(user -> user.getID().equals(matchingUser.getID()));

            if (isAlreadyConnected) {
                // Gestisci il caso in cui l'utente è già connesso
                clientWriter.writeToClient(client, "User is already connected.");
                client.close();
                System.out.println("Tentativo di doppia connessione per utente: " + nick);
            } else if (matchingUser.getStatus().equals("banned")) {
                clientWriter.writeToClient(client, "You are banned from this server.");
                client.close();
                System.out.println("Utente bannato: " + nick);
            } else {
                matchingUser.setSocketChannel(client);
                assignTempIdIfDuplicate(nick, matchingUser); // ASSEGNA ID TEMPORANEO
                connectedUsers.put(client, matchingUser);
                assignToAvailableChannel(matchingUser, client); // Assegna al canale disponibile
                clientWriter.writeToClient(client, "Login successful. Welcome, " + nick);
                System.out.println("Login riuscito per utente: " + nick);
            }
        } else {
            System.out.println("Utente non trovato, creando nuovo utente: " + nick);
            User newUser = new User(nick, password, "active", "user", client);
            assignTempIdIfDuplicate(nick, newUser); // ASSEGNA ID TEMPORANEO
            users.put(newUser.getID(), newUser);
            GestoreUtenti.saveUsers(users);  // Salva il nuovo utente nel file

            connectedUsers.put(client, newUser);
            assignToAvailableChannel(newUser, client); // Assegna al canale disponibile
            clientWriter.writeToClient(client, "Registration successful. Welcome, " + nick);
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
        clientWriter.writeToClient(client, "You have been assigned to channel: " + firstChannel);
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

    // Metodo per rimuovere l'ID temporaneo durante la disconnessione
    public void removeTempIdOnDisconnect(User user) {
        String nick = user.getNick();
        String userId = user.getID();

        Map<String, String> tempIdMap = duplicateUsersMap.get(nick);
        if (tempIdMap != null) {
            tempIdMap.remove(userId);

            // Se non ci sono più ID temporanei per questo nick, rimuovi l'intero mapping
            if (tempIdMap.isEmpty()) {
                duplicateUsersMap.remove(nick);
                tempIdCounters.remove(nick);  // Rimuovi anche il contatore se non ci sono più utenti con questo nickname
            }
        }
    }
}



    /*
    Se ci sono due utenti con lo stesso nome, ad esempio leo e leo, entrambi avranno un tempId univoco perché sono gestiti nella stessa mappa relativa a quel nick.

Se ci sono utenti con nomi diversi, ad esempio leo e ale, anche se hanno lo stesso tempId, non ci sarà nessuna interferenza perché sono gestiti in mappe separate (una per ogni nick).

3. Verifica e Gestione dei tempId per Utenti con Nomi Diversi:
La struttura della mappa evita conflitti anche se leo e ale dovessero avere lo stesso tempId. Questo perché le mappe dei tempId sono separate per ciascun nick. Ecco come funziona:

duplicateUsersMap è una mappa che ha come chiavi i nomi degli utenti (nick), e come valori delle altre mappe (Map<String, String>). Queste mappe interne contengono gli ID reali come chiave e i tempId come valore.

Quindi, se leo ha tempId 00001 e ale ha lo stesso tempId, non ci saranno conflitti, poiché leo e ale sono in mappe separate sotto la duplicateUsersMap.
     */




