
# Progetto IRC Server con Pattern Command e JSON

Questo progetto implementa un server IRC utilizzando la libreria Java NIO con `SocketChannel` e il **Pattern Command** per gestire vari comandi, inclusi quelli amministrativi, utilizzando il formato JSON.

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
  /privmsg nickname:tempid messaggio
  ```
- **Cambiare canale** in qualsiasi momento con lo stesso comando di join:
  ```bash
  /switchchannel #nuovo_canale
  ```

Il server gestisce la comunicazione tra tutti i client connessi.

### Amministratore

L'utente amministratore ha ulteriori privilegi e può:

- **Espellere un utente dal canale** con il comando:
  ```bash
  /kick nickname:tempid [nome_canale]
  ```
  Esempio JSON:
  ```json
  {
      "command": "kick",
      "message": "leo:00001",
      "channel": "general"
  }
  ```

- **Bannare e sbanare un utente dal canale** con i comandi:
  ```bash
  /ban nickname:tempid nome_canale
  /unban nickname:tempid nome_canale
  ```
  Esempio JSON ban:
  ```json
  {
      "command": "ban",
      "message": "leo:00001",
      "channel": "general"
  }
  ```

  Esempio JSON unban:
  ```json
  {
      "command": "unban",
      "message": "leo:00001",
      "channel": "general"
  }
  ```

- **Promuovere e degradare un utente** con i comandi:
  ```bash
  /promote nickname:tempid
  /unpromote nickname:tempid
  ```
  Esempio JSON promote:
  ```json
  {
      "command": "promote",
      "message": "leo:00001"
  }
  ```

  Esempio JSON unpromote:
  ```json
  {
      "command": "unpromote",
      "message": "leo:00001"
  }
  ```

### Gestione dei canali

- **Visualizzare i canali attivi**:
  ```bash
  /list
  ```
  Esempio JSON risposta dal server:
  ```json
  {
      "channels": ["general", "tech", "random", "news"]
  }
  ```

- **Visualizzare utenti presenti nei canali**:
  ```bash
  /lu
  ```
  Esempio JSON:
  ```json
  {
      "channels": [
          {
              "channel": "general",
              "users": ["user1", "user2"]
          },
          {
              "channel": "random",
              "users": ["user3"]
          }
      ]
  }
  ```

### Server

Il server permette di gestire:

- **Connessioni di utenti** su diversi canali.
- **Gestione di canali attivi**: i canali sono identificati da un prefisso `#` e rappresentano gruppi di utenti connessi.
- **Cambio di canale** per gli utenti.
- **Collisioni di nomi**: impedisce che due utenti con lo stesso nome si connettano contemporaneamente.
- **Messaggi privati**: due utenti possono scambiarsi messaggi diretti con l'uso del tempid.

### Client di prova

Nel progetto sono inclusi **due semplici client** per testare il funzionamento del server. I client possono inviare comandi al server e ricevere le risposte JSON formattate. I client sono utilizzati per verificare le funzionalità di connessione, invio messaggi e amministrazione.

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
