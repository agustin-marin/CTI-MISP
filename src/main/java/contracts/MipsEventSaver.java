package contracts;

import anonymization.Anonymizer;
import anonymization.Hasher;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import model.MetadataEvent;
import model.Response;
import models.MISP.Event;
import models.MISP.EventMISP;
import models.Policies.*;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.hyperledger.fabric.Logger.getLogger;

@Contract(
    name = "MipsEventSaver",
        info = @Info(
                title = "MIPS event saver.",
                description = "",
                version = "1.0"
        )
)

@Default
public final class MipsEventSaver implements ContractInterface {

    // Serializacion JSON
    private final Genson genson = new GensonBuilder().create();



    /**
     * Store event in the ledger
     * @param  metadataEventstring all data related to the event generated to publish.
     * @return the same metadataEvent received.
     */
    @Transaction()
    public String putEventMetaData(final Context ctx, final String key, final  String metadataEventstring) {

        MetadataEvent metadataEvent = genson.deserialize(metadataEventstring, MetadataEvent.class);
        ChaincodeStub stub = ctx.getStub();
        String serialize = genson.serialize(metadataEvent);
        System.out.println("RESULT: "+ serialize);
        stub.putStringState(key, genson.serialize(metadataEvent));
        return serialize;
    }



    @Transaction()
    public MetadataEvent getMetadataByHash(final Context ctx, final String hash){
        ChaincodeStub stub = ctx.getStub();

        String selector = new JSONObject().put("selector", new JSONObject().put("response", hash)).toString();

        QueryResultsIterator<KeyValue> result = stub.getQueryResult(selector);

        boolean b = result.iterator().hasNext();
        if (b) {
            MetadataEvent deserialize = genson.deserialize(result.iterator().next().getValue(), MetadataEvent.class);
            return deserialize;
        }
        else return null;
    }

