/**
 *
 */
package org.gdf.model;

import org.gdf.model.like.DeederLike;
import java.io.Serializable;
import java.time.LocalDate;
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
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * @author satindersingh
 *
 */
@Entity
@Table(name = "DEEDER")
public class Deeder implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstname;

    private String lastname;

    private String gender;

    private LocalDate dob;
    
    @Transient
    private String dobStr;
    
    private String email;

    private String phone;

    private String mobile;

    private String about;
    
    private boolean confirmed=true;
    
    private byte[] image;
    
    @Column(name = "PROFILE_FILE")
    private String profileFile;

    @OneToOne(optional = false, mappedBy = "deeder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private DeederAddress deederAddress;

    @OneToMany(
            mappedBy = "deeder",
            cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY
    )
    private List<Deed> deeds = new ArrayList<Deed>();

    @ManyToMany(mappedBy = "deeders")
    private Set<User> users = new HashSet<User>();
    
    @Column(name = "CREATED_ON")
    private LocalDateTime createdOn;
    
    @Column(name = "UPDATED_ON")
    private LocalDateTime updatedOn;
    
    @OneToMany(mappedBy = "deeder", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DeederLike> deederLikes=new ArrayList<>();
    
    
    @Transient
    private String profileURL;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    

    

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public List<Deed> getDeeds() {
        return deeds;
    }

    public void setDeeds(List<Deed> deeds) {
        this.deeds = deeds;
    }

    public DeederAddress getDeederAddress() {
        return deederAddress;
    }

    public void setDeederAddress(DeederAddress deederAddress) {
        this.deederAddress = deederAddress;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
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

    public String getProfileFile() {
        return profileFile;
    }

    public void setProfileFile(String profileFile) {
        this.profileFile = profileFile;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public List<DeederLike> getDeederLikes() {
        return deederLikes;
    }

    public void setDeederLikes(List<DeederLike> deederLikes) {
        this.deederLikes = deederLikes;
    }

    public String getDobStr() {
        return dobStr;
    }

    public void setDobStr(String dobStr) {
        this.dobStr = dobStr;
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
