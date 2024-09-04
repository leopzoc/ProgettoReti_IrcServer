package CommandMessaggio;

import gestoreListEUsers.GestoreListEUsers;

import java.nio.channels.SocketChannel;

public class ListAndUsersCommand implements Command {

    GestoreListEUsers gestoreListEUsers;

    public ListAndUsersCommand(GestoreListEUsers gestoreListEUsers) {
        this.gestoreListEUsers = gestoreListEUsers;
    }

    @Override
    public void execute(SocketChannel client, String command) {
        gestoreListEUsers.seeList(client,command);

    }
}
