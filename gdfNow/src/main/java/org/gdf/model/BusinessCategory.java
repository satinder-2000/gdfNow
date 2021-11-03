/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author satindersingh
 */
@Entity
@Table(name = "BUSINESS_CATEGORY")
public class BusinessCategory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String type;
    
    private String subtype;
    
    @Transient
    private String typeTemp;

    @Transient
    private String subtypeTemp;
    
    private boolean confirmed;
    

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
    
    

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
    
    
    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) id;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BusinessCategory)) {
            return false;
        }
        BusinessCategory other = (BusinessCategory) object;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.gdf.model.BusinessCategory[ id=" + id + " ]";
    }
    
}
