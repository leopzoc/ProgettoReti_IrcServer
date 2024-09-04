package Connessione;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

public class GestoreAccettazione implements IGestoreAccettazione {

    @Override
    public void Accettazione(SelectionKey key, Selector selector, Map<SocketChannel, ByteBuffer> buffers) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel client = serverChannel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        buffers.put(client, ByteBuffer.allocate(1024)); // Inizializza un buffer per il nuovo client
        System.out.println("Connected: " + client.getRemoteAddress());
    }
}
