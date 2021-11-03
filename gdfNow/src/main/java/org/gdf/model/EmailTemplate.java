    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author root
 */
@Entity
@Table(name = "EMAIL_TEMPLATE")
public class EmailTemplate implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TEMPLATE_TYPE")
    private EmailTemplateType emailTemplateType;
    
    private String file;
    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EmailTemplateType getEmailTemplateType() {
        return emailTemplateType;
    }

    public void setEmailTemplateType(EmailTemplateType emailTemplateType) {
        this.emailTemplateType = emailTemplateType;
    }

    

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    
    
}
