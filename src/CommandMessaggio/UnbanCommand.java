package CommandMessaggio;


import gestoreUnbanUtente.GestoreUnbanUtente;

import java.nio.channels.SocketChannel;

public class UnbanCommand implements Command {

    private final GestoreUnbanUtente gestoreUnbanUtente;

    public UnbanCommand(GestoreUnbanUtente gestoreUnbanUtente) {
        this.gestoreUnbanUtente = gestoreUnbanUtente;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        // Passa il messaggio JSON al gestore dell'unban
        gestoreUnbanUtente.unbanUtente(client, message);
    }
}
