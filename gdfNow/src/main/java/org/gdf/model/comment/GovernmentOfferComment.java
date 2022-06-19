/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.comment;

import org.gdf.model.AccessType;
import org.gdf.model.GovernmentOffer;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
@Table(name = "GOVERNMENT_OFFER_COMMENT")
public class GovernmentOfferComment implements Serializable { 

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String text;
    
    private LocalDateTime date;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ACCESS_TYPE")
    private AccessType accessType;
    
    @Column(name = "POSTED_BY")
    private String PostedBy;
    
    @ManyToOne
    @JoinColumn(name = "GOVERNMENT_OFFER_ID")
    private GovernmentOffer governmentOffer;
    
    private int likes;
    
    @Transient
    private String dateStr;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    

    public String getPostedBy() {
        return PostedBy;
    }

    public void setPostedBy(String PostedBy) {
        this.PostedBy = PostedBy;
    }

    public GovernmentOffer getGovernmentOffer() {
        return governmentOffer;
    }

    public void setGovernmentOffer(GovernmentOffer governmentOffer) {
        this.governmentOffer = governmentOffer;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
    
    

    public String getDateStr() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        dateStr=date.format(formatter);
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GovernmentOfferComment)) {
            return false;
        }
        GovernmentOfferComment other = (GovernmentOfferComment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.gdf.model.comment.GovernmentOfferComment[ id=" + id + " ]";
    }
    
}
