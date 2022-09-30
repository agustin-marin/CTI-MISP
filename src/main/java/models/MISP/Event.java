package models.MISP;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class Event implements Cloneable{
	
	@SerializedName("id")
	@Expose
    private Integer id;
	@SerializedName("uuid")
	@Expose
    private String uuid;
	@SerializedName("orgc_id")
	@Expose
    private Integer orgc_id;
	@SerializedName("org_id")
	@Expose
    private Integer org_id;
	@SerializedName("date")
	@Expose
    private String date;
	@SerializedName("disable_correlation")
	@Expose
    private Boolean disable_correlation;
	@SerializedName("threat_level_id")
	@Expose
    private Integer threat_level_id;
    @SerializedName("sharing_group_id")
    @Expose
    private Integer sharing_group_id;
	@SerializedName("info")
	@Expose
    private String info;
	@SerializedName("published")
	@Expose
    private Boolean published;
	@SerializedName("attribute_count")
	@Expose
    private Integer attribute_count;
	@SerializedName("analysis")
	@Expose
    private Integer analysis;
	@SerializedName("distribution")
	@Expose
    private Integer distribution;
    @SerializedName("Attribute")
    @Expose
    private List<Attribute> attribute = null;
    @SerializedName("Tag")
    @Expose
    private List<Tag> tag = null;
	@SerializedName("Object")
	@Expose
    private List<Object> object = null;
	


	public List<Tag> getTag() {
		return tag;
	}

	public void setTag(List<Tag> tag) {
		this.tag = tag;
	}

	public List<Object> getObject() {
		return object;
	}

	public void setObject(List<Object> object) {
		this.object = object;
	}
	
	// tags

    public Integer getOrgc_id() {
        return orgc_id;
    }

    public void setOrgc_id(Integer orgc_id) {
        this.orgc_id = orgc_id;
    }

    public Integer getOrg_id() {
        return org_id;
    }

    public void setOrg_id(Integer org_id) {
        this.org_id = org_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
    
	public Boolean getDisable_correlation() {
		return disable_correlation;
	}

	public void setDisable_correlation(Boolean disable_correlation) {
		this.disable_correlation = disable_correlation;
	}
    
    public Integer getThreat_level_id() {
        return threat_level_id;
    }

    public void setThreat_level_id(Integer threat_level_id) {
        this.threat_level_id = threat_level_id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public Integer getAttribute_count() {
        return attribute_count;
    }

    public void setAttribute_count(Integer attribute_count) {
        this.attribute_count = attribute_count;
    }

    public Integer getAnalysis() {
        return analysis;
    }

    public void setAnalysis(Integer analysis) {
        this.analysis = analysis;
    }

    public Integer getDistribution() {
        return distribution;
    }

    public void setDistribution(Integer distribution) {
        this.distribution = distribution;
    }

    public Integer getSharing_group_id() {
        return sharing_group_id;
    }

    public void setSharing_group_id(Integer sharing_group_id) {
        this.sharing_group_id = sharing_group_id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    

    /*@Override
    public String toString() {
        String event = "Event: \n" +
                "\t id = " + id + '\n' +
                "\t orgc_id =" + org_id + '\n' +
                "\t date =" + date + '\n' +
                "\t threat_level_id =" + threat_level_id + '\n' +
                "\t info =" + info + '\n' +
                "\t published =" + published + '\n' +
                "\t attribute_count =" + attribute_count + '\n' +
                "\t analysis =" + analysis + '\n' +
                "\t distribution =" + distribution + '\n';
        event = event+ "\tAttribute:"+  attribute +'\n';
        event = event+ "\tTags:" + tag + '\n';
        event = event + "\tObject:" + object + '\n';
        event = event + "\n}";

        return event;
    }*/

    public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public List<Attribute> getAttribute() {
		return attribute;
	}

	public void setAttribute(List<Attribute> attribute) {
		this.attribute = attribute;
	}

	public String toString() {
        String event = "Event: \n" +
                "\t id = " + id + '\n' +
                "\t orgc_id =" + org_id + '\n' +
                "\t date =" + date + '\n' +
                "\t threat_level_id =" + threat_level_id + '\n' +
                "\t info =" + info + '\n' +
                "\t published =" + published + '\n' +
                "\t attribute_count =" + attribute_count + '\n' +
                "\t analysis =" + analysis + '\n' +
                "\t distribution =" + distribution + '\n';
        String object = "";
        for(Object o : this.object) {
        	object+=o.toString();
        }
        event = event+object ;

        return event;
    }
    
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }

    public List<Attribute> getAttributes() {
        return attribute;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attribute = attributes;
    }

	public boolean hasObjects() {
		if (object == null) {
			return false;
		}
		return (object.size()> 0);
	}
    
    public Integer get_number_attributes() {
    	if( attribute == null ) {
    		return 0;
    	}
    	Integer number_attr = attribute.size();
		return number_attr;
    	
    }
    
    public Object getObjectByUuid(String uuid) {
	    return this.object.stream().filter(o -> uuid.equals(o.getUuid())).findFirst().orElse(null);
	}
    
    public Attribute getAttributeByUuid(String uuid) {
	    return this.attribute.stream().filter(a -> uuid.equals(a.getUuid())).findFirst().orElse(null);
	}
    
    
    public boolean correctFormat() {
    	if((getObject() == null || getObject().size() == 0) && 
    			(getAttributes() != null && getAttributes().size() > 0)) {
    		return true;
    	}
    	if((getAttributes() == null || getAttributes().size() == 0) && 
    			(getObject() != null && getObject().size() > 0)) {
    		return true;
    	}
    	return false;
    }
    
    public boolean isOnlyAttributes() {
    	if(getAttributes() != null && getAttributes().size() > 0) return true;
    	return false;
    }
   /* 
    @Override
    public java.lang.Object clone() throws CloneNotSupportedException {
    	// TODO Auto-generated method stub
    	Event e = (Event)super.clone();
    	LinkedList<Object> objetos = new LinkedList<Object>();
    	LinkedList<Attribute> atributos = new LinkedList<Attribute>();
    	LinkedList<Tag> tags = new LinkedList<Tag>();
    	for (Object objeto : this.object) {
			Object clon = (Object)objeto.clone();	//hacer clone
			objetos.add(clon);
		}
    	for (Tag tag : tags) {
			Tag clon = (Tag)tag.clone();
			tags.add(clon);
		}
    	for (Attribute attribute : atributos) {
			Attribute clon = (Attribute)attribute.clone();
			atributos.add(clon);
		}
    	e.setObject(objetos);
    	e.setAttributes(atributos);
    	e.setTag(tags);
    	return e;
    }
    */
}
