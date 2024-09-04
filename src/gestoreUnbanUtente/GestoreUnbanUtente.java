package gestoreUnbanUtente;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GestoreUnbanUtente implements IGestoreUnbanUtente{

    private final Map<String, Set<String>> bannedUsersByChannel; // mappa dei bannati per canale
    private final Map<SocketChannel, User> connectedUsers; // utenti connessi
    private final ClientWriter clientWriter; // per inviare risposte ai client

    public GestoreUnbanUtente(Map<String, Set<String>> bannedUsersByChannel, Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter) {
        this.bannedUsersByChannel = bannedUsersByChannel;
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
    }

    // Metodo per gestire l'unban
    @Override
    public void unbanUtente(SocketChannel mittente, String messaggio) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(messaggio).getAsJsonObject();
            String destinatario = jsonMessage.get("message").getAsString();
            String canaleDaSbannare = jsonMessage.get("channel").getAsString();

            // Estrai nickname e tempId dal destinatario
            String[] destinatarioParts = destinatario.split(":");
            String nickDestinatario = destinatarioParts[0];
            String tempId = destinatarioParts.length > 1 ? destinatarioParts[1] : null;

            // Trova l'utente bannato usando il nick e il tempID
            User userDaSbannare = trovaUtente(nickDestinatario, tempId);
            if (userDaSbannare == null) {
                clientWriter.writeToClient(mittente, "Utente non trovato o non bannato.");
                return;
            }

            // Verifica se l'utente è bannato nel canale specificato
            Set<String> bannedUsersInChannel = bannedUsersByChannel.get(canaleDaSbannare);
            if (bannedUsersInChannel == null || !bannedUsersInChannel.contains(userDaSbannare.getID())) {
                clientWriter.writeToClient(mittente, "Utente non è stato bannato dal canale.");
                return;
            }

            // Rimuovi l'utente dalla lista dei bannati
            bannedUsersInChannel.remove(userDaSbannare.getID());

            // Se la lista dei bannati per questo canale è vuota, rimuovi la voce dalla mappa
            if (bannedUsersInChannel.isEmpty()) {
                bannedUsersByChannel.remove(canaleDaSbannare);
            }

            // Notifica al mittente che l'utente è stato sbannato con successo
            clientWriter.writeToClient(mittente, "Utente sbannato con successo dal canale " + canaleDaSbannare);

            // Notifica all'utente sbannato
            clientWriter.writeToClient(userDaSbannare.getSocketChannel(), "Sei stato sbannato dal canale " + canaleDaSbannare);

        } catch (Exception e) {
            System.err.println("Errore durante l'unban dell'utente: " + e.getMessage());
        }
    }


    // Metodo per trovare l'utente bannato
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
}

/*
{
    "command": "unban",
    "message": "leo:00001",
    "channel": "nome_canale"
}
 */