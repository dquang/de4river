/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.jfree.data.xy.XYSeries;

import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WWQQ;

/**
 * Helper to create and modify StyledXYSeries.
 */
public class StyledSeriesBuilder {

    /**
     * JFreeChart and the area calculation will fail if we use Double.INFINITY
     * or Double.MAX_VALUE (probably because these are really used in
     * calculations). We define and use a more handy value instead.
     */
    final static double BIG_DOUBLE_VALUE = 1234567d;

    private static final Logger log = LogManager.getLogger
        (StyledSeriesBuilder.class);


    /**
     * Trivial, hidden constructor.
     */
    private StyledSeriesBuilder() {
    }


    /**
     * Add points to series, create gaps if certain distance
     * between points is met.
     *
     * @param series Series to add points to.
     * @param points Points to add to series, points[0] to 1st dim, points[1]
     *               to 2nd dim.
     * @param skipNANs if true, skip NAN values in points parameter. Otherwise,
     *                 the NaNs lead to gaps in graph.
     * @param distance if two consecutive entries in points[0] are more
     *                 than distance apart, create a NaN value to skip
     *                 in display.
     */
    public static void addPoints(
        XYSeries series,
        double[][] points,
        boolean skipNANs,
        double distance
    ) {
        if (points == null || points.length <= 1) {
            return;
        }
        double [] xPoints = points[0];
        double [] yPoints = points[1];
        for (int i = 0; i < xPoints.length; i++) {
            if (skipNANs &&
                (Double.isNaN(xPoints[i]) || Double.isNaN(yPoints[i]))) {
                continue;
            }
            // Create gap if distance between points > distance.
            if (i > 0 && Math.abs(xPoints[i-1] - xPoints[i]) > distance &&
                !Double.isNaN(yPoints[i-1]) && !Double.isNaN(yPoints[i])) {
                series.add((xPoints[i-1] + xPoints[i])/2, Double.NaN, false);
            }
            series.add(xPoints[i], yPoints[i], false);
        }
    }

    /**
     * Add points to series.
     *
     * @param series Series to add points to.
     * @param points Points to add to series, points[0] to 1st dim, points[1]
     *               to 2nd dim.
     * @param skipNANs if true, skip NAN values in points parameter.
     * @param transY translate y-values by this value (before scale).
     * @param factorY scale y-values by this value (after translation).
     */
    public static void addPoints(XYSeries series, double[][] points,
        boolean skipNANs, double transY, double factorY) {
        if (transY == 0d && factorY == 1d) {
            addPoints(series, points, skipNANs);
            return;
        }
        if (points == null || points.length <= 1) {
            return;
        }
        double [] xPoints = points[0];
        double [] yPoints = points[1];
        for (int i = 0; i < xPoints.length; i++) {
            if (skipNANs &&
                (Double.isNaN(xPoints[i]) || Double.isNaN(yPoints[i]))) {
                continue;
            }
            series.add(xPoints[i], factorY * (transY+yPoints[i]), false);
        }
    }

    /**
     * Add points to series.
     *
     * @param series Series to add points to.
     * @param points Points to add to series, points[0] to 1st dim, points[1]
     *               to 2nd dim.
     * @param skipNANs if true, skip NAN values in points parameter.
     */
    public static void addPoints(
        XYSeries series,
        double[][] points,
        boolean skipNANs
    ) {
        if (points == null || points.length <= 1) {
            return;
        }
        double [] xPoints = points[0];
        double [] yPoints = points[1];
        for (int i = 0; i < xPoints.length; i++) {
            if (skipNANs &&
                (Double.isNaN(xPoints[i]) || Double.isNaN(yPoints[i]))) {
                continue;
            }
            series.add(xPoints[i], yPoints[i], false);
        }
    }


    /**
     * Add points to series (km to 1st dim, w to 2nd dim).
     *
     * @param series Series to add points to.
     * @param wkms WKms to add to series.
     */
    public static void addPoints(XYSeries series, WKms wkms) {
        if (wkms == null) {
            return;
        }

        int size = wkms.size();

        for (int i = 0; i < size; i++) {
            series.add(wkms.getKm(i), wkms.getW(i), false);
        }
    }


    /**
     * Add points to dataset with an offset (shift all points by given amount).
     * @param series series to add data to.
     * @param wkms WKms of which the Ws will be shifted.
     * @param off the offset.
     */
    public static void addUpperBand(XYSeries series, WKms wkms, double off) {
        if (wkms == null) {
            return;
        }

        int size = wkms.size();

        for (int i = 0; i < size; i++) {
            series.add(wkms.getKm(i), wkms.getW(i)+off, false);
        }
    }


    /**
     * Add points to dataset with an offset (shift all points 'down' by given
     * amount).
     * @param series series to add data to.
     * @param wkms WKms of which the Ws will be shifted.
     * @param off the offset.
     */
    public static void addLowerBand(XYSeries series, WKms wkms, double off) {
        addUpperBand(series, wkms, -off);
    }


    /**
     * Add points to series (km to 1st dim, q to 2nd dim).
     *
     * @param series Series to add points to.
     * @param wqkms WQKms to add to series.
     */
    public static void addPointsKmQ(XYSeries series, WQKms wqkms) {
        if (wqkms == null) {
            return;
        }

        int size = wqkms.size();

        for (int i = 0; i < size; i++) {
            series.add(wqkms.getKm(i), wqkms.getQ(i), false);
        }
    }


