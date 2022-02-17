package contracts;

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

    private final String UUID = "uuid";

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

    public boolean comprueba(Event event, String hashAnon){
        String origen = "https://localhost:8443/";
        String responseOrigin = getFilesFromOrigin(origen, event.getUuid());
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
        //if(!hashP.equals(blockchain_hashpolicy)){
            //TODO: error, los hashes de ficheros no coinciden
            //return false;
        //}
        //comprobar hashH == hash de jerarquia en metadata
        //if(!hashH.equals(blockchain_hashhierarchy)){
            //TODO: error, los hashes de ficheros no coinciden
            //return false;
        //}
        //si correcto, anonimizar
        //obtenidas de peticion a tatis origen
        String event_anonymised = anonymize(plain_event,privacypolicy, hierarchypolicy);
        String hashA = Hashing.sha256().hashString(event_anonymised, StandardCharsets.UTF_8).toString();
        //comprobar hashA == hash Anonimizado en blockchain
        if(!hashA.equals(hashAnon)){
            //TODO: error, los eventos anonimizados no coinciden
        }else{
            return true;
        }
        System.out.println("Si coninciden");
        //si bien, devuelve ok.
        // compare to metadata.response
        return false;
    }


    @Transaction()
    public String checkPolicyHashes(final Context ctx, final Event event, final String hashAnon) {
        ChaincodeStub stub = ctx.getStub();
        // hash del event

        //stub.getQueryResult()// get metadata del event

        String s = null;

        MetadataEvent metadataEvent = genson.deserialize(s, MetadataEvent.class);

        String instance = metadataEvent.getInstance(); // TATIS
        // TODO: compare
        String blockchain_hashpolicy = metadataEvent.getPolicy();
        String blockchain_hashhierarchy = metadataEvent.getHierarchy();
        // get post
        //(hash politica, hash jerarquia) -> para comprobar tras dentro de aqui
        //que la política que se dice que tal coincide.
        // -> get a TATIS publisher, &id_evento -> devuelve {evento, politica, jerarquia}
        String origin = metadataEvent.getIpaddress();
        String url = metadataEvent.getUri();
        String responseOrigin = getFilesFromOrigin(url, event.getUuid());
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
        String event_anonymised = anonymize(plain_event,privacypolicy, hierarchypolicy);
        String hashA = Hashing.sha256().hashString(event_anonymised, StandardCharsets.UTF_8).toString();
        //comprobar hashA == hash Anonimizado en blockchain
        if(!hashA.equals(hashAnon)){
            //TODO: error, los eventos anonimizados no coinciden
        }
        System.out.println("Si coninciden");
        //si bien, devuelve ok.
        // compare to metadata.response
        Response response = metadataEvent.getResponse(); // evento anonimizado hasheado
        return "";
    }


    public boolean comprueba(Event event, String hashAnon, Event anonymised){
        String origen = "https://127.0.0.1:8443/";
        String responseOrigin = getFilesFromOrigin(origen, event.getUuid());
//        if(responseOrigin == null){
//            //TODO: tratar error
//            //o devolver no se cumple
//        }
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
        //if(!hashP.equals(blockchain_hashpolicy)){
        //TODO: error, los hashes de ficheros no coinciden
        //return false;
        //}
        //comprobar hashH == hash de jerarquia en metadata
        //if(!hashH.equals(blockchain_hashhierarchy)){
        //TODO: error, los hashes de ficheros no coinciden
        //return false;
        //}
        //si correcto, anonimizar
        //obtenidas de peticion a tatis origen
        String event_anonymised = anonymize(plain_event,privacypolicy, hierarchypolicy);
        Event e_anon = g.fromJson(event_anonymised, EventAnon.class).getEvent();
        System.out.println("EQUALS " + e_anon.equals(anonymised));
        System.out.println("E_ANON " + e_anon.toJsonString());
        System.out.println("ANONYMISED " + anonymised.toJsonString());
        setAllUuidtoNull(e_anon);
        String hashA = Hashing.sha256().hashString(e_anon.toJsonString(), StandardCharsets.UTF_8).toString();
        //comprobar hashA == hash Anonimizado en blockchain
        if(!hashA.equals(hashAnon)){
            //TODO: error, los eventos anonimizados no coinciden
            return false;
        }else{
            return true;
        }
        //si bien, devuelve ok.
        // compare to metadata.response
    }

    public SSLSocketFactory trustAllCerts() {
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        // nothing to do
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        // nothing to do
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return sslSocketFactory;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public SSLContext trustCert(String location) {
        File crtFile = new File(location);
        java.security.cert.Certificate certificate = null;
        try {
            certificate = CertificateFactory.getInstance("X.509").generateCertificate(new FileInputStream(crtFile));
        } catch (CertificateException | FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Or if the crt-file is packaged into a jar file:
        // CertificateFactory.getInstance("X.509").generateCertificate(this.class.getClassLoader().getResourceAsStream("server.crt"));


        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("anonymizer", certificate);

        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TrustManagerFactory trustManagerFactory = null;
        try {
            trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String getFilesFromOrigin(String domain, String eventUuid){
//    	Path currentpath = Paths.get("");
//		String s = currentpath.toAbsolutePath().toString();
//		final String location = s + "/AnonymizerCert/anonymizer.crt";
//		System.out.println(location);
//    	SSLContext context = trustCert(location);
        SSLSocketFactory sf = trustAllCerts();
        URL url = null;
        try {
            url = new URL(domain+"/anonymizer/event?uuid="+eventUuid);
            System.out.println(domain+"anonymize/event?uuid="+eventUuid);
            HttpsURLConnection http = (HttpsURLConnection)url.openConnection();
            http.setSSLSocketFactory(sf);
            http.setHostnameVerifier((hostname, session) -> true);
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());
            InputStream inputStream;
            StringBuilder sb = new StringBuilder();
            if(http.getResponseCode() == 200){
                inputStream =  http.getInputStream();
                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String currentLine;
                while ((currentLine = in.readLine())!=null)
                    sb.append(currentLine);
                in.close();
            }else{
                //error en la peticion o en la respuesta
                //TODO: comprobar
                inputStream = http.getErrorStream();
            }
            http.disconnect();
            System.out.println("PETICION ENVIADA " + sb.toString());
            return sb.toString();
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public String anonymize(Event event, PrivacyPolicy pol, Hierarchy hier){

        if(event == null || pol == null || hier == null){
            return null;
        }

        EventMISP e = null;
        if (pol.isOnlyAttributes() && hier.isOnlyAttributes() && event.isOnlyAttributes()) {
            e = apply_privacy(event, pol, hier, true);
            if (e == null) {
                // error en anonimización
                return null;
            } else {
                // bien anonimizado
                return e.toJsonString();
            }
        } else if (!(pol.isOnlyAttributes() || hier.isOnlyAttributes() || event.isOnlyAttributes())) {
            System.out.println("Anonimizacion de objetos");
            e = apply_privacy(event, pol, hier, false);
            System.out.println(event.toJsonString());
            if (e == null) {
                // error en anonimización
                return null;
            } else {
                // bien anonimizado
                return e.toJsonString();
            }
        } else {
            // TODO: en el frontend, tirar mensaje de que los ficheros no son concordantes
            // los dos ficheros no concuerdan
            //out.println(
            //"Error(1): The files are not concordant, all files has to be either only attributes or only objects");
            return null;
        }
    }
    //---------------------------------------FUNCIONALIDAD DE ANONIMIZACIÓN------------------------------------------------------------------------
    private EventMISP apply_privacy(Event event, PrivacyPolicy pol, Hierarchy hier,
                                    boolean only_attributes) {
        long ianon = System.currentTimeMillis();
        // check if attr or objects
        if (only_attributes) {
            HashMap<String, Integer> att_kanon = new HashMap<String, Integer>();

            // check for which attributes we have a generalization available
            ArrayList<String> hasHierarchy = new ArrayList<String>();
            for (Att_indv ai : hier.getHierarchyAttributes()) {
                hasHierarchy.add(ai.getAttributeName());
            }

            // att name and pet we must apply
            HashMap<String, String> att_pet = pol.attNamePet();

            ArrayList<String> quasi = new ArrayList<String>(); // quasi k-anonimity

            att_pet.forEach((name, pet) -> {
                if (pet.contains("k-anonimity") && hasHierarchy.contains(name)) {
                    //if has policy and hierarchy apply
                    quasi.add(name);
                }
            });

            for (String attname : quasi) {
                int k = pol.attGetK(attname);
                if (k == 0) {
                    // TODO: error que no debería suceder, de hecho, no hace falta ni comprobarlo
                    // porque ya lo has comprobado antes, borrar si quieres
                    return null;
                }
                att_kanon.put(attname, k);
            }

            for(Map.Entry<String, Integer> entry : att_kanon.entrySet()) {
                // apply transformation for each attribute
                Data.DefaultData data = Data.create();

                data.add(UUID, entry.getKey());
                data.getDefinition().setDataType(entry.getKey(), DataType.STRING);
                data.getDefinition().setAttributeType(entry.getKey(), AttributeType.QUASI_IDENTIFYING_ATTRIBUTE);

                // the uuid attribute
                data.getDefinition().setDataType(UUID, DataType.STRING);
                data.getDefinition().setAttributeType(UUID, AttributeType.INSENSITIVE_ATTRIBUTE);

                String type = hier.getTypeOfAttribute(entry.getKey());
                if (type == null) {
                    // fail that shouldnt happen
                }

                AttributeType.Hierarchy.DefaultHierarchy hierarchy = AttributeType.Hierarchy.create();
                if (type.equals("static")) {
                    // add static generalizations
                    Att_indv ai = hier.getAttIndv(entry.getKey());
                    for (Att_gn g : ai.getAttributeGeneralization()) {
                        // add each level
                        hierarchy.add(g.getGeneralization().toArray(new String[0]));
                    }
                    for (Attribute a : event.getAttributes()) {
                        // add value to dataset
                        if (a.getObject_relation().equals(entry.getKey())) {
                            String uuid = a.getUuid();
                            if (uuid == null || uuid.equals("")) {
                                uuid = java.util.UUID.randomUUID().toString();
                                a.setUuid(uuid);
                            }
                            data.add(uuid, a.getValue());
                            System.out.println("data.add(" + uuid + " ," + a.getValue() + ")");
                        }
                    }
                    data.getDefinition().setAttributeType(entry.getKey(), hierarchy);
                } else {
                    // retrieve dynamic hierarchies
                    boolean f_regex = type.equals("regex");
                    boolean f_interval = type.equals("interval");
                    // dynamic match and replace for *
                    Att_indv ai = hier.getAttIndv(entry.getKey());
                    for (Attribute a : event.getAttributes()) {
                        if (a.getObject_relation().equals(entry.getKey())) {
                            String uuid = a.getUuid();
                            if (uuid == null || uuid.equals("")) {
                                uuid = java.util.UUID.randomUUID().toString();
                                a.setUuid(uuid);
                            }
                            data.add(uuid, a.getValue());
                            String[] t;
                            if (f_regex) {
                                t = generate_regex_hierarchy(ai, a);
                                hierarchy.add(t);
                            } else if (f_interval) {
                                t = generate_interval_hierarchy(ai, a);
                                hierarchy.add(t);
                            }

                        }
                    }
                    data.getDefinition().setAttributeType(entry.getKey(), hierarchy); // add hierachy to the definition

                }
                // execute algorithm
                ARXConfiguration config = ARXConfiguration.create();
                System.out.println("K-anon " + att_kanon.get(entry.getKey()));
                config.addPrivacyModel(new KAnonymity(entry.getValue()));
                //config.setSuppressionLimit(0.02d);// hardcodeado, quitar
                ARXAnonymizer anonymizer = new ARXAnonymizer();
                ARXResult result = null;
                try {
                    result = anonymizer.anonymize(data, config);
                } catch (IOException e) {
                    // TODO: ERROR ANONIMIZACIÓN, DEVOLVER ERROR PARA ATRÁS
                    return null;
                }

                //set the values
                setNewValuesAttributes(result, data, event);
            }
            long inicioenvio = System.currentTimeMillis();
            return new EventMISP(event);
        } else {
            // anonimizar objetos
            // type of object with all posible attributes
            long inicio = System.currentTimeMillis();
            HashMap<String, ArrayList<String>> typeobj_attributes = new HashMap<String, ArrayList<String>>();
            for (Object o : event.getObject()) {
                if (!typeobj_attributes.containsKey(o.getName())) {
                    typeobj_attributes.put(o.getName(), new ArrayList<String>());
                }
                for (Attribute a : o.getAttribute()) {
                    String att_name = a.getObject_relation();
                    if (!typeobj_attributes.get(o.getName()).contains(att_name)) {
                        typeobj_attributes.get(o.getName()).add(att_name);
                        System.out.println("Añadiendo " + att_name + " a " + o.getName());
                    }
                }
            }


            for(Map.Entry<String, ArrayList<String>> entry : typeobj_attributes.entrySet()) {
                // name of quasi att, type of quasi
                // check object has policy and hierarchy
                Template objectPolicy = pol.getPolicyObject(entry.getKey());
                Hierarchy_Object objectHierarchy = hier.getHierarchyObject(entry.getKey());
                HashMap<String, String> atts_quasi = null;
                ArrayList<String> quasi_and_hierachical = null;
                boolean format_correct = true;
                boolean hasPolicies = false;
                if(objectHierarchy != null && objectPolicy != null) {
                    hasPolicies = true;
                    atts_quasi = objectHierarchy.getAttributesNameType();
                    // really we should only use hierarchies of attributes indicated quasi on the
                    // politics
                    quasi_and_hierachical = objectPolicy.getQuasiAndHierachical();
                    for (String att : quasi_and_hierachical) {
                        if (!atts_quasi.containsKey(att)) {
                            //hierarchy file does not contain hierarchy for a object
                            format_correct = false;
                            break;
                        }
                    }
                }


                if (hasPolicies && format_correct) {



                    ArrayList<String> quasi = objectPolicy.getQuasi(); // list of attributes quasi acording policy
                    // HashMap<String, ArrayList<String>> sensitive_pet =
                    // objectPolicy.getSensitiveAndPet();
                    ArrayList<String> sensitive_pet = objectPolicy.getSensitive();
                    // creating data for the algorithm
                    Data.DefaultData data = Data.DefaultData.create();

                    // uuid as insensitive
                    data.getDefinition().setDataType(UUID, DataType.STRING);
                    data.getDefinition().setAttributeType(UUID, AttributeType.INSENSITIVE_ATTRIBUTE);

                    // we add uuid as first attribute in dataset
                    String[] att_names = typeobj_attributes.get(entry.getKey()).toArray(new String[0]);
                    String[] att_names_row = new String[att_names.length + 1];
                    att_names_row[0] = UUID;
                    for (int i = 1; i < att_names_row.length; i++) {
                        att_names_row[i] = att_names[i - 1];
                    }

                    data.add(att_names_row); // add first row, names of attributes
                    ArrayList<String> all_atts = new ArrayList<String>(entry.getValue());
                    for (String s : quasi) {
                        data.getDefinition().setDataType(s, DataType.STRING); // set data type
                        data.getDefinition().setAttributeType(s, AttributeType.QUASI_IDENTIFYING_ATTRIBUTE); // set
                        // quasi
                        // attributes
                    }
                    for (String s : sensitive_pet) {
                        System.out.println("SENSITIVE " + s);
                        data.getDefinition().setDataType(s, DataType.STRING); // set data type
                        data.getDefinition().setAttributeType(s, AttributeType.SENSITIVE_ATTRIBUTE);
                    }
                    all_atts.removeAll(quasi); // remove all from quasi and sensitive, to get insensitive
                    all_atts.removeAll(sensitive_pet);
                    for (String s : all_atts) {
                        // set insensitive
                        data.getDefinition().setDataType(s, DataType.STRING);
                        data.getDefinition().setAttributeType(s, AttributeType.INSENSITIVE_ATTRIBUTE);
                    }


                    // here are the quasi and t-closeness hierachical attributes
                    HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy = new HashMap<String, AttributeType.Hierarchy.DefaultHierarchy>();
                    for (String a : quasi_and_hierachical) {
                        att_hierarchy.put(a, AttributeType.Hierarchy.create());
                    }
                    // ______> estaticas
                    this.setStaticHierarchies(hier, entry.getKey(), quasi_and_hierachical, att_hierarchy);

                    // _______> jerarquias dinamicas
                    this.setDinamicHierarchies(event, entry.getKey(), entry.getValue(), objectHierarchy, quasi_and_hierachical, atts_quasi,
                            att_hierarchy, data);

                    // add hierarchies to data algorithm
                    att_hierarchy.forEach((att, hierarchy) -> {
                        if (!sensitive_pet.contains(att)) { // because we have to set the quasi one's. The sensitive
                            // hierachical t-closeness is done in privacy declaration
                            data.getDefinition().setAttributeType(att, hierarchy);
                        }
                    });


                    ARXConfiguration config = ARXConfiguration.create();
                    //getting k of object
                    int k_of_object = 0;
                    for (Template t : pol.getTemplates()) {
                        if (t.getName().equals(entry.getKey())) {
                            k_of_object = t.getKofObject();
                            break;
                        }
                    }
                    if (k_of_object > 0) {
                        // apply k-anonimity
                        // config.setSuppressionLimit(0.02d);//hardcodeado, quitar
                        config.addPrivacyModel(new KAnonymity(k_of_object));
                    }
                    System.out.println("K - " + k_of_object);

                    // setting sensitive config
                    for (String nameatt : sensitive_pet) {
                        this.setSensitivePet(nameatt, entry.getKey(), pol, att_hierarchy, config);
                    }

                    ARXAnonymizer anonymizer = new ARXAnonymizer();
                    ARXResult result = null;
                    try {
                        result = anonymizer.anonymize(data, config);
                    } catch (IOException e) {
                        // TODO: ERROR ANONIMIZACIÓN, DEVOLVER PARA ATRÁS
                        return null;
                    }
                    //replace values in objects of event
                    setNewValueObjects(result, data, event, att_names_row);
                }
            }
            return new EventMISP(event);
        }

    }

    private void setSensitivePet(String nameatt, String k, PrivacyPolicy pol,
                                 HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy, ARXConfiguration config) {
        Pet pet_to_apply = null;
        for (Template t : pol.getTemplates()) {
            if (t.getName().equals(k)) {
                pet_to_apply = t.getAttribute(nameatt).getPets().get(0);
                break;
            }
        }
        switch (pet_to_apply.getScheme()) {
            case "t-closeness/hierachical":
                AttributeType.Hierarchy.DefaultHierarchy dh = att_hierarchy.get(nameatt);
                // 0.3 is hardocoded, find the value in policy
                double t = pet_to_apply.getMetadata().getT();
                config.addPrivacyModel(new HierarchicalDistanceTCloseness(nameatt, t, dh));
                System.out.println("AÑADIENDO PRIVACY MODEL Hierachical");
                break;
            case "t-closeness/ordered":
                // find value in policy
                double tordered = pet_to_apply.getMetadata().getT();
                config.addPrivacyModel(new OrderedDistanceTCloseness(nameatt, tordered));
                System.out.println("AÑADIENDO PRIVACY MODEL OrderedDistanceTCloseness");
                break;
            case "l-diversity/distinct":
                // find value in policy
                int l = pet_to_apply.getMetadata().getL();
                config.addPrivacyModel(new DistinctLDiversity(nameatt, l));
                System.out.println("AÑADIENDO PRIVACY MODEL DistinctLDiversity");
                break;
            case "l-diversity/entropy":
                // find value in policy
                int lentropy = pet_to_apply.getMetadata().getL();
                config.addPrivacyModel(new EntropyLDiversity(nameatt, lentropy));
                System.out.println("AÑADIENDO PRIVACY MODEL EntropyLDiversity");
                break;
            case "l-diversity/recursive":
                // find values in policy
                int lrec = pet_to_apply.getMetadata().getL();
                int crec = pet_to_apply.getMetadata().getC();
                config.addPrivacyModel(new RecursiveCLDiversity(nameatt, crec, lrec));
                System.out.println("AÑADIENDO PRIVACY MODEL RecursiveCLDiversity");
                break;
            default:
                break;
        }
    }



    private void setStaticHierarchies(Hierarchy hier, String k, ArrayList<String> quasi_and_hierachical,
                                      HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy) {
        // set static hierarchies
        for (Hierarchy_Object ho : hier.getHierarchyObjects()) {
            if (k.equals(ho.getMisp_object_template())) {
                for (Att_indv ai : ho.getAttributeHierarchies()) {
                    if (quasi_and_hierachical.contains(ai.getAttributeName())
                            && ai.getAttributeType().equals("static")) { // if is quasi indicated in the policy
                        for (Att_gn g : ai.getAttributeGeneralization()) {
                            att_hierarchy.get(ai.getAttributeName()).add(g.getGeneralization().toArray(new String[0]));
                            // hierarchy.add(g.getGeneralization().toArray(new String[0]));
                        }
                    }
                }
                break; // TODO: comprobar el funcionamiento
            }
        }
    }

    private void setDinamicHierarchies(Event event, String k, ArrayList<String> v, Hierarchy_Object ho,
                                       ArrayList<String> quasi_and_hierachical, HashMap<String, String> atts_quasi,
                                       HashMap<String, AttributeType.Hierarchy.DefaultHierarchy> att_hierarchy, Data.DefaultData data) {
        // jerarquía añadida
        // recorremos evento para recoger objetos de ese tipo
        for (Object o : event.getObject()) {
            if (o.getName().equals(k)) {
                String[] value_elements = new String[v.size() + 1]; // becacuse of uuid added in dataset
                // get or create uuid for identifiying a row
                String uuid = o.getUuid();
                if (uuid == null || uuid.equals("")) {
                    uuid = java.util.UUID.randomUUID().toString();
                    o.setUuid(uuid);
                }
                value_elements[0] = uuid;

                for (Attribute a : o.getAttribute()) {
//					for(String s : v ) {
//						System.out.println("V " + s);
//					}
                    System.out.println("Object relation " + a.getObject_relation() + v.indexOf(a.getObject_relation()));
                    value_elements[v.indexOf(a.getObject_relation()) + 1] = a.getValue(); // beacuse of uuid added in
                    // dataset
                    if (quasi_and_hierachical.contains(a.getObject_relation())
                            && atts_quasi.get(a.getObject_relation()).equals("regex")) {
                        Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
                        // TODO
                        String[] t = generate_regex_hierarchy(ai, a);
                        // hierarchy.add(t);
                        att_hierarchy.get(a.getObject_relation()).add(t);
                        // me he quedado aqui
                    } else if (quasi_and_hierachical.contains(a.getObject_relation())
                            && atts_quasi.get(a.getObject_relation()).equals("interval")) {
                        Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
                        String[] t = generate_interval_hierarchy(ai, a);
                        // hierarchy.add(t);
                        att_hierarchy.get(a.getObject_relation()).add(t);
                    }
                }
                data.add(value_elements);
            }
        }
    }



    private void setNewValueObjects(ARXResult result, Data.DefaultData data, Event event, String[] att_names_row) {
        try {
            // the execution throws null of no solution has been found or a HashMap with
            // values
            HashMap<Integer, String[]> results = printResult(result, data);
            // set objets to event
            results.forEach((index, list) -> {
                String uuid = results.get(index)[0];
                Object object = event.getObjectByUuid(uuid);
                // replace values in object retrieved
                // att names row has the name of the attributes in order
                for (int i = 1; i < list.length; i++) {
                    // we set the attribute value
                    object.setAttributeValue(att_names_row[i], list[i]); // this method throws false if
                    // couldnt be possible
                }
            });
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private void setAllUuidtoNull(Event e) {
        if(e.isOnlyAttributes()) {
            for(Attribute a : e.getAttribute()) {
                a.setUuid(null);
            }
        }else {
            for(Object o : e.getObject()) {
                o.setUuid(null);
                for(Attribute a : o.getAttribute()) {
                    a.setUuid(null);
                }
            }
        }
        return;
    }

    private void setNewValuesAttributes(ARXResult result, Data.DefaultData data, Event event) {
        // the execution throws null of no solution has been found or a HashMap with
        // values
        try {
            HashMap<Integer, String[]> results = printResult(result, data);
            results.forEach((i, r) -> {
                System.out.println("I " + i);
                for (String s : r) {
                    System.out.println("S " + r);
                }
            });
            // set objets to event
            //______> setnewvaluesattributes

            results.forEach((index, list) -> {
                String uuid = results.get(index)[0];

                Attribute a = event.getAttributeByUuid(uuid);
                // set value in attribute
                a.setValue(results.get(index)[1]);
                System.out.println("Seteamos atributo " + a.getUuid() + " a " + results.get(index)[1]);
                System.out.println("Valor en evento " + event.getAttributeByUuid(uuid).getValue());

                // replace values in object retrieved
                // att names row has the name of the attributes in order
            });
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private String[] generate_regex_hierarchy(Att_indv ai, Attribute a) {
        // add hierarchy for regex
        Att_gn g = ai.getAttributeGeneralization().get(0);
        // System.out.println("G " + g.getRegex().toString());
        ArrayList<String> transform = new ArrayList<String>();
        String value = a.getValue();
        System.out.println("data.add(" + value + ")");
        // transform.add(value);
        transform.add(value);
        for (String regex : g.getRegex()) {
            // System.out.println("REGEX-" + regex);
            transform.add(value.replaceAll(regex, "*"));
        }
        String[] t = transform.toArray(new String[0]);
        // System.out.println("T " + t);
        for (String it : t) {
            System.out.println("IT aindv " + ai.getAttributeName() + " " + it);
        }
        return t;
    }

    private String[] generate_interval_hierarchy(Att_indv ai, Attribute a) {
        Pattern lessthan = Pattern.compile("<([0-9]+)");
        Pattern lessEqualthan = Pattern.compile("<=([0-9]+)");
        Pattern biggerthan = Pattern.compile(">([0-9]+)");
        Pattern biggerEqualthan = Pattern.compile(">=([0-9]+)");
        Pattern interval = Pattern.compile("([0-9]+)-([0-9]+)");
        // patterns
        ArrayList<String> transform = new ArrayList<String>();
        transform.add(a.getValue());
        for (Att_gn g : ai.getAttributeGeneralization()) {
            // add the level of generalization according number
            for (String comparison : g.getInterval()) {
                Matcher mlt = lessthan.matcher(comparison);
                Matcher mlet = lessEqualthan.matcher(comparison);
                Matcher mbt = biggerthan.matcher(comparison);
                Matcher mbet = biggerEqualthan.matcher(comparison);
                Matcher mi = interval.matcher(comparison);
                Integer number_value = Integer.valueOf(a.getValue());
                if (mlt.matches()) {
                    if (number_value < Integer.valueOf(mlt.group(1))) {
                        transform.add(comparison);
                        System.out.println("Añadiendo < que");
                        break;
                    }
                } else if (mlet.matches()) {
                    if (number_value <= Integer.valueOf(mlet.group(1))) {
                        transform.add(comparison);
                        System.out.println(number_value + "Añadiendo <= que");
                        break;
                    }
                } else if (mbt.matches()) {
                    if (number_value > Integer.valueOf(mbt.group(1))) {
                        transform.add(comparison);
                        System.out.println(number_value + "Añadiendo > que");
                        break;
                    }
                } else if (mbet.matches()) {
                    if (number_value >= Integer.valueOf(mbet.group(1))) {
                        transform.add(comparison);
                        System.out.println(number_value + "Añadiendo >= que");
                        break;
                    }
                } else {
                    // interval
                    System.out.println("MATCHEA " + mi.matches());
                    if (number_value >= Integer.valueOf(mi.group(1)) && number_value <= Integer.valueOf(mi.group(2))) {
                        transform.add(comparison);
                        System.out.println(number_value + "Añadiendo intervalo " + comparison);
                        break;
                    }
                }
            }
        }
        String[] t = transform.toArray(new String[0]);
        for (String s : t) {
            System.out.println("Add -> " + s);
        }

        return t;
    }






    protected static HashMap<Integer, String[]> printResult(final ARXResult result, final Data data) {

        // Print time
        final DecimalFormat df1 = new DecimalFormat("#####0.00");
        final String sTotal = df1.format(result.getTime() / 1000d) + "s";
        System.out.println(" - Time needed: " + sTotal);

        // Extract
        final ARXLattice.ARXNode optimum = result.getGlobalOptimum();
        final List<String> qis = new ArrayList<String>(data.getDefinition().getQuasiIdentifyingAttributes());

        if (optimum == null) {
            System.out.println(" - No solution found!");
            return null;
        }

        // Initialize
        final StringBuffer[] identifiers = new StringBuffer[qis.size()];
        final StringBuffer[] generalizations = new StringBuffer[qis.size()];
        int lengthI = 0;
        int lengthG = 0;
        for (int i = 0; i < qis.size(); i++) {
            identifiers[i] = new StringBuffer();
            generalizations[i] = new StringBuffer();
            identifiers[i].append(qis.get(i));
            generalizations[i].append(optimum.getGeneralization(qis.get(i)));
            if (data.getDefinition().isHierarchyAvailable(qis.get(i)))
                generalizations[i].append("/").append(data.getDefinition().getHierarchy(qis.get(i))[0].length - 1);
            lengthI = Math.max(lengthI, identifiers[i].length());
            lengthG = Math.max(lengthG, generalizations[i].length());
        }

        // Padding
        for (int i = 0; i < qis.size(); i++) {
            while (identifiers[i].length() < lengthI) {
                identifiers[i].append(" ");
            }
            while (generalizations[i].length() < lengthG) {
                generalizations[i].insert(0, " ");
            }
        }

        // Print
        System.out.println(" - Information loss: " + result.getGlobalOptimum().getLowestScore() + " / "
                + result.getGlobalOptimum().getHighestScore());
        System.out.println(" - Optimal generalization");
        for (int i = 0; i < qis.size(); i++) {
            System.out.println("   * " + identifiers[i] + ": " + generalizations[i]);
        }
        System.out.println(" - Statistics");
        System.out.println(
                result.getOutput(result.getGlobalOptimum(), false).getStatistics().getEquivalenceClassStatistics());

        Iterator<String[]> transformed = result.getOutput(false).iterator();
        transformed.next();
        HashMap<Integer, String[]> map = new HashMap<Integer, String[]>();
        Integer i = 0;
        while (transformed.hasNext()) {
            System.out.println(" ");
            map.put(i, transformed.next());
            i++;
        }
        HashMap<Integer, String[]> results = new HashMap<Integer, String[]>();
        for (int z = 0; z < map.size(); z++) {
            System.out.println("z " + z);
            String[] array = map.get(z);
            results.put(z, array);
            System.out.println(Arrays.toString(array));
        }
        return results;
    }

}