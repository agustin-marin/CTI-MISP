package models.Policies;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PetMetadata {
	//TODO: Change to use a dictionary instead of this attribute
			@SerializedName("k")
			@Expose
			private Integer k;
			@SerializedName("t")
			@Expose
			private Double t;
			@SerializedName("l")
			@Expose
			private Integer l;
			
			public Integer getL() {
				return l;
			}

			public void setL(Integer l) {
				this.l = l;
			}

			@SerializedName("c")
			@Expose
			private Integer c;	//for l-diversity recursive(c,l)
			

			@SerializedName("level")
			@Expose
			private Integer level;
			

			public Integer getC() {
				return c;
			}

			public void setC(Integer c) {
				this.c = c;
			}

			/**
			* No args constructor for use in serialization
			*
			*/
			public PetMetadata() {
			}

			/**
			*
			* @param k
			*/
			public PetMetadata(Integer k) {
			super();
			this.k = k;
			}

			public Integer getK() {
			return k;
			}

			public void setK(Integer k) {
			this.k = k;
			}
			
			public Double getT() {
				return t;
			}

			public void setT(Double t) {
				this.t = t;
			}

			public Integer getLevel() {
			return level;
			}

			public void setLevel(Integer level) {
			this.level = level;
			}

}
