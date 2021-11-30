package contracts;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
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
    private final Genson genson = new GensonBuilder().create();//.rename("context","@context").create();
     /**
     * Get event from the ledger (if it exists)
     * @param key uuid + instance name from the event saved before.
     * @return EVENT
     */


    @Transaction()
    public String getEvent(final Context ctx, final String key) {
        ChaincodeStub stub = ctx.getStub();


        String event = stub.getStringState(key);
        if (event.isEmpty()) {
            String errorMessage = "Event " + key + " was not saved in the ledger before";
            throw new ChaincodeException(errorMessage, "Event does not exist");
        }
        return new JSONObject(event).toString();
    }

    /**
     * Store event in the ledger
     * @param event Json with the misp threat event
     * @param instance Name of the instance (URL)
     * @return
     */
    @Transaction()

    public String putEvent(final Context ctx, final String event, final String instance) {
        JSONObject eventobject = null;
        ChaincodeStub stub = ctx.getStub();
        eventobject = new JSONObject(event).getJSONObject("Event");
        String key = eventobject.getString("uuid") + instance;
        stub.putStringState(key, event);
        return new JSONObject().put(key,eventobject).toString();
    }

    /**
     * Query data from the ledger using couchdb query selectors: . {"selector":{"key":"value","key.key":"value"}}
     * @return List of results in string format.
     */

    @Transaction()
    public String queryEvent(final Context ctx, final String query) {
        ChaincodeStub stub = ctx.getStub();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        HashMap<String, String> results = new HashMap<>();


        for (KeyValue keyValue : queryResult) {
            results.put(keyValue.getKey(),new String(keyValue.getValue()));
        }
        return genson.serialize(results);
    }
}
