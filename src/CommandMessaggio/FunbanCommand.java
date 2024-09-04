package CommandMessaggio;

import GestoreBanDefinitivo.GestoreFunbanUtente;

import java.nio.channels.SocketChannel;

public class FunbanCommand implements Command {

    private final GestoreFunbanUtente gestoreFunbanUtente;

    public FunbanCommand(GestoreFunbanUtente gestoreFunbanUtente) {
        this.gestoreFunbanUtente = gestoreFunbanUtente;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        // Esegui il comando di sbannamento usando il gestore
        gestoreFunbanUtente.funbanUtente(client, message);
    }
}
