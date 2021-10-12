#!/bin/bash
clear
ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/local.com/orderers/orderer.local.com/msp/tlscacerts/tlsca.local.com-cert.pem
peer channel create -o localhost:7050 -c mychannel -f /opt/gopath/src/github.com/hyperledger/fabric/peer/channel-artifacts/mychannel.tx --tls --cafile $ORDERER_CA
sleep 5
peer channel join -b ./mychannel.block --tls --cafile $ORDERER_CA