package models.Policies;

import java.util.ArrayList;
import java.util.HashMap;
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

	
	//return name and type of hierarchy
		public HashMap<String, String> attgetTypes(){
			HashMap<String, String> list = new HashMap<>();
			for(Att_indv ai : this.getHierarchyAttributes()){
				list.put(ai.getAttributeName(), ai.getAttributeType());
			}
			return list;
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
	
	
	//implementation for objects
	public boolean isRegex(String object_name, String attribute_name) {
		for(Hierarchy_Object o : this.hierarchyObjects) {
			if(o.getMisp_object_template().equals(object_name)) {
				Att_indv ai = o.getAttributeIndv(attribute_name);
				if(ai != null && ai.getAttributeType().toLowerCase().equals("regex")) {
					try {
						Att_gn first_gn = ai.getAttributeGeneralization().get(0);
						//we also could check format of string values if we wanted to
						return first_gn.getRegex().size() > 0;
					} catch (NullPointerException e) {
						System.out.println("DEBUG catch regex Hierarchy.java");
						return false;
					}
				}
				return false;
			}
		}
		return false;
	}
	
	//implementation for objects
	public boolean isGeneralization(String object_name, String attribute_name) {
		for(Hierarchy_Object o : this.hierarchyObjects) {
			if(o.getMisp_object_template().equals(object_name)) {
				Att_indv ai = o.getAttributeIndv(attribute_name);
				ArrayList<Att_gn> ag  = (ArrayList)ai.getAttributeGeneralization();
				for(Att_gn a : ag) {
					System.out.println("a gn");
				}
				System.out.println("Ag size a: " + ag.size());
				if((ai!=null && (ai.getAttributeType().toLowerCase().equals("interval") || ai.getAttributeType().toLowerCase().equals("static"))) && 
						(ag != null && ag.size() > 0)) {
					System.out.println("Hierarchy.java DEBUG cumple condición");
					String attributetype = ai.getAttributeType().toLowerCase();
					System.out.println("Hierarchy.java " + attributetype);
					System.out.println("Hierarchy.java " + attribute_name);
					for(Att_gn g : ai.getAttributeGeneralization()) {
						if(attributetype.equals("static") && !g.isCorrectGeneralization()) {
							System.out.println("Hierarchy.java false condition");
							return false;
						}else if( attributetype.equals("interval") && ! g.isCorrectInterval()) {
							System.out.println("Hierarchy.java false condition");
							return false;
						}
					}
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	
	//check if has type regex and has regex in his hierarchy specification
	public boolean isRegex(String attribute_name) {
		Att_indv ai = getAttIndv(attribute_name);
		if(ai != null && ai.getAttributeType().toLowerCase().equals("regex")) {
			try {
				Att_gn first_gn = ai.getAttributeGeneralization().get(0);
				//we also could check format of string values if we wanted to
				return first_gn.getRegex().size() > 0;
			} catch (NullPointerException e) {
				return false;
			}
		}
		return false;
	}
	
	public boolean isGeneralization(String attribute_name) {
		Att_indv ai = getAttIndv(attribute_name);
		ArrayList<Att_gn> ag  = (ArrayList)ai.getAttributeGeneralization();
		if((ai!=null && (ai.getAttributeType().toLowerCase().equals("interval") || ai.getAttributeType().toLowerCase().equals("static"))) && 
				(ag != null && ag.size() > 0)) {
			for(Att_gn g : ai.getAttributeGeneralization()) {
				if(! g.isCorrectGeneralization()) return false;
			}
			return true;
		}
		return false;
	}
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
