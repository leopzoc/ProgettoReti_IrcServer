package GestoreBanDefinitivo;

import Connessione.ClientWriter;
import Connessione.GestoreDisconnesioneClient;
import GestoreIOUser.GestoreUtenti;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

public class GestoreFBanUtente {

    private final Map<SocketChannel, User> connectedUsers;
    private final ClientWriter clientWriter;
    private final GestoreDisconnesioneClient gestoreDisconnesioneClient;

    public GestoreFBanUtente(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter, GestoreDisconnesioneClient gestoreDisconnesioneClient) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.gestoreDisconnesioneClient = gestoreDisconnesioneClient;
    }

    // Metodo per gestire il ban definitivo
    public void fbanUtente(SocketChannel mittente, String messaggio) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();

            // Verifica se il ban è tramite tempID o UUID
            if (jsonMessage.has("message")) {
                // Ban tramite tempID
                String destinatario = jsonMessage.get("message").getAsString();

                // Estrai nickname e tempId dal destinatario
                String[] destinatarioParts = destinatario.split(":");
                String nickDestinatario = destinatarioParts[0];
                String tempId = destinatarioParts.length > 1 ? destinatarioParts[1] : null;

                // Trova l'utente da bannare
                User userDaBannare = trovaUtente(nickDestinatario, tempId);
                if (userDaBannare == null) {
                    clientWriter.writeToClient(mittente, "Utente non trovato o più utenti con lo stesso nome.");
                    return;
                }

                // Aggiorna lo stato nel file 'users.txt' e disconnette l'utente
                if (aggiornaStatoUtente(userDaBannare.getID())) {
                    clientWriter.writeToClient(mittente, "Utente bannato definitivamente.");
                    // Disconnette l'utente
                    gestoreDisconnesioneClient.handleClientDisconnection(userDaBannare.getSocketChannel());
                } else {
                    clientWriter.writeToClient(mittente, "Errore durante il ban dell'utente.");
                }

            } else if (jsonMessage.has("uuid")) {
                // Ban tramite UUID
                String uuid = jsonMessage.get("uuid").getAsString();

                // Aggiorna lo stato dell'utente
                if (aggiornaStatoUtente(uuid)) {
                    clientWriter.writeToClient(mittente, "Utente bannato definitivamente tramite UUID.");

                    // Controlla se l'utente è online e disconnettilo
                    Optional<User> userDaBannare = connectedUsers.values().stream()
                            .filter(user -> user.getID().equals(uuid))
                            .findFirst();

                    if (userDaBannare.isPresent()) {
                        // Se l'utente è online, disconnettilo
                        gestoreDisconnesioneClient.handleClientDisconnection(userDaBannare.get().getSocketChannel());
                    }
                } else {
                    clientWriter.writeToClient(mittente, "Errore durante il ban dell'utente tramite UUID.");
                }
            } else {
                clientWriter.writeToClient(mittente, "Formato del messaggio non valido.");
            }

        } catch (Exception e) {
            System.err.println("Errore durante il ban definitivo dell'utente: " + e.getMessage());
        }
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

    // Metodo per aggiornare lo stato dell'utente nel file 'users.txt' a "banned"
    private boolean aggiornaStatoUtente(String userId) {
        try {
            Map<String, User> users = GestoreUtenti.loadUsers();
            User userDaBannare = users.get(userId);

            if (userDaBannare != null) {
                // Modifica lo stato a "banned"
                userDaBannare.setStatus("banned");

                // Salva nuovamente gli utenti nel file 'users.txt'
                GestoreUtenti.saveUsers(users);

                return true;
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'aggiornamento dello stato dell'utente: " + e.getMessage());
        }

        return false;
    }
}
/*
{
    "command": "fban",
    "message": "leo:00001"
}
{
    "command": "fban",
    "uuid": "d600b33b-3ca3-44af-85bb-e410efc186fd"
}

 */