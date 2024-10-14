package CommandMessaggio;

import Connessione.ClientWriter;
import GestoreErrori.GestoreErrore;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.channels.SocketChannel;

public class errorCommand implements Command {
     GestoreErrore errore;
    public errorCommand(GestoreErrore errore) {
        this.errore = errore;
    }

    @Override
    public void execute(SocketChannel client, String message){
        errore.inviaErrore(client, message);
    }

}
