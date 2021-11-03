/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.cdi.publicview;

import org.gdf.ejb.VisitorBeanLocal;
import org.gdf.model.DeedCategory;
import org.gdf.model.Deeder;
import org.gdf.util.GDFConstants;
import org.gdf.util.ImageVO;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
@Named
@SessionScoped
public class DeedCagegoryViewMBean extends PaginationMBean implements Serializable{
    
    static final Logger LOGGER=Logger.getLogger(DeedCagegoryViewMBean.class.getName());
    
    @Inject
    VisitorBeanLocal visitorBeanLocal;
    
    private List<DeedCategory> deedCategories;
    
    @PostConstruct
    public void init(){
        rowsPerPage = 7; // Default rows per page (max amount of rows to be displayed at once).
        pageRange = 5; // Default page range (max amount of page links to be displayed at once).
        totalRows=visitorBeanLocal.getAllDeedCategoryCount();
        LOGGER.info("DeedCagegoryViewMBean: Total "+totalRows+" records exist in the Database");
    }

    public List<DeedCategory> getDeedCategories() {
        if (deedCategories==null){
           loadDeedCategories(); 
        }
        return deedCategories;
    }

    public void setDeedCategories(List<DeedCategory> deedCategories) {
        this.deedCategories = deedCategories;
    }

    @Override
    void page(int firstRow) {
        this.firstRow = firstRow;
        loadDeedCategories();
    }

    private void loadDeedCategories() {
        deedCategories=visitorBeanLocal.getAllDeedCategories(firstRow, rowsPerPage);
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
    
}
