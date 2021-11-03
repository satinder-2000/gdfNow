/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.publicview;

import org.gdf.ejb.VisitorBeanLocal;
import org.gdf.model.Deed;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;


@Named(value = "deedViewMBean")
@SessionScoped
public class DeedViewMBean extends PaginationMBean implements Serializable {
    
    static final Logger LOGGER=Logger.getLogger(DeedViewMBean.class.getName());
       
    @Inject
    private VisitorBeanLocal visitorBeanLocal;
    
    private List<Deed> deeds;
    
    @PostConstruct
    public void init(){
        rowsPerPage = 5; // Default rows per page (max amount of rows to be displayed at once).
        pageRange = 10; // Default page range (max amount of page links to be displayed at once).
        totalRows=visitorBeanLocal.getAllDeedsCount();
        LOGGER.info("DeedViewMBean: Total "+totalRows+" loaded from the Database");
    }
    
    public List<Deed> getDeeds() {
        if (deeds==null){
            loadDeeds();
        }
        return deeds;
    }

    public void setDeeds(List<Deed> deeds) {
        this.deeds = deeds;
    }
    
    private void loadDeeds() {
        deeds=visitorBeanLocal.getAllDeeds(firstRow, rowsPerPage);
        HttpSession session=(HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);//create true since it is publicly accessible page
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
        loadDeeds();
    }

    
    
   
    
}
