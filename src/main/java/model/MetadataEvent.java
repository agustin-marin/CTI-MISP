package model;

import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class MetadataEvent {

    @Property()
    private String date;


    public MetadataEvent(@JsonProperty("date") final String date) {
        this.date = date;
    }

}
