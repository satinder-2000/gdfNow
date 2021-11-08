package org.gdf.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


/**
 *
 * @author root
 */
public class ImageUtil {
    
    static final Logger LOGGER = Logger.getLogger(ImageUtil.class.getName());

    public static BufferedImage resizeImage(InputStream image, int widthTarget) throws IOException {
        BufferedImage originalImage = ImageIO.read(image);
        int widthO = originalImage.getWidth();
        int heightO = originalImage.getHeight();
        double aspectRatio = (double) heightO / widthO;
        LOGGER.log(Level.INFO, "Image width is: {0} and height is: {1} and aspectRatio is: {2}", new Object[]{widthO, heightO, aspectRatio});
        int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
        int widthN = widthTarget;
        int heightN = (int) (widthN * aspectRatio);
        BufferedImage resizeImageJpg = resizeImage(originalImage, type, widthN, heightN);
        return resizeImageJpg;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int type,
        Integer img_width, Integer img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage drawIcon(int imgSize, String text) {
        int a = imgSize;
        BufferedImage bimg = new BufferedImage(a, a, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g = bimg.createGraphics();
        g.setColor(Color.decode("#cc0000"));
        g.fill(new Ellipse2D.Float(0, 0, a, a));
        g.setColor(Color.WHITE);
        int fontSize = a / 2;
        String toDraw = text.toUpperCase();
        g.setFont(new Font("sansserif", Font.PLAIN, fontSize));
        int b = g.getFontMetrics().stringWidth(toDraw);
        g.drawString(toDraw, (a / 2 - b / 2), a / 2 + fontSize / 3);
        g.dispose();
        return bimg;
    }
    
    
}
