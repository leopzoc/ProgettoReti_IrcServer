package client2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SimpleIrcClient {

    private JFrame frame;
    private JTextField ipField;
    private JTextField portField;
    private JTextField nickField;  // Aggiunto campo per nickname
    private JPasswordField passwordField;  // Aggiunto campo per password
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton connectButton;
    private JButton sendButton;


    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimpleIrcClient::new);
    }

    public SimpleIrcClient() {
        frame = new JFrame("Simple IRC Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);  // Aumentato un po' l'altezza della finestra
        frame.setLayout(new BorderLayout());

        // Top panel per IP, porta, nickname e password
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(3, 2));  // Disposizione a griglia 3x2

        ipField = new JTextField("127.0.0.1", 10);
        portField = new JTextField("5000", 5);
        nickField = new JTextField("nickname", 10);  // Campo per nickname
        passwordField = new JPasswordField(10);  // Campo per password

        connectButton = new JButton("Connect");

        topPanel.add(new JLabel("IP:"));
        topPanel.add(ipField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(new JLabel("Nickname:"));  // Etichetta per nickname
        topPanel.add(nickField);
        topPanel.add(new JLabel("Password:"));  // Etichetta per password
        topPanel.add(passwordField);
        topPanel.add(connectButton);

        frame.add(topPanel, BorderLayout.NORTH);

        // Center panel per visualizzare i messaggi
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel per input e invio messaggi
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Action listeners
        connectButton.addActionListener(new ConnectButtonListener());
        sendButton.addActionListener(new SendButtonListener());

        frame.setVisible(true);
    }

    private class ConnectButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            String nick = nickField.getText();  // Ottiene il nickname inserito
            String password = new String(passwordField.getPassword());  // Ottiene la password inserita

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(ip, port), 2000);
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                messageArea.append("Connected to " + ip + ":" + port + "\n");

                // Invia automaticamente il messaggio di login dopo la connessione
                String loginJson = createLoginJson(nick, password);
                sendMessage(loginJson);

                // Avvia un thread per ricevere messaggi dal server
                new Thread(() -> {
                    try {
                        while (true) {
                            String response = receiveMessage();
                            if (response != null) {
                                messageArea.append("Received: " + response + "\n");
                            }
                        }
                    } catch (IOException ex) {
                        messageArea.append("Error receiving message from server.\n");
                    }
                }).start();

            } catch (IOException ex) {
                messageArea.append("Failed to connect to " + ip + ":" + port + "\n");
            }
        }

        private String createLoginJson(String nick, String password) {
            return String.format("{\"command\": \"login\", \"nick\": \"%s\", \"password\": \"%s\"}", nick, password);
        }
    }




    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = inputField.getText();
            if (message.isEmpty()) {
                return;
            }

            if (out != null) {
                String jsonMessage = createJsonMessage(message);
                try {
                    sendMessage(jsonMessage);
                    messageArea.append("Sent: " + message + "\n");
                    inputField.setText("");
                } catch (IOException ex) {
                    messageArea.append("Failed to send message.\n");
                }
            } else {
                messageArea.append("You must connect to a server first.\n");
            }
        }
        //Elabora il messaggio inserito dall'utente e genera un JSON formattato correttamente in base al comando specificato. 
        //Questo JSON viene poi utilizzato per inviare i messaggi sul lato client.
        private String createJsonMessage(String message) {
            String command;
            String recipientWithTempId = "";
            String uuid = "";
            String channel = ""; // Canale opzionale

            if (message.startsWith("/fban")) {
                command = "fban";
                String[] parts = message.substring(6).trim().split(" ", 2); // Suddivide in "leo:00001" o "uuid"

                // Controlla se il primo parametro è un UUID o un nickname:tempid
                if (parts[0].contains("-")) { // UUID
                    uuid = parts[0].trim();
                    return String.format("{\"command\": \"%s\", \"uuid\": \"%s\"}", command, uuid);
                } else {
                    recipientWithTempId = parts[0].trim();  // Il formato "leo:00001"
                    return String.format("{\"command\": \"%s\", \"message\": \"%s\"}", command, recipientWithTempId);
                }
            } else if (message.startsWith("/funban")) {  // Comando funban
                command = "funban";
                String[] parts = message.substring(8).trim().split(" ", 2); // Suddivide in "uuid"

                if (parts[0].contains("-")) { // UUID
                    uuid = parts[0].trim();
                    return String.format("{\"command\": \"%s\", \"uuid\": \"%s\"}", command, uuid);
                } else {
                    return "{}"; // Se non contiene un UUID valido
                }

            } else if (message.startsWith("/promote")) {  // Comando promote
                command = "promote";
                String[] parts = message.substring(9).trim().split(" ", 2); // Suddivide in "leo:00001" o "uuid"

                // Controlla se il primo parametro è un UUID o un nickname:tempid
                if (parts[0].contains("-")) { // UUID
                    uuid = parts[0].trim();
                    return String.format("{\"command\": \"%s\", \"uuid\": \"%s\"}", command, uuid);
                } else {
                    recipientWithTempId = parts[0].trim();  // Il formato "leo:00001"
                    return String.format("{\"command\": \"%s\", \"message\": \"%s\"}", command, recipientWithTempId);
                }

            } else if (message.startsWith("/unpromote")) {  // Comando unpromote
                command = "unpromote";
                String[] parts = message.substring(11).trim().split(" ", 2); // Suddivide in "leo:00001" o "uuid"

                // Controlla se il primo parametro è un UUID o un nickname:tempid
                if (parts[0].contains("-")) { // UUID
                    uuid = parts[0].trim();
                    return String.format("{\"command\": \"%s\", \"uuid\": \"%s\"}", command, uuid);
                } else {
                    recipientWithTempId = parts[0].trim();  // Il formato "leo:00001"
                    return String.format("{\"command\": \"%s\", \"message\": \"%s\"}", command, recipientWithTempId);
                }

            } else if (message.startsWith("/join")) {
                command = "switch_channel";
                String content = message.substring(5).trim();
                return String.format("{\"command\": \"%s\", \"message\": \"%s\"}", command, content);

            } else if (message.startsWith("/msg")) {
                command = "send_message";
                String content = message.substring(4).trim();
                return String.format("{\"command\": \"%s\", \"message\": \"%s\"}", command, content);

            } else if (message.startsWith("/privmsg")) {
                command = "privmsg";
                String[] parts = message.substring(9).trim().split(" ", 2); // Suddivide in "destinatario" e "messaggio"

                if (parts.length == 2) {
                    String recipient = parts[0].trim();  // Il destinatario del messaggio
                    String privMessage = parts[1].trim();  // Il contenuto del messaggio

                    // Restituisci il JSON formattato con recipient e message
                    return String.format("{\"command\": \"%s\", \"recipient\": \"%s\", \"message\": \"%s\"}",
                            command, recipient, privMessage);
                } else {
                    // Se manca il messaggio o il destinatario, restituisci un JSON vuoto o un errore
                    return "{}";
                }
            }
            else if (message.equalsIgnoreCase("/list")) {
                command = "list";
                return String.format("{\"command\": \"%s\"}", command);

            } else if (message.equalsIgnoreCase("/lu")) {
                command = "lu";
                return String.format("{\"command\": \"%s\"}", command);

            } else if (message.equalsIgnoreCase("/fusers")) {
                command = "fusers";
                return String.format("{\"command\": \"%s\"}", command);

            } else if (message.equalsIgnoreCase("/users")) {
                command = "users";
                return String.format("{\"command\": \"%s\"}", command);

            } else if (message.startsWith("/kick")) {
                command = "kick";
                String[] parts = message.substring(6).trim().split(" ", 2);
                if (parts.length >= 1) {
                    recipientWithTempId = parts[0].trim();  // Il formato "leo:00001"
                }
                if (parts.length == 2) {
                    channel = parts[1].trim();  // Canale, se presente
                }

                if (channel.isEmpty()) {
                    return String.format("{\"command\": \"%s\", \"message\": \"%s\"}",
                            command, recipientWithTempId);
                } else {
                    return String.format("{\"command\": \"%s\", \"message\": \"%s\", \"channel\": \"%s\"}",
                            command, recipientWithTempId, channel);
                }

            } else if (message.startsWith("/ban")) {
                command = "ban";
                String[] parts = message.substring(5).trim().split(" ", 2);  // Suddivide in "leo:00001" e "nome_canale"
                if (parts.length >= 1) {
                    recipientWithTempId = parts[0].trim();  // Il formato "leo:00001"
                }
                if (parts.length == 2) {
                    channel = parts[1].trim();  // Canale, se presente
                }

                return String.format("{\"command\": \"%s\", \"message\": \"%s\", \"channel\": \"%s\"}",
                        command, recipientWithTempId, channel);

            } else if (message.startsWith("/unban")) {
                command = "unban";
                String[] parts = message.substring(7).trim().split(" ", 2);  // Suddivide in "leo:00001" e "nome_canale"
                if (parts.length >= 1) {
                    recipientWithTempId = parts[0].trim();  // Il formato "leo:00001"
                }
                if (parts.length == 2) {
                    channel = parts[1].trim();  // Canale, se presente
                }

                return String.format("{\"command\": \"%s\", \"message\": \"%s\", \"channel\": \"%s\"}",
                        command, recipientWithTempId, channel);

            }
            else if (message.equalsIgnoreCase("/disconnect")) {
                // Comando di disconnessione
                command = "disconnect";
                return String.format("{\"command\": \"%s\", \"nick\": \"%s\"}", command, "leo");  // Sostituisci "leo" con il nickname dell'utente loggato
            }
            else if (message.startsWith("/register")) {
                // Comando di registrazione
                command = "register";
                String[] parts = message.substring(10).trim().split(" ", 2);  // Suddivide in "nick password"
                if (parts.length == 2) {
                    String nick = parts[0].trim();
                    String password = parts[1].trim();
                    return String.format("{\"command\": \"%s\", \"nick\": \"%s\", \"password\": \"%s\"}", command, nick, password);
                } else {
                    return "{}"; // Formato errato
                }
            }
            else {
                return "{}"; // Comando non valido
            }
        }
    }

        private void sendMessage(String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(4 + messageBytes.length);
        buffer.putInt(messageBytes.length); // Scrivi la lunghezza del messaggio come prefisso
        buffer.put(messageBytes); // Scrivi il messaggio
        buffer.flip();
        out.write(buffer.array());
        out.flush();
    }

    private String receiveMessage() throws IOException {
        int length = in.readInt(); // Legge la lunghezza del messaggio
        if (length > 0) {
            byte[] responseBytes = new byte[length];
            in.readFully(responseBytes); // Legge il contenuto del messaggio
            return new String(responseBytes, StandardCharsets.UTF_8);
        }
        return null;
    }
}

