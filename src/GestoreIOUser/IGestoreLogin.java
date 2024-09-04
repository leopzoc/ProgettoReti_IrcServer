package GestoreIOUser;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface IGestoreLogin {

    public void handleLogin(SocketChannel client, JsonObject jsonMessage) throws IOException;

}
