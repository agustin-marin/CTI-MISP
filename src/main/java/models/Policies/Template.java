package models.Policies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Generated;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Template {

	@SerializedName("attributes")
	@Expose
	private List<AttPolicy> attributes = null;
	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("uuid")
	@Expose
	private String uuid;
	@SerializedName("k-anonimity")
	@Expose
	private boolean kAnonimity;
	@SerializedName("k")
	@Expose
	private Integer k;
	

	public boolean iskAnonimity() {
		return kAnonimity;
	}

	public void setkAnonimity(boolean kAnonimity) {
		this.kAnonimity = kAnonimity;
	}

	public Integer getK() {
		return k;
	}

	public void setK(Integer k) {
		this.k = k;
	}

	/**
	 * No args constructor for use in serialization
	 *
	 */
	public Template() {
	}

	/**
	 *
	 * @param pets
	 * @param name
	 * @param attributes
	 * @param uuid
	 */
	public Template(List<AttPolicy> attributes, String name, String uuid) {
		super();
		this.attributes = attributes;
		this.name = name;
		this.uuid = uuid;
	}

	public List<AttPolicy> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<AttPolicy> attributes) {
		this.attributes = attributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public int getKofObject() {
		if(iskAnonimity()) return this.k;
		return 0;
	}
	
	
	public ArrayList<String> getQuasi(){
		String QUASI = "quasi"; 	//for comparison
		ArrayList<String> quasi_list = new ArrayList<String>();
		for(AttPolicy ap: attributes) {
			for(Pet pet : ap.getPets()) {
				if(pet.getScheme().equals(QUASI)) {
					quasi_list.add(ap.getName());
					break;
				}
			}
		}
		return quasi_list;
	}
	
	//TODO: hacer comprobaciones de que hierachical y quasi no puede haber en un mismo atributo, en el frontend
	public ArrayList<String> getQuasiAndHierachical(){
		String QUASI = "quasi"; 	//for comparison
		String TCLOS_HIERACHICAL = "t-closeness/hierachical";
		ArrayList<String> quasi_list = new ArrayList<String>();
		for(AttPolicy ap: attributes) {
			for(Pet pet : ap.getPets()) {
				if(pet.getScheme().equals(QUASI) || pet.getScheme().equals(TCLOS_HIERACHICAL)) {
					quasi_list.add(ap.getName());
					break;
				}
			}
		}
		return quasi_list;
	}
	
	public HashMap<String, ArrayList<String>> getSensitiveAndPet(){
		String TCLOS = "t-closeness";
		String LDIV = "l-diversity";
		HashMap<String, ArrayList<String>> sensitive_list = new HashMap<String, ArrayList<String>>();
		for(AttPolicy ap : attributes) {
			for(Pet pet : ap.getPets()) {
				if(pet.getScheme().contains(TCLOS) || pet.getScheme().contains(LDIV)) {
					if( ! sensitive_list.containsKey(ap.getName()) ) {
						sensitive_list.put(ap.getName(), new ArrayList<String>());
					}
					sensitive_list.get(ap.getName()).add(pet.getScheme());
				}
			}
		}
		return sensitive_list;
	}
	
	public ArrayList<String> getSensitive(){
		String TCLOS = "t-closeness";
		String LDIV = "l-diversity";
		ArrayList<String> sensitive_list = new ArrayList<String>();
		for(AttPolicy ap: attributes) {
			for(Pet pet : ap.getPets()) {
				if(pet.getScheme().contains(TCLOS) || pet.getScheme().contains(LDIV)) {
					sensitive_list.add(ap.getName());
					break;
				}
			}
		}
		return sensitive_list;
	}
	
	public AttPolicy getAttribute(String name) {
		for(AttPolicy a : attributes) {
			if(a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}
	
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }



}
