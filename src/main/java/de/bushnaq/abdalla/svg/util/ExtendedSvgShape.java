package de.bushnaq.abdalla.svg.util;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGShape;
import org.w3c.dom.Element;

import java.awt.*;

public class ExtendedSvgShape extends SVGShape {

    private final SvgExtendedPolygon   extendedPolygon;
    private final SvgExtendedRectangle extendedRectangle;

    public ExtendedSvgShape(SVGGeneratorContext generatorContext) {
        super(generatorContext);
        extendedRectangle = new SvgExtendedRectangle(generatorContext);
        extendedPolygon   = new SvgExtendedPolygon(generatorContext);
    }

    @Override
    public Element toSVG(Shape shape) {
        if (shape instanceof RectangleWithToolTip) {
            return this.extendedRectangle.toSVG((RectangleWithToolTip) shape);
        }
        if (shape instanceof RectangleWithLink) {
            return this.extendedRectangle.toSVG((RectangleWithLink) shape);
        }
        if (shape instanceof ExtendedRectangle) {
            return this.extendedRectangle.toSVG((ExtendedRectangle) shape);
        }
        if (shape instanceof ExtendedPolygon) {
            return this.extendedPolygon.toSVG((ExtendedPolygon) shape);
        }
        return super.toSVG(shape);
    }
}