/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.UserDeederBeanLocal;
import org.gdf.model.Access;
import org.gdf.model.AccessType;
import org.gdf.model.Deeder;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
@Named(value = "viewNominatedDeedersMBean")
@RequestScoped
public class ViewNominatedDeedersMBean {
    
    static final Logger LOGGER=Logger.getLogger(ViewNominatedDeedersMBean.class.getName());
    
    @Inject
    UserDeederBeanLocal userDeederBeanLocal;
    
    List<Deeder> nominatedDeeders;
    
    Map<String, ImageVO> imageMap;
    
    @PostConstruct
    public void init(){
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String userIdStr=request.getParameter("userId");
        int userId=Integer.valueOf(userIdStr);
        nominatedDeeders=userDeederBeanLocal.getNominatedDeeders(userId);
        //imageMap=new HashMap<>();
        HttpSession session=request.getSession();
        if(session.getAttribute(GDFConstants.TEMP_IMAGE_MAP)!=null){//This imageMap is set on the index page where Images pertaining to BO, GO and NO are set. 
            imageMap=(Map)session.getAttribute(GDFConstants.TEMP_IMAGE_MAP);
        }else{//The User logged in starightway and we will have to create it
            imageMap=new HashMap<>();
            session.setAttribute(GDFConstants.TEMP_IMAGE_MAP, imageMap);
        }
        for (Deeder nd : nominatedDeeders) {//In the same image map, we intend to set DR images
            String profileFile=nd.getProfileFile();
            String type=profileFile.substring(profileFile.indexOf('.')+1);
            ImageVO imgVO=new ImageVO(type,nd.getImage());
            imageMap.put("DR"+nd.getId(), imgVO);
        }
        
        LOGGER.log(Level.INFO, "Nominated Deeders loaded for User {0} found:{1}", new Object[]{userId, nominatedDeeders.size()});
    }
    
    

    public List<Deeder> getNominatedDeeders() {
        return nominatedDeeders;
    }

    public void setNominatedDeeders(List<Deeder> nominatedDeeders) {
        this.nominatedDeeders = nominatedDeeders;
    }
    
    
    
    
    
}
