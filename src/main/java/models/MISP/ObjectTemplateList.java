package models.MISP;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ObjectTemplateList {
    @SerializedName("ObjectTemplate")
    private List<ObjectTemplate> objectTemplate;

	/*@SerializedName("Organisation")
	@Expose
	private Organisation organisation;*/


    /**
     * No args constructor for use in serialization
     *
     */
    public ObjectTemplateList() {
    }

    /**
     *
     * @param
     * @param objectTemplate
     */
    public ObjectTemplateList(List<ObjectTemplate> objectTemplate) {
        super();
        this.objectTemplate = objectTemplate;
        //this.organisation = organisation;
    }

    public List<ObjectTemplate> getObjectTemplate() {
        return objectTemplate;
    }

    public void setObjectTemplate(List<ObjectTemplate> objectTemplate) {
        this.objectTemplate = objectTemplate;
    }

	/*
	public Organisation getOrganisation() {
	return organisation;
	}

	public void setOrganisation(Organisation organisation) {
	this.organisation = organisation;
	}*/


    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
