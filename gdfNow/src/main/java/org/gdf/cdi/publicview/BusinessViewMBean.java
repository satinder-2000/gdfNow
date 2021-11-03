/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.publicview;

import org.gdf.ejb.VisitorBeanLocal;
import org.gdf.model.Business;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
@Named(value = "businessViewMBean")
@SessionScoped
public class BusinessViewMBean extends PaginationMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(BusinessViewMBean.class.getName());
    
    private Map<String, ImageVO> imageMap;
    
    @Inject
    private VisitorBeanLocal visitorBeanLocal;
    
    private List<Business> businesses;
    
    @PostConstruct
    public void init(){
        imageMap=new HashMap<>();
        rowsPerPage = 5; // Default rows per page (max amount of rows to be displayed at once).
        pageRange = 10; // Default page range (max amount of page links to be displayed at once).
        totalRows=visitorBeanLocal.getAllBusinessCount();
        LOGGER.info("BusinessViewMBean: Total "+totalRows+" loaded from the Database");
    }

    public List<Business> getBusinesses() {
        if (businesses==null){
            loadBusinesses();
        }
        return businesses;
    }

    public void setBusinesses(List<Business> businesses) {
        this.businesses = businesses;
    }
    
    private void loadBusinesses() {
        businesses=visitorBeanLocal.getAllBusiness(firstRow, rowsPerPage);
        HttpSession session=(HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);//create true since it is publicly accessible page
        //Set the Imgaes first in the session. They will be displayed on the JSF page.
        for(Business b: businesses){
            ImageVO imageVO=new ImageVO("PBUS"+b.getId(), b.getImage());
            imageMap.put("PBUS"+b.getId(), imageVO);
        }
        session.setAttribute(GDFConstants.PUB_VIEW_IMAGE_MAP_BUS, imageMap);
        
        // Set currentPage, totalPages and pages.
        currentPage = (totalRows / rowsPerPage) - ((totalRows - firstRow) / rowsPerPage) + 1;
        totalPages = (totalRows / rowsPerPage) + ((totalRows % rowsPerPage != 0) ? 1 : 0);
        int pagesLength = Math.min(pageRange, totalPages);
        pages = new Integer[pagesLength];

        // firstPage must be greater than 0 and lesser than totalPages-pageLength.
        int firstPage = Math.min(Math.max(0, currentPage - (pageRange / 2)), totalPages - pagesLength);

        // Create pages (page numbers for page links).
        for (int i = 0; i < pagesLength; i++) {
            pages[i] = ++firstPage;
        }
    }
    
    @Override
    void page(int firstRow) {
        this.firstRow = firstRow;
        loadBusinesses();
    }

}
