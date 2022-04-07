#!/bin/bash

./gradlew clean
./gradlew installDist
echo "Hoidhasaccessto1Realm"
scp -r build/install/EventSC/ hoid@misp:blockchain/hyperledger/chaincode/

ssh hoid@misp

