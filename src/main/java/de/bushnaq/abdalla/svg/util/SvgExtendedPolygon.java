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