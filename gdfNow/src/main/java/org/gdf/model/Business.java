/**
 *
 */
package org.gdf.model;

import java.io.Serializable;
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

/**
 * @author satindersingh
 *
 */
@Entity
@Table(name = "BUSINESS")
public class Business implements Serializable {

    private static final long serialVersionUID = -5201243723433582970L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String email;

    private String website;

    private String description;
    
    private boolean confirmed;
    
    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;
        
    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedOn;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "BUSINESS_CATEGORY_ID", nullable = false, updatable = true)
    private BusinessCategory businessCategory;

    @OneToOne(optional = false, mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private BusinessAddress businessAddress;

    @OneToMany(
            mappedBy = "business",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    List<BusinessOffer> businessOffers = new ArrayList<BusinessOffer>();
    
    @Column(name = "LOGO_FILE")
    private String logoFile;
    
    @Transient
    private String logoURL;
    
    private byte[] image;
    
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
    
    

    public BusinessCategory getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(BusinessCategory businessCategory) {
        this.businessCategory = businessCategory;
    }

    public BusinessAddress getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(BusinessAddress businessAddress) {
        this.businessAddress = businessAddress;
    }

    public List<BusinessOffer> getBusinessOffers() {
        return businessOffers;
    }

    public void setBusinessOffers(List<BusinessOffer> businessOffers) {
        this.businessOffers = businessOffers;
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

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
    
    
    
    @Override
    public String toString() {
        return "Business{" + "id=" + id + ", name=" + name + ", email=" + email + ", website=" + website + '}';
    }
    
    

}
