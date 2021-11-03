package org.gdf.model;

import org.gdf.model.like.DeederLike;
import org.gdf.model.like.NgoLike;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "NGO")
public class Ngo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String website;
    
    private String email;
    
    private String description;
    
    private boolean confirmed;
    
    private byte[] image;
    

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "NGO_CATEGORY_ID", nullable = false, updatable = true)
    private NgoCategory ngoCategory;

    @OneToOne(optional = false, mappedBy = "ngo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private NgoAddress ngoAddress;

    @ManyToMany
    @JoinTable(name = "NGO_DEED_CATEGORY", joinColumns = @JoinColumn(name = "NGO_ID"), inverseJoinColumns = @JoinColumn(name = "DEEDCATEGORY_ID"))
    private Set<DeedCategory> deedCategories = new HashSet<DeedCategory>();
    
    @OneToMany(mappedBy = "ngo", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<NgoLike> ngoLikes=new ArrayList<>();

    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;

    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedOn;
    
    @OneToMany(
            mappedBy = "ngo",
            cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY
    )
    private List<Deed> deeds = new ArrayList<Deed>();
    
    @OneToMany(
            mappedBy = "ngo",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<NgoOffer> ngoOffers=new ArrayList<>();
    
    @Column(name = "LOGO_FILE")
    private String logoFile;
    
    @Transient
    private String logoURL;

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

    public NgoCategory getNgoCategory() {
        return ngoCategory;
    }

    public void setNgoCategory(NgoCategory ngoCategory) {
        this.ngoCategory = ngoCategory;
    }

    public NgoAddress getNgoAddress() {
        return ngoAddress;
    }

    public void setNgoAddress(NgoAddress ngoAddress) {
        this.ngoAddress = ngoAddress;
    }

    public Set<DeedCategory> getDeedCategories() {
        return deedCategories;
    }

    public void setDeedCategories(Set<DeedCategory> deedCategories) {
        this.deedCategories = deedCategories;
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

    public List<NgoOffer> getNgoOffers() {
        return ngoOffers;
    }

    public void setNgoOffers(List<NgoOffer> ngoOffers) {
        this.ngoOffers = ngoOffers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<Deed> getDeeds() {
        return deeds;
    }

    public void setDeeds(List<Deed> deeds) {
        this.deeds = deeds;
    }

    public List<NgoLike> getNgoLikes() {
        return ngoLikes;
    }

    public void setNgoLikes(List<NgoLike> ngoLikes) {
        this.ngoLikes = ngoLikes;
    }

    
    
    

}
