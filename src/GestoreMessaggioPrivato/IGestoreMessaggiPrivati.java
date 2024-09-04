package GestoreMessaggioPrivato;

import java.nio.channels.SocketChannel;

public interface IGestoreMessaggiPrivati {
    void inviaMessaggioPrivato(SocketChannel mittente, String messaggio);
}
