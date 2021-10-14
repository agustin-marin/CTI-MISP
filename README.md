# Threat Events Smart Contract/ ChainCode
This smart contract is able to recolect events and save them in the blockchain. For now it is only implemented to received the event and store it with a key: composed by 'event's uuid' + 'instance url' obtained from the event threat JSON, and value: full clear event JSON. 

## Methods/Transactions
### getEvent ( String )
getEvent is a mehotd used to obtain an event publised in the ledger before. As a parameter, you need to introduce the key (uuid + instance) to get the event.
```
    public String getEvent(final Context ctx, final String key) {

```
#### EXAMPLE

```
IMAGE OR CODE
```

### putEvent ( String, String )
putEvent is a method used to publish an event in the ledger. It needs two parameters, the first one is the JSON event  encoded in string format and the second one is the instance url.
```
    public String putEvent(final Context ctx, final String event, final String instance) {

```
#### EXAMPLE
```
```
### ((OPTIONAL)couchDB)) queryEvent ( String )
queryEvent (which depends on couchDB enabled on the blockchain) receives a richQuery selector from couchDB and returns the list of matches.
```
    public String queryEvent(final Context ctx, final String query) {
```
#### EXAMPLE
```

```

## Installation
Here we have the installation steps used by UMU to install the Chaincode in a default hyperledger-fabric blockchain network.
### PLUGIN
We use the plugin in Gradle 'application' to define the main class, compile and build the project.
 REFERENCIA A BUILD.GRADLE
```
build.gradle
plugins {
    id 'application'

    [..]
    application {
    mainClass = 'contracts.NoeliaDataSaver'
}
```
### COMPILE
In order to compile/build the chaincode, in the root of the project:
```
./gradlew clean installDist 
```
This command will generate a new folder in the path build/install/ called 'EventSC' with the new jar/binaries.
### INSTALL
Now we have the compiled project, we just need to package and install the chaincode (using the CLI) commands:
```
# EVENTSC is  the compiled folder from the last section
peer lifecycle chaincode package ./packagedEventsc.tar.gz --path  ./EventSC  --lang java --label EventSC1
peer lifecycle chaincode install ./packagedEventsc.tar.gz
```

### SCRIPT
