package client3;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class SimpleIrcClient {

    private JFrame frame;
    private JTextField ipField;
    private JTextField portField;
    private JTextField nickField;
    private JPasswordField passwordField;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton connectButton;
    private JButton loginButton;
    private JButton registerButton;
    private JButton sendButton;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    private String nick;  // Nickname dell'utente loggato
    private Gson gson;    // Istanza di Gson per la gestione dei JSON

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleIrcClient::new);
    }

    public SimpleIrcClient() {
        gson = new Gson();
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Simple IRC Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        // Top panel per IP e porta
        JPanel connectionPanel = new JPanel(new GridLayout(2, 2));
        ipField = new JTextField("127.0.0.1");
        portField = new JTextField("5000");
        connectButton = new JButton("Connect");

        connectionPanel.add(new JLabel("IP:"));
        connectionPanel.add(ipField);
        connectionPanel.add(new JLabel("Port:"));
        connectionPanel.add(portField);

        // Middle panel per nickname, password, login e register
        JPanel authPanel = new JPanel(new GridLayout(3, 2));
        nickField = new JTextField();
        passwordField = new JPasswordField();
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        // Disabilita i pulsanti login e register finché non si è connessi
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        authPanel.add(new JLabel("Nickname:"));
        authPanel.add(nickField);
        authPanel.add(new JLabel("Password:"));
        authPanel.add(passwordField);
        authPanel.add(loginButton);
        authPanel.add(registerButton);

        // Unisci i due pannelli superiori
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(connectionPanel, BorderLayout.NORTH);
        topPanel.add(connectButton, BorderLayout.CENTER);
        topPanel.add(authPanel, BorderLayout.SOUTH);

        frame.add(topPanel, BorderLayout.NORTH);

        // Center panel per visualizzare i messaggi
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel per input e invio messaggi
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);  // Disabilita il pulsante send finché non si è loggati

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Action listeners
        connectButton.addActionListener(new ConnectButtonListener());
        loginButton.addActionListener(new LoginButtonListener());
        registerButton.addActionListener(new RegisterButtonListener());
        sendButton.addActionListener(new SendButtonListener());
        inputField.addActionListener(new SendButtonListener());  // Permette di inviare con Enter

        frame.setVisible(true);
    }

    // UC_Login
    private void login() throws IOException {
        nick = nickField.getText();
        String password = new String(passwordField.getPassword());
        String loginJson = gson.toJson(new LoginCommand(nick, password));
        sendMessage(loginJson);
    }

    // UC_Register
    private void register() throws IOException {
        nick = nickField.getText();
        String password = new String(passwordField.getPassword());
        String registerJson = gson.toJson(new RegisterCommand(nick, password));
        sendMessage(registerJson);
    }

    // UC_SwitchChannel
    private void switchChannel(String channel) throws IOException {
        String switchChannelJson = gson.toJson(new SwitchChannelCommand(channel));
        sendMessage(switchChannelJson);
    }

    // UC_ChannelMessage
    private void sendChannelMessage(String message) throws IOException {
        String messageJson = gson.toJson(new ChannelMessageCommand(message));
        sendMessage(messageJson);
    }

    // UC_PrivateMessage
    private void sendPrivateMessage(String recipient, String message) throws IOException {
        String privMsgJson = gson.toJson(new PrivateMessageCommand(recipient, message));
        sendMessage(privMsgJson);
    }

    // UC_Disconnect
    private void disconnect() throws IOException {
        String disconnectJson = gson.toJson(new DisconnectCommand(nick));
        sendMessage(disconnectJson);
        closeConnection();
    }

    // UC_SendMessage
    private void sendMessage(String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageBytes.length);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);
        buffer.flip();
        out.write(buffer.array());
        out.flush();
    }

    // UC_ReceiveMessage
    private String receiveMessage() throws IOException {
        int length;
        try {
            length = in.readInt();
        } catch (EOFException e) {
            throw new IOException("Server closed the connection.");
        }

        if (length > 0) {
            byte[] responseBytes = new byte[length];
            in.readFully(responseBytes);
            return new String(responseBytes, StandardCharsets.UTF_8);
        }
        return null;
    }

    private void closeConnection() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            messageArea.append("Disconnected from server.\n");
            // Disabilita i pulsanti dopo la disconnessione
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);
            sendButton.setEnabled(false);
        } catch (IOException e) {
            messageArea.append("Error closing connection.\n");
        }
    }

    // Listener per il pulsante Connect
    private class ConnectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 2000);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                messageArea.append("Connected to " + ip + ":" + port + "\n");

                // Abilita i pulsanti login e register
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);

                // Avvia thread per ricevere messaggi
                new Thread(new Receiver()).start();

            } catch (IOException ex) {
                messageArea.append("Failed to connect to " + ip + ":" + port + "\n");
            }
        }
    }

    // Listener per il pulsante Login
    private class LoginButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                login();
            } catch (IOException ex) {
                messageArea.append("Error during login.\n");
            }
        }
    }

    // Listener per il pulsante Register
    private class RegisterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                register();
            } catch (IOException ex) {
                messageArea.append("Error during registration.\n");
            }
        }
    }

    // Listener per il pulsante Send
    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = inputField.getText().trim();
            if (input.isEmpty()) return;

            try {
                if (input.startsWith("/")) {
                    handleCommand(input);
                } else {
                    sendChannelMessage(input);  // Invia messaggio al canale
                    // Append il messaggio inviato alla messageArea
                    messageArea.append("You: " + input + "\n");
                }
                inputField.setText("");
            } catch (IOException ex) {
                messageArea.append("Error sending message.\n");
            }
        }

        private void handleCommand(String message) throws IOException {
            // Questa funzione gestisce tutti i comandi elencati
            String command;
            String recipient = "";
            String uuid = "";
            String channel = "";
            String[] parts;

            if (message.startsWith("/join")) {
                command = "switch_channel";
                parts = message.substring(5).trim().split(" ", 2);
                if (parts.length >= 1) {
                    channel = parts[0].trim();
                    switchChannel(channel);
                } else {
                    messageArea.append("Usage: /join <channel>\n");
                }
            } else if (message.startsWith("/privmsg")) {
                command = "privmsg";
                parts = message.substring(8).trim().split(" ", 2);
                if (parts.length == 2) {
                    recipient = parts[0].trim();
                    String privMessage = parts[1].trim();
                    sendPrivateMessage(recipient, privMessage);
                    // Append il messaggio inviato alla messageArea
                    messageArea.append("To " + recipient + ": " + privMessage + "\n");
                } else {
                    messageArea.append("Usage: /privmsg <recipient> <message>\n");
                }
            } else if (message.startsWith("/disconnect")) {
                disconnect();
            } else if (message.startsWith("/msg")) {
                command = "send_message";
                String content = message.substring(4).trim();
                sendChannelMessage(content);
                // Append il messaggio inviato alla messageArea
                messageArea.append("You: " + content + "\n");
            } else if (message.startsWith("/fban")) {
                command = "fban";
                parts = message.substring(5).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (target.contains("-")) {  // UUID
                        uuid = target;
                        sendMessage(gson.toJson(new CommandWithUuid(command, uuid)));
                    } else {
                        sendMessage(gson.toJson(new CommandWithMessage(command, target)));
                    }
                    messageArea.append("Command sent: " + message + "\n");
                } else {
                    messageArea.append("Usage: /fban <uuid|nick:tempid>\n");
                }
            } else if (message.startsWith("/funban")) {
                command = "funban";
                parts = message.substring(7).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (target.contains("-")) {  // UUID
                        uuid = target;
                        sendMessage(gson.toJson(new CommandWithUuid(command, uuid)));
                        messageArea.append("Command sent: " + message + "\n");
                    } else {
                        messageArea.append("Usage: /funban <uuid>\n");
                    }
                } else {
                    messageArea.append("Usage: /funban <uuid>\n");
                }
            } else if (message.startsWith("/promote")) {
                command = "promote";
                parts = message.substring(8).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (target.contains("-")) {  // UUID
                        uuid = target;
                        sendMessage(gson.toJson(new CommandWithUuid(command, uuid)));
                    } else {
                        sendMessage(gson.toJson(new CommandWithMessage(command, target)));
                    }
                    messageArea.append("Command sent: " + message + "\n");
                } else {
                    messageArea.append("Usage: /promote <uuid|nick:tempid>\n");
                }
            } else if (message.startsWith("/unpromote")) {
                command = "unpromote";
                parts = message.substring(10).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (target.contains("-")) {  // UUID
                        uuid = target;
                        sendMessage(gson.toJson(new CommandWithUuid(command, uuid)));
                    } else {
                        sendMessage(gson.toJson(new CommandWithMessage(command, target)));
                    }
                    messageArea.append("Command sent: " + message + "\n");
                } else {
                    messageArea.append("Usage: /unpromote <uuid|nick:tempid>\n");
                }
            } else if (message.startsWith("/kick")) {
                command = "kick";
                parts = message.substring(5).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (parts.length == 2) {
                        channel = parts[1].trim();
                        sendMessage(gson.toJson(new CommandWithMessageAndChannel(command, target, channel)));
                    } else {
                        sendMessage(gson.toJson(new CommandWithMessage(command, target)));
                    }
                    messageArea.append("Command sent: " + message + "\n");
                } else {
                    messageArea.append("Usage: /kick <nick:tempid> [channel]\n");
                }
            } else if (message.startsWith("/ban")) {
                command = "ban";
                parts = message.substring(4).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (parts.length == 2) {
                        channel = parts[1].trim();
                        sendMessage(gson.toJson(new CommandWithMessageAndChannel(command, target, channel)));
                    } else {
                        sendMessage(gson.toJson(new CommandWithMessage(command, target)));
                    }
                    messageArea.append("Command sent: " + message + "\n");
                } else {
                    messageArea.append("Usage: /ban <nick:tempid> [channel]\n");
                }
            } else if (message.startsWith("/unban")) {
                command = "unban";
                parts = message.substring(6).trim().split(" ", 2);
                if (parts.length >= 1) {
                    String target = parts[0].trim();
                    if (parts.length == 2) {
                        channel = parts[1].trim();
                        sendMessage(gson.toJson(new CommandWithMessageAndChannel(command, target, channel)));
                    } else {
                        sendMessage(gson.toJson(new CommandWithMessage(command, target)));
                    }
                    messageArea.append("Command sent: " + message + "\n");
                } else {
                    messageArea.append("Usage: /unban <nick:tempid> [channel]\n");
                }
            } else if (message.equalsIgnoreCase("/list")) {
                command = "list";
                sendMessage(gson.toJson(new SimpleCommand(command)));
                messageArea.append("Command sent: /list\n");
            } else if (message.equalsIgnoreCase("/lu")) {
                command = "lu";
                sendMessage(gson.toJson(new SimpleCommand(command)));
                messageArea.append("Command sent: /lu\n");
            } else if (message.equalsIgnoreCase("/fusers")) {
                command = "fusers";
                sendMessage(gson.toJson(new SimpleCommand(command)));
                messageArea.append("Command sent: /fusers\n");
            } else if (message.equalsIgnoreCase("/users")) {
                command = "users";
                sendMessage(gson.toJson(new SimpleCommand(command)));
                messageArea.append("Command sent: /users\n");
            } else {
                messageArea.append("Unknown command.\n");
            }
        }
    }

    // Thread per ricevere messaggi dal server
    private class Receiver implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String response = receiveMessage();
                    if (response != null) {
                        handleServerResponse(response);
                    }
                }
            } catch (IOException ex) {
                messageArea.append("Disconnected from server.\n");
            }
        }

        private void handleServerResponse(String response) {
            // UC_ReceiveMessage
            // Utilizzo di Gson per il parsing del messaggio JSON
            try {
                JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

                if (jsonObject.has("status")) {
                    String status = jsonObject.get("status").getAsString();
                    String message = jsonObject.get("message").getAsString();

                    switch (status) {
                        case "success":
                            // Messaggio di successo
                            messageArea.append("Success: " + message + "\n");
                            // Se il login o la registrazione hanno avuto successo, abilita il pulsante send
                            if (message.contains("Login successful") || message.contains("Registration successful")) {
                                sendButton.setEnabled(true);
                            }
                            break;
                        case "error":
                            // Messaggio di errore
                            messageArea.append("Error: " + message + "\n");
                            break;
                        case "info":
                            // Messaggio informativo
                            messageArea.append("Info: " + message + "\n");
                            break;
                        case "kicked":
                            // Gestione dell'espulsione
                            messageArea.append("Kicked: " + message + "\n");
                            break;
                        default:
                            messageArea.append("Status [" + status + "]: " + message + "\n");
                            break;
                    }
                } else if (jsonObject.has("type")) {
                    String type = jsonObject.get("type").getAsString();
                    String message = jsonObject.get("message").getAsString();

                    switch (type) {
                        case "chat":
                            // Messaggio di chat
                            String sender = jsonObject.get("sender").getAsString();
                            messageArea.append(sender + ": " + message + "\n");
                            break;
                        default:
                            messageArea.append("Received: " + response + "\n");
                            break;
                    }
                } else {
                    messageArea.append("Received: " + response + "\n");
                }
            } catch (JsonSyntaxException | NullPointerException e) {
                messageArea.append("Received: " + response + "\n");
            }
        }
    }

    // Classi per i comandi, utili per la serializzazione con Gson
    private class LoginCommand {
        String command = "login";
        String nick;
        String password;

        LoginCommand(String nick, String password) {
            this.nick = nick;
            this.password = password;
        }
    }

    private class RegisterCommand {
        String command = "register";
        String nick;
        String password;

        RegisterCommand(String nick, String password) {
            this.nick = nick;
            this.password = password;
        }
    }

    private class SwitchChannelCommand {
        String command = "switch_channel";
        String message;

        SwitchChannelCommand(String channel) {
            this.message = channel;
        }
    }

    private class ChannelMessageCommand {
        String command = "send_message";
        String message;

        ChannelMessageCommand(String message) {
            this.message = message;
        }
    }

    private class PrivateMessageCommand {
        String command = "privmsg";
        String recipient;
        String message;

        PrivateMessageCommand(String recipient, String message) {
            this.recipient = recipient;
            this.message = message;
        }
    }

    private class DisconnectCommand {
        String command = "disconnect";
        String nick;

        DisconnectCommand(String nick) {
            this.nick = nick;
        }
    }

    private class CommandWithUuid {
        String command;
        String uuid;

        CommandWithUuid(String command, String uuid) {
            this.command = command;
            this.uuid = uuid;
        }
    }

    private class CommandWithMessage {
        String command;
        String message;

        CommandWithMessage(String command, String message) {
            this.command = command;
            this.message = message;
        }
    }

    private class CommandWithMessageAndChannel {
        String command;
        String message;
        String channel;

        CommandWithMessageAndChannel(String command, String message, String channel) {
            this.command = command;
            this.message = message;
            this.channel = channel;
        }
    }

    private class SimpleCommand {
        String command;

        SimpleCommand(String command) {
            this.command = command;
        }
    }
}
