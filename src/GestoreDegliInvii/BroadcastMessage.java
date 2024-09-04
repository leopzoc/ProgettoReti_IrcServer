package GestoreDegliInvii;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BroadcastMessage implements IBroadcastMessage {
    private Map<SocketChannel, User> connectedUsers;
    private ExecutorService broadcastExecutor;
    private Map<String, Set<SocketChannel>> channels;
    private ClientWriter clientWriter;
    private final Map<String, Map<String, String>> duplicateUsersMap;

    public BroadcastMessage(Map<SocketChannel, User> connectedUsers, ExecutorService broadcastExecutor, Map<String, Set<SocketChannel>> channels, ClientWriter clientWriter,Map<String, Map<String, String>> duplicateUsersMap) {
        this.connectedUsers = connectedUsers;
        this.broadcastExecutor = broadcastExecutor;
        this.channels = channels;
        this.clientWriter = clientWriter;
        this.duplicateUsersMap = duplicateUsersMap;
    }
/*
{
    "sender": "nome_utente",
    "message": "questo è il messaggio"
}

 */
@Override
public void broadcastMessage(SocketChannel sender, String message) {
    User senderUser = connectedUsers.get(sender);
    if (senderUser != null) {
        broadcastExecutor.submit(() -> {
            String channelName = senderUser.getChannel();
            if (channelName != null) {
                Set<SocketChannel> clientsInChannel = channels.get(channelName);
                if (clientsInChannel != null) {
                    System.out.println("Broadcasting message to channel: " + channelName);

                    // Verifica se il nome utente ha un ID temporaneo
                    String senderNick = senderUser.getNick();
                    if (duplicateUsersMap.containsKey(senderNick)) {
                        Map<String, String> tempIdMap = duplicateUsersMap.get(senderNick);
                        String tempId = tempIdMap.get(senderUser.getID());
                        if (tempId != null) {
                            // Aggiungi l'ID temporaneo al nome utente
                            senderNick = senderNick + "(" + tempId + ")";
                        }
                    }

                    for (SocketChannel client : clientsInChannel) {
                        if (client != sender) {
                            try {
                                // Creazione del JSON di risposta
                                JsonObject jsonResponse = new JsonObject();
                                jsonResponse.addProperty("sender", senderNick);
                                jsonResponse.addProperty("message", message);

                                String jsonResponseString = jsonResponse.toString();
                                System.out.println("Sending message to client: " + client.getRemoteAddress());
                                clientWriter.writeToClient(client, jsonResponseString);
                            } catch (IOException e) {
                                System.err.println("Error sending message to client: " + e.getMessage());
                            }
                        }
                    }
                } else {
                    System.err.println("Channel " + channelName + " not found or no clients in channel.");
                }
            } else {
                System.err.println("Sender " + senderUser.getNick() + " is not in a channel.");
            }
        });
    }
}

}
