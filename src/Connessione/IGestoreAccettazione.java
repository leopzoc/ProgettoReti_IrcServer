package Connessione;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;

public interface IGestoreAccettazione {
    public void Accettazione(SelectionKey key, Selector selector, Map<SocketChannel, ByteBuffer> buffers)throws IOException;

}
