/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Feedback;
import org.gdf.model.Issue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author root
 */
@Stateless
public class FooterServiceBean implements FooterServiceBeanLocal {
    
    static final Logger LOGGER=Logger.getLogger(FooterServiceBean.class.getCanonicalName());
    
    @PersistenceContext(name = "gdfPU")
    EntityManager em;
    
    

    @Override
    public Feedback saveFeedback(Feedback feedback) {
        em.persist(feedback);
        em.flush();
        LOGGER.log(Level.INFO, "Feedback persisted with ID {0}", feedback.getId());
        return feedback;
    }

    @Override
    public Issue logIssue(Issue issue) {
        em.persist(issue);
        em.flush();
        LOGGER.log(Level.INFO, "Issue persisted with ID {0}", issue.getId());
        return issue;
        
    }

    
}
