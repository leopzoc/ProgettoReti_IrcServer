
# Progetto IRC Server con Pattern Command e JSON

Questo progetto implementa un server IRC utilizzando la libreria Java NIO con `SocketChannel` e il **Pattern Command** per gestire vari comandi, inclusi quelli amministrativi, utilizzando il formato JSON.

## Funzionalità principali

### Client IRC

il client irc puo loggarsi come user o come admin dipende dai privileggi che ha nel file users.txt che risiede nel server.

Il client può:

- **Connettersi a un server** specificando un nome utente e la password.
## Comandi per gli utenti:

#### /join canale:
Permette di unirsi a un canale specificato. Ad esempio, /join generale ti farà entrare nel canale "generale".

#### /msg Ciao mondo: 
Invia un messaggio al canale corrente. Ad esempio, /msg Ciao mondo invierà il messaggio "Ciao mondo" a tutti i membri del canale.

#### /list: 
Visualizza la lista di tutti i canali disponibili nel server.

#### /users: 
Mostra l'elenco di tutti gli utenti connessi nel canale corrente.

#### /lu: List and user


#### /privmsg: messaggio privato
Comando per inviare un messaggio privato


### Comandi per gli amministratori:
#### /ban [utente] [canale]:
Espelle permanentemente un utente da un canale specifico o da tutti i canali se non viene specificato un canale. Ad esempio, /ban leo:00001 bannerà l'utente "leo:00001" dal server.


#### /fban [utente]:

Esegue un ban definitivo di un utente in modo forzato. Ad esempio, /fban uuid-1234 bannerà l'utente con UUID uuid-1234.

#### /unban [utente] [canale]:

Rimuove il ban di un utente da un canale o dall'intero server. Ad esempio, /unban leo:00001 generale rimuoverà il ban dell'utente "leo:00001" dal canale "generale".

#### /funban [utente]: 
Rimuove il ban definitivo di un utente. Ad esempio, /funban uuid-1234 rimuoverà il ban definitivo dell'utente con UUID uuid-1234.

#### /kick [utente] [canale]: 
Espelle temporaneamente un utente da un canale. Ad esempio, /kick leo:00001 generale espellerà "leo:00001" dal canale "generale".

#### /promote [utente]:
Promuove un utente a un ruolo superiore (ad esempio da utente normale ad amministratore). Ad esempio, /promote leo:00001 promuoverà l'utente "leo:00001".

#### /unpromote [utente]: 
Declassa un utente a un ruolo inferiore (ad esempio da amministratore a utente normale). Ad esempio, /unpromote leo:00001 declasserà "leo:00001".

# formattazione dei comandi 
ogni comando viene inviato tramite json quindi non ce necessita di creare comandi concreti come negli altri irc server/client infatti /join /list puo essere facilmente cambiato in /entra /canali dovuto al fatto che il server interpreta questi tipi di json associati al comando 


Comandi per gli utenti
/join canale:

```json
{
  "command": "switch_channel",
  "message": "nome_canale"
}
```
/msg Ciao mondo:

```json
{
  "command": "send_message",
  "message": "Ciao mondo"
}
```
/list:

```json
{
  "command": "list"
}
```
/users:

```json
{
  "command": "users"
}
```
/lu (list and user):

```json
{
  "command": "lu"
}
```

Comando per inviare un messaggio privato
/privmsg destinatario messaggio:

```json
{
  "command": "privmsg",
  "recipient": "destinatario",
  "message": "messaggio"
}
```
Ad esempio, se vuoi inviare il messaggio "Ciao" all'utente "leo:00001", il JSON sarà:

```json
{
  "command": "privmsg",
  "recipient": "leo:00001",
  "message": "Ciao"
}
```

## Comandi per gli amministratori
/ban leo:00001 nome_canale:

``` json
{
  "command": "ban",
  "message": "leo:00001",
  "channel": "nome_canale"
}
```
/fban uuid-1234:

``` json
{
  "command": "fban",
  "uuid": "uuid-1234"
}
```
/unban leo:00001 nome_canale:

