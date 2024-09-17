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
                    JsonObject errorMessage = new JsonObject();
                    errorMessage.addProperty("status", "error");
                    errorMessage.addProperty("message", "Utente non trovato o più utenti con lo stesso nome.");
                    clientWriter.writeToClient(mittente, errorMessage.toString());
                    return;
                }

                // Aggiorna lo stato nel file 'users.txt' e disconnette l'utente
                if (aggiornaStatoUtente(userDaBannare.getID())) {
                    JsonObject successMessage = new JsonObject();
                    successMessage.addProperty("status", "success");
                    successMessage.addProperty("message", "Utente bannato definitivamente.");
                    clientWriter.writeToClient(mittente, successMessage.toString());
                    // Disconnette l'utente
                    gestoreDisconnesioneClient.handleClientDisconnection(userDaBannare.getSocketChannel());
                } else {
                    JsonObject errorMessage = new JsonObject();
                    errorMessage.addProperty("status", "error");
                    errorMessage.addProperty("message", "Errore durante il ban dell'utente.");
                    clientWriter.writeToClient(mittente, errorMessage.toString());
                }

            } else if (jsonMessage.has("uuid")) {
                // Ban tramite UUID
                String uuid = jsonMessage.get("uuid").getAsString();

                // Aggiorna lo stato dell'utente
                if (aggiornaStatoUtente(uuid)) {
                    JsonObject successMessage = new JsonObject();
                    successMessage.addProperty("status", "success");
                    successMessage.addProperty("message", "Utente bannato definitivamente tramite UUID.");
                    clientWriter.writeToClient(mittente, successMessage.toString());

                    // Controlla se l'utente è online e disconnettilo
                    Optional<User> userDaBannare = connectedUsers.values().stream()
                            .filter(user -> user.getID().equals(uuid))
                            .findFirst();

                    if (userDaBannare.isPresent()) {
                        // Se l'utente è online, disconnettilo
                        gestoreDisconnesioneClient.handleClientDisconnection(userDaBannare.get().getSocketChannel());
                    }
                } else {
                    JsonObject errorMessage = new JsonObject();
                    errorMessage.addProperty("status", "error");
                    errorMessage.addProperty("message", "Errore durante il ban dell'utente tramite UUID.");
                    clientWriter.writeToClient(mittente, errorMessage.toString());                }
            } else {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("status", "error");
                errorMessage.addProperty("message", "Formato del messaggio non valido.");
                clientWriter.writeToClient(mittente, errorMessage.toString());
            }

        }  catch (Exception e) {
            System.err.println("Errore durante il ban definitivo dell'utente: " + e.getMessage());

            // Invia un messaggio di errore in formato JSON in caso di eccezione
            JsonObject errorMessage = new JsonObject();
            errorMessage.addProperty("status", "error");
            errorMessage.addProperty("message", "Errore durante il ban definitivo dell'utente.");
            try {
                clientWriter.writeToClient(mittente, errorMessage.toString());
            } catch (IOException ioException) {
                System.err.println("Errore durante l'invio del messaggio di errore: " + ioException.getMessage());
            }
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

/*
server:
Modifiche principali:
Messaggi di errore in formato JSON:

Se l'utente non viene trovato o c'è un errore, viene inviato un messaggio di errore in formato JSON:

json

{
  "status": "error",
  "message": "Utente non trovato o più utenti con lo stesso nome."
}
Se c'è un errore durante il ban tramite UUID o tempID:

json

{
  "status": "error",
  "message": "Errore durante il ban dell'utente tramite UUID/tempID."
}
Messaggi di successo in formato JSON:

Se l'utente viene bannato con successo, viene inviato un messaggio di conferma in formato JSON:
json

{
  "status": "success",
  "message": "Utente bannato definitivamente."
}
Se l'utente viene bannato tramite UUID:
json

{
  "status": "success",
  "message": "Utente bannato definitivamente tramite UUID."
}
Gestione degli errori:

In caso di eccezione, viene inviato un messaggio di errore generico al mittente in formato JSON:
json

{
  "status": "error",
  "message": "Errore durante il ban definitivo dell'utente."
}

 */