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
@Table(name="DEED_CATEGORY")
public class DeedCategory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	
	String type;
	
	String subtype;
        
        private boolean confirmed;
        
        private String description;
        
        @Transient
        private String typeTemp;
        
        @Transient
        private String subTypeTemp;
        
        

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeTemp() {
        return typeTemp;
    }

    public void setTypeTemp(String typeTemp) {
        this.typeTemp = typeTemp;
    }

    public String getSubTypeTemp() {
        return subTypeTemp;
    }

    public void setSubTypeTemp(String subTypeTemp) {
        this.subTypeTemp = subTypeTemp;
    }
    
    
        
        

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeedCategory other = (DeedCategory) obj;
		if (subtype == null) {
			if (other.subtype != null)
				return false;
		} else if (!subtype.equals(other.subtype))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeedCategory [type=" + type + ", subtype=" + subtype + "]";
	}
	
	

}
