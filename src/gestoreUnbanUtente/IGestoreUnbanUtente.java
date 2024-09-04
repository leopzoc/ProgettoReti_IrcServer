package gestoreUnbanUtente;

import java.nio.channels.SocketChannel;

public interface IGestoreUnbanUtente {
    public void unbanUtente(SocketChannel mittente, String messaggio);
}
