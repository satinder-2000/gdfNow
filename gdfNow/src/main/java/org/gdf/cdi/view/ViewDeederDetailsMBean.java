/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.DeederBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.Deeder;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
@Named(value = "viewDeederDetailsMBean")
@ViewScoped
public class ViewDeederDetailsMBean implements Serializable{
    
    static final Logger LOGGER=Logger.getLogger(ViewDeederDetailsMBean.class.getName());
    
    @Inject
    private DeederBeanLocal deederBeanLocal;
    
    Deeder deeder; 
    
    
    @PostConstruct
    public void init(){
        /*
         * NOTE Alternate View id when Deeder is loaded by Email.. From where is that call made?? This one changed on 16/03 to display UD from ActivityLog.
         */
        ExternalContext extContext=FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request=(HttpServletRequest)extContext.getRequest();
        if (!request.getParameter("deederId").isEmpty()){
            String deederIdS=request.getParameter("deederId");
            int deederId=Integer.valueOf(deederIdS);
            deeder=deederBeanLocal.getDeeder(deederId);
            //Create an ImageVO object temporarily just for the sake of displaying the profile Image.
            String fileName=deeder.getProfileFile();
            String type=fileName.substring(fileName.indexOf('.')+1);
            ImageVO vo=new ImageVO(type,deeder.getImage());
            HttpSession session=request.getSession(true);
            session.setAttribute(GDFConstants.TEMP_IMAGE_2, vo);//How will this session get killed? ANS- Only when User kills/leaves the browser.
            LOGGER.log(Level.INFO, "Deeder Loaded with ID:{0}", deeder.getId());
        }else throw new RuntimeException("No valid Parameter found");
    }

    public Deeder getDeeder() {
        return deeder;
    }

    public void setDeeder(Deeder deeder) {
        this.deeder = deeder;
    }
    
    
    
    
    
}
