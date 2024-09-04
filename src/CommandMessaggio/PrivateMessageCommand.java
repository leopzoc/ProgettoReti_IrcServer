package CommandMessaggio;

import GestoreMessaggioPrivato.GestoreMessaggiPrivati;

import java.nio.channels.SocketChannel;

public class PrivateMessageCommand implements Command {

    private GestoreMessaggiPrivati gestoreMessaggiPrivati;

    public PrivateMessageCommand(GestoreMessaggiPrivati gestoreMessaggiPrivati) {
        this.gestoreMessaggiPrivati = gestoreMessaggiPrivati;

    }

    @Override
    public void execute(SocketChannel client, String message) {
        gestoreMessaggiPrivati.inviaMessaggioPrivato(client, message);
    }
}
