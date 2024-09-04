package Connessione;

import CommandMessaggio.CommandHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import CommandMessaggio.CommandHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GestioneLetturaClient implements IGestoreLetturaClient {

    private final IGestoreDisconnesioneClient gestoreDisconnesioneClient;
    private final CommandHandler commandHandler;

    public GestioneLetturaClient(IGestoreDisconnesioneClient gestoreDisconnesioneClient, CommandHandler commandHandler) {
        this.gestoreDisconnesioneClient = gestoreDisconnesioneClient;
        this.commandHandler = commandHandler;
    }

    @Override
    public void leggoClient(SelectionKey key, Map<SocketChannel, ByteBuffer> buffers) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        if (!client.isOpen()) {
            return;  // Esci se il canale è già chiuso
        }

        ByteBuffer buffer = buffers.get(client);
        if (buffer == null) {
            buffer = ByteBuffer.allocate(1024);
            buffers.put(client, buffer);
        }

        try {
            int bytesRead = client.read(buffer);
            if (bytesRead == -1) {
                gestoreDisconnesioneClient.handleClientDisconnection(client);
            } else if (bytesRead > 0) {
                buffer.flip();
                while (buffer.remaining() >= Integer.BYTES) {
                    buffer.mark();
                    int messageLength = buffer.getInt();

                    if (buffer.remaining() < messageLength) {
                        buffer.reset();
                        break; // Il messaggio non è ancora completo
                    }

                    byte[] messageBytes = new byte[messageLength];
                    buffer.get(messageBytes);
                    String message = new String(messageBytes, StandardCharsets.UTF_8);

                    handleClientMessage(client, message);
                }
                buffer.compact(); // Preparare il buffer per ulteriori letture
            }
        } catch (SocketException e) {
            System.err.println("Connection reset by peer: " + client.getRemoteAddress());
            gestoreDisconnesioneClient.handleClientDisconnection(client);
        } catch (IOException e) {
            System.err.println("IOException during client read: " + e.getMessage());
            gestoreDisconnesioneClient.handleClientDisconnection(client);
        }
    }

    private void handleClientMessage(SocketChannel client, String message) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String command = jsonMessage.get("command").getAsString();

            // Chiama il CommandHandler per gestire il comando
            commandHandler.handleCommand(command, client, message);
        } catch (Exception e) {
            System.out.println("Errore nel parsing del messaggio JSON: " + e.getMessage());
        }
    }
}
