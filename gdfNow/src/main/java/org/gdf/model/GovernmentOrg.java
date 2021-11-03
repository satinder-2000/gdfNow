package org.gdf.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="GOVERNMENT_ORGS")
public class GovernmentOrg implements Serializable {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "COUNTRY_CODE")
    private String countryCode;

    private String ministry;

    private String department;

    private boolean confirmed;
    
    @Transient
    private String ministryTemp;

    @Transient
    private String departmentTemp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    

    public String getMinistry() {
        return ministry;
    }

    public void setMinistry(String ministry) {
        this.ministry = ministry;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getMinistryTemp() {
        return ministryTemp;
    }

    public void setMinistryTemp(String ministryTemp) {
        this.ministryTemp = ministryTemp;
    }

    public String getDepartmentTemp() {
        return departmentTemp;
    }

    public void setDepartmentTemp(String departmentTemp) {
        this.departmentTemp = departmentTemp;
    }
    
    
	
	
	
	
	

}
