package models.Policies;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Att_agrupation {
	@SerializedName("att_names")
	@Expose
	private List<String> att_names;
	@SerializedName("apply_to_all")
	@Expose
	private boolean apply_to_all;
	@SerializedName("scheme")
	@Expose
	private String scheme;
	@SerializedName("metadata")
	@Expose
	private Att_agrupation_metadata metadata;
	public List<String> getAtt_names() {
		return att_names;
	}
	public void setAtt_names(List<String> att_names) {
		this.att_names = att_names;
	}
	public boolean isApply_to_all() {
		return apply_to_all;
	}
	public void setApply_to_all(boolean apply_to_all) {
		this.apply_to_all = apply_to_all;
	}
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
