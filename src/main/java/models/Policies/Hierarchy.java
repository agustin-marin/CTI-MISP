package models.Policies;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Hierarchy{
	
	@SerializedName("hierarchy-description")
	@Expose
	private String hierarchyDescription;
	
	@SerializedName("uuid")
	@Expose
	private String uuid;
	
	@SerializedName("organization")
	@Expose
	private String organization;

	@SerializedName("version")
	@Expose
	private String version;

	@SerializedName("creator")
	@Expose
	private String creator;

	
	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@SerializedName("hierarchy_objects")
	@Expose
	private List<Hierarchy_Object> hierarchyObjects;	//objects hierarchies list

	@SerializedName("hierarchy_attributes")
	@Expose
	private List<Att_indv> hierarchyAttributes;	//attributes hierarchies list
	
	public Hierarchy() {};
	
	public Hierarchy(String description,ArrayList<Hierarchy_Object> hierarchyObjects, ArrayList<Att_indv> hierarchyAttributes) {
		this.hierarchyDescription = description;
		this.hierarchyObjects = hierarchyObjects;
		this.hierarchyAttributes = hierarchyAttributes;
	}
	
	public String getHierarchyDescription() {
		return hierarchyDescription;
	}

	public void setHierarchyDescription(String hierarchyDescription) {
		this.hierarchyDescription = hierarchyDescription;
	}

	public List<Hierarchy_Object> getHierarchyObjects() {
		return hierarchyObjects;
	}

	public void setHierarchyObjects(List<Hierarchy_Object> hierarchyObjects) {
		this.hierarchyObjects = hierarchyObjects;
	}

	public List<Att_indv> getHierarchyAttributes() {
		return hierarchyAttributes;
	}

	public void setHierarchyAttributes(List<Att_indv> hierarchyAttributes) {
		this.hierarchyAttributes = hierarchyAttributes;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTypeOfAttribute(String name) {
		for(Att_indv ai : hierarchyAttributes) {
			if(ai.getAttributeName().equals(name)) {
				return ai.getAttributeType();
			}
		}
		return null;
	}
	
	public Att_indv getAttIndv(String name) {
		for(Att_indv ai : hierarchyAttributes) {
			if(ai.getAttributeName().equals(name)) {
				return ai;
			}
		}
		return null;
	}

	public Hierarchy_Object getHierarchyObject(String name) {
		for(Hierarchy_Object ho : hierarchyObjects) {
			if(ho.getMisp_object_template().equals(name)) return ho;
		}
		return null;
	}
	
	public boolean isOnlyAttributes() {
		if(getHierarchyAttributes() != null && getHierarchyAttributes().size() > 0) return true;
		return false;
	}
	
	
	public boolean format_correct() {
		if((getHierarchyAttributes() == null || getHierarchyAttributes().size() == 0) 
				&& (getHierarchyObjects()!=null && getHierarchyObjects().size() > 0)) {
			//estaria bien añadir que cada una de las jerarquias individuales esta correcta
			return true;
		}
		if((getHierarchyObjects() == null || getHierarchyObjects().size() == 0) 
				&& (getHierarchyAttributes()!=null && getHierarchyAttributes().size() > 0)) {
			return true;
		}
		
		return false;
	}
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
