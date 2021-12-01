package contracts;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import model.MetadataEvent;
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

import java.util.HashMap;

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
}
