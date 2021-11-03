/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model;

import org.gdf.model.comment.BusinessOfferComment;
import org.gdf.model.like.BusinessOfferLike;
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

/**
 *
 * @author satindersingh
 */
@Entity
@Table(name = "BUSINESS_OFFER")
public class BusinessOffer implements Serializable {
    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "DEED_ID")
    private Deed deed;
    
    @ManyToOne
    @JoinColumn(name = "BUSINESS_ID")
    private Business business;
    
    
    @Column(name = "TYPE")
    private String offerType;
    
    private String description;
    
    private String value;
    
    @Column(name = "OFFERED_ON")
    private LocalDateTime offeredOn;
    
    @Transient
    private String offerDate; 
    
    @Transient
    private String intro;
    
    @OneToMany(mappedBy = "businessOffer", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BusinessOfferComment> businessOfferComments = new ArrayList<>();
    
    
    @OneToMany(mappedBy = "businessOffer", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BusinessOfferLike> businessOfferLikes=new ArrayList<>();
      

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

    public LocalDateTime getOfferedOn() {
        return offeredOn;
    }

    public void setOfferedOn(LocalDateTime offeredOn) {
        this.offeredOn = offeredOn;
    }

    public String getOfferDate() {
        if (offeredOn!=null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            offerDate=offeredOn.format(formatter);
        }
        return offerDate;
    }

    public void setOfferDate(String offerDate) {
        this.offerDate = offerDate;
    }

    
    
    

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public Deed getDeed() {
        return deed;
    }

    public void setDeed(Deed deed) {
        this.deed = deed;
    }

    public List<BusinessOfferComment> getBusinessOfferComments() {
        return businessOfferComments;
    }

    public void setBusinessOfferComments(List<BusinessOfferComment> businessOfferComments) {
        this.businessOfferComments = businessOfferComments;
    }

    public List<BusinessOfferLike> getBusinessOfferLikes() {
        return businessOfferLikes;
    }

    public void setBusinessOfferLikes(List<BusinessOfferLike> businessOfferLikes) {
        this.businessOfferLikes = businessOfferLikes;
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
