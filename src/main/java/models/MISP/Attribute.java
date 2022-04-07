package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Attribute {
    private Integer id;
    private String type;
    private String category;
    private Boolean to_ids;
    private String uuid;
    private Integer event_id;
    private Integer distribution;
    private String comment;
    private String object_relation;
	private String value;
    
	
	public Attribute (String object_relation, String value) {
		this.object_relation = object_relation;
		this.value = value;
	}
	
    public String getObject_relation() {
		return object_relation;
	}

	public void setObject_relation(String object_relation) {
		this.object_relation = object_relation;
	}





    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public Integer getEvent_id() {
        return event_id;
    }

    public void setEvent_id(Integer event_id) {
        this.event_id = event_id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getTo_ids() {
        return to_ids;
    }

    public void setTo_ids(Boolean to_ids) {
        this.to_ids = to_ids;
    }

    public Integer getDistribution() {
        return distribution;
    }

    public void setDistribution(Integer distribution) {
        this.distribution = distribution;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

   /* @Override
    public String toString() {
        return "{ \n" +
                "\t\t\t id = " + id + '\n' +
                "\t\t\t  type =" + type + '\n' +
                "\t\t\t  category =" + category + '\n' +
                "\t\t\t  to_ids =" + to_ids + '\n' +
                "\t\t\t  uuid =" + uuid + '\n' +
                "\t\t\t  event_id =" + event_id + '\n' +
                "\t\t\t  distribution =" + distribution + '\n' +
                "\t\t\t  comment =" + comment + '\n' +
                "\t\t\t  object_relation ="+ object_relation +'\n'+
                "\t\t\t  value =" + value + "\n }" ;
    }
    public String toString() {
        return "{ \n" +
                "\t\t\t id = " + id + '\n' +
                "\t\t\t  type = " + type + '\n' +
                "\t\t\t  category = " + category + '\n' +
                "\t\t\t  to_ids = " + to_ids + '\n' +
                "\t\t\t  uuid = " + uuid + '\n' +
                "\t\t\t  event_id = " + event_id + '\n' +
                "\t\t\t  distribution = " + distribution + '\n' +
                "\t\t\t  comment = " + comment + '\n' +
                "\t\t\t  object_relation = "+ object_relation +'\n'+
                "\t\t\t  value = " + value + "\n }" ;
    }*/
    
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }

    @Override
    protected java.lang.Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
}
