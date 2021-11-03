/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.servlet;

import org.gdf.model.Access;
import org.gdf.util.GDFConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author root
 */
//@WebServlet("/ImageServlet")
public class ImageServlet extends HttpServlet {
    
    static final Logger LOGGER=Logger.getLogger(ImageServlet.class.getName());
    
    
    String relativeWebPathLogo = "/resources/images/GDFNow.jpeg";
    
    byte[] byteImg;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        FileInputStream fis=null;
        try {
            
            String logoPath=getServletContext().getRealPath(relativeWebPathLogo);
            File file=new File(logoPath);
            fis = new FileInputStream(file);
            int size=fis.available();
            byteImg =new byte[size];
            fis.read(byteImg);
        } catch (FileNotFoundException ex) {
            LOGGER.severe(ex.getMessage());
            throw new ServletException(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.severe(ex.getMessage());
            throw new ServletException(ex.getMessage());
        }
        finally {
            try {
                fis.close();
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
        }
        
    }
    
    
    

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session= request.getSession();
        Object accessOb=session.getAttribute(GDFConstants.ACCESS);
         
        if(accessOb==null){//Render Gdf Logo
           response.setContentType("image/jpeg");
           response.getOutputStream().write(byteImg);
        }else{
            Access access=(Access)accessOb;
            response.setContentType("image/jpg");
            response.getOutputStream().write(access.getImage());
        }
        
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
