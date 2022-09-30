package anonymization;

import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import models.MISP.Attribute;
import models.MISP.Event;
import models.MISP.EventMISP;
import models.MISP.Object;
import models.Policies.*;
import org.deidentifier.arx.*;
import org.deidentifier.arx.criteria.*;

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

public class Anonymizer {

    public Anonymizer() {
    }

    private final String UUID = "uuid";
    private final String FIXED_PORT = "8095";
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
            url = new URL("http://"+domain+"/anonymizer/event?uuid="+eventUuid);
            System.out.println(domain+"/anonymize/event?uuid="+eventUuid);
            //HttpsURLConnection http = (HttpsURLConnection)url.openConnection();
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            //http.setSSLSocketFactory(sf);
            //http.setHostnameVerifier((hostname, session) -> true);
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


    public String request_orignal_events(String domain, String eventUuid){
        URL url = null;
        HttpURLConnection http = null;
        try {
            //192.168.1.100:8080/anonymizer/getEvents?timestamp=1648806656
            //url = new URL("http://"+domain+":8085/anonymizer/event?uuid="+eventUuid);
            url = new URL("http://"+domain+":"+FIXED_PORT+"/anonymizer/event?uuid="+eventUuid);
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.setDoOutput(true);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
        if(http.getResponseCode() == 200){
                DataInputStream input = new DataInputStream( http.getInputStream() );
                StringBuilder sb = new StringBuilder();
                String linea = null;
                while((linea = input.readLine())!=null) {
                    sb.append(linea);
                }
                input.close();

                System.out.println(sb.toString());
                System.out.println("Resp Code:"+http.getResponseCode());
                System.out.println("Resp Message:"+ http.getResponseMessage());
                http.disconnect();
                return sb.toString();
            }else{
                //error
            }
            }catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }

