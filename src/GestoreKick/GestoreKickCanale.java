package GestoreKick;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GestoreKickCanale implements IGestoreKickCanale {
    private final Map<SocketChannel, User> connectedUsers;
    private final Map<String, Set<SocketChannel>> channels;
    private final ClientWriter clientWriter;
    private final Map<String, Map<String, String>> duplicateUsersMap;

    public GestoreKickCanale(Map<SocketChannel, User> connectedUsers, Map<String, Set<SocketChannel>> channels, ClientWriter clientWriter, Map<String, Map<String, String>> duplicateUsersMap) {
        this.connectedUsers = connectedUsers;
        this.channels = channels;
        this.clientWriter = clientWriter;
        this.duplicateUsersMap = duplicateUsersMap;
    }

    @Override
    public void kickUser(SocketChannel admin, String messaggioKick) throws IOException {
        JsonObject jsonKick = JsonParser.parseString(messaggioKick).getAsJsonObject();
        String recipient = jsonKick.get("message").getAsString();
        String[] recipientParts = recipient.split(":");
        String nickDaKickare = recipientParts[0];
        String tempId = recipientParts.length > 1 ? recipientParts[1] : null;

        // Cerca l'utente da kickare globalmente (non limitato a un solo canale)
        User userDaKickare = trovaUtenteGlobalmente(nickDaKickare, tempId);

        if (userDaKickare != null) {
            // Ottieni il canale in cui si trova attualmente l'utente
            String canaleAttuale = userDaKickare.getChannel();

            // Rimuovi l'utente dal canale attuale
            Set<SocketChannel> clientsInChannel = channels.get(canaleAttuale);
            if (clientsInChannel != null) {
                clientsInChannel.remove(userDaKickare.getSocketChannel());
            }

            // Sposta l'utente nel primo canale disponibile
            String firstChannel = channels.keySet().iterator().next();
            if (!firstChannel.equals(userDaKickare.getChannel())) {
                userDaKickare.setChannel(firstChannel);
                channels.get(firstChannel).add(userDaKickare.getSocketChannel());
                clientWriter.writeToClient(userDaKickare.getSocketChannel(), "Sei stato espulso e spostato nel canale: " + firstChannel);
                System.out.println("Utente " + userDaKickare.getNick() + " (" + tempId + ") è stato espulso dal canale: " + canaleAttuale);
            } else {
                clientWriter.writeToClient(admin, "Utente già nel canale di destinazione.");
            }
        } else {
            clientWriter.writeToClient(admin, "Utente non trovato o più utenti con lo stesso nome.");
        }
    }

    // Metodo che cerca l'utente globalmente in tutti i canali
    private User trovaUtenteGlobalmente(String nick, String tempId) {
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



//admin command
/*
{
        "command": "kick",
        "message": "john_doe",
        "channel": "general"
        }

        {
    "command": "kick",
    "message": "leo:00001"
}
 */
//il