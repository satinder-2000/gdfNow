/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "ON_HOLD")
public class OnHold implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCESS_TYPE")
    private AccessType accessType;

    @Column(name = "ENTITY_ID")
    private int entityId;

    @Column(name = "PROFILE_FILE")
    private String profileFile;
    
    @Column(name = "COUNTRY_CODE")
    private String countryCode;
    
    private String name;
    
    private byte[] image;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getProfileFile() {
        return profileFile;
    }

    public void setProfileFile(String profileFile) {
        this.profileFile = profileFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
    
    
    
}