        http.disconnect();
        return null;
    }




    public String anonymize(Event event, PrivacyPolicy pol, Hierarchy hier){

        if(event == null || pol == null || hier == null){
            return null;
        }

        PrivacyOperations privacyoperations = new PrivacyOperations("","");
        EventMISP e = null;
        String errorMessage = null;
        if (pol.isOnlyAttributes() && hier.isOnlyAttributes() && event.isOnlyAttributes()) {
            java.lang.Object result = privacyoperations.apply_privacy(event, pol, hier, true, null);
            if (result instanceof String) {
                errorMessage = (String) result;
            } else if (result instanceof EventMISP) {
                e = (EventMISP) result;
            }
            if(e == null){
                return null; //o mensaje de error
            }else{
                return e.toJsonString();
            }
        } else if (!(pol.isOnlyAttributes() || hier.isOnlyAttributes() || event.isOnlyAttributes())) {
            System.out.println("Anonimizacion de objetos");
            java.lang.Object result = apply_privacy(event, pol, hier, false);
            System.out.println(event.toJsonString());
            if (result instanceof String) {
                errorMessage = (String) result;
            } else if (result instanceof EventMISP) {
                e = (EventMISP) result;
            }
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

    private EventMISP alternative_anonymization(Event event, PrivacyPolicy policy, Hierarchy hierarchy){
        HashMap<String, String> attributePolicyPet = policy.attNamePet();
        HashMap<String, String> attributeHierarchyType = hierarchy.attgetTypes();

        for(Attribute attribute : event.getAttributes()){

        }
        return null;
    }

    private String getGeneralization(Att_indv ai, Attribute a, Integer level){
        for(Att_gn a_gn : ai.getAttributeGeneralization()){
            if(a_gn.getGeneralization().get(0).equals(a.getValue())){
                //sustituir
                String rturn;
                try {
                    rturn = a_gn.getGeneralization().get(level-1);
                }catch(IndexOutOfBoundsException e){
                    return null;
                }
                return rturn;
            }
        }
        return null;
    }

    //for object of event of objects
    private void replaceSuppresionGeneralization(Object object, HashMap<String, Integer> att_level, HashMap<String, String> att_pet, Hierarchy hier){
        for(Attribute attribute : object.getAttribute()){
            String attribute_name = attribute.getObject_relation();
            if(att_level.containsKey(attribute_name)){
                String pet = att_pet.get(attribute_name);
                switch (pet){
                    case "suppression":
                        //TODO: comprobación de tipo de jerarquía correcto
                        Att_indv ai = hier.getAttIndv(attribute_name);
                        if(! ai.getAttributeType().equals("regex")){
                            //TODO: tirar error, la supresion debe ir por
                            //TODO: regex.
                        }
                        String replace = getSupression(hier.getAttIndv(attribute_name),
                                attribute, att_level.get(attribute_name));
                        attribute.setValue(replace);
                        break;
                    case "generalization":
                        Att_indv hi = hier.getAttIndv(attribute_name);
                        if(hi.getAttributeType().equals("static")){
                            String replc = getGeneralization(hi, attribute, att_level.get(attribute_name));
                            attribute.setValue(replc);
                        }else if(hi.getAttributeType().equals("interval")){
                            String interval = generalize_interval(hi, attribute, att_level.get(attribute_name));
                            attribute.setValue(interval);
                        }else{
                            //TODO: error porque debería de ser de estos tipos
                        }
                        break;
                    default:
                        //TODO: no tiene porque entrar aqui
                        break;
                }
            }
        }
    }

    private void replaceSuppresionGeneralization(Event event, HashMap<String, Integer> att_level, HashMap<String, String> att_pet, Hierarchy hier){
        for(Attribute attribute : event.getAttributes()){
            String attribute_name = attribute.getObject_relation();
            if(att_level.containsKey(attribute_name)){
                String pet = att_pet.get(attribute_name);
                switch (pet){
                    case "suppression":
                        //TODO: comprobación de tipo de jerarquía correcto
                        Att_indv ai = hier.getAttIndv(attribute_name);
                        if(! ai.getAttributeType().equals("regex")){
                            //TODO: tirar error, la supresion debe ir por
                            //TODO: regex.
                        }
                        String replace = getSupression(hier.getAttIndv(attribute_name),
                                attribute, att_level.get(attribute_name));
                        attribute.setValue(replace);
                        break;
                    case "generalization":
                        Att_indv hi = hier.getAttIndv(attribute_name);
                        if(hi.getAttributeType().equals("static")){
                            String replc = getGeneralization(hi, attribute, att_level.get(attribute_name));
                            attribute.setValue(replc);
                        }else if(hi.getAttributeType().equals("interval")){
                            String interval = generalize_interval(hi, attribute, att_level.get(attribute_name));
                            attribute.setValue(interval);
                        }else{
                            //TODO: error porque debería de ser de estos tipos
                        }
                        break;
                    default:
                        //TODO: no tiene porque entrar aqui
                        break;
                }
            }
        }
    }
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
            HashMap<String, String> attribute_hierarchy = hier.attgetTypes();
            HashMap<String, Integer> att_level = pol.attLevelSupression();

            ArrayList<String> quasi = new ArrayList<String>(); // quasi k-anonimity

            //replacing suppresion and generalization value
            replaceSuppresionGeneralization(event, att_level, att_pet, hier);
            //---------------------------------------------------------

            att_pet.forEach((name, pet) -> {
                if (pet.contains("k-anonimity") && hasHierarchy.contains(name)) {
                    //if has policy and hierarchy apply
                    quasi.add(name);
                }else if(pet.contains("k-anonimity") && ! hasHierarchy.contains(name)){
                    //TODO: esto es un error, una politica que indica aplicar k-anonymity
                    //TODO: pero no hay jerarquia que lo permita
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
                    //new schemes
                    boolean f_supression = att_pet.get(entry.getKey()).equals("supression");
                    boolean f_generalization = att_pet.get(entry.getKey()).equals("generalization");
                    //
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
                            }/* else if(f_supression){
                                //supression in all values, set att value to the result of applying regex over original value
                                String new_att_value = getSupression(ai, a, att_level.get(entry.getKey()));
                                a.setValue(new_att_value);
                            }else if(f_generalization){
                                //generalization in all values, 2 options, interval o static
                                String check_type = attribute_hierarchy.get(entry.getKey());
                                switch (check_type){
                                    case "interval":
                                        String interval = generalize_interval(ai, a, att_level.get(entry.getKey()));
                                        a.setValue(interval);
                                        break;
                                    case "static":

                                        break;
                                    default:
                                        break;
                                }

                            }*/

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
                    // TODO: ERROR ANONIMIZACIÓN, DEVOLVER ERROR PARA ATRAS
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
                    this.setDinamicHierarchies(event, entry.getKey(), entry.getValue(), objectHierarchy, objectPolicy, quasi_and_hierachical, atts_quasi,
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
                        // TODO: ERROR ANONIMIZACIÓN, DEVOLVER PARA ATRAS
                        return null;
                    }
                    //replace values in objects of event
                    setNewValueObjects(result, data, event, att_names_row);
                }
            }
            return new EventMISP(event);
        }

    }

    //return new att value after aplying correspondant regex to original att value
    private String getSupression(Att_indv ai, Attribute a, Integer level){
        try {
            String regex = ai.getAttributeGeneralization().get(0).getRegex().get(level-1);
            String value = a.getValue();
            String problems = "("+regex+")";
            Pattern p = Pattern.compile(problems);
            Matcher m = p.matcher(value);
            if(m.find()) {
                System.out.println("SI TIENE QUE MATCHEAR " + m.group(1));
            }
            String replace = value.replaceAll(regex, "*".repeat(m.group(1).length()));
            System.out.println("REPLACE " + replace);
            return replace;
        }catch (Exception e){
            //null pointer or every other error
            return null;
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

    private void setDinamicHierarchies(Event event, String k, ArrayList<String> v, Hierarchy_Object ho, Template object_policy,
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
                    //TODO: -> antes de aquí setear valores de supresion y generalización
                    Integer suppresion_level;
                    //
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
                    }else if((suppresion_level = object_policy.isSuppresion(k, a.getObject_relation())) != null){
                        //TODO: mejorable en eficiencia, ya que la función is suppresion recorre la estructura de pol
                        //es suppresion
                        Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
                        String replace = getSupression(ai, a, suppresion_level);
                        if(replace == null){
                            //TODO: error
                        }
                        //set new value
                        a.setValue(replace);
                    }else if((suppresion_level = object_policy.isGeneralization(k, a.getObject_relation())) != null){
                        //TODO: mejorar en eficiencia en estos dos ultimos if - NECESARIO
                        //caso de atributos cuya política sea de generalizacion
                        Att_indv ai = ho.getAttributeIndv(a.getObject_relation());
                        switch (ai.getAttributeType()){
                            case "static":
                                String replace = getGeneralization(ai, a, suppresion_level);
                                a.setValue(replace);
                                break;
                            case "interval":
                                String rplace = generalize_interval(ai, a, suppresion_level);
                                a.setValue(rplace);
                                break;
                            default:
                                //TODO: no deberia entrar aquí, si lo hace es por un error en el fichero de jerarquías
                                break;
                        }
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

    public void setAllUuidtoNull(Event e) {
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

    //return string that references interval that replace original value
    private String generalize_interval(Att_indv ai, Attribute a, Integer level){
        Pattern lessthan = Pattern.compile("<([0-9]+)");
        Pattern lessEqualthan = Pattern.compile("<=([0-9]+)");
        Pattern biggerthan = Pattern.compile(">([0-9]+)");
        Pattern biggerEqualthan = Pattern.compile(">=([0-9]+)");
        Pattern interval = Pattern.compile("([0-9]+)-([0-9]+)");
        String rturn = null;
        Att_gn g = ai.getAttributeGeneralization().get(level);
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
                    rturn = comparison;
                    System.out.println("Añadiendo < que");
                    break;
                }
            } else if (mlet.matches()) {
                if (number_value <= Integer.valueOf(mlet.group(1))) {
                    rturn = comparison;
                    System.out.println(number_value + "Añadiendo <= que");
                    break;
                }
            } else if (mbt.matches()) {
                if (number_value > Integer.valueOf(mbt.group(1))) {
                    rturn = comparison;
                    System.out.println(number_value + "Añadiendo > que");
                    break;
                }
            } else if (mbet.matches()) {
                if (number_value >= Integer.valueOf(mbet.group(1))) {
                    rturn = comparison;
                    System.out.println(number_value + "Añadiendo >= que");
                    break;
                }
            } else {
                // interval
                System.out.println("MATCHEA " + mi.matches());
                if (number_value >= Integer.valueOf(mi.group(1)) && number_value <= Integer.valueOf(mi.group(2))) {
                    rturn = comparison;
                    System.out.println(number_value + "Añadiendo intervalo " + comparison);
                    break;
                }
            }
        }
        return rturn;
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
