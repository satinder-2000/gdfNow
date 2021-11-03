/**
 *
 */
package org.gdf.model;

import org.gdf.model.comment.DeedComment;
import org.gdf.model.like.DeedLike;
import java.io.Serializable;
import java.time.LocalDate;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

//import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * @author satindersingh
 *
 */
@Entity
@Table(name = "DEED")
public class Deed implements Serializable {

    private static final long serialVersionUID = 2841119039013316404L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String title;

    private String description;

    @Column(name = "DEED_DATE")
    private LocalDate deedDate;
    
    @Transient
    private String dateStr;
    
    
    
    private String link1;

    private String link2;

    private String link3;

    @Transient
    private String intro;
    
    private boolean confirmed=true;
    

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "DEEDCATEGORY_ID", nullable = false, updatable = true)
    private DeedCategory deedCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEEDER_ID", nullable = true)
    private Deeder deeder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NGO_ID", nullable = true)
    private Ngo ngo;

    @OneToOne(optional = false, mappedBy = "deed", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DeedAddress deedAddress;
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BusinessOffer> businessOffers=new ArrayList<>();
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<GovernmentOffer> governmentOffers=new ArrayList<>();
    
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NgoOffer> ngoOffers=new ArrayList<>();
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DeedComment> deedComments=new ArrayList<>();
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DeedLike> deedLikes=new ArrayList<>();
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getLink1() {
        return link1;
    }

    public void setLink1(String link1) {
        this.link1 = link1;
    }

    public String getLink2() {
        return link2;
    }

    public void setLink2(String link2) {
        this.link2 = link2;
    }

    public String getLink3() {
        return link3;
    }

    public void setLink3(String link3) {
        this.link3 = link3;
    }

    public DeedCategory getDeedCategory() {
        return deedCategory;
    }

    public void setDeedCategory(DeedCategory deedCategory) {
        this.deedCategory = deedCategory;
    }

    @Override
    public String toString() {
        return "Deed [id=" + id + ", description=" + description + ", date=" + dateStr
                + ", deedCategory=" + deedCategory + ", confirmed=" + confirmed + ", Deeder ID=" + deeder.getId() + "]";
    }

    public DeedAddress getDeedAddress() {
        return deedAddress;
    }

    public void setDeedAddress(DeedAddress deedAddress) {
        this.deedAddress = deedAddress;
    }

    public Deeder getDeeder() {
        return deeder;
    }

    public void setDeeder(Deeder deeder) {
        this.deeder = deeder;
    }

    public LocalDate getDeedDate() {
        return deedDate;
    }

    public void setDeedDate(LocalDate deedDate) {
        this.deedDate = deedDate;
    }

    

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public List<BusinessOffer> getBusinessOffers() {
        return businessOffers;
    }

    public void setBusinessOffers(List<BusinessOffer> businessOffers) {
        this.businessOffers = businessOffers;
    }

    public List<GovernmentOffer> getGovernmentOffers() {
        return governmentOffers;
    }

    public void setGovernmentOffers(List<GovernmentOffer> governmentOffers) {
        this.governmentOffers = governmentOffers;
    }

    public List<NgoOffer> getNgoOffers() {
        return ngoOffers;
    }

    public void setNgoOffers(List<NgoOffer> ngoOffers) {
        this.ngoOffers = ngoOffers;
    }

    public List<DeedComment> getDeedComments() {
        return deedComments;
    }

    public void setDeedComments(List<DeedComment> deedComments) {
        this.deedComments = deedComments;
    }

    public List<DeedLike> getDeedLikes() {
        return deedLikes;
    }

    public void setDeedLikes(List<DeedLike> deedLikes) {
        this.deedLikes = deedLikes;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
    }

    
    
    
    
 }
