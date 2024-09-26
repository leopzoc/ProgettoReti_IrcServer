package server;

import CommandMessaggio.CommandHandler;
import Connessione.*;
import GestoreBanDefinitivo.GestoreFBanUtente;
import GestoreBanDefinitivo.GestoreFunbanUtente;
import GestoreCambioCanale.SwitchChannelChange;
import GestoreDegliInvii.BroadcastMessage;
import GestoreDeiBan.GestoreBanUtente;
import GestoreIOUser.GestoreLogin;
import GestoreIOUser.GestoreUtenti;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;




    public class Server {
        private final Map<String, Set<SocketChannel>> channels; //StringCanale #gereal -> lista di canali connessi
        private final Map<SocketChannel, User> connectedUsers = new ConcurrentHashMap<>(); // canaleSocket -> utente
        private final Map<SocketChannel, ByteBuffer> pendingData = new ConcurrentHashMap<>(); // socketcanale del utente -> buffer associato
        //il nome l'abbiamo lasciato come broadcastExecutor come ricordo della sua prima funzione asincrona ossia essere utilizzata solo come Executor per mandare messaggi in broadcast ora gestisce tutti gli altri invii
        private final ExecutorService broadcastExecutor = Executors.newCachedThreadPool(); // per invio dei messaggi in broadcast, con la nuova implementazione gestisce anche altri tipi di invio

        private final Map<String, Set<String>> bannedUsersByChannel = new ConcurrentHashMap<>(); //persone bannate dai canali "stringe" no channelsocket

        private final Map<String, Map<String, String>> duplicateUsersMap = new ConcurrentHashMap<>();//mappa nick -> (ID univoco : id temporaneo)
        /*
        duplicateUsersMap = {
    "leo": {
        "e0fbc7dc-667b-4292-a836-d8f52c236984": "00001",
        "8224d5b0-a5be-48cd-8d26-ddc45af03db6": "00002"
    }
}
         */
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        /*
        Chiave (String): La chiave è il nickname dell'utente. Questa chiave rappresenta il nome dell'utente per cui stiamo gestendo gli ID temporanei.
Valore (Integer): Il valore è un contatore che tiene traccia dell'ultimo ID temporaneo assegnato a un utente con quel nickname.
         */

        private final Map<String, Integer> tempIdCounters = new ConcurrentHashMap<>(); //


        private static final String userFilePath = "users.txt";
        private String IP;
        private int port;
        private final Selector selector;
        private GestoreUtenti gestoreUtenti;

        private final IGestoreAccettazione gestoreAccettazione;
        private final IGestoreLetturaClient gestioneLetturaClient;

        {
            try {
                selector = Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Server(Configurazione configurazione, GestoreUtenti gestoreUtenti) throws IOException {
            this.channels = configurazione.getChannels();
            this.IP = configurazione.getIP();
            this.port = configurazione.getPort();
            this.gestoreUtenti = gestoreUtenti;




            //inizializzatore del client writer
            ClientWriter clientWriter = new ClientWriterImpl(pendingData);
            IGestoreDisconnesioneClient gestoreDisconnesioneClient = new GestoreDisconnesioneClient(channels, connectedUsers, pendingData, selector,duplicateUsersMap,tempIdCounters);
            //inizializzo i gestori
            GestoreLogin gestoreLogin = new GestoreLogin(connectedUsers, clientWriter,channels,duplicateUsersMap,tempIdCounters);
            BroadcastMessage gestoreSendBrodcastMessage = new BroadcastMessage(connectedUsers,broadcastExecutor,channels,clientWriter,duplicateUsersMap);
            SwitchChannelChange switchChannelChange = new SwitchChannelChange(connectedUsers,channels,bannedUsersByChannel,clientWriter);
            GestoreListView listView = new GestoreListView(channels,clientWriter,broadcastExecutor);
            GestoreViewUser gestoreViewUser = new GestoreViewUser(channels,connectedUsers,clientWriter,duplicateUsersMap,broadcastExecutor);
            GestoreMessaggiPrivati gestoreMessaggiPrivati = new GestoreMessaggiPrivati(connectedUsers,clientWriter,duplicateUsersMap,broadcastExecutor);
            GestoreKickCanale gestoreKickCanale = new GestoreKickCanale(connectedUsers,channels,clientWriter,duplicateUsersMap);
            GestoreListEUsers gestoreListEUsers = new GestoreListEUsers(channels,connectedUsers,clientWriter,broadcastExecutor);
            GestoreBanUtente gestoreBanUtente = new GestoreBanUtente(connectedUsers,clientWriter,channels,bannedUsersByChannel,duplicateUsersMap);
            GestoreUnbanUtente gestoreUnbanUtente = new GestoreUnbanUtente(bannedUsersByChannel,connectedUsers,clientWriter);
            GestoreFBanUtente gestoreFBanUtente = new GestoreFBanUtente(connectedUsers,clientWriter, (GestoreDisconnesioneClient) gestoreDisconnesioneClient);
            GestoreFunbanUtente gestoreFunbanUtente = new GestoreFunbanUtente(clientWriter,userFilePath);
            GestoreVisualizzaUtentiAdmin gestoreVisualizzaUtentiAdmin = new GestoreVisualizzaUtentiAdmin(connectedUsers,clientWriter,userFilePath);
            GestorePromuoviUtente gestorePromuoviUtente = new GestorePromuoviUtente(connectedUsers,clientWriter,userFilePath);
            GestoreUnpromuoviUtente gestoreUnpromuoviUtente = new GestoreUnpromuoviUtente(connectedUsers,clientWriter,userFilePath);
            CommandHandler commandHandler = new CommandHandler(gestoreLogin, gestoreSendBrodcastMessage,switchChannelChange,listView,gestoreViewUser,gestoreListEUsers,gestoreMessaggiPrivati,gestoreKickCanale,gestoreBanUtente,gestoreUnbanUtente,gestoreFBanUtente,gestoreFunbanUtente,gestoreVisualizzaUtentiAdmin,gestorePromuoviUtente,gestoreUnpromuoviUtente,connectedUsers);

            gestoreAccettazione = new GestoreAccettazione();
            gestioneLetturaClient = new GestioneLetturaClient(gestoreDisconnesioneClient, commandHandler);
        }

        public static void main(String[] args) {
            try {
                // Configurazione del server
                Map<String, Set<SocketChannel>> channels = new ConcurrentHashMap<>();
                Configurazione configurazione = new Configurazione(channels, "", 0);

                // Inizializza GestoreUtenti qui
                GestoreUtenti gestoreUtenti = new GestoreUtenti(userFilePath);

                configurazione.configurazioneServer();

                // Creazione e avvio del server con la configurazione
                Server server = new Server(configurazione, gestoreUtenti);
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



//metodo di start del server
        public void start() {
            //apertura e avvio del serversocketchannel stiamo usando java nio
            try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.socket().bind(new InetSocketAddress(IP, port));
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
                Map<SocketChannel, ByteBuffer> buffers = new ConcurrentHashMap<>(); // mappa per gestire i ByteBuffer dei client

                System.out.println("Server avviato: \n in attesa di connessioni:");

                while (true) {
                    if (selector.select() == 0) continue; // conta i client pronti a essere "accettati o trasmettere o essere scritti"

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    List<SelectionKey> selectionKeyList = new ArrayList<>(selectedKeys); //una copia del selectedkeys

                    for (SelectionKey key : selectionKeyList) {
                        if (key.isAcceptable()) {
                            gestoreAccettazione.Accettazione(key, selector, buffers); // Passiamo buffers, non pendingData
                        } else if (key.isReadable()) {
                            gestioneLetturaClient.leggoClient(key, buffers); // Passiamo buffers, non pendingData
                        } else if (key.isWritable()) {
                            scrivoAlClient(key);  // Questo può rimanere invariato se pendingData è ancora corretto qui
                        }
                    }
                    selectedKeys.clear(); // Pulisci le chiavi gestite
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
            shutdownServer();
            }
        }
//chiudi tutto nel broadcastExecutor
        private void shutdownServer() {
            broadcastExecutor.shutdown();
            for (var clients : channels.values()) {
                for (var client : clients) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }


    // routine del server per scrivere i dati pendenti (potevamo farlo girare su un thread)
        private void scrivoAlClient(SelectionKey key) throws IOException {
            SocketChannel client = (SocketChannel) key.channel(); //client dove scrivere
            ByteBuffer buffer = pendingData.get(client); //ci prendiamo i dati pendenti

            if (buffer != null) { //se non è nullo
                client.write(buffer); //scrivi

                if (!buffer.hasRemaining()) { //se il buffer non ha cose rimanenti togli il client dalla pendingdata
                    pendingData.remove(client);  // Rimuovi il buffer se tutti i dati sono stati inviati
                    key.interestOps(SelectionKey.OP_READ);  // Torna a leggere solo
                }
            }
        }
    }



