package GestoreKick;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface IGestoreKickCanale {
    public void kickUser(SocketChannel admin, String messaggioKick) throws IOException;
}
