package models.MISP;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Requirements {

	@SerializedName("required")
	@Expose
	private List<String> required = null;

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Requirements() {
	}

	/**
	 *
	 * @param required
	 */
	public Requirements(List<String> required) {
		super();
		this.required = required;
	}

	public List<String> getRequired() {
		return required;
	}

	public void setRequired(List<String> required) {
		this.required = required;
	}

}