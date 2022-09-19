package models.Policies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Att_dp_param {
	@SerializedName("scheme")
	@Expose
	private String scheme;
	@SerializedName("metadata")
	@Expose
	private Att_agrupation_metadata metadata;
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	public Att_agrupation_metadata getMetadata() {
		return metadata;
	}
	public void setMetadata(Att_agrupation_metadata metadata) {
		this.metadata = metadata;
	}
	public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
