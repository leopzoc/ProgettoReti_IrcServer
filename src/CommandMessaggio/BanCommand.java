package CommandMessaggio;

import GestoreDeiBan.GestoreBanUtente;

import java.nio.channels.SocketChannel;

public class BanCommand implements Command {

    private final GestoreBanUtente gestoreBanUtente;

    public BanCommand(GestoreBanUtente gestoreBanUtente) {
        this.gestoreBanUtente = gestoreBanUtente;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        // Passa il messaggio JSON al gestore del ban
        gestoreBanUtente.banUtente(client, message);
    }
}
