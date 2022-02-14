package models.Policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Hierarchy_Object {
	
	@SerializedName("misp-object-template")
	@Expose
	private String misp_object_template;
	@SerializedName("attribute-hierarchies")
	@Expose
	private List<Att_indv> attributeHierarchies;
	
	
	
	
	
	public Hierarchy_Object() {};
	
	public Hierarchy_Object(String obj_template, ArrayList<Att_indv> hierarchies) {
		this.misp_object_template = obj_template;
		this.attributeHierarchies = hierarchies;
	}
	
	public String getMisp_object_template() {
		return misp_object_template;
	}
	public void setMisp_object_template(String misp_object_template) {
		this.misp_object_template = misp_object_template;
	}
	public List<Att_indv> getAttributeHierarchies() {
		return attributeHierarchies;
	}
	public void setAttributeHierarchies(List<Att_indv> attributeHierarchies) {
		this.attributeHierarchies = attributeHierarchies;
	}
	public ArrayList<String> getAttributesName(){
		ArrayList<String> names = new ArrayList<String>();
		for(Att_indv ai : attributeHierarchies) {
			names.add(ai.getAttributeName());
		}
		return names;
	}
	
	public Att_indv getAttributeIndv(String name) {
		for(Att_indv ai : getAttributeHierarchies()) {
			if(ai.getAttributeName().equals(name)) return ai;
		}
		return null;
	}
	
	public HashMap<String, String> getAttributesNameType(){
		HashMap<String, String> attname_type = new HashMap<String, String>();
		for(Att_indv ai : attributeHierarchies) {
			attname_type.put(ai.getAttributeName(), ai.getAttributeType());
		}
		return attname_type;
	}
	
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
