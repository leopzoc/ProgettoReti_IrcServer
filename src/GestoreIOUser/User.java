package GestoreIOUser;


import java.nio.channels.SocketChannel;



import java.nio.channels.SocketChannel;
import java.util.UUID;

public class User {
    private String id;
    private String nick;
    private String password;
    private String status; // active, banned, etc.
    private String role;   // user, admin, etc.
    private String channel;
    private SocketChannel socketChannel;
    private String tempId; // ID temporaneo per i nomi duplicati



    // Costruttore per nuovi utenti, genera un nuovo UUID
    public User(String nick, String password, String status, String role, SocketChannel socketChannel) {
        this.id = UUID.randomUUID().toString(); // Genera un UUID univoco
        this.nick = nick;
        this.password = password;
        this.status = status;
        this.role = role;
        this.socketChannel = socketChannel;
        this.channel = ""; // Canale iniziale vuoto
    }

    // Costruttore per utenti esistenti (caricati da file)
    public User(String id, String nick, String password, String status, String role, SocketChannel socketChannel) {
        this.id = id;  // Usa l'UUID esistente
        this.nick = nick;
        this.password = password;
        this.status = status;
        this.role = role;
        this.socketChannel = socketChannel;
        this.channel = ""; // Canale iniziale vuoto
    }

    public String getID() {
        return id;
    }

    public String getNick() {
        return nick;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
    public String getTempId() {
        return tempId;
    }

    public void setTempId(String tempId) {
        this.tempId = tempId;
    }
}
