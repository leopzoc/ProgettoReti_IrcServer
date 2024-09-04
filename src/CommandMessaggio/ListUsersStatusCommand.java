package CommandMessaggio;

import GestoreVisualizzaUtentiAdmin.GestoreVisualizzaUtentiAdmin;

import java.nio.channels.SocketChannel;

public class ListUsersStatusCommand implements Command {

    private final GestoreVisualizzaUtentiAdmin gestoreVisualizzaUtentiAdmin;

    public ListUsersStatusCommand(GestoreVisualizzaUtentiAdmin gestoreVisualizzaUtentiAdmin) {
        this.gestoreVisualizzaUtentiAdmin = gestoreVisualizzaUtentiAdmin;
    }

    @Override
    public void execute(SocketChannel client, String message) {
        // Esegui il comando per visualizzare la lista di utenti con lo stato
        gestoreVisualizzaUtentiAdmin.visualizzaUtenti(client);
    }
}
