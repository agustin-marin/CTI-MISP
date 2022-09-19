package models.Policies;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Att_agrupation_metadata {
	@SerializedName("att_low_bounds")
	@Expose
	private List<String> att_low_bounds;
	@SerializedName("att_high_bounds")
	@Expose
	private List<String> att_high_bounds;
	@SerializedName("epsilon")
	@Expose
	private double epsilon;
	@SerializedName("delta")
	@Expose
	private double delta;
	@SerializedName("sensitivity")
	@Expose
	private double sensitivity;
	public List<String> getAtt_low_bounds() {
		return att_low_bounds;
	}
	public void setAtt_low_bounds(List<String> att_low_bounds) {
		this.att_low_bounds = att_low_bounds;
	}
	public List<String> getAtt_high_bounds() {
		return att_high_bounds;
	}
	public void setAtt_high_bounds(List<String> att_high_bounds) {
		this.att_high_bounds = att_high_bounds;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	public double getDelta() {
		return delta;
	}
	public void setDelta(double delta) {
		this.delta = delta;
	}
	public double getSensitivity() {
		return sensitivity;
	}
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}
	public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
}
