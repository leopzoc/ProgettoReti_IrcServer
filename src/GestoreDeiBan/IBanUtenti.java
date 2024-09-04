package GestoreDeiBan;

import java.nio.channels.SocketChannel;

public interface IBanUtenti {
    public void banUtente(SocketChannel mittente, String messaggio);
}
