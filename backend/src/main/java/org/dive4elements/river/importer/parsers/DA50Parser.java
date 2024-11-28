/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.TreeMap;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.XY;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.backend.utils.EpsilonComparator;


/**
 * To create cross-sections, generate: Map[double,list[xy]] from files
 * in da50 format.
 */
public class DA50Parser extends LineParser implements CrossSectionParser
{
    /** Private log. */
    private static Logger log = LogManager.getLogger(DA50Parser.class);

    /** The current line to which add points. */
    private List<XY> currentLine;

    /** Data collected so far, last element will be currentLine. */
    protected Map<Double, List<XY>> data;


    /** Trivial constructor. */
    public DA50Parser() {
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
        return null;
    }


    /**
     * Return the data parsed.
     * @return map of stations (km) to list of points.
     */
    @Override
    public Map<Double, List<XY>> getData() {
        return data;
    }


    /** Walk a directory tree and attempt parsing all *.d50 files. */
    public void parseDA50s(File root, final Callback callback) {

        FileTools.walkTree(root, new FileTools.FileVisitor() {
            @Override
            public boolean visit(File file) {
                // TODO check presence of TIM file.
                if (file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(".d50")
                && (callback == null || callback.accept(file))) {
                    reset();
                    try {
                        parse(file);
                        log.info("parsing done");
                        if (callback != null) {
                            callback.parsed(DA50Parser.this);
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
    }


    /**
     * Called for each line. Try to extract info from a da50 line.
     */
    @Override
    protected void handleLine(int lineNum, String line) {
        String pointId  = line.substring(0,2);
        String streetId = line.substring(2,9);
        String station  = line.substring(9,18);
        String free     = line.substring(18,20);
        String gkLRight = line.substring(20,30);
        String gkLHigh  = line.substring(30,40);
        String gkRRight = line.substring(40,50);
        String gkRHigh  = line.substring(50,60);
        String distance = line.substring(60,70);

        // TODO Intersect/Correlate these with e.g. TIM files.
        // TODO note that as-is these points are really useless.
        currentLine = new ArrayList<XY>();
        currentLine.add(new XY(0, 10,0));
        currentLine.add(new XY(Double.parseDouble(distance), 10, 1));
    }


    /** Called when file is fully consumed. */
    @Override
    protected void finish() {
        log.info("Parsed " + data.size() + " lines");
    }


    /** Parses files given as arguments. */
    public static void main(String [] args) {

        DA50Parser parser = new DA50Parser();

        log.warn("Start parsing files.");
        for (String arg: args) {
            parser.parseDA50s(new File(arg), null);
            log.warn("Parsing a file.");
        }
        log.error("Finished parsing files.");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
