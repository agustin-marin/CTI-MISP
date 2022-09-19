package models.Policies;

import java.util.List;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
	@SerializedName("dp_params")
	@Expose
	private Att_dp_param dp_params;
	@SerializedName("dp")
	@Expose
	private boolean dp;
	

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
	
	public boolean isDp() {
		return dp;
	}

	public void setDp(boolean dp) {
		this.dp = dp;
	}
	
	public Att_dp_param getDp_params() {
		return dp_params;
	}

	public void setDp_params(Att_dp_param dp_params) {
		this.dp_params = dp_params;
	}

	//IF K-ANONYMITY RETURNS TRUE
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


	//NEWER VERSION OF GETKANONYMITY
	public int getkIfAnonymity(){
		Pet pet = pets.get(0);
		if(pet.getScheme().equals("k-anonymity")){
			return pet.getMetadata().getK();
		}
		return 0;
	}



	//IF K-ANONYMITY RETURNS K ASSOCIATED
	public int getKanonymity() {
		for (Pet pet: this.pets ) {
			String scheme = pet.getScheme();
			if (scheme.contains("k-anonymity")){
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