    @Transaction()
    public MetadataEvent getMetadataByKey(final Context ctx, final String uuid, final String date){
        ChaincodeStub stub = ctx.getStub();

        String result = stub.getStringState(uuid + "_" + date);
        System.out.println("RESULT: "+result);
        return genson.deserialize(result, MetadataEvent.class);
    }
    /**
     * query a metadataEvent from the ledger based on the metadata filled elements filled on the parameter. (element
     * filled  is represented as != "" or != null)
     *
     * @param metadata filled only with de params of the query
     * @return List of results in string format.
     */
    @Transaction()
    public String queryEventMetaData(final Context ctx,final  MetadataEvent metadata) {
        ChaincodeStub stub = ctx.getStub();

        JSONObject query = new JSONObject();
        if (metadata.getDatetime() != null && !"".equals(metadata.getDatetime())){
            query.put("datetime", metadata.getDatetime());
        }
        if (metadata.getTimestamp() != null){
            query.put("timestamp", metadata.getTimestamp());
        }
        if (metadata.getBody() != null && metadata.getBody().getSha256() != null
                && !"".equals(metadata.getBody().getSha256())){
            query.put("body",new JSONObject().put("sha256",metadata.getBody().getSha256()));
        }

        if (metadata.getInstance() != null &&  !"".equals(metadata.getInstance())){
            query.put("instance",metadata.getInstance());
        }
        if (metadata.getIpaddress() != null &&  !"".equals(metadata.getIpaddress())){
            query.put("ipaddress",metadata.getIpaddress());
        }

        if (metadata.getMethod() != null &&  !"".equals(metadata.getMethod())){
            query.put("method",metadata.getMethod());
        }
        if (metadata.getResponse() != null && metadata.getResponse().getSha256() != null
                && !"".equals(metadata.getResponse().getSha256())){
            query.put("response",new JSONObject().put("sha256",metadata.getResponse().getSha256()));
        }
        if (metadata.getToken() != null && metadata.getToken().getSha256() != null
                && !"".equals(metadata.getToken().getSha256())){
            query.put("token",new JSONObject().put("sha256",metadata.getToken().getSha256()));
        }
        if (metadata.getUri() != null &&  !"".equals(metadata.getUri())){
            query.put("uri",metadata.getUri());
        }
        if (metadata.getUsername() != null &&  !"".equals(metadata.getUsername())){
            query.put("username",metadata.getUsername());
        }
        JSONObject selector = new JSONObject().put("selector", query);

        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(selector.toString());
        HashMap<String, String> results = new HashMap<>();

        for (KeyValue keyValue : queryResult) {
            results.put(keyValue.getKey(),new String(keyValue.getValue()));
        }
        return genson.serialize(results);
    }
    @Transaction()
    public String checkPolicyHashes(final Context ctx, final String eventString, final String uuid, final String date) {
        ChaincodeStub stub = ctx.getStub();
        // hash del event

        //stub.getQueryResult()// get metadata del event

        String s = null;
        MetadataEvent metadataEvent = getMetadataByKey(ctx, uuid, date);
        // TODO: compare
        String blockchain_hashpolicy = metadataEvent.getPolicy();
        String blockchain_hashhierarchy = metadataEvent.getHierarchy();
        // get post
        //(hash politica, hash jerarquia) -> para comprobar tras dentro de aqui
        //que la política que se dice que tal coincide.
        // -> get a TATIS publisher, &id_evento -> devuelve {evento, politica, jerarquia}
        String url = metadataEvent.getInstance();
        String hashAnon = metadataEvent.getResponse().getSha256();

        Anonymizer anonymizer = new Anonymizer();
        Event event = genson.deserialize(eventString, Event.class);
        //String responseOrigin = anonymizer.getFilesFromOrigin(url, event.getUuid());
        String responseOrigin = anonymizer.request_orignal_events(url, event.getUuid());
        if(responseOrigin == null){
            //TODO: tratar error
            //o devolver no se cumple
            return "Error, origin not reachable or event not existing";
        }
        //TODO: check what to do when response is null or error
        Gson g = new Gson();
        System.out.println("Raw event from origin " + responseOrigin);
        EventAnon e = g.fromJson(responseOrigin, EventAnon.class);
        Event plain_event = e.getEvent();
        PrivacyPolicy privacypolicy = e.getPrivacyPolicy();
        Hierarchy hierarchypolicy = e.getHierarchypolicy();
        //aqui comprobar todo.

        String hashP = Hashing.sha256().hashString(privacypolicy.toJsonString(), StandardCharsets.UTF_8).toString();
        String hashH = Hashing.sha256().hashString(hierarchypolicy.toJsonString(), StandardCharsets.UTF_8).toString();
        //comprobar hashP == hash de politica en metadata
        if(!hashP.equals(blockchain_hashpolicy)){
            //TODO: error, los hashes de ficheros no coinciden
        }
        //comprobar hashH == hash de jerarquia en metadata
        if(!hashH.equals(blockchain_hashhierarchy)){
            //TODO: error, los hashes de ficheros no coinciden
        }
        //si correcto, anonimizar
        //obtenidas de peticion a tatis origen
        String event_anonymised = anonymizer.anonymize(plain_event,privacypolicy, hierarchypolicy);
        //DESCOMENTAR FUNCIONABA SOLO 1 ORGANIZACION Event e_anon = g.fromJson(event_anonymised, EventAnon.class).getEvent();
                Event e_anon = g.fromJson(event_anonymised, EventAnon.class).getEvent();
        //System.out.println("e_anon: "+e_anon.toJsonString());
        anonymizer.setAllUuidtoNull(e_anon);
        System.out.println("even_anonymised: "+e_anon.toJsonString());
        String hashA = Hashing.sha256().hashString(Hasher.checkHashes(e_anon), StandardCharsets.UTF_8).toString();
        //comprobar hashA == hash Anonimizado en blockchain
        //hardoded - ***QUITAR
        //hashA = hashAnon;
        //
        if(!hashA.equals(hashAnon)){
            System.out.println("No Coinciden los hashes\n"+
            "HASH - A: "+ hashA + "\n"+
            "HASH - B: "+ hashAnon);
            //TODO: error, los eventos anonimizados no coinciden
            System.out.println("Se modifica el codigo de la blockchain");
            System.out.println(responseOrigin);
            return "Error: Event hashes are not equal";
        }
        //TODO: se supone que si llega aquí los hashes coinciden
        System.out.println("Hash published in blockchain hashA:" + hashA);
        System.out.println("Hash anonymization over raw event hashAnon " + hashAnon);
        System.out.println("HASHES MATCH");
        //si bien, devuelve ok.
        // compare to metadata.response
        Response response = metadataEvent.getResponse(); // evento anonimizado hasheado
        return "OK";
    }


}