package CommandMessaggio;

import GestoreIOUser.GestoreRegistrazione;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class CommandRegistrazione implements Command {
    GestoreRegistrazione registrazione;

    public CommandRegistrazione(GestoreRegistrazione registrazione) {
        this.registrazione = registrazione;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
        try {
            registrazione.handleRegistration(client,jsonMessage);
        } catch (IOException e) {
            System.err.println("errore durante la registrazione"+e.getMessage());
        }

    }
}
