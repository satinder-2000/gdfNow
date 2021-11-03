/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdf.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author satindersingh
 */
public class ImageResizeUtil {
    
    static final Logger LOGGER=Logger.getLogger(ImageResizeUtil.class.getName());
    
    public static BufferedImage resizeImage(InputStream image, int widthTarget) throws IOException{
        BufferedImage originalImage = ImageIO.read(image);
        int widthO=originalImage.getWidth();
        int heightO=originalImage.getHeight();
        double aspectRatio=(double)heightO/widthO;
        LOGGER.log(Level.INFO, "Image width is: {0} and height is: {1} and aspectRatio is: {2}", new Object[]{widthO,heightO,aspectRatio});
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB: originalImage.getType();
        int widthN=widthTarget;
        int heightN=(int)(widthN*aspectRatio);
        BufferedImage resizeImageJpg = resizeImage(originalImage, type, widthN, heightN);
        return resizeImageJpg;
    }
    
    private static BufferedImage resizeImage(BufferedImage originalImage, int type,
            Integer img_width, Integer img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height,null);
        g.dispose();
        return resizedImage;
    }
    
}
