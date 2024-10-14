package GestoreErrori;

import Connessione.ClientWriter;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

public class GestoreErrore {
    private ExecutorService broadcastExecutor;
    private ClientWriter clientWriter;
    public GestoreErrore (ExecutorService broadcastExecutor, ClientWriter clientWriter ) {
        this.broadcastExecutor = broadcastExecutor;
        this.clientWriter = clientWriter;

    }


    // Metodo per inviare un messaggio di errore al client in modo asincrono
    public void inviaErrore(SocketChannel client, String messaggioErrore) {
        broadcastExecutor.submit(() -> {
            try {
                clientWriter.writeToClient(client, messaggioErrore);
                System.out.println("Errore inviato al client: " + messaggioErrore);
            } catch (IOException e) {
                System.err.println("Errore durante l'invio del messaggio di errore: " + e.getMessage());


            }
        });
    }
}

