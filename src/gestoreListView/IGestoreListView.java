package gestoreListView;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;

public interface IGestoreListView {

    public void seeList(SocketChannel client, String listCommand);
}
