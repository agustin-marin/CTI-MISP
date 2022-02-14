package models.MISP;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MISPResponse {
	@SerializedName("response")
	@Expose
	private ArrayList<EventMISP> response;
	
	
	public ArrayList<EventMISP> getResponse() {
		return response;
	}

	public void setResponse(ArrayList<EventMISP> response) {
		this.response = response;
	}

	public String toJsonString(){
	        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	        return gson.toJson(this);
	    }
}
