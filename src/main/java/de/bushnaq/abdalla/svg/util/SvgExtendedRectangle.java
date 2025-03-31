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

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGRectangle;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

public class SvgExtendedRectangle extends SVGRectangle {

    public SvgExtendedRectangle(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }

    public Element toSVG(ExtendedRectangle rect) {
        Element element = super.toSVG(rect);
        if (element == null)
            return null;
        if (!rect.isVisible()) {
            element.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0");
        }
        if (rect.getLink() != null) {
            Element anchor = super.generatorContext.getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "a");
            anchor.setAttributeNS(null, SVGConstants.XLINK_HREF_ATTRIBUTE, rect.getLink());
            anchor.appendChild(element);
            if (rect.getToolTip() != null) {
                element.setAttribute("alt", rect.getToolTip());
            }
            return anchor;
        } else if (rect.getToolTip() != null) {
            element.setAttribute("alt", rect.getToolTip());
        }
        return element;
    }

    public Element toSVG(RectangleWithToolTip rect) {
        Element element = super.toSVG(rect);
        if (element == null)
            return null;
        if (rect.getTitle() != null) {
            element.setAttribute("alt", rect.getTitle());
        }
        return element;
    }

    public Element toSVG(RectangleWithLink rect) {
        Element element = super.toSVG(rect);
        if (element == null)
            return null;
        if (rect.getLink() != null) {
            Element anchor = super.generatorContext.getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "a");
            anchor.setAttributeNS(null, SVGConstants.XLINK_HREF_ATTRIBUTE, rect.getLink());
            anchor.appendChild(element);
            return anchor;
        }
        return element;
    }
}