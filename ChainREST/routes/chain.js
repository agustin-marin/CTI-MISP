var express = require('express');
var fs = require('fs');
var router = express.Router();
const { default: fabricNetworkSimple } = require('fabric-network-simple');

var conf = fabricNetworkSimple.config = {
  channelName: "mychannel",
  contractName: "GuardianSC",
  connectionProfile: {
    name: "local.fabric",
    version: "1.0.0",
    channels : {
      mychannel : {
        orderers : [ "orderer.local.com" ],
        peers : {
          "peer0.org1.local.com" : {
            endorsingPeer : true,
            chaincodeQuery : true,
            ledgerQuery : true,
            eventSource : true,
            discover : true
          }
        }
      },
    },
    organizations : {
      Org1 : {
        mspid : "Org1MSP",
        peers : [ "peer0.org1.local.com"],
        certificateAuthorities : [ "ca.org1.local.com" ]
      }
    },
    orderers : {
      "orderer.local.com" : {
        url : "grpcs://orderer.local.com:7050",
        tlsCACerts: {
          path:
            "/home/agustin/MegaSync/noelia-blockchain/ChainREST/test/ordererOrganizations/local.com/orderers/orderer.local.com/msp/tlscacerts/tlsca.local.com-cert.pem",
        },
      }
    },
    peers : {
      "peer0.org1.local.com" : {
        "url" : "grpcs://peer0.org1.local.com:7051",
        tlsCACerts: {
          path:
            "/home/agustin/MegaSync/noelia-blockchain/ChainREST/test/peerOrganizations/org1.local.com/peers/peer0.org1.local.com/msp/tlscacerts/tlsca.org1.local.com-cert.pem",
        },
      },
    },
  },
  certificateAuthorities : {
      "ca.org1.local.com" : {
        "url" : "https://ca.org1.local.com:7054",
        "httpOptions" : {
          "verify" : false
        },
        "registrar" : [ {
          "enrollId" : "admin",
          "enrollSecret" : "adminpw"
        } ]
      }
  },
  identity: {
    mspid: 'Org1MSP',
    certificate: '-----BEGIN CERTIFICATE-----\nMIICJDCCAcqgAwIBAgIRAMlMppCojn6hPAtv28R3myswCgYIKoZIzj0EAwIwbzEL\nMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\ncmFuY2lzY28xFzAVBgNVBAoTDm9yZzEubG9jYWwuY29tMRowGAYDVQQDExFjYS5v\ncmcxLmxvY2FsLmNvbTAeFw0yMTEwMDUxNjM0MDBaFw0zMTEwMDMxNjM0MDBaMGkx\nCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1TYW4g\nRnJhbmNpc2NvMQ4wDAYDVQQLEwVhZG1pbjEdMBsGA1UEAwwUQWRtaW5Ab3JnMS5s\nb2NhbC5jb20wWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAARH4kEd0Sv9vBrRxN12\nn/Nep0LMEv2uEgM1xOd9KNop8+pShEbEniBvQbwUQGnfV2V9OYEX0IrlK/a2lHlr\n3uGSo00wSzAOBgNVHQ8BAf8EBAMCB4AwDAYDVR0TAQH/BAIwADArBgNVHSMEJDAi\ngCBDuFDdCDNtvaH7Jgc2Lw/y+QPvdJoBc7XduNniNQby/TAKBggqhkjOPQQDAgNI\nADBFAiEA9FzeQo/JmQR8/qz2hOR1f8UnYo59PnWVWTigWP77G3ECIA+kaMvzSSZ8\nbLl4CZmSq2heDA8eCvzjOv2CbN3V1zbh\n-----END CERTIFICATE-----\n',
    privateKey: '-----BEGIN PRIVATE KEY-----\nMIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQghNV7F74u5hgszy4n\nuqtdJtjMt6Q3eNishEX/5M11qWWhRANCAARH4kEd0Sv9vBrRxN12n/Nep0LMEv2u\nEgM1xOd9KNop8+pShEbEniBvQbwUQGnfV2V9OYEX0IrlK/a2lHlr3uGS\n-----END PRIVATE KEY-----\n',
  },
  settings: {
    enableDiscovery: true,
    asLocalhost: false,
  }
}
asyncCall();
var fabconnection;

function initConection() {
  return new Promise(resolve => {
    fabconnection = new fabricNetworkSimple(conf);
  });
}
  
async function asyncCall() {
  console.log('Init fabric connection');
  await initConection();
}

router.get('/', function(req, res, next) {
    res.status(200).send("This is the ledger endpoint POST \n Endpoints: \n pushdata \n pulldata");
});

router.post('/pushdata', function(req, res, next) {
  var key = req.body.key;
  var data = req.body.data;
  console.log("key: "+key);
  console.log("data: "+data);
  fabconnection.invokeChaincode("pushData", [key,data], {}).then(queryChaincodeResponse => {
    res.status(200).send(queryChaincodeResponse.invokeResult);
  }).catch ( error => {
    console.log(error);
    res.status(404).send(error);
  });
});

router.post('/pulldata/', function(req, res, next) {
  var query = req.body.query;
  console.log(query);
  //fabconnection.invokeChaincode('addservice', [JSON.stringify(servicedid), domain, JSON.stringify(predicates), status], {})
  fabconnection.queryChaincode('pullData',[query],{}).then(queryChaincodeResponse => {
    console.log('result: '+ queryChaincodeResponse.queryResult)
    res.status(200).send(queryChaincodeResponse.queryResult)//JSON.parse(queryChaincodeResponse.queryResult[0]));
  }).catch ( error => {
    console.log(error);
    res.status(404).send(error);
  });
});




module.exports = router;
