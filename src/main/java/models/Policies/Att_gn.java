package models.Policies;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Att_gn {
	
	@SerializedName("generalization")
	@Expose
	private List<String> generalization;
	@SerializedName("interval")
	@Expose
	private List<String> interval;
	@SerializedName("regex")
	@Expose
	private List<String> regex;
	
	public Att_gn() {
		this.generalization = new ArrayList<String>();
		this.interval = new ArrayList<String>();
		this.regex = new ArrayList<String>();
	}
	
	public List<String> getGeneralization() {
		return generalization;
	}
	public void setGeneralization(List<String> generalization) {
		this.generalization = generalization;
	}
	public List<String> getInterval() {
		return interval;
	}
	public void setInterval(List<String> interval) {
		this.interval = interval;
	}
	public List<String> getRegex() {
		return regex;
	}
	public void setRegex(List<String> regex) {
		this.regex = regex;
	}
	
	//check there is generalization and size of generalization is at least 2
	public boolean isCorrectGeneralization() {
		return generalization!=null && generalization.size() > 1;
	}
	
	public boolean isCorrectInterval() {
		return interval!=null && interval.size() > 1;
	}
	
	public String toJsonString() {
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
}
