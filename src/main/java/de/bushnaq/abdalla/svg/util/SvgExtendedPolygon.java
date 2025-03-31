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
import org.apache.batik.svggen.SVGPolygon;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;

public class SvgExtendedPolygon extends SVGPolygon {

    public SvgExtendedPolygon(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }

    public Element toSVG(ExtendedPolygon polygon) {
        Element element = super.toSVG(polygon);
        if (element == null) {
            return null;
        }
        if (!polygon.isVisible()) {
            element.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0");
        }
        if (polygon.getLink() != null) {
            Element anchor = super.generatorContext.getDOMFactory().createElementNS("http://www.w3.org/2000/svg", "a");
            anchor.setAttributeNS(null, SVGConstants.XLINK_HREF_ATTRIBUTE, polygon.getLink());
            anchor.appendChild(element);
            if (polygon.getToolTip() != null) {
                element.setAttribute("alt", polygon.getToolTip());
            }
            return anchor;
        } else if (polygon.getToolTip() != null && !polygon.getToolTip().isEmpty()) {
            element.setAttribute("alt", polygon.getToolTip());
        }
        return element;
    }

}