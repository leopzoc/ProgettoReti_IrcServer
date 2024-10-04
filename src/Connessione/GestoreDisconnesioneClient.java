package Connessione;

import GestoreIOUser.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    private final ClientWriter clientWriter;

    public GestoreDisconnesioneClient(Map<String, Set<SocketChannel>> channels,
                                      Map<SocketChannel, User> connectedUsers,
                                      Map<SocketChannel, ByteBuffer> pendingData,
                                      Selector selector,
                                      Map<String, Map<String, String>> duplicateUsersMap,
                                      Map<String, Integer> tempIdCounters, ClientWriter clientWriter) {
        this.channels = channels;
        this.connectedUsers = connectedUsers;
        this.pendingData = pendingData;
        this.selector = selector;
        this.duplicateUsersMap = duplicateUsersMap;
        this.tempIdCounters = tempIdCounters;
        this.clientWriter = clientWriter;
    }

    //disconnetti il client
    @Override
    public void handleClientDisconnection(SocketChannel client) {
        try {
            // Verifica che il client non sia null e che il socket sia ancora aperto
            if (client != null && client.isOpen()) {
                SelectionKey key = client.keyFor(selector);
                if (key != null) {
                    key.cancel();  // Cancella la chiave di selezione
                }

                // Prova a chiudere il socket e logga il risultato
                try {
                    System.out.println("Tentativo di chiusura del socket per: " + client.getRemoteAddress());
                    client.close();
                    System.out.println("Socket chiuso con successo per: " + client.getRemoteAddress());
                } catch (IOException e) {
                    System.err.println("Errore durante la chiusura del socket: " + e.getMessage());
                }
            } else {
                System.out.println("Client già disconnesso o null.");
            }

            // Rimozione dell'utente dalle strutture dati
            User user = connectedUsers.remove(client);
            if (user != null) {
                System.out.println("Utente disconnesso: " + user.getNick());

                // Rimuovi l'ID temporaneo se presente
                removeTempIdOnDisconnect(user);

                // Rimuovi il client da tutti i canali
                channels.values().forEach(clients -> clients.remove(client));

                // Rimuovi eventuali dati pendenti associati al client
                pendingData.remove(client);

                // Logga la rimozione completa
                System.out.println("Rimozione del client completata per l'utente: " + user.getNick());
            } else {
                System.out.println("Nessun utente associato trovato per questo client.");
            }

        } catch (Exception e) {
            System.err.println("Errore non previsto durante la disconnessione: " + e.getMessage());
        }
    }


    public void handleClientDisconnection(SocketChannel client, String message) {
        try {
            // Parsing del messaggio JSON
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();

            // Verifica che il JSON contenga i campi necessari
            if (jsonMessage.has("command") && jsonMessage.has("nick")) {
                String username = jsonMessage.get("nick").getAsString();
                String command = jsonMessage.get("command").getAsString();

                if ("disconnect".equals(command)) {
                    // Invia un messaggio al client che sta per essere disconnesso
                    JsonObject response = new JsonObject();
                    response.addProperty("status", "info");
                    response.addProperty("message", "You are about to be disconnected. Goodbye, " + username);
                    clientWriter.writeToClient(client, response.toString());


                    // Procedura di disconnessione
                    handleClientDisconnection(client);
                } else {
                    System.out.println("Comando sconosciuto: " + command);
                }
            } else {
                System.out.println("JSON non valido, mancano i campi richiesti.");
            }
        } catch (Exception e) {
            System.err.println("Errore durante la gestione della disconnessione del client: " + e.getMessage());
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


