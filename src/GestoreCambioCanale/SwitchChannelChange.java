package GestoreCambioCanale;

import Connessione.ClientWriter;
import GestoreIOUser.User;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SwitchChannelChange implements ISwitchChannelChange {

    Map<SocketChannel, User> connectedUsers;
    Map<String, Set<SocketChannel>> channels;
    Map<String, Set<String>> bannedUsersByChannel; // Mappa canale -> utenti bannati (ID)
    ClientWriter clientWriter;

    public SwitchChannelChange(Map<SocketChannel, User> connectedUsers, Map<String, Set<SocketChannel>> channels,
                               Map<String, Set<String>> bannedUsersByChannel, ClientWriter clientWriter) {
        this.connectedUsers = connectedUsers;
        this.channels = channels;
        this.bannedUsersByChannel = bannedUsersByChannel;
        this.clientWriter = clientWriter;
    }

    @Override
    public void switchChannel(SocketChannel client, String channelName) throws IOException {
        User user = connectedUsers.get(client);

        if (user != null) {
            // Verifica se l'utente è bannato dal canale di destinazione
            Set<String> bannedUsers = bannedUsersByChannel.get(channelName);
            if (bannedUsers != null && bannedUsers.contains(user.getID())) {
                // L'utente è bannato, invia un messaggio di errore in formato JSON
                JsonObject response = new JsonObject();
                response.addProperty("status", "error");
                response.addProperty("message", "Non puoi unirti al canale " + channelName + " perché sei stato bannato.");
                clientWriter.writeToClient(client, response.toString());

                System.out.println("User " + user.getNick() + " tried to join a banned channel: " + channelName);
                return;
            }

            // Rimuovi l'utente dal canale corrente, se presente
            Set<SocketChannel> currentChannelClients = channels.get(user.getChannel());
            if (currentChannelClients != null) {
                currentChannelClients.remove(client);
            }

            // Aggiungi l'utente al nuovo canale
            user.setChannel(channelName);
            channels.computeIfAbsent(channelName, k -> ConcurrentHashMap.newKeySet()).add(client);

            System.out.println("User " + user.getNick() + " switched to channel: " + channelName);

            // Invia messaggio di conferma in formato JSON
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "You have joined " + channelName);
            clientWriter.writeToClient(client, response.toString());
        }
    }
}


/*
Modifiche principali:
Messaggi in formato JSON:

Se l'utente è bannato dal canale di destinazione, viene inviato un messaggio di errore con un oggetto JSON:
json

{
  "status": "error",
  "message": "Non puoi unirti al canale channelName perché sei stato bannato."
}
Se l'utente riesce a cambiare canale, viene inviato un messaggio di conferma in formato JSON:
json

{
  "status": "success",
  "message": "You have joined channelName."
}
 */