package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ObjectTemplateElement {
	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("object_template_id")
	@Expose
	private String objectTemplateId;
	@SerializedName("object_relation")
	@Expose
	private String objectRelation;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("ui-priority")
	@Expose
	private String uiPriority;
	@SerializedName("categories")
	@Expose
	private List<String> categories = null;
	@SerializedName("sane_default")
	@Expose
	private List<String> saneDefault = null;
	@SerializedName("values_list")
	@Expose
	private List<String> valuesList = null;
	@SerializedName("description")
	@Expose
	private String description;
	@SerializedName("disable_correlation")
	@Expose
	private Boolean disableCorrelation;
	@SerializedName("multiple")
	@Expose
	private Boolean multiple;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ObjectTemplateElement() {
	}

	/*
	/**
	 *
	 * @param objectTemplateId
	 * @param valuesList
	 * @param saneDefault
	 * @param objectRelation
	 * @param description
	 * @param id
	 * @param categories
	 * @param type
	 * @param disableCorrelation
	 * @param uiPriority
	 */
	/*public ObjectTemplateElement(String id, String objectTemplateId, String objectRelation, String type, String uiPriority, List<String> categories, List<String> saneDefault, List<String> valuesList, String description, Boolean disableCorrelation, Boolean multiple) {
		super();
		this.id = id;
		this.objectTemplateId = objectTemplateId;
		this.objectRelation = objectRelation;
		this.type = type;
		this.uiPriority = uiPriority;
		this.categories = categories;
		this.saneDefault = saneDefault;
		this.valuesList = valuesList;
		this.description = description;
		this.disableCorrelation = disableCorrelation;
		this.multiple = multiple;
	}*/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObjectTemplateId() {
		return objectTemplateId;
	}

	public void setObjectTemplateId(String objectTemplateId) {
		this.objectTemplateId = objectTemplateId;
	}

	public String getObjectRelation() {
		return objectRelation;
	}

	public void setObjectRelation(String objectRelation) {
		this.objectRelation = objectRelation;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUiPriority() {
		return uiPriority;
	}

	public void setUiPriority(String uiPriority) {
		this.uiPriority = uiPriority;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getSaneDefault() {
		return saneDefault;
	}

	public void setSaneDefault(List<String> saneDefault) {
		this.saneDefault = saneDefault;
	}

	public List<String> getValuesList() {
		return valuesList;
	}

	public void setValuesList(List<String> valuesList) {
		this.valuesList = valuesList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean getDisableCorrelation() {
		return disableCorrelation;
	}

	public void setDisableCorrelation(Boolean disableCorrelation) {
		this.disableCorrelation = disableCorrelation;
	}

	public Boolean getMultiple() {
		return multiple;
	}

	public void setMultiple(Boolean multiple) {
		this.multiple = multiple;
	}
	
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
