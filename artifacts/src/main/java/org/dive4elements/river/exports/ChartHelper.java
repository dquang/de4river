/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeries;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.jfree.DoubleBounds;
import org.dive4elements.river.jfree.TimeBounds;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartHelper {

    private static final Logger log = LogManager.getLogger(ChartHelper.class);


    /**
     * This method returns the ranges of the XYDataset <i>dataset</i> as array
     * with [xrange, yrange].
     *
     * @param dataset The dataset which should be evaluated.
     *
     * @return an array with x and y ranges.
     */
    public static Bounds[] getBounds(XYSeriesCollection dataset) {
        int seriesCount = dataset != null ? dataset.getSeriesCount() : 0;

        if (seriesCount == 0) {
            log.warn("Dataset is empty or has no Series set.");
            return null;
        }

        boolean foundValue = false;

        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (int i = 0, m = seriesCount; i < m; i++) {
            for (int j = 0, n = dataset.getItemCount(i); j < n; j++) {
                double x = dataset.getXValue(i, j);
                double y = dataset.getYValue(i, j);

                if (Double.isNaN(x) || Double.isNaN(y)) {
                    log.warn("Item " + j + " in Series " + i + " is broken");
                    continue;
                }

                foundValue = true;

                if (x < minX) {
                    minX = x;
                }

                if (x > maxX) {
                    maxX = x;
                }

                if (y < minY) {
                    minY = y;
                }

                if (y > maxY) {
                    maxY = y;
                }
            }
        }

        return foundValue
            ? new Bounds[] {
                    new DoubleBounds(minX, maxX),
                    new DoubleBounds(minY, maxY) }
            : null;
    }


    public static Bounds[] getBounds(XYDataset dataset) {
        if (dataset instanceof XYSeriesCollection) {
            return getBounds((XYSeriesCollection) dataset);
        }
        else if(dataset instanceof TimeSeriesCollection) {
            return getBounds((TimeSeriesCollection) dataset);
        }
        else {
            log.warn("Unknown XYDataset instance: " + dataset.getClass());
            return null;
        }
    }


    public static Bounds[] getBounds(TimeSeriesCollection collection) {
        int seriesCount = collection != null ? collection.getSeriesCount() : 0;

        if (seriesCount == 0) {
            log.warn("TimeSeriesCollection is empty or has no Series set.");
            return null;
        }

        boolean foundValue = false;

        long lowerX = Long.MAX_VALUE;
        long upperX = -Long.MAX_VALUE;

        double lowerY = Double.MAX_VALUE;
        double upperY = -Double.MAX_VALUE;

        for (int i = 0, m = seriesCount; i < m; i++) {
            TimeSeries series = collection.getSeries(i);

            for (int j = 0, n = collection.getItemCount(i); j < n; j++) {
                RegularTimePeriod rtp = series.getTimePeriod(j);

                if (rtp == null) {
                    continue;
                }

                foundValue = true;

                long start = rtp.getFirstMillisecond();
                long end   = rtp.getLastMillisecond();

                if (start < lowerX) {
                    lowerX = start;
                }

                if (end > upperX) {
                    upperX = end;
                }

                double y = series.getValue(j).doubleValue();

                lowerY = Math.min(lowerY, y);
                upperY = Math.max(upperY, y);
            }
        }

        if (foundValue) {
            return new Bounds[] {
                new TimeBounds(lowerX, upperX),
                new DoubleBounds(lowerY, upperY)
            };
        }

        return null;
    }


    /**
     * Expand bounds by percent.
     *
     * @param bounds The bounds to expand.
     * @param percent The percentage to expand.
     *
     * @return a new, expanded bounds.
     */
    public static Bounds expandBounds(Bounds bounds, double percent) {
        if (bounds == null) {
            return null;
        }

        double value  = (Double) bounds.getLower();
        double expand = Math.abs(value / 100 * percent);

        return expand != 0
            ? new DoubleBounds(value-expand, value+expand)
            : new DoubleBounds(-0.01 * percent, 0.01 * percent);
    }


    /**
     * Expand range by percent.
     *
     * @param range The range to expand.
     * @param percent The percentage to expand.
     *
     * @return a new, expanded range.
     */
    public static Range expandRange(Range range, double percent) {
        if (range == null) {
            return null;
        }

        double value  = range.getLowerBound();
        double expand = Math.abs(value / 100 * percent);

        return expand != 0
            ? new Range(value-expand, value+expand)
            : new Range(-0.01 * percent, 0.01 * percent);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
