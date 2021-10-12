# Noelia's Blockchain Guide
## How to run Blockchain

### From Zero
```
sh LanzarBlockchain.sh
#### or (its the same, whatever you wants)
cd hyperledger/docker-crypto/
sh scripts/LanzarBlockchain.sh
```
### Rerun
```
sh RELanzarBlockchain.sh
#### or (its the same, whatever you wants)
cd hyperledger/docker-crypto/
sh scripts/ReLanzarBlockchain.sh
```
In both cases, when all windows shows no errors/ no connection errors/ warnings and [cli.common] logs shows: ``successfuly submitted proposal to join channel -> blockchain runs with no errors. In other result send SCREENSHOTS to MANAGER(agustin.marinf@um.es).


### How to install smartcontract on ledger
Afte successfull blockchain execution do :
```
docker exec -it cli bash
sh scripts/installSC.sh NoeliaSC 1
```
where NoeliaSC is the name of the smartcontract and 1 is the version

### How to compile smartcontract
On smartcontract directory (hyperledger/chaincodes/NoeliaSC/) DO:
```
./gradlew installDist
```
It will generate in the path build/install/[FOLDERNAME] the jar, etc. You need to copy FOLDERNAME to hyperledger/docker-crypto/chaincode/FOLDERNAME where the script installSC.sh is going to package and install the smartcontract in the CLI.
## How to run API REST

#### Prerequisites
```
NODE 
NPM 
TMUX
```
add to your own /etc/hosts file this:
```
127.0.0.1 orderer.local.com
127.0.0.1 peer0.org1.local.com
127.0.0.1 ca.local.com
127.0.0.1 machine.local.com

```
### OPTION 1 SCRIPT


```

sh Apirest.sh
##### or (its the same, whatever you wants)
cd ChainREST

npm install

DEBUG=chainapi:* npm start
```
###Stop and run API REST
```
tmux a
ctrl + C
DEBUG=chainapi:* npm start
```
### Paths to API REST

#### Pushdata

In order to save data in the blockchain use path: POST 'http://localhost:3000/chain/pushdata'
with a body:
```
{ "key" :"sensor",
  "data" :"{\"temp\":40,\"specs\":{\"model\":\"rs3\",\"battery\":\"4000mha\",\"year\":2020}}"}
```
The body is a JSON with 2 elements: element "key" value string and element "data" with string that contains the json which is going to be pushed to the blockchain.

#### Pulldata
In order to retrieve data from the blockchain use the path: POST 'http://localhost:3000/chain/pulldata'
with a body:
```
{"query": "{\"selector\":{\"temp \": {\"$lt\": 39}}}"}
```
The body is a JSON with an element "query" with value: a string which contains the JSON with the couchDB rich query[couchDB rich query](https://docs.couchdb.org/en/latest/api/database/find.html)


```
CONSULTA

{"query": "{\"selector\":{\"$not\": {\"specs .year \": 2021}}}"}

RESPUESTA
{
   "sensor1776514668886619":"{\"specs \":{\"battery \":\"4000mha \",\"model \":\"rs3 \",\"year \":2020},\"temp\":39}",
   "sensor1776551599448561":"{\"specs \":{\"battery \":\"3000mha \",\"model \":\"rs4\",\"year \":2020},\"temp\":37}",
   "sensor1776491480619746":"{\"specs \":{\"battery \":\"4000mha \",\"model \":\"rs3 \",\"year \":2020},\"temp\":40}"
}

CONSULTA
{"query": "{\"selector\":{\"specs .model \": \"rs4\"}}"}

RESPUESTA
{
   "sensor1776565050754258":"{\"specs \":{\"battery \":\"3000mha \",\"model \":\"rs4\",\"year \":2021},\"temp\":36}",
   "sensor1776576615423281":"{\"specs \":{\"battery \":\"3000mha \",\"model \":\"rs4\",\"year \":2021},\"temp\":35}",
   "sensor1776551599448561":"{\"specs \":{\"battery \":\"3000mha \",\"model \":\"rs4\",\"year \":2020},\"temp\":37}"
}
```
Another Example

