package GestoreMessaggioPrivato;


import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;
import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

public class GestoreMessaggiPrivati implements IGestoreMessaggiPrivati {

    private final Map<SocketChannel, User> connectedUsers;
    private final ClientWriter clientWriter;
    private final Map<String, Map<String, String>> duplicateUsersMap;

    public GestoreMessaggiPrivati(Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter, Map<String, Map<String, String>> duplicateUsersMap) {
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.duplicateUsersMap = duplicateUsersMap;
    }

    @Override
    public void inviaMessaggioPrivato(SocketChannel mittente, String messaggio) {
        try {
                System.out.println(messaggio);

                JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();

                // Verifica che il JSON contenga i campi necessari
                if (!jsonMessage.has("recipient") || !jsonMessage.has("message")) {
                    JsonObject errore = new JsonObject();
                    errore.addProperty("status", "error");
                    errore.addProperty("message", "Messaggio JSON non valido. Manca il campo recipient o message.");
                    clientWriter.writeToClient(mittente, errore.toString());
                    return;
                }

            String recipient = jsonMessage.get("recipient").getAsString();
            String contenutoMessaggio = jsonMessage.get("message").getAsString();

            // Dividi il recipient in nick e tempId
            String[] recipientParts = recipient.split(":");
            String nickDestinatario = recipientParts[0];
            String tempId = recipientParts.length > 1 ? recipientParts[1] : null;

            // Trova l'utente destinatario in base al nome e al tempId (se presente)
            User userDestinatario = trovaUtenteDestinatario(nickDestinatario, tempId);

            if (userDestinatario != null) {
                JsonObject risposta = new JsonObject();

                // Verifica se il mittente ha un ID temporaneo
                String senderNick = connectedUsers.get(mittente).getNick();
                String senderTempId = connectedUsers.get(mittente).getTempId();
                if (senderTempId != null) {
                    senderNick = senderNick + "(" + senderTempId + ")";
                }
                contenutoMessaggio = "[privato]" + contenutoMessaggio;
                risposta.addProperty("sender", senderNick);
                risposta.addProperty("message", contenutoMessaggio);

                clientWriter.writeToClient(userDestinatario.getSocketChannel(), risposta.toString());
            } else {
                // Invia un messaggio di errore in formato JSON
                JsonObject errore = new JsonObject();
                errore.addProperty("status", "error");
                errore.addProperty("message", "Utente non trovato o più utenti con lo stesso nome.");
                clientWriter.writeToClient(mittente, errore.toString());
            }
        }  catch (IOException e) {
        System.err.println("Errore durante l'invio del messaggio privato: " + e.getMessage());

        // Invia un messaggio di errore generico in formato JSON al mittente
        JsonObject errore = new JsonObject();
        errore.addProperty("status", "error");
        errore.addProperty("message", "Errore durante l'invio del messaggio privato.");
        try {
            clientWriter.writeToClient(mittente, errore.toString());
        } catch (IOException ioException) {
            System.err.println("Errore durante l'invio del messaggio di errore: " + ioException.getMessage());
        }
    }
}

/*
Se l'utente destinatario non viene trovato:
json
{
  "status": "error",
  "message": "Utente non trovato o più utenti con lo stesso nome."
}
Se si verifica un errore durante l'invio del messaggio:
json

{
  "status": "error",
  "message": "Errore durante l'invio del messaggio privato."
}
 */



    private User trovaUtenteDestinatario(String nick, String tempId) {
        for (User user : connectedUsers.values()) {
            // Se tempId è nullo o vuoto, cerca solo per nickname
            if ((tempId == null || tempId.isEmpty()) && user.getNick().equals(nick)) {
                return user;
            }
            // Se tempId è presente, cerca per nickname e tempId
            if (user.getNick().equals(nick) && tempId != null && tempId.equals(user.getTempId())) {
                return user;
            }
        }
        return null;
    }


}



/*
Il client che invia un messaggio privato dovrà inviare un JSON con il seguente formato:

{
    "command": "privmsg",
    "recipient": "nick_destinatario:tempid",
    "message": "contenuto_del_messaggio"
}

il server invia al destinatario


{
    "sender": "nick_mittente(tempid)" (opzionale),
    "message": "contenuto_del_messaggio"
}





 */
