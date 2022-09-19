package models.MISP;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;


public class Tag {

    private String id;
    private String name;
    private String colour;
    private Boolean exportable;
    @SerializedName("hide_tag")
    private Boolean hideTag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public Boolean getExportable() {
        return exportable;
    }

    public void setExportable(Boolean exportable) {
        this.exportable = exportable;
    }

    public Boolean getHideTag() {
        return hideTag;
    }

    public void setHideTag(Boolean hideTag) {
        this.hideTag = hideTag;
    }
   /* @Override
    public String toString() {
        String s = "{\n "+
                "\t\t id = " + id + '\n' +
                "\t\t name =" + name + '\n' +
                "\t\t colour =" + colour + '\n' +
                "\t\t exportable =" + exportable + '\n' +
                "\t\t hideTag =" + hideTag + "\n}";

        return s;
    }*/
    
    public String toJsonString(){
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        return gson.toJson(this);
    }
    
    
    
    
    
    @Override
    protected java.lang.Object clone() throws CloneNotSupportedException {
    	// TODO Auto-generated method stub
    	return super.clone();
    }
}

