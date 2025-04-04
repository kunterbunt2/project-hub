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

package de.bushnaq.abdalla.svg.util;

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

public class ExtendedGraphics2D extends Graphics2D {

    private final Graphics2D graphics;

    public ExtendedGraphics2D(Graphics2D graphics) {
        this.graphics = graphics;
    }

    public void addRenderingHints(Map<?, ?> hints) {
        graphics.addRenderingHints(hints);
    }

    public void clearRect(int x, int y, int width, int height) {
        graphics.clearRect(x, y, width, height);
    }

    public void clip(Shape s) {
        graphics.clip(s);
    }

    public void clipRect(int x, int y, int width, int height) {
        graphics.clipRect(x, y, width, height);
    }

    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        graphics.copyArea(x, y, width, height, dx, dy);
    }

    public Graphics create() {
        return graphics.create();
    }

    public Graphics create(int x, int y, int width, int height) {
        return graphics.create(x, y, width, height);
    }

    public void dispose() {
        graphics.dispose();
    }

    public void draw(Shape s) {
        graphics.draw(s);
    }

    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        graphics.draw3DRect(x, y, width, height, raised);
    }

    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        graphics.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        graphics.drawBytes(data, offset, length, x, y);
    }

    public void drawChars(char[] data, int offset, int length, int x, int y) {
        graphics.drawChars(data, offset, length, x, y);
    }

    public void drawGlyphVector(GlyphVector g, float x, float y) {
        graphics.drawGlyphVector(g, x, y);
    }

    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return graphics.drawImage(img, xform, obs);
    }

    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        graphics.drawImage(img, op, x, y);
    }

    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return graphics.drawImage(img, x, y, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return graphics.drawImage(img, x, y, width, height, observer);
    }

    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        return graphics.drawImage(img, x, y, bgcolor, observer);
    }

    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return graphics.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return graphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return graphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
    }

    public void drawOval(int x, int y, int width, int height) {
        graphics.drawOval(x, y, width, height);
    }

    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics.drawPolygon(xPoints, yPoints, nPoints);
    }

    public void drawPolygon(Polygon p) {
        graphics.drawPolygon(p);
    }

    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        graphics.drawPolyline(xPoints, yPoints, nPoints);
    }

    public void drawRect(int x, int y, int width, int height) {
        //graphics.drawRect(x, y, width, height);
        //fixing the misalignment of the border
        graphics.fillRect(x, y, width + 1, 1);
        graphics.fillRect(x, y + 1, 1, height - 2 + 1);
        graphics.fillRect(x + width - 1 + 1, y + 1, 1, height - 2 + 1);
        graphics.fillRect(x, y + height - 1 + 1, width + 1, 1);
    }

    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        graphics.drawRenderableImage(img, xform);
    }

    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        graphics.drawRenderedImage(img, xform);
    }

    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        graphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void drawString(String str, int x, int y) {
        graphics.drawString(str, x, y);
    }

    public void drawString(String str, int x, int y, String toolTip) {
        if (graphics instanceof ExtendedSvgGraphics2D) {
            ((ExtendedSvgGraphics2D) graphics).drawString(str, x, y, toolTip);
        } else {
            graphics.drawString(str, x, y);
        }
    }

    public void drawString(String str, int x, int y, String toolTip, String link) {
        if (graphics instanceof ExtendedSvgGraphics2D) {
            ((ExtendedSvgGraphics2D) graphics).drawString(str, x, y, toolTip, link);
        } else {
            graphics.drawString(str, x, y);
        }
    }

    public void drawString(String str, float x, float y) {
        graphics.drawString(str, x, y);
    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        graphics.drawString(iterator, x, y);
    }

    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        graphics.drawString(iterator, x, y);
    }

    public void drawStringWithLink(String str, int x, int y, String link) {
        if (graphics instanceof ExtendedSvgGraphics2D) {
            ((ExtendedSvgGraphics2D) graphics).drawStringWithLink(str, x, y, link);
        } else {
            graphics.drawString(str, x, y);
        }
    }

    public boolean equals(Object obj) {
        return graphics.equals(obj);
    }

    public void fill(Shape s) {
        graphics.fill(s);
    }

    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        graphics.fill3DRect(x, y, width, height, raised);
    }

    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        graphics.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    public void fillOval(int x, int y, int width, int height) {
        graphics.fillOval(x, y, width, height);
    }

    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        graphics.fillPolygon(xPoints, yPoints, nPoints);
    }

    public void fillPolygon(Polygon p) {
        graphics.fillPolygon(p);
    }

    public void fillRect(int x, int y, int width, int height) {
        graphics.fillRect(x, y, width, height);
    }

    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        graphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    public void finalize() {
        graphics.finalize();
    }

    public Color getBackground() {
        return graphics.getBackground();
    }

    public Shape getClip() {
        return graphics.getClip();
    }

    public Rectangle getClipBounds() {
        return graphics.getClipBounds();
    }

    public Rectangle getClipBounds(Rectangle r) {
        return graphics.getClipBounds(r);
    }

    public Rectangle getClipRect() {
        return graphics.getClipRect();
    }

    public Color getColor() {
        return graphics.getColor();
    }

    public Composite getComposite() {
        return graphics.getComposite();
    }

    public GraphicsConfiguration getDeviceConfiguration() {
        return graphics.getDeviceConfiguration();
    }

    public Font getFont() {
        return graphics.getFont();
    }

    public FontMetrics getFontMetrics() {
        return graphics.getFontMetrics();
    }

    public FontMetrics getFontMetrics(Font f) {
        return graphics.getFontMetrics(f);
    }

    public FontRenderContext getFontRenderContext() {
        return graphics.getFontRenderContext();
    }

    public Paint getPaint() {
        return graphics.getPaint();
    }

    public Object getRenderingHint(Key hintKey) {
        return graphics.getRenderingHint(hintKey);
    }

    public RenderingHints getRenderingHints() {
        return graphics.getRenderingHints();
    }

    public Stroke getStroke() {
        return graphics.getStroke();
    }

    public AffineTransform getTransform() {
        return graphics.getTransform();
    }

    public int hashCode() {
        return graphics.hashCode();
    }

    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return graphics.hit(rect, s, onStroke);
    }

    public boolean hitClip(int x, int y, int width, int height) {
        return graphics.hitClip(x, y, width, height);
    }

    public void rotate(double theta) {
        graphics.rotate(theta);
    }

    public void rotate(double theta, double x, double y) {
        graphics.rotate(theta, x, y);
    }

    public void scale(double sx, double sy) {
        graphics.scale(sx, sy);
    }

    public void setBackground(Color color) {
        graphics.setBackground(color);
    }

    public void setClip(int x, int y, int width, int height) {
        graphics.setClip(x, y, width, height);
    }

    public void setClip(Shape clip) {
        graphics.setClip(clip);
    }

    public void setColor(Color c) {
        graphics.setColor(c);
    }

    public void setComposite(Composite comp) {
        graphics.setComposite(comp);
    }

    public void setFont(Font font) {
        graphics.setFont(font);
    }

    public void setPaint(Paint paint) {
        graphics.setPaint(paint);
    }

    public void setPaintMode() {
        graphics.setPaintMode();
    }

    public void setRenderingHint(Key hintKey, Object hintValue) {
        graphics.setRenderingHint(hintKey, hintValue);
    }

    public void setRenderingHints(Map<?, ?> hints) {
        graphics.setRenderingHints(hints);
    }

    public void setStroke(Stroke s) {
        graphics.setStroke(s);
    }

    public void setTransform(AffineTransform Tx) {
        graphics.setTransform(Tx);
    }

    public void setXORMode(Color c1) {
        graphics.setXORMode(c1);
    }

    public void shear(double shx, double shy) {
        graphics.shear(shx, shy);
    }

    public String toString() {
        return graphics.toString();
    }

    public void transform(AffineTransform Tx) {
        graphics.transform(Tx);
    }

    public void translate(int x, int y) {
        graphics.translate(x, y);
    }

    public void translate(double tx, double ty) {
        graphics.translate(tx, ty);
    }

}
