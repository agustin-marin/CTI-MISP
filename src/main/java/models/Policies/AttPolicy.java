package models.Policies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AttPolicy {

	@SerializedName("name")
	@Expose
	private String name;
	@SerializedName("type")
	@Expose
	private String type;
	@SerializedName("pets")
	@Expose
	private List<Pet> pets = null;
	

	public List<Pet> getPets() {
		return pets;
	}

	public void setPets(List<Pet> pets) {
		this.pets = pets;
	}


	public AttPolicy() {
	}

	/**
	 *
	 * @param name
	 * @param type
	 */
	public AttPolicy(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public boolean applykanonymity() {
		for (Pet pet: this.pets ) {
			String scheme = pet.getScheme();
			if (scheme.equals("k-anonymity")){
				return true;
			}
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	public int getKanonymity() {
		for (Pet pet: this.pets ) {
			String scheme = pet.getScheme();
			if (scheme.contains("k-anonimity")){
				System.out.println("k" + pet.getMetadata().getK());
				return pet.getMetadata().getK();
			}
		}
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double getTcloseness() {
		for (Pet pet: this.pets ) {
			String scheme = pet.getScheme();
			if (scheme.contains("t-closeness")){
				return pet.getMetadata().getT();
			}
		}
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	//returns l and c, in case not found or not type recursive l-diversity, the two values are 0
	public int[] getLDIVRecursive() {
		int c = 0;
		int l = 0;
		String LDIV_RECURSIVE = "l-diversity/recursive";
		for (Pet pet: this.pets ) {
			String scheme = pet.getScheme();
			if (scheme.equals(LDIV_RECURSIVE)){
				l = pet.getMetadata().getL();
				c = pet.getMetadata().getC();
				break;
			}
		}
		int[] values = new int[2];
		values[0] = l;
		values[1] = c;
		return values;
	}

}
