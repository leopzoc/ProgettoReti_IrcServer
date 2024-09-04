package GestoreViewUser;

import java.nio.channels.SocketChannel;

public interface IGestoreViewUser {
    public void seeList(SocketChannel client, String listCommand);
}
