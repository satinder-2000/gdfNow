/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.view;

import org.gdf.ejb.BusinessBeanLocal;
import org.gdf.ejb.GovernmentBeanLocal;
import org.gdf.model.Deeder;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

@Named(value = "viewGovernmentRewardedDeedersMBean")
@RequestScoped
public class ViewGovernmentRewardedDeedersMBean {
    
    static final Logger LOGGER=Logger.getLogger(ViewGovernmentRewardedDeedersMBean.class.getName());
    
    @Inject
    GovernmentBeanLocal governmentBeanLocal;
    
    List<Deeder> rewardedDeeders;
    
    Map<String,ImageVO> imgMap;
    
    @PostConstruct
    public void init(){
        HttpServletRequest request= (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String governmentIdStr=request.getParameter("governmentId");
        int governmentId=Integer.valueOf(governmentIdStr);
        rewardedDeeders=governmentBeanLocal.getGovernmentRewardedDeeders(governmentId);
        HttpSession session=request.getSession();//Clearly session has already been created.
        if (session.getAttribute(GDFConstants.TEMP_IMAGE_MAP)!=null){
            imgMap = (Map) session.getAttribute(GDFConstants.TEMP_IMAGE_MAP);
        }else{
            imgMap=new HashMap<>();
            session.setAttribute(GDFConstants.TEMP_IMAGE_MAP, imgMap);
        }
        for (Deeder rd : rewardedDeeders) {
            String profileFile=rd.getProfileFile();
            String type=profileFile.substring(profileFile.indexOf('.')+1);
            ImageVO imgVO=new ImageVO(type,rd.getImage());
            imgMap.put("GRD"+rd.getId(), imgVO);
         }
        LOGGER.info("MBean received "+rewardedDeeders.size()+"Deeders from the back end");
    }

    public List<Deeder> getRewardedDeeders() {
        return rewardedDeeders;
    }

    public void setRewardedDeeders(List<Deeder> rewardedDeeders) {
        this.rewardedDeeders = rewardedDeeders;
    }
    
    
    
}