``` json
{
  "command": "unban",
  "message": "leo:00001",
  "channel": "nome_canale"
}
```
/funban uuid-1234:

``` json
{
  "command": "funban",
  "uuid": "uuid-1234"
}
```
/kick leo:00001 nome_canale:

``` json
{
  "command": "kick",
  "message": "leo:00001",
  "channel": "nome_canale"
}
```
/promote leo:00001:

``` json

{
  "command": "promote",
  "message": "leo:00001"
}
```
/unpromote leo:00001:

``` json
{
  "command": "unpromote",
  "message": "leo:00001"
}
```

### Server

Il server permette di gestire:

- **Connessioni di utenti** su diversi canali.
- **Gestione di canali attivi**: i canali possono essere identificati da un prefisso `#` ma non è necessario e rappresentano gruppi di utenti connessi. 
- **Cambio di canale** per gli utenti.
- **Collisioni di nomi**: impedisce che due utenti con lo stesso nome si connettano contemporaneamente.
- **Messaggi privati**: due utenti possono scambiarsi messaggi diretti con l'uso del tempid.

### Messaggi che invia il server ai client

#### Gestore login
In questo gestore di login, il sistema invia diversi messaggi ai client tramite il metodo clientWriter.writeToClient(). Ecco i vari messaggi che il gestore invia, in base a diverse situazioni durante la gestione del login:

Login riuscito:

Se l'utente è autenticato con successo:

"Login successful. Welcome, " + nick
Questo messaggio conferma il login con successo e dà il benvenuto all'utente con il suo nickname.
Utente già connesso:

Se l'utente tenta di connettersi nuovamente mentre è già connesso:

"User is already connected."
Questo messaggio notifica al client che l'utente è già connesso al server.
Utente bannato:

Se l'utente è stato bannato e tenta di connettersi:

"You are banned from this server."
Questo avverte il client che l'utente non può connettersi perché è stato bannato.
Registrazione riuscita:

Se il nickname non è già presente nel sistema e viene creato un nuovo utente:

"Registration successful. Welcome, " + nick
Questo messaggio informa il client che la registrazione è avvenuta con successo e dà il benvenuto al nuovo utente.
Assegnazione a un canale:

Dopo il login o la registrazione, l'utente viene assegnato a un canale:

"You have been assigned to channel: " + firstChannel

Questo messaggio notifica l'utente del canale a cui è stato assegnato.
Inoltre, il sistema potrebbe inviare ulteriori messaggi se vengono implementati nuovi comportamenti, ma questi sono i principali messaggi che il gestore invia attualmente.




#### Gestore broadcastMessage


Situazioni in cui vengono inviati messaggi
Messaggio di broadcast: Quando un utente invia un messaggio, questo viene trasmesso a tutti gli altri utenti nel suo stesso canale (eccetto il mittente). Il messaggio che viene inviato è strutturato in JSON come segue:

```json

{
    "sender": "nome_utente",
    "message": "questo è il messaggio"
}
```
sender: Questo è il nickname del mittente del messaggio. Se il mittente ha un ID temporaneo (in caso di nickname duplicato), l'ID temporaneo viene aggiunto tra parentesi al nickname, ad esempio: "sender": "utente(00001)".
message: Questo è il contenuto del messaggio inviato dall'utente.
Gestione degli ID temporanei: Se l'utente mittente ha un nickname duplicato, viene assegnato un ID temporaneo (come 00001, 00002, ecc.). In tal caso, il nickname dell'utente sarà formattato come:

```json

"sender": "nome_utente(00001)"
```
Processo di invio
Il gestore recupera il canale a cui appartiene l'utente che invia il messaggio e ottiene tutti i client (socket) connessi a quel canale.
Per ogni client nel canale, escluso il mittente, viene inviato il messaggio in formato JSON.
Se il canale non è trovato o l'utente mittente non è in un canale, vengono stampati messaggi di errore, ma non viene inviato nulla ai client.






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
