/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import java.io.Serializable;
import javax.persistence.Column;
import org.gdf.model.Ngo;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.gdf.model.EntityType;

/**
 *
 * @author root
 */
@Entity
@Table(name = "NGO_LIKE")
public class NgoLike implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    
    @ManyToOne
    @JoinColumn(name = "NGO_ID")
    private Ngo ngo;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ACCESS_TYPE")
    private EntityType accessType;
    
    @Column(name = "LIKE_BY_NAME")
    private String likeByName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
    }

    public EntityType getAccessType() {
        return accessType;
    }

    public void setAccessType(EntityType accessType) {
        this.accessType = accessType;
    }

    public String getLikeByName() {
        return likeByName;
    }

    public void setLikeByName(String likeByName) {
        this.likeByName = likeByName;
    }
    
    
    
}
