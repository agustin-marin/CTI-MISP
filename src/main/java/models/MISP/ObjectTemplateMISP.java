package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ObjectTemplateMISP {
	@SerializedName("ObjectTemplate")
	@Expose
	private ObjectTemplate objectTemplate;

	/*@SerializedName("Organisation")
	@Expose
	private Organisation organisation;*/

	@SerializedName("ObjectTemplateElement")
	@Expose
	private List<ObjectTemplateElement> objectTemplateElement = null;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ObjectTemplateMISP() {
	}

	/**
	 *
	 * @param objectTemplate
	 *
	 */
	public ObjectTemplateMISP(ObjectTemplate objectTemplate) {
		super();
		this.objectTemplate = objectTemplate;
		//this.organisation = organisation;
	}

	public ObjectTemplate getObjectTemplate() {
		return objectTemplate;
	}

	public void setObjectTemplate(ObjectTemplate objectTemplate) {
		this.objectTemplate = objectTemplate;
	}

	/*
	public Organisation getOrganisation() {
	return organisation;
	}

	public void setOrganisation(Organisation organisation) {
	this.organisation = organisation;
	}*/

	public List<ObjectTemplateElement> getObjectTemplateElement() {
		return objectTemplateElement;
	}

	public void setObjectTemplateElement(List<ObjectTemplateElement> objectTemplateElement) {
		this.objectTemplateElement = objectTemplateElement;
	}
	
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }

}
