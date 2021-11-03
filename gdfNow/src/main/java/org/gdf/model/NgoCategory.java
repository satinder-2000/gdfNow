/**
 *
 */
package org.gdf.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author satindersingh
 *
 */
@Entity
@Table(name = "NGO_CATEGORY")
public class NgoCategory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String type;

    private String subtype;

    private boolean confirmed;
    
    @Transient
    private String typeTemp;

    @Transient
    private String subtypeTemp;
    
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getTypeTemp() {
        return typeTemp;
    }

    public void setTypeTemp(String typeTemp) {
        this.typeTemp = typeTemp;
    }

    public String getSubtypeTemp() {
        return subtypeTemp;
    }

    public void setSubtypeTemp(String subtypeTemp) {
        this.subtypeTemp = subtypeTemp;
    }
    
    

}
