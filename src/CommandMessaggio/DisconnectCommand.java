package CommandMessaggio;

import Connessione.GestoreDisconnesioneClient;

import java.nio.channels.SocketChannel;

public class DisconnectCommand implements Command {
    GestoreDisconnesioneClient gestoreDisconnesioneClient;

    public DisconnectCommand(GestoreDisconnesioneClient gestoreDisconnesioneClient  ) {
        this.gestoreDisconnesioneClient = gestoreDisconnesioneClient;
    }

    @Override
    public void execute(SocketChannel client, String command) {
        gestoreDisconnesioneClient.handleClientDisconnection(client,command);

    }
}
