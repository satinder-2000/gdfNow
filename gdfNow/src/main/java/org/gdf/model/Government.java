    package org.gdf.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "GOVERNMENT")
public class Government {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "OFFICE_NAME")
    private String officeName;

    private String email;

    @Column(name = "OFFICE_FUNCTION")
    private String officeFunction;

    private String website;

    @Column(name = "CONTACT_NAME")
    private String contactName;
    
    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;
        
    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedOn;

    @OneToOne(optional = false, mappedBy = "government", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private GovernmentAddress governmentAddress;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "GOVT_ORGS_ID", nullable = false, updatable = true)
    GovernmentOrg governmentOrg;

    @OneToMany(
            mappedBy = "government",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    List<GovernmentOffer> governmentOffers = new ArrayList<>();
    
    @Column(name = "LOGO_FILE")
    private String logoFile;
    
    @Transient
    private String logoURL;
    
    private boolean confirmed;
    
    private byte[] image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOfficeFunction() {
        return officeFunction;
    }

    public void setOfficeFunction(String officeFunction) {
        this.officeFunction = officeFunction;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(String logoFile) {
        this.logoFile = logoFile;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    public GovernmentAddress getGovernmentAddress() {
        return governmentAddress;
    }

    public void setGovernmentAddress(GovernmentAddress governmentAddress) {
        this.governmentAddress = governmentAddress;
    }

    public GovernmentOrg getGovernmentOrg() {
        return governmentOrg;
    }

    public void setGovernmentOrg(GovernmentOrg governmentOrg) {
        this.governmentOrg = governmentOrg;
    }

    public List<GovernmentOffer> getGovernmentOffers() {
        return governmentOffers;
    }

    public void setGovernmentOffers(List<GovernmentOffer> governmentOffers) {
        this.governmentOffers = governmentOffers;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
    
    
    

 }
