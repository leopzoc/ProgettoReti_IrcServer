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
                clientWriter.writeToClient(mittente, "Utente sbannato con successo tramite UUID.");
            } else {
                clientWriter.writeToClient(mittente, "Errore durante lo sbannamento dell'utente tramite UUID.");
            }

        } catch (Exception e) {
            System.err.println("Errore durante lo sbannamento definitivo dell'utente: " + e.getMessage());
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
