package CommandMessaggio;

import java.nio.channels.SocketChannel;

public interface Command  {
    public void execute(SocketChannel client, String message);
}
