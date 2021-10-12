# API Guide
## Change routes
In order to change or add routes in the API REST you can go to the file: [chain.js](/ChainREST/routes/chain.js) and add at the bottom of the file:
```
router.post('/NEWROUTE/', function(req, res, next) {
  var bodyjsonelement = req.body.bodyjsonelement;
  console.log(query);
  
  fabconnection.queryChaincode('NEWSMARTCONTRACTMETHOD',[bodyjsonelement],{}).then(queryChaincodeResponse => {
    console.log('result: '+ queryChaincodeResponse.queryResult)
    res.status(200).send(queryChaincodeResponse.queryResult)
  }).catch ( error => {
    console.log(error);
    res.status(404).send(error);
  });
});
```
where NEWROUTE is the name of the route you want, bodyjsonelement is an element obtained of the json of the body with the same name ({"bodyjsonelement":{}}). NEWSMARTCONTRACTMETHOD is the name of the smart contract method you want to call. the next parameter is an array of the necessary parameters of the smartcontract method. EXAMPLE '/hyperledger/chaincodes/NoeliaSC/src/main/java/contracts/NoeliaDataSaver.java':
```
@Transaction()
    public String pushData(final Context ctx, final String key, final String data) {
```
