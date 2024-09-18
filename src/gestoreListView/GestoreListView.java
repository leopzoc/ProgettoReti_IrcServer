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



import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import Connessione.ClientWriter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GestoreListView implements IGestoreListView {

    private final Map<String, Set<SocketChannel>> channels;
    private final ClientWriter clientWriter;
    private final ExecutorService executorService;  // Aggiungi l'ExecutorService

    public GestoreListView(Map<String, Set<SocketChannel>> channels, ClientWriter clientWriter, ExecutorService executorService) {
        this.channels = channels;
        this.clientWriter = clientWriter;
        this.executorService = executorService;  // Inizializza l'ExecutorService
    }

    @Override
    public void seeList(SocketChannel client, String listCommand) {
        executorService.submit(() -> {  // Esegui il task in modo asincrono
            try {
                // Prendi uno snapshot della mappa channels
                Set<String> channelSnapshot = channels.keySet().stream()
                        .collect(Collectors.toSet());  // Copia della chiave per evitare modifiche concorrenti

                JsonArray channelsArray = new JsonArray();

                for (String channel : channelSnapshot) {
                    channelsArray.add(channel);  // Aggiunge il nome del canale all'array
                }

                JsonObject responseJson = new JsonObject();
                responseJson.add("channels", channelsArray);  // Aggiunge l'array dei canali al JSON di risposta

                // Converti la risposta in una stringa e inviala al client
                clientWriter.writeToClient(client, responseJson.toString());

            } catch (IOException e) {
                System.err.println("Errore durante l'invio della lista canali: " + e.getMessage());
            }
        });
    }
}

/*
{
    "channels": ["general", "tech", "random", "news"]
}
*/



/*
{
    "channels": ["general", "tech", "random", "news"]
}


 */