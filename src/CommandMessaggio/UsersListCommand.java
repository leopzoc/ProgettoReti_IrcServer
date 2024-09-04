package CommandMessaggio;

import GestoreViewUser.GestoreViewUser;
import gestoreListEUsers.GestoreListEUsers;
import gestoreListView.GestoreListView;

import java.nio.channels.SocketChannel;

public class UsersListCommand implements Command {
    GestoreViewUser listView;
    public UsersListCommand(GestoreViewUser listView) {
        this.listView = listView;

    }

    @Override
    public void execute(SocketChannel client, String listCommand) {

        listView.seeList(client, listCommand);




    }
}
