package CommandMessaggio;

import GestoreDegliInvii.BroadcastMessage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SendMessageBroadcastCommand implements Command {

 private BroadcastMessage BroadcastGestoreSenderMessage;


 public SendMessageBroadcastCommand(BroadcastMessage BroadcastGestoreSenderMessage) {
     this.BroadcastGestoreSenderMessage = BroadcastGestoreSenderMessage;
 }

    @Override
    public void execute(SocketChannel client, String message) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String messageToBroadcast = jsonMessage.get("message").getAsString();
            BroadcastGestoreSenderMessage.broadcastMessage(client, messageToBroadcast);
        } catch (JsonSyntaxException e) {
            System.err.println("Errore durante l'invio del messaggio in broadcast: " + e.getMessage());
        }
    }

}
/*
{
    "sender": "nome_utente",
    "message": "questo è il messaggio"
}
 */