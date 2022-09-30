package anonymization;

import com.google.gson.Gson;
import models.MISP.Event;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;


public class Hasher {

    public Hasher(){

    }

    /*
    public boolean compare_events(String event1, String event2){
        return checkHashes(event1).equals(checkHashes(event2));
    }
     */

    public static String generate_Concatenated_Data(ArrayList<String> fields_ordered, ArrayList<Pair> json_pairs){
            StringBuilder sb = new StringBuilder();
            for(String field : fields_ordered){
                sb.append(field + json_pairs.stream().
                        filter(key -> key.getKey().equals(field))
                            .findFirst().orElse(null).getValue());
            }
            return sb.toString();
    }
    //public String checkHashes(String estring){
        //Event event  = new Gson().fromJson(estring, Event.class);
    public static String checkHashes(Event event){
        String eventjson = event.toJsonString();
        JSONObject e = new JSONObject(eventjson);
        Iterator<String> keys = e.keys();

        ArrayList<Pair> pairs = new ArrayList<Pair>();
        while(keys.hasNext()){
            String key = keys.next();
            System.out.println(key);
            pairs.add(new Pair(key, e.get(key)));
        }
        ArrayList<String> fields = new ArrayList<>(e.keySet());
        Collections.sort(fields);
        return generate_Concatenated_Data(fields, pairs);
    }

}
