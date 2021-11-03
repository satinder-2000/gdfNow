/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Feedback;
import org.gdf.model.Issue;
import javax.ejb.Local;

/**
 *
 * @author root
 */
@Local
public interface FooterServiceBeanLocal {
    
    public Feedback saveFeedback(Feedback feedback);
    
    public Issue logIssue(Issue issue);

}
