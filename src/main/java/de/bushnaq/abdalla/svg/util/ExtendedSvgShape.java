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