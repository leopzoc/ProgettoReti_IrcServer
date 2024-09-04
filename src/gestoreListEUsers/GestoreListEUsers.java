package gestoreListEUsers;

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

public class GestoreListEUsers implements IGestoreListView {

    private final Map<String, Set<SocketChannel>> channels;
    private final Map<SocketChannel, User> connectedUsers;
    private final ClientWriter clientWriter;

    public GestoreListEUsers(Map<String, Set<SocketChannel>> channels, Map<SocketChannel, User> connectedUsers, ClientWriter clientWriter) {
        this.channels = channels;
        this.connectedUsers = connectedUsers;
        this.clientWriter = clientWriter;
    }

    @Override
    public void seeList(SocketChannel client, String channelName) {
        try {
            JsonArray channelsArray = new JsonArray();

            for (Map.Entry<String, Set<SocketChannel>> entry : channels.entrySet()) {
                String channel = entry.getKey();
                Set<SocketChannel> clientsInChannel = entry.getValue();

                JsonObject channelJson = new JsonObject();
                channelJson.addProperty("channel", channel);

                JsonArray usersArray = new JsonArray();
                for (SocketChannel socket : clientsInChannel) {
                    User user = connectedUsers.get(socket);
                    if (user != null) {
                        // Aggiungi il nickname e, se presente, l'ID temporaneo
                        String userNick = user.getNick();
                        if (user.getTempId() != null && !user.getTempId().isEmpty()) {
                            userNick += "(" + user.getTempId() + ")";
                        }
                        usersArray.add(new JsonPrimitive(userNick));
                    }
                }

                channelJson.add("users", usersArray);
                channelsArray.add(channelJson);
            }

            JsonObject responseJson = new JsonObject();
            responseJson.add("channels", channelsArray);

            // Converti la risposta in una stringa e inviala al client
            clientWriter.writeToClient(client, responseJson.toString());

        } catch (IOException e) {
            System.err.println("Errore durante l'invio della lista canali: " + e.getMessage());
        }
    }
}





/*


{
        "channels": [
        {
        "channel": "general",
        "users": ["user1", "user2"]
        },
        {
        "channel": "random",
        "users": ["user3"]
        }
        ]
        }
*/