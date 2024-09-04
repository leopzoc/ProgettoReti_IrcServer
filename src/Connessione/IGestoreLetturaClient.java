package Connessione;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;

public interface IGestoreLetturaClient {
    public void leggoClient(SelectionKey key, Map<SocketChannel, ByteBuffer> buffers) throws IOException;
}
