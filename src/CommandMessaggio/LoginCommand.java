package CommandMessaggio;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import GestoreIOUser.GestoreLogin;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LoginCommand implements Command {

    private final GestoreLogin gestoreLogin;

    public LoginCommand(GestoreLogin gestoreLogin) {
        this.gestoreLogin = gestoreLogin;
    }



    @Override
    public void execute(SocketChannel client, String message) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            gestoreLogin.handleLogin(client, jsonMessage);
        } catch (IOException e) {
            System.err.println("Errore durante il login: " + e.getMessage());
        }
    }
}

