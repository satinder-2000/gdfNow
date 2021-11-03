/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.util;

import java.io.Serializable;

/**
 *
 * @author root
 */
public class ImageVO implements Serializable {
    
    public String type;
    
    public byte[] image;
    
    public ImageVO(String type, byte[] image){
        this.type=type;
        this.image=image;
        
    }
    
}
