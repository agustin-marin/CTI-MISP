package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class EventMISP {
    @SerializedName("Event")
    @Expose
    private Event event;

    public EventMISP(Event event) {
    	this.event = event;
    }

    public void setEvent(Event event){
        this.event=event;
    }

    public Event getEvent() {
        return event;}

    public EventMISP getEventMISP() {
    	return this;
    }
    
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
    
    @Override
    public String toString() {
    	return event.toString();
    }

}
