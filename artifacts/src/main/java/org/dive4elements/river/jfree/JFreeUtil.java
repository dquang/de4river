/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;

import org.dive4elements.river.artifacts.math.Function;
import org.dive4elements.river.themes.ThemeDocument;

public class JFreeUtil {

    private static final Logger log = LogManager.getLogger(JFreeUtil.class);

    /** Do not instantiate. */
    private JFreeUtil() {
    }


    /**
     * True if \param hotspot collides with a Entity in \param entities.
     * @param hotspot Shape to compare against other shapes (bounds only).
     * @param entities entities against which to compare shape.
     * @param exclusiveEntityClass If not null, consider only entities of
     *        given class.
     * @return true if a collision (non-zero intersection) exists between
     *        shapes.
     */
    public static boolean collides(Shape hotspot, EntityCollection entities,
        Class exclusiveEntityClass) {
        if (entities == null) return false;

        Rectangle2D hotspotBox = hotspot.getBounds2D();

        for (Iterator i = entities.iterator(); i.hasNext(); ) {
            Object next = i.next();
            ChartEntity entity = (ChartEntity) next;
            if (exclusiveEntityClass == null
                || exclusiveEntityClass.isInstance(entity))
                {
                if (entity.getArea().intersects(hotspotBox)) {
                    // Found collision, early stop.
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * This function samples a randomized line that contains of x and y values
     * between <i>startX</i>, <i>endX</i>, <i>startY</i> and <i>endY</i>. The
     * number of points in the line is specified by <i>num</i>.
     *
     * @param num The number of points in the line.
     * @param startX The min value of the x values.
     * @param endX The max value of the x values.
     * @param startY The min value of the y values.
     * @param endY The max value of the y values.
     * @return an array with [allX-values, allY-values].
     * @throws IllegalArgumentException
     */
    public static double[][] randomizeLine(
        int    num,
        double startX,
        double endX,
        double startY,
        double endY
    ) throws IllegalArgumentException
    {
        if (num <= 0) {
            throw new IllegalArgumentException("Parameter 'num' has to be > 0");
        }

        Random random = new Random();

        double[] x = new double[num];
        double[] y = new double[num];

        for (int i = 0; i < num; i++) {
            double xFac = random.nextDouble();
            double yFac = random.nextDouble();

            x[i] = startX + xFac * (endX - startX);
            y[i] = startY + yFac * (endY - startY);

            log.debug("Created new point: " + x[i] + "|" + y[i]);
        }

        return new double[][] { x, y };
    }


    public static StyledXYSeries sampleFunction2D(
        Function func,
        ThemeDocument theme,
        String   seriesKey,
        int      samples,
        double   start,
        double   end
    ) {
        StyledXYSeries series = new StyledXYSeries(seriesKey, theme);

        double step = (end - start) / (samples - 1);

        for (int i = 0; i < samples; i++) {
            double x = start + (step * i);
            series.add(x, func.value(x));
        }

        return series;
    }

    public static StyledXYSeries sampleFunction2DPositive(
        Function func,
        ThemeDocument theme,
        String   seriesKey,
        int      samples,
        double   start,
        double   end
    ) {
        StyledXYSeries series = new StyledXYSeries(seriesKey, theme);

        double step = (end - start) / (samples - 1);

        for (int i = 0; i < samples; i++) {
            double x = start + (step * i);
            double v = func.value(x);
            if (x > 0d && v > 0d) {
                series.add(x, v);
            }
        }

        return series;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
