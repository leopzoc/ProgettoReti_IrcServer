package CommandMessaggio;

import GestoreDeiBan.GestoreBanUtente;

import java.nio.channels.SocketChannel;

public class BanCommand implements Command {
//gestore del ban
    private final GestoreBanUtente gestoreBanUtente;
//ban command
    public BanCommand(GestoreBanUtente gestoreBanUtente) {
        this.gestoreBanUtente = gestoreBanUtente;
    }
//esecuzione del ban command
    @Override
    public void execute(SocketChannel client, String message) {
        // Passa il messaggio JSON al gestore del ban
        gestoreBanUtente.banUtente(client, message);
    }
}
