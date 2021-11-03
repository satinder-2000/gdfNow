/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.ejb;

import org.gdf.model.Deeder;
import org.gdf.model.like.DeederLike;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author satindersingh
 */
@Local
public interface DeederBeanLocal {

    public Deeder getDeeder(int deederId);
    
    public Deeder getDeeder(String email);

    public Deeder amendDeeder(Deeder deeder);
    
    
    public boolean deederExists(String email);
    
    public Deeder createDeeder(Deeder deeder);
    
    
    public List<DeederLike> getDeederLikes(int deederId);
    
    public DeederLike addDeederLike(DeederLike dl);

    

}
