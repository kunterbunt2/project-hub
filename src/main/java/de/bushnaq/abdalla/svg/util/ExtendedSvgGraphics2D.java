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

import org.apache.batik.svggen.*;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

public class ExtendedSvgGraphics2D extends SVGGraphics2D {
    public ExtendedSvgGraphics2D(Document domFactory) {
        super(domFactory);
        super.shapeConverter = new ExtendedSvgShape(super.generatorCtx);
    }

    public ExtendedSvgGraphics2D(Document domFactory, ImageHandler imageHandler, ExtensionHandler extensionHandler, boolean textAsShapes) {
        super(domFactory, imageHandler, extensionHandler, textAsShapes);
        super.shapeConverter = new ExtendedSvgShape(buildSVGGeneratorContext(domFactory, imageHandler, extensionHandler));
    }

    public ExtendedSvgGraphics2D(ExtendedSvgGraphics2D g) {
        super(g);
        super.shapeConverter = g.shapeConverter;
    }

    public ExtendedSvgGraphics2D(SVGGeneratorContext generatorCtx, boolean textAsShapes) {
        super(generatorCtx, textAsShapes);
        super.shapeConverter = new ExtendedSvgShape(generatorCtx);
    }

    public void drawString(String s, float x, float y, String toolTip) {
        if (textAsShapes) {
            GlyphVector gv = getFont().createGlyphVector(getFontRenderContext(), s);
            drawGlyphVector(gv, x, y);
            return;
        }

        if (generatorCtx.isEmbeddedFontsOn()) {
            // record that the font is being used to draw this
            // string, this is so that the SVG Font element will
            // only create glyphs for the characters that are
            // needed
            domTreeManager.getGraphicContextConverter().getFontConverter().recordFontUsage(s, getFont());
        }

        // Account for the font transform if there is one
        AffineTransform savTxf = getTransform();
        AffineTransform txtTxf = transformText(x, y);

        Element text = getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
        text.setAttributeNS(null, SVG_X_ATTRIBUTE, generatorCtx.doubleString(x));
        text.setAttributeNS(null, SVG_Y_ATTRIBUTE, generatorCtx.doubleString(y));

        text.setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, XML_PRESERVE_VALUE);
        text.appendChild(getDOMFactory().createTextNode(s));

        if (toolTip != null) {
            text.setAttribute("alt", toolTip);
        }

        domGroupManager.addElement(text, DOMGroupManager.FILL);

        if (txtTxf != null) {
            this.setTransform(savTxf);
        }
    }

    public void drawString(String s, float x, float y, String toolTip, String link) {
        if (textAsShapes) {
            GlyphVector gv = getFont().createGlyphVector(getFontRenderContext(), s);
            drawGlyphVector(gv, x, y);
            return;
        }

        if (generatorCtx.isEmbeddedFontsOn()) {
            // record that the font is being used to draw this
            // string, this is so that the SVG Font element will
            // only create glyphs for the characters that are
            // needed
            domTreeManager.getGraphicContextConverter().getFontConverter().recordFontUsage(s, getFont());
        }

        // Account for the font transform if there is one
        AffineTransform savTxf = getTransform();
        AffineTransform txtTxf = transformText(x, y);

        Element text = getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
        text.setAttributeNS(null, SVG_X_ATTRIBUTE, generatorCtx.doubleString(x));
        text.setAttributeNS(null, SVG_Y_ATTRIBUTE, generatorCtx.doubleString(y));

        text.setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, XML_PRESERVE_VALUE);
        text.appendChild(getDOMFactory().createTextNode(s));

        if (toolTip != null) {
            text.setAttribute("alt", toolTip);
        }
        if (link != null) {
            Element anchor = getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "a");
            anchor.setAttributeNS(null, SVGConstants.XLINK_HREF_ATTRIBUTE, link);
            anchor.appendChild(text);
            domGroupManager.addElement(anchor, DOMGroupManager.FILL);
        } else {
            domGroupManager.addElement(text, DOMGroupManager.FILL);
        }

        if (txtTxf != null) {
            this.setTransform(savTxf);
        }
    }

    public void drawStringWithLink(String s, float x, float y, String link) {
        if (textAsShapes) {
            GlyphVector gv = getFont().createGlyphVector(getFontRenderContext(), s);
            drawGlyphVector(gv, x, y);
            return;
        }

        if (generatorCtx.isEmbeddedFontsOn()) {
            // record that the font is being used to draw this
            // string, this is so that the SVG Font element will
            // only create glyphs for the characters that are
            // needed
            domTreeManager.getGraphicContextConverter().getFontConverter().recordFontUsage(s, getFont());
        }

        // Account for the font transform if there is one
        AffineTransform savTxf = getTransform();
        AffineTransform txtTxf = transformText(x, y);

        Element text = getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_TEXT_TAG);
        text.setAttributeNS(null, SVG_X_ATTRIBUTE, generatorCtx.doubleString(x));
        text.setAttributeNS(null, SVG_Y_ATTRIBUTE, generatorCtx.doubleString(y));

        text.setAttributeNS(XML_NAMESPACE_URI, XML_SPACE_QNAME, XML_PRESERVE_VALUE);
        text.appendChild(getDOMFactory().createTextNode(s));

        if (link != null) {
            Element anchor = getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "a");
            anchor.setAttributeNS(null, SVGConstants.XLINK_HREF_ATTRIBUTE, link);
            anchor.appendChild(text);
            domGroupManager.addElement(anchor, DOMGroupManager.FILL);
        } else {
            domGroupManager.addElement(text, DOMGroupManager.FILL);
        }

        if (txtTxf != null) {
            this.setTransform(savTxf);
        }
    }

    private AffineTransform transformText(float x, float y) {
        AffineTransform txtTxf = null;
        Font            font   = getFont();
        if (font != null) {
            txtTxf = font.getTransform();
            if (txtTxf != null && !txtTxf.isIdentity()) {
                //
                // The additional transform applies about the text's origin
                //
                AffineTransform t = new AffineTransform();
                t.translate(x, y);
                t.concatenate(txtTxf);
                t.translate(-x, -y);
                this.transform(t);
            } else {
                txtTxf = null;
            }
        }
        return txtTxf;
    }

}