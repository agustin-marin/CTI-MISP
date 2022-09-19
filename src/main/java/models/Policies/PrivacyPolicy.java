package models.Policies;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
* No args constructor for use in serialization
*
*/

public class PrivacyPolicy {

	@SerializedName("attributes")
	@Expose
	private List<AttPolicy> attributes = null;
	@SerializedName("creator")
	@Expose
	private String creator;
	@SerializedName("uuid")
	@Expose
	private String uuid;
	@SerializedName("organization")
	@Expose
	private String organization;
	@SerializedName("templates")
	@Expose
	private List<Template> templates = null;
	@SerializedName("version")
	@Expose
	private String version;
	@SerializedName("dp")
	@Expose
	private boolean dp;
	
	
	public PrivacyPolicy() {
	}

	/**
	*
	* @param creator
	* @param organization
	* @param templates
	* @param attributes
	* @param version
	*/
	public PrivacyPolicy(List<AttPolicy> attributes, String creator, String organization, List<Template> templates, String version) {
	super();
	this.attributes = attributes;
	this.creator = creator;
	this.organization = organization;
	this.templates = templates;
	this.version = version;
	}

	public List<AttPolicy> getAttributes() {
	return attributes;
	}

	public void setAttributes(List<AttPolicy> attributes) {
	this.attributes = attributes;
	}

	public String getCreator() {
	return creator;
	}

	public void setCreator(String creator) {
	this.creator = creator;
	}

	public String getOrganization() {
	return organization;
	}

	public void setOrganization(String organization) {
	this.organization = organization;
	}

	public List<Template> getTemplates() {
	return templates;
	}

	public void setTemplates(List<Template> templates) {
	this.templates = templates;
	}

	public String getVersion() {
	return version;
	}

	public void setVersion(String version) {
	this.version = version;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	
	public boolean isDp() {
		return dp;
	}

	public void setDp(boolean dp) {
		this.dp = dp;
	}

	public AttPolicy getAtt(String attname) {
		for(AttPolicy ap : this.getAttributes()) {
			if(ap.getName().equals(attname)) return ap;
		}
		return null;
	}

	//returns HashMap with attribute name and first pet(is our actual approach as over an attribute only can be applied one pet)
	public HashMap<String, String> attNamePet(){
		HashMap<String, String> list = new HashMap<String, String>();
		for(AttPolicy ap: attributes) {
			list.put(ap.getName(), ap.getPets().get(0).getScheme());
		}
		return list;
	}
	
	//return 0 in case dont have k-anonimity
	public Integer attGetK(String attname) {
		for(AttPolicy ap : attributes) {
			if(ap.getName().equals(attname)) return ap.getKanonymity();
		}
		return 0;
	}
	
	public double attGetT(String attname) {
		for(AttPolicy ap : attributes) {
			if(ap.getName().equals(attname)) return ap.getTcloseness();
		}
		return 0;
	}
	
	public Integer attGetCLDiversity(String attname) {
		//TODO: posible errata
		int t;
		int c;
		for(AttPolicy ap : attributes) {
			if(ap.getName().equals(attname));
		}
		return 0;
	}
	
	
	
	//return hashmap with name of attribute and level in case it has all supression or all generalization scheme
		public HashMap<String, Integer> attLevelSupression(){
			String SUPPRESSION = "suppression";
			String GENERALIZATION = "generalization";
			HashMap<String, Integer> list = new HashMap<>();
			for(AttPolicy ap : attributes){
				Pet pet = ap.getPets().get(0);
				String scheme = pet.getScheme();
				if(scheme.equals(SUPPRESSION) || scheme.equals(GENERALIZATION)) list.put(ap.getName(), pet.getLevel());
			}
			return list;
		}
	
	
	//return attribute names that has t-closeness policy of an object
	public ArrayList<String> getObjectTclosenessAttributes(String object_name){
		ArrayList<String> att_names = new ArrayList<String>();
		for(Template t : getTemplates()) {
			if(t.getName().equals(object_name)) {
				for(AttPolicy ap : t.getAttributes()) {
					for(Pet p : ap.getPets()) {
						if(p.getScheme().equals("t-closeness")) {
							att_names.add(ap.getName());
							break;
						}
					}
				}
				break;
			}
		}
		return att_names;
	}
	
	//return attributes name and t of an object - return only the first t //TODO: fix the number of t that could
	//be accepted by an attribute. I mean, could apply more than one t-closeness¿?
	public HashMap<String, Double> getAttsAndTObject(String object_name){
		HashMap<String, Double> att_names = new HashMap<String, Double>();
		for(Template t : getTemplates()) {
			if(t.getName().equals(object_name)) {
				for(AttPolicy ap : t.getAttributes()) {
					for(Pet p : ap.getPets()) {
						if(p.getScheme().contains("t-closeness")) {
							att_names.put(ap.getName(), p.getMetadata().getT());
							break;
						}
					}
				}
				break;
			}
		}
		return att_names;
	}
	
	
	public Template getPolicyObject(String name){
		for(Template t : templates) {
			if(t.getName().equals(name)) return t;
		}
		return null;
	}
	
	
	public boolean isOnlyAttributes() {
		if(getAttributes() == null || getAttributes().size() == 0) {
			return false;
		}
		return true;
	}
	
	public boolean formatCorrect() {
		if((getAttributes() == null || getAttributes().size() == 0) 
				&& (getTemplates()!=null && getTemplates().size() > 0)) {
			return true;
		}
		if((getTemplates() == null || getTemplates().size() == 0) 
				&& (getAttributes()!=null && getAttributes().size() > 0)) {
			return true;
		}
		
		return false;
	}
	
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
    
}

