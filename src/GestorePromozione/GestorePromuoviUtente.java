package GestorePromozione;

import Connessione.ClientWriter;
import GestoreIOUser.GestoreUtenti;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

public class GestorePromuoviUtente {

    private final Map<SocketChannel, User> connectedUsers; // Utenti connessi
    private final ClientWriter clientWriter; // Per inviare risposte al client
    private final String userFilePath; // Percorso del file users.txt

    public GestorePromuoviUtente(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter, String userFilePath) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.userFilePath = userFilePath;
    }

    public void promuoviUtente(SocketChannel mittente, String messaggio) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();
            String uuid = null;
            String destinatario = null;

            // Controlla se il comando è tramite UUID o tramite nick + tempId
            if (jsonMessage.has("uuid")) {
                uuid = jsonMessage.get("uuid").getAsString();
            } else if (jsonMessage.has("message")) {
                destinatario = jsonMessage.get("message").getAsString();
            }

            User userDaPromuovere = null;

            // Trova l'utente da promuovere tramite UUID o nick + tempid
            if (uuid != null) {
                userDaPromuovere = trovaUtenteDaUUID(uuid);
            } else if (destinatario != null) {
                String[] destinatarioParts = destinatario.split(":");
                String nickDestinatario = destinatarioParts[0];
                String tempId = destinatarioParts.length > 1 ? destinatarioParts[1] : null;
                userDaPromuovere = trovaUtente(nickDestinatario, tempId);
            }

            // Se l'utente non è stato trovato, invia un messaggio di errore
            if (userDaPromuovere == null) {
                clientWriter.writeToClient(mittente, "Utente non trovato.");
                return;
            }

            // Promuovi l'utente a admin se non lo è già
            if (!userDaPromuovere.getRole().equals("admin")) {
                userDaPromuovere.setRole("admin");

                // Aggiorna il file degli utenti
                aggiornaRuoloUtenteNelFile(userDaPromuovere);

                // Se l'utente è connesso, notificalo della promozione
                if (connectedUsers.containsValue(userDaPromuovere)) {
                    clientWriter.writeToClient(userDaPromuovere.getSocketChannel(), "Sei stato promosso a admin.");
                }

                // Notifica l'admin che ha eseguito il comando
                clientWriter.writeToClient(mittente, "Utente " + userDaPromuovere.getNick() + " è stato promosso a admin.");
            } else {
                clientWriter.writeToClient(mittente, "L'utente è già admin.");
            }

        } catch (Exception e) {
            System.err.println("Errore durante la promozione dell'utente: " + e.getMessage());
        }
    }

    // Metodo per trovare l'utente tramite UUID
    private User trovaUtenteDaUUID(String uuid) throws IOException {
        Map<String, User> allUsers = GestoreUtenti.loadUsers();
        return allUsers.get(uuid); // Restituisce l'utente se esiste, altrimenti null
    }

    // Metodo per trovare l'utente tramite nick e tempId
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

    // Metodo per aggiornare il ruolo dell'utente nel file users.txt
    private void aggiornaRuoloUtenteNelFile(User userDaPromuovere) throws IOException {
        Map<String, User> allUsers = GestoreUtenti.loadUsers();
        allUsers.put(userDaPromuovere.getID(), userDaPromuovere); // Aggiorna l'utente con il nuovo ruolo
        GestoreUtenti.saveUsers(allUsers); // Salva le modifiche nel file
    }
}

/*
{
    "command": "promote",
    "message": "leo:00001"
}

{
    "command": "promote",
    "uuid": "e0fbc7dc-667b-4292-a836-d8f52c236984"
}

 */