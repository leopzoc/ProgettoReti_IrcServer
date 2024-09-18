package GestoreViewUser;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import gestoreListView.IGestoreListView;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class GestoreViewUser implements IGestoreListView {
    private final Map<String, Set<SocketChannel>> channels;
    private final Map<SocketChannel, User> connectedUsers;
    private final ClientWriter clientWriter;
    private final Map<String, Map<String, String>> duplicateUsersMap;
    private final ExecutorService executorService; // Usare l'executor passato dal server

    public GestoreViewUser(Map<String, Set<SocketChannel>> channels, Map<SocketChannel, User> connectedUsers,
                           ClientWriter clientWriter, Map<String, Map<String, String>> duplicateUsersMap,
                           ExecutorService executorService) {
        this.channels = channels;
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
        this.duplicateUsersMap = duplicateUsersMap;
        this.executorService = executorService; // Usa l'executor passato come parametro
    }

    @Override
    public void seeList(SocketChannel client, String listCommand) {
        executorService.submit(() -> {
            try {
                // Creiamo uno snapshot delle mappe per evitare race condition
                Map<String, Set<SocketChannel>> channelsSnapshot = Map.copyOf(channels);
                Map<SocketChannel, User> connectedUsersSnapshot = Map.copyOf(connectedUsers);

                JsonArray usersArray = new JsonArray();

                for (Set<SocketChannel> clientsInChannel : channelsSnapshot.values()) {
                    for (SocketChannel socket : clientsInChannel) {
                        User user = connectedUsersSnapshot.get(socket);
                        if (user != null) {
                            String userNick = user.getNick();

                            // Verifica se il nome utente ha un ID temporaneo
                            if (duplicateUsersMap.containsKey(userNick)) {
                                Map<String, String> tempIdMap = duplicateUsersMap.get(userNick);
                                String tempId = tempIdMap.get(user.getID());
                                if (tempId != null) {
                                    // Aggiungi l'ID temporaneo al nome utente
                                    userNick = userNick + "(" + tempId + ")";
                                }
                            }

                            usersArray.add(new JsonPrimitive(userNick));
                        }
                    }
                }

                JsonObject responseJson = new JsonObject();
                responseJson.add("users", usersArray); // Aggiunge l'array degli utenti al JSON di risposta

                // Converti la risposta in una stringa e inviala al client
                clientWriter.writeToClient(client, responseJson.toString());

            } catch (IOException e) {
                System.err.println("Errore durante l'invio della lista utenti: " + e.getMessage());
            }
        });
    }
}

