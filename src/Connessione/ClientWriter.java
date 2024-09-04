package Connessione;


import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ClientWriter {
    void writeToClient(SocketChannel client, String message) throws IOException;
}
