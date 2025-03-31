/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.shadow;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Shadow {
    public static  int           LEFT_TOP     = 17;
    public static  int           RIGHT_BOTTOM = 48;
    private static BufferedImage bottom;
    private static BufferedImage left;
    private static BufferedImage leftBottom;
    private static BufferedImage leftTop;
    private static BufferedImage right;
    private static BufferedImage rightBottom;
    private static BufferedImage rightTop;
    private static BufferedImage top;

    public Shadow() throws IOException {
        if (top == null) {
            leftBottom  = ImageIO.read(this.getClass().getResourceAsStream("left-bottom.png"));
            leftTop     = ImageIO.read(this.getClass().getResourceAsStream("left-top.png"));
            rightBottom = ImageIO.read(this.getClass().getResourceAsStream("right-bottom.png"));
            rightTop    = ImageIO.read(this.getClass().getResourceAsStream("right-top.png"));
            top         = ImageIO.read(this.getClass().getResourceAsStream("top.png"));
            left        = ImageIO.read(this.getClass().getResourceAsStream("left.png"));
            right       = ImageIO.read(this.getClass().getResourceAsStream("right.png"));
            bottom      = ImageIO.read(this.getClass().getResourceAsStream("bottom.png"));
        }
    }

    private static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        BufferedImage image = new BufferedImage(width, height, transparency);
        image.coerceData(true);
        return image;

    }

    public BufferedImage drop(BufferedImage image) throws IOException {
        BufferedImage passepartout = createCompatibleImage(image.getWidth() + LEFT_TOP + RIGHT_BOTTOM, image.getHeight() + LEFT_TOP + RIGHT_BOTTOM,
                Transparency.TRANSLUCENT);
        Graphics2D g2 = passepartout.createGraphics();
        for (int x = leftTop.getWidth(); x < passepartout.getWidth() - rightTop.getWidth(); x++) {
            g2.drawImage(top, x, 0, null);
        }
        for (int y = leftTop.getHeight(); y < passepartout.getHeight() - leftBottom.getHeight(); y++) {
            g2.drawImage(left, 0, y, null);
        }
        for (int y = rightTop.getHeight(); y < passepartout.getHeight() - rightBottom.getHeight(); y++) {
            g2.drawImage(right, passepartout.getWidth() - right.getWidth(), y, null);
        }
        for (int x = leftBottom.getWidth(); x < passepartout.getWidth() - rightBottom.getWidth(); x++) {
            g2.drawImage(bottom, x, passepartout.getHeight() - bottom.getHeight(), null);
        }

        g2.drawImage(leftTop, 0, 0, null);
        g2.drawImage(rightTop, passepartout.getWidth() - rightTop.getWidth(), 0, null);
        g2.drawImage(leftBottom, 0, passepartout.getHeight() - leftBottom.getHeight(), null);
        g2.drawImage(rightBottom, passepartout.getWidth() - rightBottom.getWidth(), passepartout.getHeight() - rightBottom.getHeight(), null);

        g2.drawImage(image, LEFT_TOP, LEFT_TOP, null);
        return passepartout;
    }
}
