package org.gdf.model;

import org.gdf.model.comment.NgoOfferComment;
import org.gdf.model.like.NgoOfferLike;
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
@Table(name = "NGO_OFFER")
public class NgoOffer implements Serializable {

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
    @JoinColumn(name = "NGO_ID")
    private Ngo ngo;

    @Column(name = "OFFERED_ON")
    private LocalDateTime offeredOn;
    
    @Transient
    private String offerDate; 
    
    private String value;
    
    @Transient
    private String intro;
    
    @OneToMany(mappedBy = "ngoOffer", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NgoOfferComment> ngoOfferComments=new ArrayList<>();
    
    @OneToMany(mappedBy = "ngoOffer", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<NgoOfferLike> ngoOfferLikes=new ArrayList<>();
    
    

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

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            offerDate = offeredOn.format(formatter);
        }
        return offerDate;
    }

    public void setOfferDate(String offerDate) {
        this.offerDate = offerDate;
    }
    
    

    public List<NgoOfferComment> getNgoOfferComments() {
        return ngoOfferComments;
    }

    public void setNgoOfferComments(List<NgoOfferComment> ngoOfferComments) {
        this.ngoOfferComments = ngoOfferComments;
    }

    public List<NgoOfferLike> getNgoOfferLikes() {
        return ngoOfferLikes;
    }

    public void setNgoOfferLikes(List<NgoOfferLike> ngoOfferLikes) {
        this.ngoOfferLikes = ngoOfferLikes;
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
