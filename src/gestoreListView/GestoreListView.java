package gestoreListView;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.rmi.MarshalledObject;
import java.util.Map;
import java.util.Set;


import Connessione.ClientWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;

public class GestoreListView implements IGestoreListView {

    private final Map<String, Set<SocketChannel>> channels;
    private final ClientWriter clientWriter;

    public GestoreListView(Map<String, Set<SocketChannel>> channels, ClientWriter clientWriter) {
        this.channels = channels;
        this.clientWriter = clientWriter;
    }

    @Override
    public void seeList(SocketChannel client, String listCommand) {
        try {
            JsonArray channelsArray = new JsonArray();

            for (String channel : channels.keySet()) {
                channelsArray.add(channel); // Aggiunge il nome del canale all'array
            }

            JsonObject responseJson = new JsonObject();
            responseJson.add("channels", channelsArray); // Aggiunge l'array dei canali al JSON di risposta

            // Converti la risposta in una stringa e inviala al client
            clientWriter.writeToClient(client, responseJson.toString());

        } catch (IOException e) {
            System.err.println("Errore durante l'invio della lista canali: " + e.getMessage());
        }
    }
}

/*
{
    "channels": ["general", "tech", "random", "news"]
}


 */