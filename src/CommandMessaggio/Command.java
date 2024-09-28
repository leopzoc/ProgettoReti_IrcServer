package CommandMessaggio;

import java.nio.channels.SocketChannel;
//interfaccia command
public interface Command  {
    public void execute(SocketChannel client, String message);
}
