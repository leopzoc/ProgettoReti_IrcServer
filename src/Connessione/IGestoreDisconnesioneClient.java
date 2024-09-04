package Connessione;

import java.nio.channels.SocketChannel;

public interface IGestoreDisconnesioneClient {
   public void handleClientDisconnection(SocketChannel client);

}
