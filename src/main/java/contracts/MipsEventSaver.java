package contracts;

import anonymization.Anonymizer;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import model.MetadataEvent;
import model.Response;
import models.MISP.Attribute;
import models.MISP.Event;
import models.MISP.EventMISP;
import models.MISP.Object;
import models.Policies.*;
import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.*;
import org.hyperledger.fabric.Logger;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONObject;
import org.json.JSONPointer;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param  metadataEvent all data related to the event generated to publish.
     * @return the same metadataEvent received.
     */
    @Transaction()
    public MetadataEvent putEventMetaData(final Context ctx, final MetadataEvent metadataEvent) {
        ChaincodeStub stub = ctx.getStub();
        String key = metadataEvent.getBody()+"_" + metadataEvent.getTimestamp();
        stub.putStringState(key, genson.serialize(metadataEvent));
        return metadataEvent;
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
    public String checkPolicyHashes(final Context ctx, final Event event, final String hashAnon) {
        ChaincodeStub stub = ctx.getStub();
        // hash del event

        //stub.getQueryResult()// get metadata del event

        String s = null;
        MetadataEvent metadataEvent = getMetadataByHash(ctx, hashAnon);
        // TODO: compare
        String blockchain_hashpolicy = metadataEvent.getPolicy();
        String blockchain_hashhierarchy = metadataEvent.getHierarchy();
        // get post
        //(hash politica, hash jerarquia) -> para comprobar tras dentro de aqui
        //que la política que se dice que tal coincide.
        // -> get a TATIS publisher, &id_evento -> devuelve {evento, politica, jerarquia}
        String url = metadataEvent.getInstance();

        Anonymizer anonymizer = new Anonymizer();

        String responseOrigin = anonymizer.getFilesFromOrigin(url, event.getUuid());
        if(responseOrigin == null){
            //TODO: tratar error
            //o devolver no se cumple
        }
        //TODO: check what to do when response is null or error
        Gson g = new Gson();
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
        Event e_anon = g.fromJson(event_anonymised, EventAnon.class).getEvent();
        anonymizer.setAllUuidtoNull(e_anon);
        String hashA = Hashing.sha256().hashString(e_anon.toJsonString(), StandardCharsets.UTF_8).toString();
        //comprobar hashA == hash Anonimizado en blockchain
        if(!hashA.equals(hashAnon)){
            //TODO: error, los eventos anonimizados no coinciden
            return "Error: Event hashes are not equal";
        }
        //TODO: se supone que si llega aquí los hashes coinciden
        System.out.println("Si coninciden");
        //si bien, devuelve ok.
        // compare to metadata.response
        Response response = metadataEvent.getResponse(); // evento anonimizado hasheado
        return "OK";
    }


}