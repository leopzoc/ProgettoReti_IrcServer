package GestoreCambioCanale;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ISwitchChannelChange {
    public void switchChannel(SocketChannel client, String channelName) throws IOException;
}
