package model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class MetadataEvent {

    @Property()
    private String datetime;

    @Property
    private Long timestamp;

    @Property
    private Token token;

    @Property
    private String uri;

    @Property
    private Response response;

    @Property
    private String method;

    @Property
    private String ipaddress;

    @Property
    private String instance;

    @Property
    private String username;
    @Property
    private Body body;

    public MetadataEvent(@JsonProperty("datetime") final String datetime, @JsonProperty("timestamp") final long timestamp,
                         @JsonProperty("token") final  Token token,@JsonProperty("url") final  String uri,
                         @JsonProperty("response") final  Response response,@JsonProperty("method") final  String method,
                         @JsonProperty("ipaddress") final  String ipaddress, @JsonProperty("instance") final String instance,
                         @JsonProperty("username") final  String username, @JsonProperty("body") final  Body body) {
        this.datetime = datetime;
        this.body = body;
        this.timestamp = timestamp;
        this.token = token;
        this.uri = uri;
        this.response = response;
        this.method = method;
        this.ipaddress = ipaddress;
        this.instance = instance;
        this.username = username;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
