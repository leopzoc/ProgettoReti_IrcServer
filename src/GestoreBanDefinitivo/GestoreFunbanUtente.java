package GestoreBanDefinitivo;

import Connessione.ClientWriter;
import GestoreIOUser.GestoreUtenti;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class GestoreFunbanUtente   {

    private final ClientWriter clientWriter;
    private final String userFilePath;

    public GestoreFunbanUtente(ClientWriter clientWriter, String userFilePath) {
        this.clientWriter = clientWriter;
        this.userFilePath = userFilePath;
    }


    public void funbanUtente(SocketChannel mittente, String messaggio) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();
            String uuid = jsonMessage.get("uuid").getAsString();

            // Aggiorna lo stato dell'utente nel file 'users.txt' da "banned" a "active"
            if (aggiornaStatoUtente(uuid, "active")) {
                JsonObject successMessage = new JsonObject();
                successMessage.addProperty("status", "success");
                successMessage.addProperty("message", "Utente sbannato con successo tramite UUID.");
                clientWriter.writeToClient(mittente, successMessage.toString());
            } else {
                JsonObject errorMessage = new JsonObject();
                errorMessage.addProperty("status", "error");
                errorMessage.addProperty("message", "Errore durante lo sbannamento dell'utente tramite UUID.");
                clientWriter.writeToClient(mittente, errorMessage.toString());
            }

        }catch (Exception e) {
            System.err.println("Errore durante lo sbannamento definitivo dell'utente: " + e.getMessage());

            // Invia un messaggio di errore in formato JSON in caso di eccezione
            JsonObject errorMessage = new JsonObject();
            errorMessage.addProperty("status", "error");
            errorMessage.addProperty("message", "Errore durante lo sbannamento definitivo dell'utente.");
            try {
                clientWriter.writeToClient(mittente, errorMessage.toString());
            } catch (IOException ioException) {
                System.err.println("Errore durante l'invio del messaggio di errore: " + ioException.getMessage());
            }
        }
    }

    // Metodo per aggiornare lo stato dell'utente da "banned" a "active"
    private boolean aggiornaStatoUtente(String uuid, String nuovoStato) {
        try {
            // Carica tutti gli utenti dal file
            Map<String, User> users = GestoreUtenti.loadUsers();

            // Trova l'utente corrispondente all'UUID
            User user = users.get(uuid);
            if (user != null && user.getStatus().equals("banned")) {
                // Aggiorna lo stato a "active"
                user.setStatus(nuovoStato);

                // Salva gli utenti aggiornati nel file
                GestoreUtenti.saveUsers(users);

                return true; // Aggiornamento riuscito
            }
        } catch (IOException e) {
            System.err.println("Errore durante l'aggiornamento dello stato dell'utente: " + e.getMessage());
        }
        return false; // Errore o utente non trovato
    }
}

/*
Messaggio di successo in formato JSON:

Se l'utente viene sbannato con successo, viene inviato un messaggio di conferma in formato JSON:
json
{
  "status": "success",
  "message": "Utente sbannato con successo tramite UUID."
}
Messaggio di errore in formato JSON:

Se si verifica un errore durante lo sbannamento dell'utente, viene inviato un messaggio di errore in formato JSON:
json
{
  "status": "error",
  "message": "Errore durante lo sbannamento dell'utente tramite UUID."
}
Gestione delle eccezioni:

In caso di eccezione, viene inviato un messaggio di errore generico al mittente in formato JSON:
json
{
  "status": "error",
  "message": "Errore durante lo sbannamento definitivo dell'utente."
}
 */