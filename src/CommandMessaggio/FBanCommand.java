package CommandMessaggio;



import GestoreBanDefinitivo.GestoreFBanUtente;

import java.nio.channels.SocketChannel;

public class FBanCommand implements Command {

    private final GestoreFBanUtente gestoreFBanUtente;

    public FBanCommand(GestoreFBanUtente gestoreFBanUtente) {
        this.gestoreFBanUtente = gestoreFBanUtente;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        gestoreFBanUtente.fbanUtente(client, message);
    }
}
