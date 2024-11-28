/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.geom;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;

import org.dive4elements.river.artifacts.math.Linear;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gnu.trove.TDoubleArrayList;

/**
 * Utility to create lines (intersect water with cross-section etc).
 */
public class Lines
{
    private static Logger log = LogManager.getLogger(Lines.class);

    public static final double EPSILON = 1e-4;

    public static enum Mode { UNDEF, WET, DRY };


    /** Never instantiate Lines, use static functions instead. */
    protected Lines() {
    }


    /**
     * Calculate area of polygon with four vertices.
     * @return area of polygon with four vertices.
     */
    public static double area(Point2D p1, Point2D p2, Point2D p3, Point2D p4) {
        double[] x = new double[] {
            p1.getX(), p2.getX(), p3.getX(), p4.getX(), p1.getX() };
        double[] y = new double[] {
            p1.getY(), p2.getY(), p3.getY(), p4.getY(), p1.getY() };
        double area = 0d;
        for (int i=0; i <4; i++) {
            area += (x[i] * y[i+1]) - (x[i+1] * y[i]);
        }
        return Math.abs(area * 0.5d);
    }


    /**
     * Calculate the 'length' of the given lines.
     * @param lines lines of which to calculate length.
     */
    public static double length(List<Line2D> lines) {
        double sum = 0d;
        for (Line2D line: lines) {
            double xDiff = line.getX1() - line.getX2();
            double yDiff = line.getY1() - line.getY2();
            sum += Math.sqrt(xDiff*xDiff + yDiff*yDiff);
        }
        return sum;
    }


    /** List of lines and a double-precision area. */
    private static class ListWithArea {
        public List<Line2D> lines;
        public double area;
        public ListWithArea(List<Line2D> lines, double area) {
            this.lines = lines;
            this.area = area;
        }
    }


    /**
     * For a cross section given as points and a waterlevel (in meters),
     * create a set of lines that represent the water surface, assuming it
     * is distributed horizontally equally.
     * @param points the points describing the river bed.
     * @param waterLevel the height of the horizontal water line.
     * @return A list of Lines representing the water surface and the
     *         calculated area between water surface and river bed.
     */
    public static ListWithArea fillWater(
        List<Point2D> points,
        double waterLevel
    ) {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("fillWater");
            log.debug("----------------------------");
        }

        List<Line2D> result = new ArrayList();

        int N = points.size();

        if (N == 0) {
            return new ListWithArea(result, 0d);
        }

        if (N == 1) {
            Point2D p = points.get(0);
            // Only generate point if over profile
            if (waterLevel > p.getY()) {
                result.add(new Line2D.Double(
                    p.getX(), waterLevel,
                    p.getX(), waterLevel));
            }
            // TODO continue calculating area.
            return new ListWithArea(result, 0d);
        }

