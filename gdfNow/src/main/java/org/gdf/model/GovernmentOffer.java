package org.gdf.model;

import org.gdf.model.comment.GovernmentOfferComment;
import org.gdf.model.like.GovernmentOfferLike;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "GOVERNMENT_OFFER")
public class GovernmentOffer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "TYPE")
    private String offerType;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEED_ID")
    private Deed deed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GOVERNMENT_ID")
    private Government government;

    @Column(name = "OFFERED_ON")
    private LocalDateTime offeredOn;
    
    @Transient
    private String offerDate; 
    
    private String value;
    
    @Transient
    private String intro;
    
    @OneToMany(mappedBy = "governmentOffer", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GovernmentOfferComment> governmentOfferComments=new ArrayList<>();
    
    @OneToMany(mappedBy = "governmentOffer", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<GovernmentOfferLike> governmentOfferLikes=new ArrayList<>();
    
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOfferType() {
        return offerType;
    }

    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }

    public Government getGovernment() {
        return government;
    }

    public void setGovernment(Government government) {
        this.government = government;
    }

    public LocalDateTime getOfferedOn() {
        return offeredOn;
    }

    public void setOfferedOn(LocalDateTime offeredOn) {
        this.offeredOn = offeredOn;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    public String getOfferDate() {
        if (offeredOn != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            offerDate = offeredOn.format(formatter);
        }
        return offerDate;
    }

    public void setOfferDate(String offerDate) {
        this.offerDate = offerDate;
    }

    public List<GovernmentOfferComment> getGovernmentOfferComments() {
        return governmentOfferComments;
    }

    public void setGovernmentOfferComments(List<GovernmentOfferComment> governmentOfferComments) {
        this.governmentOfferComments = governmentOfferComments;
    }

    public List<GovernmentOfferLike> getGovernmentOfferLikes() {
        return governmentOfferLikes;
    }

    public void setGovernmentOfferLikes(List<GovernmentOfferLike> governmentOfferLikes) {
        this.governmentOfferLikes = governmentOfferLikes;
    }
    
    public String getIntro() {
        if (this.description.length() > 50) {
            intro = description.substring(0, 50);
        } else {
            intro = description;
        }
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

}
