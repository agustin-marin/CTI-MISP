package models.Policies;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import models.MISP.Event;
import models.MISP.EventMISP;
import models.Policies.Hierarchy;

public class EventAnon {

	@SerializedName("Event")
	@Expose
	private Event event;
	@SerializedName("Privacy-policy")
	@Expose
	private PrivacyPolicy privacyPolicy;
	@SerializedName("Hierarchy-policy")
	@Expose
	private Hierarchy hierarchypolicy;
	
	
	public EventAnon(Event event, PrivacyPolicy privacyPolicy, Hierarchy hierarchyPolicy) {
		super();
		this.event = event;
		this.privacyPolicy = privacyPolicy;
		this.hierarchypolicy = hierarchyPolicy;
	}

	public Hierarchy getHierarchypolicy() {
		return hierarchypolicy;
	}

	public void setHierarchypolicy(Hierarchy hierarchypolicy) {
		this.hierarchypolicy = hierarchypolicy;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public PrivacyPolicy getPrivacyPolicy() {
		return privacyPolicy;
	}

	public void setPrivacyPolicy(PrivacyPolicy privacyPolicy) {
		this.privacyPolicy = privacyPolicy;
	}
	
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
    

}
