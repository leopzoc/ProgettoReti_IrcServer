package GestoreDegliInvii;

import java.nio.channels.SocketChannel;

public interface IBroadcastMessage {
    public void broadcastMessage(SocketChannel sender, String message);

}
