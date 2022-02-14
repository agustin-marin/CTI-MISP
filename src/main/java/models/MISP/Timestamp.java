package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Timestamp {
	@SerializedName("timestamp")
	@Expose
	private long timestamp;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	 public String toJsonString(){
	        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	        return gson.toJson(this);
	    }
}
