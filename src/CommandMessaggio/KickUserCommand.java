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
            //  il messaggio ricevuto va al gestore del kick
            gestoreKickCanale.kickUser(client, message);

        } catch (JsonSyntaxException e) {
            System.err.println("Errore durante il parsing del messaggio di kick: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Errore nell'esecuzione del kick utente: " + e.getMessage(), e);
        }
    }
}
