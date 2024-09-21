
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

Gestore login
Login riuscito:

```json
{
  "status": "success",
  "message": "Login successful. Welcome, nickname"
}
```
Utente già connesso:

```json
{
  "status": "error",
  "message": "User is already connected."
}
```
Utente bannato:

```json
{
  "status": "error",
  "message": "You are banned from this server."
}
```
Registrazione riuscita:

```json
{
  "status": "success",
  "message": "Registration successful. Welcome, nickname"
}
```
Assegnazione a un canale:

```json
{
  "status": "success",
  "message": "You have been assigned to channel: firstChannel"
}
```
Gestore broadcastMessage
Messaggio di broadcast:
```json
{
  "sender": "nome_utente",
  "message": "questo è il messaggio"
}
```
Gestore switchChannel
Errore durante il cambio di canale:

```json
{
  "status": "error",
  "message": "Non puoi unirti al canale channelName perché sei stato bannato."
}
```
Cambio di canale riuscito:

```json
{
  "status": "success",
  "message": "You have joined channelName."
}
```
Gestore kick
Espulsione riuscita:

```json
{
  "status": "kicked",
  "message": "Sei stato espulso e spostato nel canale: firstChannel"
}
```
Errore - Utente già nel canale di destinazione:

```json
{
  "status": "error",
  "message": "Utente già nel canale di destinazione."
}
```
Errore - Utente non trovato:

```json
{
  "status": "error",
  "message": "Utente non trovato o più utenti con lo stesso nome."
}
```
Gestore ban
Utente bannato con successo:

```json
{
  "status": "banned",
  "message": "Sei stato bannato dal canale nome_canale e sei stato spostato nella lobby."
}
```
Errore - Utente non trovato:

```json
{
  "status": "error",
  "message": "Utente non trovato o più utenti con lo stesso nome."
}
```
Errore - Non puoi bannare utenti nella lobby:

```json
{
  "status": "error",
  "message": "Non puoi bannare utenti nella lobby."
}
```
Gestore fban
Ban definitivo riuscito:

```json
{
  "status": "success",
  "message": "Utente bannato definitivamente."
}
```
Errore durante il ban:

```json
{
  "status": "error",
  "message": "Errore durante il ban dell'utente tramite UUID."
}
```
Gestore funban
Utente sbannato con successo:

```json
{
  "status": "success",
  "message": "Utente sbannato con successo tramite UUID."
}
```
Errore durante lo sbannamento:

```json
{
  "status": "error",
  "message": "Errore durante lo sbannamento dell'utente tramite UUID."
}
```
Gestore promote
Promozione riuscita:

```json
{
  "status": "success",
  "message": "Utente Nickname è stato promosso a admin."
}
```

Errore - Utente già admin:

```json
{
  "status": "info",
  "message": "L'utente è già admin."
}
```

Gestore unpromote
Rimozione dei privilegi di admin riuscita:

```json
{
  "status": "success",
  "message": "Utente Nickname non è più admin."
}
```
Errore - Utente non è admin:

```json
{
  "status": "info",
  "message": "L'utente non è admin."
}
```
Gestore visualizzaUtenti
Errore durante la visualizzazione degli utenti:
```json
{
  "status": "error",
  "message": "Errore durante la visualizzazione degli utenti."
}
```








### Client di prova

Nel progetto sono inclusi **due semplici client** per testare il funzionamento del server. I client possono inviare comandi al server e ricevere le risposte JSON formattate. I client sono utilizzati per verificare le funzionalità di connessione, invio messaggi e amministrazione.

## Requisiti

- **Java 8** o superiore.
- **Maven** (facoltativo per la gestione delle dipendenze).


## Contributi

I contributi sono benvenuti. Puoi aprire una pull request o segnalare problemi nell'apposita sezione [Issues](https://github.com/leopzoc/ProgettoReti_IrcServer/issues).

## Autori

- **leopzoc** -  (Leonardo Pulzone)
- **Pasquale Mazzocchi** (non di disponendo di un account github viene menzionato negli autori ma non è presente come collaboratore)
- **Fabrizio Auremma** (non di disponendo di un account github viene menzionato negli autori ma non è presente come collaboratore)

---

Per maggiori dettagli o assistenza, visita la [documentazione ufficiale di Java NIO](https://docs.oracle.com/javase/8/docs/api/java/nio/package-summary.html).
