/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Deed;
import org.gdf.model.comment.DeedComment;
import org.gdf.model.like.DeedLike;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface DeedBeanLocal {
    
    public Deed createDeed(Deed deed);

    public Deed getDeedWithDeeder(int deedId);

    public List<DeedComment> getDeedComments(int deedId);

    public void addComment(Deed deed);

    public List<Deed> getDeedsSummary();

    public List<Deed> getDeedsOfDeeder(String deederEmail);
    
    public List<Deed> getDeedsOfDeeder(int deederId);

    public Deed getDeedDetails(int deedId);
    
    public DeedLike addDeedLike(DeedLike deedLike);
    
    public List<DeedLike> getDeedLikes(int deedId);

    public DeedComment addCommentLike(DeedComment dc);
    
    public Deed confirmDeed(Deed deed);

    
    
}
