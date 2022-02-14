package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ObjectTemplate {

	@SerializedName("id")
	@Expose
	private String id;
	@SerializedName("user_id")
	@Expose
	private String userId;
	@SerializedName("org_id")
	@Expose
	private String orgId;
	@SerializedName("uuid")
	@Expose
	private String uuid;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("meta-category")
	@Expose
	private String metaCategory;
	@SerializedName("description")
	@Expose
	private String description;
	@SerializedName("version")
	@Expose
	private String version;
	@SerializedName("requirements")
	@Expose
	private Requirements requirements;
	@SerializedName("fixed")
	@Expose
	private Boolean fixed;
	@SerializedName("active")
	@Expose
	private Boolean active;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public ObjectTemplate() {
	}

	/**
	 *
	 * @param requirements
	 * @param metaCategory
	 * @param name
	 * @param description
	 * @param active
	 * @param fixed
	 * @param id
	 * @param userId
	 * @param uuid
	 * @param version
	 * @param orgId
	 */
	/*public ObjectTemplate(String id, String userId, String orgId, String uuid, String name, String metaCategory, String description, String version, Requirements requirements, Boolean fixed, Boolean active) {
		super();
		this.id = id;
		this.userId = userId;
		this.orgId = orgId;
		this.uuid = uuid;
		this.name = name;
		this.metaCategory = metaCategory;
		this.description = description;
		this.version = version;
		this.requirements = requirements;
		this.fixed = fixed;
		this.active = active;
	}*/

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Requirements getRequirements() {
		return requirements;
	}

	public void setRequirements(Requirements requirements) {
		this.requirements = requirements;
	}

	public Boolean getFixed() {
		return fixed;
	}

	public void setFixed(Boolean fixed) {
		this.fixed = fixed;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}