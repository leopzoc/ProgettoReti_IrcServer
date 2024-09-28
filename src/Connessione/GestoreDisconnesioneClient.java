package Connessione;

import GestoreIOUser.User;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class GestoreDisconnesioneClient implements IGestoreDisconnesioneClient {

    private final Map<String, Set<SocketChannel>> channels; // associa un canale a un set di socketClient
    private final Map<SocketChannel, User> connectedUsers; // associa un socketClient al rispettivo utente
    private final Map<SocketChannel, ByteBuffer> pendingData;
    private final Selector selector;
    private final Map<String, Map<String, String>> duplicateUsersMap;
    private final Map<String, Integer> tempIdCounters;

    public GestoreDisconnesioneClient(Map<String, Set<SocketChannel>> channels,
                                      Map<SocketChannel, User> connectedUsers,
                                      Map<SocketChannel, ByteBuffer> pendingData,
                                      Selector selector,
                                      Map<String, Map<String, String>> duplicateUsersMap,
                                      Map<String, Integer> tempIdCounters) {
        this.channels = channels;
        this.connectedUsers = connectedUsers;
        this.pendingData = pendingData;
        this.selector = selector;
        this.duplicateUsersMap = duplicateUsersMap;
        this.tempIdCounters = tempIdCounters;
    }

    //disconnetti il client
    @Override
    public void handleClientDisconnection(SocketChannel client) {
        try {
            if (client != null && client.isOpen()) {
                SelectionKey key = client.keyFor(selector);
                if (key != null) {
                    key.cancel();
                }
                client.close();
            }

            User user = connectedUsers.remove(client);
            pendingData.remove(client);
            channels.values().forEach(clients -> clients.remove(client));

            // Rimuovi l'ID temporaneo se presente
            if (user != null) {
                removeTempIdOnDisconnect(user);
            }

            System.out.println("Disconnected: " + (client != null ? client.getRemoteAddress() : "client null"));
        } catch (IOException e) {
            System.err.println("Errore durante la gestione della disconnessione del client: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Errore non previsto durante la disconnessione: " + e.getMessage());
        }
    }

    // Metodo per rimuovere l'ID temporaneo durante la disconnessione
    public void removeTempIdOnDisconnect(User user) {
        String nick = user.getNick();
        String userId = user.getID();

        Map<String, String> tempIdMap = duplicateUsersMap.get(nick);
        if (tempIdMap != null) {
            tempIdMap.remove(userId);

            // Se non ci sono più ID temporanei per questo nick, rimuovi l'intero mapping
            if (tempIdMap.isEmpty()) {
                duplicateUsersMap.remove(nick);
                tempIdCounters.remove(nick);  // Rimuovi anche il contatore se non ci sono più utenti con questo nickname
            }
        }
    }
}


        /*
        @Override
        public void handleClientDisconnection(SocketChannel client) {
            try {
                if (client != null && client.isOpen()) {
                    SelectionKey key = client.keyFor(selector);
                    if (key != null) {
                        key.cancel();
                    }
                    client.close();
                }

                // Rimuovi l'utente da connectedUsers e ottieni l'oggetto User rimosso
                User user = connectedUsers.remove(client);

                if (user != null) {
                    // Rimuovi il client da tutti i canali in cui è presente
                    channels.values().forEach(clients -> clients.remove(client));

                    // Rimuovi il buffer di dati pendenti
                    pendingData.remove(client);

                    // Se l'utente aveva un ID temporaneo, rimuovilo dalla mappa dei duplicati
                    String tempId = user.getTempId();
                    if (tempId != null && duplicateUsersMap.containsKey(user.getNick())) {
                        Map<String, String> tempMap = duplicateUsersMap.get(user.getNick());
                        tempMap.remove(user.getID());

                        // Se la mappa è vuota, rimuovila completamente
                        if (tempMap.isEmpty()) {
                            duplicateUsersMap.remove(user.getNick());
                        }

                        // Aggiorna il contatore degli ID temporanei
                        tempIdCounters.computeIfPresent(user.getNick(), (nick, counter) -> counter - 1);
                    }
                }

                System.out.println("Disconnected: " + (client != null ? client.getRemoteAddress() : "client null"));
            } catch (IOException e) {
                System.err.println("Errore durante la gestione della disconnessione del client: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Errore non previsto durante la disconnessione: " + e.getMessage());
            }
        }

         */


