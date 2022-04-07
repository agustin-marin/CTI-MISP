package models.MISP;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;


public class Object implements Cloneable {

	private String id;
	private String name;
	@SerializedName("meta-category")
	private String metaCategory;
	private String description;
	private String templateUuid;
	private String templateVersion;
	private String eventId;
	private String uuid;
	private String timestamp;
	private String distribution;
	private String sharingGroupId;
	private String comment;
	private Boolean deleted;
	private String template_uuid;
	private java.lang.Object firstSeen;
	private java.lang.Object lastSeen;
	private List<java.lang.Object> objectReference = null;
	@SerializedName("Attribute")
	private List<Attribute> attribute = null;

	//Consttructor
	
	//cifrar iban y account-name---> 

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMetaCategory() {
		return metaCategory;
	}

	public void setMetaCategory(String metaCategory) {
		this.metaCategory = metaCategory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTemplateUuid() {
		return templateUuid;
	}

	public void setTemplateUuid(String templateUuid) {
		this.templateUuid = templateUuid;
	}

	public String getTemplateVersion() {
		return templateVersion;
	}

	public void setTemplateVersion(String templateVersion) {
		this.templateVersion = templateVersion;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getDistribution() {
		return distribution;
	}

	public void setDistribution(String distribution) {
		this.distribution = distribution;
	}

	public String getSharingGroupId() {
		return sharingGroupId;
	}

	public void setSharingGroupId(String sharingGroupId) {
		this.sharingGroupId = sharingGroupId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public java.lang.Object getFirstSeen() {
		return firstSeen;
	}

	public void setFirstSeen(java.lang.Object firstSeen) {
		this.firstSeen = firstSeen;
	}

	public java.lang.Object getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(java.lang.Object lastSeen) {
		this.lastSeen = lastSeen;
	}

	public List<java.lang.Object> getObjectReference() {
		return objectReference;
	}

	public void setObjectReference(List<java.lang.Object> objectReference) {
		this.objectReference = objectReference;
	}

	public List<Attribute> getAttribute() {
		return attribute;
	}

	public void setAttribute(List<Attribute> attribute) {
		this.attribute = attribute;
	}
	public String getTemplate_uuid() {
		return template_uuid;
	}

	public void setTemplate_uuid(String template_uuid) {
		this.template_uuid = template_uuid;
	}
	
	public boolean setAttributeValue(String name, String value) {
		for(Attribute a : this.attribute) {
			if(a.getObject_relation().equals(name)) {
				a.setValue(value);
				return true;
			}
		}
		return false;
	}
	
    /*@Override
    public String toString() {
        return "{ \n" +
                "\t\t id = " + id + '\n' +
                "\t\t name =" + name + '\n' +
                "\t\t metaCategory =" + metaCategory + '\n' +
                "\t\t description =" + description + '\n' +
                "\t\t uuid =" + uuid + '\n' +
                "\t\t eventId =" + eventId + '\n' +
                "\t\t distribution =" + distribution + '\n' +
                "\t\t comment =" + comment + '\n' +
                "\t\t Attribute =" + attribute + "\n }" ;
    }*/
	
	
	
	public String toString() {
        String attributes="";
        for(Attribute a : this.attribute) {
        	attributes+=a.toJsonString();
        }
		return "\n\t\t{ \n" +
                "\t\t id = " + id + '\n' +
                "\t\t name = " + name + '\n' +
                "\t\t metaCategory = " + metaCategory + '\n' +
                "\t\t description = " + description + '\n' +
                "\t\t uuid = " + uuid + '\n' +
                "\t\t eventId = " + eventId + '\n' +
                "\t\t distribution = " + distribution + '\n' +
                "\t\t comment = " + comment + '\n' +
                "\t\t Attributes = "  +
                  attributes + 
                "\n \t\t}" ;
    }
	
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {
    	 Object o = (Object)super.clone();
    		
    		LinkedList<Attribute> atributos = new LinkedList<Attribute>();
    		//las referencias a first seen y last seen tienen alising
    		//las referencias a object reference tienen aliasing
    		for (Attribute a : this.attribute) {
				Attribute clon = (Attribute)a.clone();
				atributos.add(clon);
			}
    		o.setAttribute(atributos);
    		return o;
    }

}