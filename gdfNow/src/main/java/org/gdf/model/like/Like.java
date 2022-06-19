/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model.like;

import org.gdf.model.EntityType;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 *
 * @author satindersingh
 */
@MappedSuperclass
public class Like implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected int id;
    
    
    private LocalDateTime time;
    
    @Column(name = "ACCESS_ID")
    private int accessId;
    
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
    
    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getAccessId() {
        return accessId;
    }

    public void setAccessId(int accessId) {
        this.accessId = accessId;
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
