# Progetto IRC Server

Questo progetto implementa un semplice server IRC utilizzando la libreria Java NIO con `SocketChannel`. Il progetto consente a più client di connettersi, comunicare attraverso diversi canali, e include funzionalità amministrative per gestire gli utenti.

## Funzionalità principali

### Client

Il client può:

- **Connettersi a un server** specificando un nome utente. Non è richiesta la password.
  
- **Richiedere la lista dei canali** attivi inviando il comando:
  ```bash
  /list
  ```

- **Connettersi a un canale** specifico utilizzando il comando:
  ```bash
  /join #channel_name
  ```

- **Visualizzare gli utenti connessi** al canale con il comando:
  ```bash
  /users
  ```

- **Inviare messaggi** nel canale connesso con il comando:
  ```bash
  /msg messaggio
  ```

- **Inviare un messaggio privato a un utente** specifico con il comando:
  ```bash
  /privmsg nickname messaggio
  ```

- **Cambiare canale** in qualsiasi momento con lo stesso comando di join:
  ```bash
  /join #nuovo_canale
  ```

Il server gestisce la comunicazione tra tutti i client connessi.

### Amministratore

L'utente amministratore ha ulteriori privilegi e può:

- **Espellere un utente dal canale** con il comando:
  ```bash
  /kick nickname
  ```

- **Bannare e sbanare un utente dal canale** con i comandi:
  ```bash
  /ban nickname
  /unban nickname
  ```

- **Promuovere un utente a moderatore** con il comando:
  ```bash
  /promote nickname
  ```

### Server

Il server permette di gestire:

- **Connessioni di utenti** su diversi canali.
- **Gestione di canali attivi**: i canali sono identificati da un prefisso `#` e rappresentano gruppi di utenti connessi.
- **Cambio di canale** per gli utenti.
- **Collisioni di nomi**: impedisce che due utenti con lo stesso nome si connettano contemporaneamente.
- **Messaggi privati**: due utenti possono scambiarsi messaggi diretti.

## Requisiti

- **Java 8** o superiore.
- **Maven** (facoltativo per la gestione delle dipendenze).

## Istruzioni per l'esecuzione

1. Clona il repository:
   ```bash
   git clone https://github.com/leopzoc/ProgettoReti_IrcServer.git
   cd ProgettoReti_IrcServer
   ```

2. Compila il progetto:
   ```bash
   javac -d out src/server/*.java
   ```

3. Esegui il server:
   ```bash
   java -cp out server.Server
   ```

4. Esegui un client per connetterti al server:
   ```bash
   java -cp out client.Client
   ```

## Contributi

I contributi sono benvenuti. Puoi aprire una pull request o segnalare problemi nell'apposita sezione [Issues](https://github.com/leopzoc/ProgettoReti_IrcServer/issues).

## Autori

- **leopzoc** - Implementazione e sviluppo del progetto.

---

Per maggiori dettagli o assistenza, visita la [documentazione ufficiale di Java NIO](https://docs.oracle.com/javase/8/docs/api/java/nio/package-summary.html).
