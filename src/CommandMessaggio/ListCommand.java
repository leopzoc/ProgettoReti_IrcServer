package CommandMessaggio;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gestoreListView.GestoreListView;

import java.nio.channels.SocketChannel;

public class ListCommand implements Command {

    GestoreListView listView;
    public ListCommand(GestoreListView listView) {
        this.listView = listView;

    }


    @Override
    public void execute(SocketChannel client, String listCommand) {

            listView.seeList(client, listCommand);




    }
}
