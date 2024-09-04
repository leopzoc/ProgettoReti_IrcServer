package CommandMessaggio;


import GestoreKick.GestoreKickCanale;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class KickUserCommand implements Command {

    private GestoreKickCanale gestoreKickCanale;

    public KickUserCommand(GestoreKickCanale gestoreKickCanale) {
        this.gestoreKickCanale = gestoreKickCanale;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        try {
            // Parse the incoming JSON message
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String messaggioKick = jsonMessage.get("message").getAsString(); // Nickname:TempId
            String canaleTarget = jsonMessage.has("channel") ? jsonMessage.get("channel").getAsString() : null;

            // Costruisci il messaggio di kick con le informazioni ricevute
            JsonObject jsonKick = new JsonObject();
            jsonKick.addProperty("message", messaggioKick);  // Contiene nickname:tempid
            if (canaleTarget != null) {
                jsonKick.addProperty("channel", canaleTarget); // Aggiungi il canale se esiste
            }

            // Pass the parsed JSON message to the kick handler
            gestoreKickCanale.kickUser(client, jsonKick.toString());

        } catch (JsonSyntaxException e) {
            System.err.println("Errore durante il parsing del messaggio di kick: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'esecuzione del kick utente: " + e.getMessage(), e);
        }
    }
}
