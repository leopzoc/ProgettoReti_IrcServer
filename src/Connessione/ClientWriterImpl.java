package Connessione;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ClientWriterImpl implements ClientWriter {

    private final Map<SocketChannel, ByteBuffer> pendingData;

    public ClientWriterImpl(Map<SocketChannel, ByteBuffer> pendingData) {
        this.pendingData = pendingData;
    }

    @Override
    public synchronized void  writeToClient(SocketChannel client, String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageBytes.length);
        buffer.putInt(messageBytes.length); // Prefisso con la lunghezza del messaggio
        buffer.put(messageBytes); // Aggiungi il messaggio
        buffer.flip(); // Prepara il buffer per la scrittura

        int bytesWritten = client.write(buffer); //per vedere quanti byte ho scritto, al momento non serve e stato utilizzato per il debug
        //System.out.println("byte scritti" + bytesWritten+" su client"+client.getRemoteAddress());
        if (buffer.hasRemaining()) { //non riesco a inviare tutto perchè sto usando delle socket non bloccati
            // Se non è stato possibile scrivere tutti i dati, memorizzali in pendingData
            pendingData.put(client, buffer);

            // Registra il canale per l'operazione di scrittura
            SelectionKey key = client.keyFor(client.provider().openSelector());
            key.interestOps(SelectionKey.OP_WRITE | key.interestOps()); //e pronto per essere scritto ma quello che gia ho registrato come maschera non la togliere "interestOps"
        }
    }
}
