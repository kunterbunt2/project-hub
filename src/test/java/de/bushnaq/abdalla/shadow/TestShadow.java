package de.bushnaq.abdalla.shadow;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestShadow {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void dropShadow100() throws Exception {
        BufferedImage image  = ImageIO.read(this.getClass().getResourceAsStream("100x100.png"));
        Shadow        shadow = new Shadow();
        image = shadow.drop(image);
        writeToDisk("target/100.png", image);
    }

    @Test
    public void dropShadow200() throws Exception {
        BufferedImage image  = ImageIO.read(this.getClass().getResourceAsStream("200x200.png"));
        Shadow        shadow = new Shadow();
        image = shadow.drop(image);
        writeToDisk("target/200.png", image);
    }

    private void writeToDisk(String imageFileName, BufferedImage _image) throws Exception {
        try {
            File output = new File(imageFileName);
            ImageIO.write(_image, "png", output);
        } catch (Exception e) {
            // System.err.println("deleted=" + deleted);
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}
