/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.java2d;

import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import java.util.HashMap;
import java.util.Map;

public class ShapeUtils
{
    // TODO: Use enum
    public static final int MEASURED     = 0;
    public static final int DIGITIZED    = 1;
    public static final int INTERPOLATED = 2;

    public static final boolean DIGITIZED_FILL    = false;
    public static final boolean MEASURED_FILL     = true;
    public static final boolean INTERPOLATED_FILL = false;

   public static final Shape DIGITIZED_SHAPE =
        createCross(4f);

    public static final Shape MEASURED_SHAPE =
        new Rectangle2D.Double(-2, -2, 4, 4);

    public static final Shape INTERPOLATED_SHAPE =
        new Ellipse2D.Double(-2, -2, 4, 4);

    protected static Map<Long, Shape> scaledShapesCache =
        new HashMap<Long, Shape>();

    public static final Shape createCross(float size) {
        float half = size * 0.5f;
        GeneralPath p = new GeneralPath();
        p.moveTo(-half, -half);
        p.lineTo(half, half);
        p.closePath();
        p.moveTo(-half, half);
        p.lineTo(half, -half);
        p.closePath();
        return p;
    }

    public static Shape scale(Shape shape, float factor) {
        if (factor == 1f) {
            return shape;
        }
        AffineTransform xform =
            AffineTransform.getScaleInstance(factor, factor);

        GeneralPath gp = new GeneralPath(shape);
        return gp.createTransformedShape(xform);
    }

    public static synchronized Shape getScaledShape(int type, float size) {

        Long hash = Long.valueOf(
            (((long)type) << 32) | Float.floatToIntBits(size));

        Shape shape = scaledShapesCache.get(hash);

        if (shape == null) {
            switch (type) {
                case MEASURED:
                    shape = MEASURED_SHAPE;
                    break;
                case DIGITIZED:
                    shape = DIGITIZED_SHAPE;
                    break;
                default:
                    shape = INTERPOLATED_SHAPE;
            }
            scaledShapesCache.put(hash, shape = scale(shape, size));
        }

        return shape;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
