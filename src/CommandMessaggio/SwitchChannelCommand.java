package CommandMessaggio;

import GestoreCambioCanale.SwitchChannelChange;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SwitchChannelCommand implements Command {

    private SwitchChannelChange switchChannelChange;

    public SwitchChannelCommand(SwitchChannelChange switchChannelChange) {
        this.switchChannelChange = switchChannelChange;
    }


    @Override
    public void execute(SocketChannel client, String message) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String canaleAlto = jsonMessage.get("message").getAsString();
            switchChannelChange.switchChannel(client, canaleAlto);
        } catch (JsonSyntaxException e) {
            System.err.println("Errore durante il cambio canale: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);//se non riesce a cambiare canale
        }
    }
}

/*
{
    "command": "switch_channel",
    "message": "new_channel_name"
}

 */
