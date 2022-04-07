import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import contracts.MipsEventSaver;
import models.MISP.Event;

import java.nio.charset.StandardCharsets;

public class Anonimization {

    public static void main(String[] args) {
        MipsEventSaver m = new MipsEventSaver();
        String sevent = "{\"uuid\":\"20fbf9b2-35df-4205-b2aa-f06367aa56a1\",\"date\":\"2021-6-16\",\"threat_level_id\":1,\"info\":\"testevent-anomaly\",\"published\":false,\"analysis\":0,\"distribution\":0,\"Attribute\":[{\"type\":\"ip-src\",\"category\":\"Network activity\",\"uuid\":\"08dab4ec-e797-4692-8334-d6a1155d0b3c\",\"object_relation\":\"ip-src\",\"value\":\"10.20.30*\"},{\"type\":\"ip-src\",\"category\":\"Network activity\",\"uuid\":\"10df74df-554a-420d-87dc-628c48d421c6\",\"object_relation\":\"ip-src\",\"value\":\"10.20.30*\"},{\"type\":\"ip-src\",\"category\":\"Network activity\",\"uuid\":\"9f58e644-4043-4051-9809-b9a737e28b4c\",\"object_relation\":\"ip-src\",\"value\":\"10.20.30*\"},{\"type\":\"ip-src\",\"category\":\"Network activity\",\"uuid\":\"2b37869e-0999-490f-b46d-dd9025146d86\",\"object_relation\":\"ip-src\",\"value\":\"10.20.45*\"},{\"type\":\"ip-src\",\"category\":\"Network activity\",\"uuid\":\"080bd07c-7f6e-4127-a6d2-bdbce0361196\",\"object_relation\":\"ip-src\",\"value\":\"10.20.45*\"},{\"type\":\"ip-dst\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst\",\"value\":\"198.204.318.291\"},{\"type\":\"ip-dst\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst\",\"value\":\"198.204.318.241\"},{\"type\":\"ip-dst\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst\",\"value\":\"198.204.318.241\"},{\"type\":\"ip-dst\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst\",\"value\":\"198.204.318.259\"},{\"type\":\"port\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst-port\",\"value\":\"8080\"},{\"type\":\"port\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst-port\",\"value\":\"80\"},{\"type\":\"port\",\"category\":\"Network activity\",\"object_relation\":\"ip-dst-port\",\"value\":\"8080\"},{\"type\":\"datetime\",\"category\":\"Other\",\"object_relation\":\"creation_date\",\"value\":\"2021-04-3\"}]}";
        Gson g = new Gson();
        Event e = g.fromJson(sevent, Event.class);
        String hashe = Hashing.sha256().hashString(e.toJsonString(), StandardCharsets.UTF_8).toString();
       //System.out.println("Comprueba " + m.comprueba(e, hashe ));
    }
}
