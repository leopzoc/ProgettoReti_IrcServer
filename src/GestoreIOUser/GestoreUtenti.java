package GestoreIOUser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestoreUtenti {
    private static String userFilePath;

    public GestoreUtenti(String userFilePath) {
        GestoreUtenti.userFilePath = userFilePath;
    }

    public static void creaFileUtenti() {
        if (userFilePath == null) {
            throw new IllegalStateException("Il percorso del file utente non è stato inizializzato.");
        }

        Path path = Paths.get(userFilePath);
        try {
            if (Files.notExists(path)) {
                // Crea il file 'users.txt'
                Files.createFile(path);
                System.out.println("File 'users.txt' creato con successo.");

                // Crea il primo utente admin con un UUID specifico
                String adminUUID = "8224d5b0-a5be-48cd-8d26-ddc45af03db6"; // UUID predefinito
                User primoAdmin = new User(adminUUID, "admin", "admin", "active", "admin", null);
                Map<String, User> users = new HashMap<>();
                users.put(primoAdmin.getID(), primoAdmin);

                // Salva l'utente admin nel file
                saveUsers(users);
                System.out.println("Primo utente admin creato con successo con UUID: " + adminUUID);
            } else {
                System.out.println("Il file 'users.txt' esiste già.");
            }
        } catch (IOException e) {
            System.out.println("Errore nella creazione del file 'users.txt': " + e.getMessage());
        }
    }


    public static Map<String, User> loadUsers() throws IOException {
        Map<String, User> users = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(userFilePath));
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 5) {
                String id = parts[0];       // UUID
                String nick = parts[1];
                String password = parts[2];
                String status = parts[3];
                String role = parts[4];
                User user = new User(id, nick, password, status, role, null);
                users.put(id, user);
            }
        }
        return users;
    }

    public static void saveUsers(Map<String, User> users) throws IOException {
        List<String> lines = new ArrayList<>();
        for (User user : users.values()) {
            lines.add(user.getID() + ":" + user.getNick() + ":" + user.getPassword() + ":" + user.getStatus() + ":" + user.getRole());
        }
        Files.write(Paths.get(userFilePath), lines);
    }


}