    /**
     * Add points to series (km to 1st dim, q to 2nd dim), adding points
     * to achieve a step-like curve.
     *
     * @param series Series to add points to.
     * @param wqkms WQKms to add to series.
     */
    public static void addStepPointsKmQ(XYSeries series, WQKms wqkms) {
        if (wqkms == null) {
            return;
        }

        int size = wqkms.size();

        for (int i = 0; i < size; i++) {
            if (i==0) {
                series.add(wqkms.getKm(i), wqkms.getQ(i), false);
            } else if (i == size-1) {
                series.add(wqkms.getKm(i), wqkms.getQ(i), false);
            } else {
                //Add two points.
                double prevX;
                double prevQ;
                if (wqkms.getKm(i + 1) < wqkms.getKm(i)) {
                    /* Depending on the data direction the previous km / q
                     * might have a larger index when we draw
                     * right to left data. */
                    prevX = wqkms.getKm(i + 1);
                    prevQ = wqkms.getQ(i + 1);
                } else {
                    prevX = wqkms.getKm(i - 1);
                    prevQ = wqkms.getQ(i - 1);
                }
                double halveX = (prevX + wqkms.getKm(i)) / 2d;
                series.add(halveX, prevQ, false);
                series.add(halveX, wqkms.getQ(i), false);
            }
        }
    }


    /**
     * Add points to series (q to 1st dim, w to 2nd dim).
     *
     * @param series Series to add points to.
     * @param wqkms WQKms to add to series.
     */
    public static void addPointsQW(XYSeries series, WQKms wqkms) {
        if (wqkms == null) {
            return;
        }

        int size = wqkms.size();

        for (int i = 0; i < size; i++) {
            series.add(wqkms.getQ(i), wqkms.getW(i), false);
        }
    }

    /**
     * Add points to series (q to 1st dim, w to 2nd dim), adding wTrans to the
     * W values and scaling it with wScale.
     *
     * @param series Series to add points to.
     * @param qws to add to series.
     * @param wAdd Value to add to each Q while adding to series.
     * @param wScale multiply with
     */
    public static void addPointsQW(
        XYSeries series,
        double[][] qws,
        double wTrans,
        double wScale
    ) {
        if (qws == null || qws.length == 0) {
            return;
        }

        double x[] = qws[0];
        double y[] = qws[1];

        for (int i = 0; i < x.length; i++) {
            series.add(x[i], wScale * (y[i] + wTrans), false);
        }
    }
    /**
     * Add points to series (q to 1st dim, w to 2nd dim), adding wTrans to the
     * W values and scaling it with wScale.
     *
     * @param series Series to add points to.
     * @param wqkms WQKms to add to series.
     * @param wAdd Value to add to each Q while adding to series.
     * @param wScale multiply with
     */
    public static void addPointsQW(
        XYSeries series,
        WQKms wqkms,
        double wTrans,
        double wScale
    ) {
        if (wqkms == null) {
            return;
        }

        int size = wqkms.size();

        for (int i = 0; i < size; i++) {
            series.add(wqkms.getQ(i), wScale * (wqkms.getW(i) + wTrans), false);
        }
    }

    /**
     * Add points to series (q to 1st dim, w to 2nd dim).
     *
     * @param series Series to add points to.
     * @param qs the Qs to add, assumed same length than ws.
     * @param ws the Ws to add, assumed same length than qs.
     */
    public static void addPointsQW(XYSeries series, double[] qs, double ws[]) {
        if (ws == null || qs == null) {
            return;
        }

        int size = qs.length;

        for (int i = 0; i < size; i++) {
            series.add(qs[i], ws[i], false);
        }
    }

    /**
     * Add points to series (q to 1st dim, w to 2nd dim), with
     * scaling and translation.
     *
     * @param series Series to add points to.
     * @param qs the Qs to add, assumed same length than ws.
     * @param ws the Ws to add, assumed same length than qs.
     */
    public static void addPointsQW(XYSeries series, double[] qs, double ws[],
        double wTrans, double wScale) {
        if (ws == null || qs == null) {
            return;
        }

        int size = qs.length;

        for (int i = 0; i < size; i++) {
            series.add(qs[i], wScale * (ws[i]+wTrans), false);
        }
    }



    /**
     * Add points to series (q to 1st dim, w to 2nd dim).
     *
     * @param series Series to add points to.
     * @param wwqq WWQQ to add to series.
     */
    public static void addPoints(XYSeries series, WWQQ wwqq) {
        if (wwqq == null) {
            return;
        }

        int size = wwqq.size();

        for (int i = 0; i < size; i++) {
            series.add(wwqq.getW1(i), wwqq.getW2(i), false);
        }
    }


    /**
     * Create a Series such that an infinitely big area can be filled
     * between the newly created and the given series.
     */
    public static XYSeries createGroundAtInfinity(XYSeries series) {
        XYSeries ground =
            new XYSeries(series.getKey() + /** TODO rand + */ "INF");
        ground.add(series.getMinX(), -BIG_DOUBLE_VALUE);
        ground.add(series.getMaxX(), -BIG_DOUBLE_VALUE);
        return ground;
    }


    /**
     * Create a Series such that an infinitely big area can be filled
     * between the newly created and the given series.
     */
    public static XYSeries createCeilingAtInfinity(XYSeries series) {
        XYSeries ground =
            new XYSeries(series.getKey() + /** TODO rand + */ "INF");
        ground.add(series.getMinX(), BIG_DOUBLE_VALUE);
        ground.add(series.getMaxX(), BIG_DOUBLE_VALUE);
        return ground;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
