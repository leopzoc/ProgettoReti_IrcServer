package CommandMessaggio;

import GestoreBanDefinitivo.GestoreFBanUtente;
import GestoreBanDefinitivo.GestoreFunbanUtente;
import GestoreCambioCanale.SwitchChannelChange;
import GestoreDegliInvii.BroadcastMessage;
import GestoreDeiBan.GestoreBanUtente;
import GestoreIOUser.GestoreLogin;
import GestoreIOUser.User;
import GestoreKick.GestoreKickCanale;
import GestoreMessaggioPrivato.GestoreMessaggiPrivati;
import GestorePromozione.GestorePromuoviUtente;
import GestorePromozione.GestoreUnpromuoviUtente;
import GestoreViewUser.GestoreViewUser;
import GestoreVisualizzaUtentiAdmin.GestoreVisualizzaUtentiAdmin;
import gestoreListEUsers.GestoreListEUsers;
import gestoreListView.GestoreListView;
import gestoreUnbanUtente.GestoreUnbanUtente;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

//invoker
public class CommandHandler {

    private final Map<String, Command> commonCommands = new HashMap<>();
    private final Map<String, Command> adminCommands = new HashMap<>();
    private final Map<SocketChannel,User> connectedUsers;


    public CommandHandler(GestoreLogin gestoreLogin, BroadcastMessage sendMessageBroadcastCommand, SwitchChannelChange swichChannelChange, GestoreListView gestoreListView, GestoreViewUser gestoreViewUser, GestoreListEUsers gestoreListEUser, GestoreMessaggiPrivati gestoreMessaggiPrivati, GestoreKickCanale gestorekickCanale, GestoreBanUtente gestoreBanUtente, GestoreUnbanUtente gestoreUnBan, GestoreFBanUtente gestoreFBanUtente, GestoreFunbanUtente gestoreFunbanUtente, GestoreVisualizzaUtentiAdmin gestoreVisualizzaUtentiAdmin, GestorePromuoviUtente gestorePromuoviUtente, GestoreUnpromuoviUtente gestoreUnpromuoviUtente, Map<SocketChannel, User> connectedUsers) {
        // Aggiungi il comando di login
        this.connectedUsers = connectedUsers;
        commonCommands.put("login", new LoginCommand(gestoreLogin));
        commonCommands.put("send_message", new SendMessageBroadcastCommand(sendMessageBroadcastCommand));
        commonCommands.put("switch_channel", new SwitchChannelCommand(swichChannelChange));
        commonCommands.put("list", new ListCommand(gestoreListView));
        commonCommands.put("users", new UsersListCommand(gestoreViewUser));
        commonCommands.put("privmsg",new PrivateMessageCommand(gestoreMessaggiPrivati));
        commonCommands.put("lu",new ListAndUsersCommand(gestoreListEUser));
        // Comandi amministrativi
        adminCommands.put("kick", new KickUserCommand(gestorekickCanale));
        adminCommands.put("ban", new BanCommand(gestoreBanUtente));
        adminCommands.put("unban", new UnbanCommand(gestoreUnBan));
        adminCommands.put("fban", new FBanCommand(gestoreFBanUtente));
        adminCommands.put("funban", new FunbanCommand(gestoreFunbanUtente));
        adminCommands.put("fusers", new ListUsersStatusCommand(gestoreVisualizzaUtentiAdmin));
        adminCommands.put("promote", new PromoteCommand(gestorePromuoviUtente));
        adminCommands.put("unpromote", new UnpromoteCommand(gestoreUnpromuoviUtente));

        // Altri comandi amministrativi possono essere aggiunti qui
    }


    public void handleCommand(String command, SocketChannel client, String message) {
        // Gestione dei comandi comuni
        User user = connectedUsers.get(client);
        if (commonCommands.containsKey(command)) {
            commonCommands.get(command).execute(client, message);
        }
        // Gestione dei comandi amministrativi
        else if (adminCommands.containsKey(command)) {
            if (user != null && user.getRole().equals("admin")) {
                adminCommands.get(command).execute(client, message);
            } else {
                System.out.println(client  + user.getID() + "accesso negato al comando "+ command);
            }
        } else {
            System.out.println("Unknown command: " + command + client + user.getID());
        }
    }
}



/*
public class CommandHandler {

    private final Map<String, Command> commonCommands = new HashMap<>();
    private final Map<String, Command> adminCommands = new HashMap<>();

    public CommandHandler(UserService userService, ChannelService channelService, MessageService messageService) {
        // Comandi comuni
        commonCommands.put("login", new LoginCommand(userService, channelService));
        commonCommands.put("send_message", new SendMessageCommand(messageService, userService));
        commonCommands.put("switch_channel", new SwitchChannelCommand(channelService, userService));

        // Comandi amministrativi
        adminCommands.put("kick_user", new KickUserCommand(userService, channelService));
        adminCommands.put("ban_user", new BanUserCommand(userService));
        // Altri comandi amministrativi possono essere aggiunti qui
    }

    public void handleCommand(String command, SocketChannel client, String message) {
        User user = userService.getUser(client);

        // Gestione dei comandi comuni
        if (commonCommands.containsKey(command)) {
            commonCommands.get(command).execute(client, message);
        }
        // Gestione dei comandi amministrativi
        else if (adminCommands.containsKey(command)) {
            if (user != null && user.getRole().equals("admin")) {
                adminCommands.get(command).execute(client, message);
            } else {
                userService.writeToClient(client, "Access denied: You do not have the necessary privileges to execute this command.");
            }
        } else {
            System.out.println("Unknown command: " + command);
        }
    }

 */