        double minX =  Double.MAX_VALUE;
        double minY =  Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        // To ensure for sequences of equals x's that
        // the original index order is preserved.
        for (Point2D p: points) {
            double x = p.getX(), y = p.getY();
            if (x < minX) minX = x;
            if (x > maxX) maxX = x;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        if (minY > waterLevel) { // profile completely over water level
            log.debug("complete over water");
            return new ListWithArea(result, 0d);
        }

        if (waterLevel > maxY) { // water floods profile
            log.debug("complete under water");
            result.add(new Line2D.Double(minX, waterLevel, maxX, waterLevel));
            return new ListWithArea(result, 0d);
        }

        // Water is sometimes above, sometimes under profile.
        Mode mode = Mode.UNDEF;

        double startX = minX;

        double area = 0d;
        // Walking along the profile.
        for (int i = 1; i < N; ++i) {
            Point2D p1 = points.get(i-1);
            Point2D p2 = points.get(i);

            if (p1.getY() < waterLevel && p2.getY() < waterLevel) {
                // completely under water
                if (debug) {
                    log.debug("under water: " + p1 + " " + p2);
                }
                if (mode != Mode.WET) {
                    startX = p1.getX();
                    mode = Mode.WET;
                }
                area += area(p1, p2,
                    new Point2D.Double(p2.getX(), waterLevel),
                    new Point2D.Double(p1.getX(), waterLevel));
                continue;
            }

            // TODO trigger area calculation
            if (p1.getY() > waterLevel && p2.getY() > waterLevel) {
                if (debug) {
                    log.debug("over water: " + p1 + " " + p2);
                }
                // completely over water
                if (mode == Mode.WET) {
                    log.debug("over/wet");
                    result.add(new Line2D.Double(
                        startX, waterLevel,
                        p1.getX(), waterLevel));
                }
                mode = Mode.DRY;
                continue;
            }

            // TODO trigger area calculation
            if (Math.abs(p1.getX() - p2.getX()) < EPSILON) {
                // vertical line
                switch (mode) {
                    case WET:
                        log.debug("vertical/wet");
                        mode = Mode.DRY;
                        result.add(new Line2D.Double(
                            startX, waterLevel,
                            p1.getX(), waterLevel));
                        break;
                    case DRY:
                        log.debug("vertical/dry");
                        mode = Mode.WET;
                        startX = p2.getX();
                        break;
                    default: // UNDEF
                        log.debug("vertical/undef");
                        if (p2.getY() < waterLevel) {
                            mode = Mode.WET;
                            startX = p2.getX();
                        }
                        else {
                            mode = Mode.DRY;
                        }
                }
                continue;
            }

            // check if waterlevel directly hits the vertices;

            boolean p1W = Math.abs(waterLevel - p1.getY()) < EPSILON;
            boolean p2W = Math.abs(waterLevel - p2.getY()) < EPSILON;

            // TODO trigger area calculation
            if (p1W || p2W) {
                if (debug) {
                    log.debug("water hits vertex: "
                        + p1 + " " + p2 + " " + mode);
                }
                if (p1W && p2W) { // parallel to water -> dry
                    log.debug("water hits both vertices");
                    if (mode == Mode.WET) {
                        result.add(new Line2D.Double(
                            startX, waterLevel,
                            p1.getX(), waterLevel));
                    }
                    mode = Mode.DRY;
                }
                else if (p1W) { // p1 == waterlevel
                    log.debug("water hits first vertex");
                    if (p2.getY() > waterLevel) { // --> dry
                        if (mode == Mode.WET) {
                            result.add(new Line2D.Double(
                                startX, waterLevel,
                                p1.getX(), waterLevel));
                        }
                        mode = Mode.DRY;
                    }
                    else { // --> wet
                        if (mode != Mode.WET) {
                            startX = p1.getX();
                            mode = Mode.WET;
                        }
                        area += area(p1, p2,
                            new Point2D.Double(p2.getX(), waterLevel),
                            new Point2D.Double(p2.getX(), waterLevel));
                    }
                }
                else { // p2 == waterlevel
                    log.debug("water hits second vertex");
                    if (p1.getY() > waterLevel) { // --> wet
                        if (mode != Mode.WET) {
                            startX = p2.getX();
                            mode = Mode.WET;
                        }
                    }
                    else { // --> dry
                        if (mode == Mode.WET) {
                            result.add(new Line2D.Double(
                                startX, waterLevel,
                                p2.getX(), waterLevel));
                        }
                        mode = Mode.DRY;
                        area += area(p1, p2,
                            new Point2D.Double(p1.getX(), waterLevel),
                            new Point2D.Double(p1.getX(), waterLevel));
                    }
                }
                if (debug) {
                    log.debug("mode is now: " + mode);
                }
                continue;
            }

            // TODO trigger area calculation
            // intersection case
            double x = Linear.linear(
                waterLevel,
                p1.getY(), p2.getY(),
                p1.getX(), p2.getX());

            if (debug) {
                log.debug("intersection p1:" + p1);
                log.debug("intersection p2:" + p2);
                log.debug("intersection at x: " + x);
            }

            // Add area of that part of intersection that is 'wet'.
            if (p1.getY() > waterLevel) {
                area += area(new Point2D.Double(x, waterLevel),
                             p2,
                             new Point2D.Double(p2.getX(), waterLevel),
                             new Point2D.Double(x, waterLevel));
            }
            else {
                area += area(new Point2D.Double(x, waterLevel),
                             p1,
                             new Point2D.Double(p1.getX(), waterLevel),
                             new Point2D.Double(x, waterLevel));
            }

            switch (mode) {
                case WET:
                    log.debug("intersect/wet");
                    mode = Mode.DRY;
                    result.add(new Line2D.Double(
                        startX, waterLevel,
                        x, waterLevel));
                    break;

                case DRY:
                    log.debug("intersect/dry");
                    mode   = Mode.WET;
                    startX = x;
                    break;

                default: // UNDEF
                    log.debug("intersect/undef");
                    if (p2.getY() > waterLevel) {
                        log.debug("intersect/undef/over");
                        mode = Mode.DRY;
                        result.add(new Line2D.Double(
                            p1.getX(), waterLevel,
                            x, waterLevel));
                    }
                    else {
                        mode = Mode.WET;
                        startX = x;
                    }
            } // switch mode
        } // for all points p[i] and p[i-1]

        if (mode == Mode.WET) {
            result.add(new Line2D.Double(
                startX, waterLevel,
                maxX, waterLevel));
        }

        return new ListWithArea(result, area);
    }


    /**
     * Class holding points that form lines and the calculated length.
     */
    public static class LineData {
        public double [][] points;
        public double width;
        public double area;
        public LineData(double[][] points, double width, double area) {
            this.points = points;
            this.width = width;
            this.area = area;
        }
    }


    /** Return length of a single line. */
    public static double lineLength(Line2D line) {
        double xDiff = line.getX1() - line.getX2();
        double yDiff = line.getY1() - line.getY2();
        return Math.sqrt(xDiff*xDiff + yDiff*yDiff);
    }


    /**
     * @param points the riverbed.
     */
    public static LineData createWaterLines(
        List<Point2D> points,
        double        waterlevel
    ) {
        ListWithArea listAndArea = fillWater(points, waterlevel);
        List<Line2D> lines = listAndArea.lines;

        TDoubleArrayList lxs = new TDoubleArrayList();
        TDoubleArrayList lys = new TDoubleArrayList();
        double linesLength = 0.0f;

        for (Iterator<Line2D> iter = lines.iterator(); iter.hasNext();) {
            Line2D  line = iter.next();
            Point2D p1   = line.getP1();
            Point2D p2   = line.getP2();
            lxs.add(p1.getX());
            lys.add(p1.getY());
            lxs.add(p2.getX());
            lys.add(p2.getY());

            // Length calculation.
            linesLength += lineLength(line);

            if (iter.hasNext()) {
                lxs.add(Double.NaN);
                lys.add(Double.NaN);
            }
        }

        return new LineData(
            new double [][] { lxs.toNativeArray(), lys.toNativeArray() },
            linesLength, listAndArea.area
            );
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
