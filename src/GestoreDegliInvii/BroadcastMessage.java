package GestoreDegliInvii;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BroadcastMessage implements IBroadcastMessage {
    private Map<SocketChannel, User> connectedUsers;
    private ExecutorService broadcastExecutor;
    private Map<String, Set<SocketChannel>> channels;
    private ClientWriter clientWriter;
    private final Map<String, Map<String, String>> duplicateUsersMap;

    public BroadcastMessage(Map<SocketChannel, User> connectedUsers, ExecutorService broadcastExecutor, Map<String, Set<SocketChannel>> channels, ClientWriter clientWriter, Map<String, Map<String, String>> duplicateUsersMap) {
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
            // L'invio del messaggio avviene in modo asincrono
            broadcastExecutor.submit(() -> {
                String channelName = senderUser.getChannel();
                if (channelName != null) {
                    Set<SocketChannel> clientsInChannelSnapshot;

                    // **MODIFICA**: Sincronizzazione dell'accesso alla mappa `channels` per evitare condizioni di gara
                    // quando accediamo o modifichiamo la lista dei client connessi a un canale.
                    synchronized (channels) {
                        Set<SocketChannel> clientsInChannel = channels.get(channelName);
                        if (clientsInChannel != null) {
                            // **MODIFICA**: Creiamo uno snapshot della lista dei client per evitare modifiche concorrenti.
                            // Questo snapshot è una copia della lista attuale dei client nel canale.
                            clientsInChannelSnapshot = new HashSet<>(clientsInChannel);
                        } else {
                            // Se il canale non ha client o non esiste, inizializziamo uno snapshot vuoto
                            clientsInChannelSnapshot = Collections.emptySet();
                        }
                    }

                    if (!clientsInChannelSnapshot.isEmpty()) {
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

                        // **MODIFICA**: Utilizziamo lo snapshot creato per inviare il messaggio.
                        // Anche se la lista `clientsInChannel` originale cambia, l'invio utilizzerà una copia stabile.
                        for (SocketChannel client : clientsInChannelSnapshot) {
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
                        System.err.println("No clients in channel: " + channelName);
                    }
                } else {
                    System.err.println("Sender " + senderUser.getNick() + " is not in a channel.");
                }
            });
        }
    }
}
