package CommandMessaggio;

import GestorePromozione.GestoreUnpromuoviUtente;

import java.nio.channels.SocketChannel;

public class UnpromoteCommand implements Command {

    private final GestoreUnpromuoviUtente gestoreUnpromuoviUtente;

    public UnpromoteCommand(GestoreUnpromuoviUtente gestoreUnpromuoviUtente) {
        this.gestoreUnpromuoviUtente = gestoreUnpromuoviUtente;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        gestoreUnpromuoviUtente.unpromuoviUtente(client, message);
    }
}

