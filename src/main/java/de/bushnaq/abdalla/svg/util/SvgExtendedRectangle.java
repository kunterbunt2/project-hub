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