/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author satindersingh
 */
@Entity
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "DATE")
    private LocalDateTime date;
    
    private String message;
    
    @Column(name="ACCESS_ID")
    private int accessId;
    
    @Column(name="ACTIVITY_TYPE")
    private String activityType;
    
    @Column(name="ENTITY_NAME")
    private String entityName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getAccessId() {
        return accessId;
    }

    public void setAccessId(int accessId) {
        this.accessId = accessId;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
    
    
    
    
    @Override
    public String toString() {
        return "com.gdf.model.Activity[ id=" + id + " ]";
    }
    
}
