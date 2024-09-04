package CommandMessaggio;

import GestorePromozione.GestorePromuoviUtente;

import java.nio.channels.SocketChannel;

public class PromoteCommand implements Command {

    private final GestorePromuoviUtente gestorePromuoviUtente;

    public PromoteCommand(GestorePromuoviUtente gestorePromuoviUtente) {
        this.gestorePromuoviUtente = gestorePromuoviUtente;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        gestorePromuoviUtente.promuoviUtente(client, message);
    }
}
