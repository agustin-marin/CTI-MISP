package models.Policies;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Att_indv {
	
	public static final String STATIC = "static";
	public static final String INTERVAL = "interval";
	public static final String REGEX = "regex";
	
	@SerializedName("attribute-name")
	@Expose
	private String attributeName;
	@SerializedName("attribute-type")
	@Expose
	private String attributeType;
	@SerializedName("attribute-generalization")
	@Expose
	private List<Att_gn> attributeGeneralization;
	
	public Att_indv() {};
	
	public Att_indv(String name, String type, ArrayList<Att_gn> generalization) {
		this.attributeName = name;
		this.attributeType = type;
		this.attributeGeneralization = generalization;
	}
	public String getAttributeName() {
		return attributeName;
	}
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	public String getAttributeType() {
		return attributeType;
	}
	public void setAttributeType(String attributeType) {
		this.attributeType = attributeType;
	}
	public List<Att_gn> getAttributeGeneralization() {
		return attributeGeneralization;
	}
	public void setAttributeGeneralization(List<Att_gn> attributeGeneralization) {
		this.attributeGeneralization = attributeGeneralization;
	}
	public String toJsonString() {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
