package models.Policies;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pet {
	@SerializedName("scheme")
	@Expose
	private String scheme;
	@SerializedName("metadata")
	@Expose
	private PetMetadata metadata;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Pet() {
	}

	/**
	 *
	 * @param metadata
	 * @param scheme
	 */
	public Pet(String scheme, PetMetadata metadata) {
		super();
		this.scheme = scheme;
		this.metadata = metadata;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public PetMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(PetMetadata metadata) {
		this.metadata = metadata;
	}

	public String getLevel() {
		return metadata.getLevel()!=null ?  metadata.getLevel() : "medium";
	}

}
