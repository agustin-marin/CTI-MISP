#!/bin/bash
a=$2
ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/local.com/orderers/orderer.local.com/msp/tlscacerts/tlsca.local.com-cert.pem
rutapeer=/opt/gopath/src/github.com/hyperledger/fabric/peer
rutachaincode=/opt/gopath/src/github.com/chaincode/
PEERADDRESSES="--peerAddresses peer0.org1.local.com:7051 --tlsRootCertFiles  ${rutapeer}/crypto/peerOrganizations/org1.local.com/peers/peer0.org1.local.com/tls/ca.crt"


fn () {
    set -x

    peer lifecycle chaincode package $1package$a.tar.gz --path $rutachaincode/$1/  --lang java --label $1$a
    echo
    peer lifecycle chaincode install $1package$a.tar.gz
    echo
    peer lifecycle chaincode queryinstalled
    PACKAGEID=$( peer lifecycle chaincode queryinstalled | grep "$1$a" | cut -d" " -f3 | cut -f1 -d",")
    echo
    peer lifecycle chaincode approveformyorg -o localhost:7050 --channelID test --name $1 --version $a --package-id $PACKAGEID --sequence $a --tls --cafile $ORDERER_CA
    echo

    peer lifecycle chaincode checkcommitreadiness --channelID test --name $1 --version $a --sequence $a --tls --cafile $ORDERE_CA --output json  
    echo
    peer lifecycle chaincode commit -o localhost:7050 --channelID test --name $1 --version $a --sequence $a --tls --cafile $ORDERER_CA $PEERADDRESSES
    echo
    peer lifecycle chaincode querycommitted --channelID test --name $1 --cafile $ORDERER_CA 
    echo
    #reset ; peer chaincode invoke -o localhost:7050 --tls --cafile $ORDERER_CA -C test -n $1  $PEERADDRESSES -c '{"function":"generateToken","Args":["1","2","3","4","5"]}'
}





fn $1

echo 'USO DEL SCRIPT: ./installSC.sh [nombre_de_la_carpeta_que_contiene_el_chaincode] [version_del_chaincode]'
#peer chaincode invoke -o localhost:7050 --tls --cafile $ORDERER_CA -C test -n $1  $PEERADDRESSES -c '{"function":"","Args":[]}'
