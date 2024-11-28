/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.XY;

import org.dive4elements.river.importer.parsers.tim.Coordinate;

import org.dive4elements.river.backend.utils.DateGuesser;
import org.dive4elements.river.backend.utils.EpsilonComparator;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * To create cross-sections, generate: Map<double,list<xy>> from files
 * in w80 format.
 */
public class W80Parser extends LineParser implements CrossSectionParser
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(W80Parser.class);


    /** The current line to which add points. */
    private List<XY> currentLine;


    /** Data collected so far, last element will be currentLine. */
    protected Map<Double, List<XY>> data;


    /** Anchor to project to. */
    private static class Anchor extends Coordinate {

        private static final double EPSILON = 1e-5;

        private double station;

        public Anchor(double x, double y, double z, double station) {
            super(x, y, z);
            this.station = station;
        }

        public boolean sameStation(double station) {
            return Math.abs(this.station - station) < EPSILON;
        }
    }


    /** Reference point for simple projection. */
    private Anchor anchor;


    /**
     * Reference point for distance calculations, introduced to
     * deal with bends in the lines.
     * Array has two entrys: first is GK-Right, second GK-High.
     */
    private double[] lastPointGK;


    /** Measurement date of anchor as listed in w80 file. */
    private Date anchorDate;


    private double distanceToLastPoint(double gkr, double gkh) {
        double dx = gkr - lastPointGK[0];
        double dy = gkh - lastPointGK[1];
        double d  = dx*dx + dy*dy;

        return Math.sqrt(d);
    }


    /** Trivial constructor. */
    public W80Parser() {
        data = new TreeMap<Double, List<XY>>(EpsilonComparator.CMP);
    }


    /** Get the description of the cross section parsed. */
    @Override
    public String getDescription() {
        return FileTools.removeExtension(getFileName());
    }


    /** Get the year of this cross sections measurement. */
    @Override
    public Integer getYear() {
        if (anchorDate == null) {
            return null;
        }
        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(anchorDate);
        return dateCalendar.get(Calendar.YEAR);
    }


    /**
     * Return the data parsed.
     * @return map of stations (km) to list of points.
     */
    @Override
    public Map<Double, List<XY>> getData() {
        return data;
    }


    public void parseW80s(File root, final Callback callback) {

        FileTools.walkTree(root, new FileTools.FileVisitor() {
            @Override
            public boolean visit(File file) {
                if (file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(".w80")
                && (callback == null || callback.accept(file))) {
                    reset();
                    try {
                        parse(file);
                        log.info("parsing done");
                        if (callback != null) {
                            callback.parsed(W80Parser.this);
                        }
                    }
                    catch (IOException ioe) {
                        log.error("IOException while parsing file");
                        return false;
                    }
                }
                return true;
            }
        });
    }


    /** Called before consuming first line of file. */
    public void reset() {
        data.clear();
        currentLine = new ArrayList<XY>();
        anchor = null;
        anchorDate = null;
        lastPointGK = new double[] {0d,0d};
    }


    /**
     * Get the Index of the last cross-section lines point.
     * @return last points index, -1 if not available.
     */
    private int getLastPointIdx() {
        if (currentLine == null || currentLine.isEmpty()) {
            return -1;
        }
        XY lastPoint = this.currentLine.get(currentLine.size()-1);
        return lastPoint.getIndex();
    }


    private double getLastPointX() {
        if (currentLine == null || currentLine.isEmpty()) {
            return 0d;
        }
        XY lastPoint = this.currentLine.get(currentLine.size()-1);
        return lastPoint.getX();
    }


    /**
     * Add a Point (YZ,Index) to the current cross section line.
     * @param y The y coordinate of new point.
     * @param z The z coordinate of new point.
     * @param idx Ignored, the parameter of new point.
     * @return true if point could been added, false otherwise (e.g. not
     *         parsable y or z values.
     */
    private boolean addPoint(
        double gkr,
        double gkh,
        double height,
        String idx
    ) {
        // Calculate distance between this and lst point (add distances).
        double d = distanceToLastPoint(gkr, gkh);
        double totalX = getLastPointX() + d;

        // We ignore idx, and increment instead.
        int index;
        int lastPointIdx = getLastPointIdx();
        if (lastPointIdx <= 0) {
            index = 1;
        } else {
            index = lastPointIdx + 1;
        }

        this.lastPointGK[0] = gkr;
        this.lastPointGK[1] = gkh;
        currentLine.add(new XY(totalX, height/1000d, index));
        return true;
    }


    /**
     * Called for each line. Try to extract info from a w80 line.
     */
    @Override
    protected void handleLine(int lineNum, String line) {
        // The 'shore' field shows which side of the river
        // the shore is measured.
        // Therefore, the points have to be added in the correct order (also
        // because later distances are calculated which cannot be
        // negative.
        String pointId = line.substring(0,20);
        String station = line.substring(9,15);
        String shore   = line.substring(15,16);
        // TODO: There is 'station' and a 'shore'-code behind.
        // 1 = left, 2 = right. none = middle
        String pointIndex = line.substring(16,21);
        // For GK, first seven digits are of interest.
        String gkRight = line.substring(20,30);
        String gkHigh  = line.substring(30,40);
        String date    = line.substring(40,46);
        /* Fields not (yet?) of interest for FLYS
        String locType = line.substring(46,47);
        */
        String height  = line.substring(47,54);
        String dateH   = line.substring(54,60);
        /* Fields not (yet?) of interest for FLYS
        String typeH   = line.substring(60,61);
        String kindH   = line.substring(61,64);
        */
        String dateDec = line.substring(64,70);
        /* Fields not (yet?) of interest for FLYS
        String note    = line.substring(70,78);
        String actual  = line.substring(78);
        */

        double stationKm = Double.parseDouble(station) / 1000d;
        double gkRightKm = Double.parseDouble(gkRight.substring(0,7));
        double gkHighKm  = Double.parseDouble(gkHigh.substring(0,7));
        double heightM   = Double.parseDouble(height);

        // New (or first) line.
        if (anchor == null || !anchor.sameStation(stationKm)) {
            anchor = new Anchor(gkRightKm, gkHighKm, heightM, stationKm);
            lastPointGK[0] = gkRightKm;
            lastPointGK[1] = gkHighKm;
            currentLine = new ArrayList<XY>();
            data.put(stationKm, currentLine);
            currentLine.add(new XY(0d, heightM, 0));
            try {
                anchorDate = DateGuesser.guessDate(date);
            }
            catch (IllegalArgumentException iae) {
                log.warn("W80: Invalid date '" + date + "'.");
            }
        }
        else {
            addPoint(gkRightKm, gkHighKm, heightM, pointIndex);
        }
    }


    /** Called when file is fully consumed. */
    @Override
    protected void finish() {
        log.info("Parsed " + data.size() + " lines");
    }


    /** Parses files given as arguments. */
    public static void main(String [] args) {

        W80Parser parser = new W80Parser();

        log.warn("Start parsing files.");
        for (String arg: args) {
            log.warn("Parsing a file.");
            parser.parseW80s(new File(arg), null);
        }
        log.error("Finished parsing files.");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
