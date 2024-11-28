/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.ImportHYK;
import org.dive4elements.river.importer.ImportHYKEntry;
import org.dive4elements.river.importer.ImportHYKFormation;
import org.dive4elements.river.importer.ImportHYKFlowZone;
import org.dive4elements.river.importer.ImportHYKFlowZoneType;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Calendar;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class HYKParser
{
    private static Logger log = LogManager.getLogger(HYKParser.class);

    public interface Callback {
        boolean hykAccept(File file);
        void    hykParsed(HYKParser parser);
    } // interface Callback

    public static enum State {
        LINE_1, LINE_2, LINE_3, LINE_4, LINE_5, LINE_6
    };

    private static final String ENCODING = "ISO-8859-1";

    protected Map<String, ImportHYKFlowZoneType> flowZoneTypes;

    protected ImportHYK hyk;

    public HYKParser() {
        flowZoneTypes = new HashMap<String, ImportHYKFlowZoneType>();
    }

    public ImportHYK getHYK() {
        return hyk;
    }

    private static Date yearToDate(Integer year) {
        if (year == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.set(year, 0, 1, 12, 0, 0);
        long ms = cal.getTimeInMillis();
        cal.setTimeInMillis(ms - ms%1000);
        return cal.getTime();
    }

    public boolean parse(File file) {

        boolean debug = log.isDebugEnabled();

        log.info("Parsing HYK file '" + file + "'");

        LineNumberReader in = null;

        String description =
            file.getParentFile().getName() + "/" + file.getName();

        hyk = new ImportHYK(null, description);

        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line;

            State state = State.LINE_1;

            int numFormations = 0;

            BigDecimal km         = null;
            BigDecimal top        = null;
            BigDecimal bottom     = null;
            BigDecimal distanceVL = null;
            BigDecimal distanceHF = null;
            BigDecimal distanceVR = null;

            Integer    year       = null;
            int        numZones   = 0;

            ImportHYKFlowZoneType [] fzts     = null;
            BigDecimal            [] coords   = null;
            int                      coordPos = 0;

            ImportHYKEntry     entry     = null;
            ImportHYKFormation formation = null;

            BigDecimal lastZoneEnd = null;

            while ((line = in.readLine()) != null) {

                if (line.startsWith("*") || line.startsWith("----")) {
                    continue;
                }

                line = line.trim();

                if (state != State.LINE_5 && line.length() == 0) {
                    continue;
                }

                String [] parts = line.split("\\s+");

                if (debug) {
                    log.debug("'" + line + "': " + state);
                }

                switch (state) {
                    case LINE_1:
                        if (parts.length < 2) {
                            log.error("HYK 1: not enough elements in line " +
                                in.getLineNumber());
                            return false;
                        }

                        if (parts.length == 2) {
                            // no year given
                            year = null;
                        }
                        else {
                            try {
                                year = Integer.valueOf(parts[1]);
                            }
                            catch (NumberFormatException nfe) {
                                log.error(
                                    "year is not an integer in line " +
                                    in.getLineNumber());
                                return false;
                            }
                        }
                        try {
                            km = new BigDecimal(parts[0]);
                            numFormations = Integer.parseInt(
                                parts[parts.length > 2 ? 2 : 1]);
                        }
                        catch (NumberFormatException nfe) {
                            log.error(
                                "parsing number of formations " +
                                "or km failed in line " + in.getLineNumber());
                            return false;
                        }
                        entry = new ImportHYKEntry(hyk, km, yearToDate(year));
                        hyk.addEntry(entry);

                        state = State.LINE_2;
                        break;

                    case LINE_2:
                        if (parts.length < 3) {
                            log.error("HYK 2: not enough elements in line " +
                                in.getLineNumber());
                            return false;
                        }
                        try {
                            numZones = Integer.parseInt(parts[0]);
                            bottom   = new BigDecimal(parts[1]);
                            top      = new BigDecimal(parts[2]);
                        }
                        catch (NumberFormatException nfe) {
                            log.error(
                                "HYK: parsing num zones, bottom or top height "
                                + "failed in line " + in.getLineNumber());
                            return false;
                        }

                        if (parts.length > 3) {
                            try {
                                lastZoneEnd = new BigDecimal(parts[3]);
                            }
                            catch (NumberFormatException nfe) {
                                log.error(
                                    "HYK: parsing last flow zone end in " +
                                    "failed in line " + in.getLineNumber());
                                return false;
                            }
                        }
                        else {
                            lastZoneEnd = null;
                        }

                        formation = new ImportHYKFormation();
                        formation.setBottom(bottom);
                        formation.setTop(top);
                        entry.addFormation(formation);

                        state = State.LINE_3;
                        break;

                    case LINE_3:
                        if (parts.length != numZones) {
                            log.error(
                                "HYK: number of flow zones mismatches " +
                                "in line " + in.getLineNumber());
                            return false;
                        }

                        fzts = new ImportHYKFlowZoneType[parts.length];
                        for (int i = 0; i < fzts.length; ++i) {
                            fzts[i] = getFlowZoneType(parts[i]);
                        }
                        coords = new BigDecimal[numZones];
                        state = State.LINE_4;
                        break;

                    case LINE_4:
                        try {
                            int N = Math.min(parts.length, coords.length);
                            for (coordPos = 0; coordPos < N; ++coordPos) {
                                coords[coordPos] =
                                    new BigDecimal(parts[coordPos]);
                            }
                        }
                        catch (NumberFormatException nfe) {
                            log.error("HYK: cannot parse number in line " +
                                in.getLineNumber());
                            return false;
                        }
                        state = State.LINE_5;
                        break;

                    case LINE_5:
                        if (parts.length + coordPos < coords.length) {
                            log.error("HYK 5: not enough elements in line " +
                                in.getLineNumber());
                            return false;
                        }
                        try {
                            for (int i = 0;
                                i < parts.length && coordPos < coords.length;
                                ++i, ++coordPos
                            ) {
                                coords[coordPos] = new BigDecimal(parts[i]);
                            }
                        }
                        catch (NumberFormatException nfe) {
                            log.error("HYK: cannot parse number in line " +
                                in.getLineNumber());
                            return false;
                        }
                        for (int i = 0; i < coords.length; ++i) {
                            BigDecimal a = coords[i];
                            BigDecimal b = i == coords.length-1
                                ? (lastZoneEnd != null
                                    ? lastZoneEnd
                                    : coords[i])
                                : coords[i+1];

                            if (a.compareTo(b) > 0) {
                                log.warn(
                                    "HYK: zone coordinates swapped in line " +
                                    in.getLineNumber());
                                BigDecimal c = a; a = b; b = c;
                            }
                            ImportHYKFlowZone zone = new ImportHYKFlowZone(
                                formation, fzts[i], a, b);
                            formation.addFlowZone(zone);
                        }
                        state = State.LINE_6;
                        break;

                    case LINE_6:
                        if (parts.length < 3) {
                            log.error("HYK 6: not enough elements in line " +
                                in.getLineNumber());
                            return false;
                        }
                        try {
                            distanceVL = new BigDecimal(parts[0]);
                            distanceHF = new BigDecimal(parts[1]);
                            distanceVR = new BigDecimal(parts[2]);
                        }
                        catch (NumberFormatException nfe) {
                            log.error("HYK: cannot parse number in line " +
                                in.getLineNumber());
                            return false;
                        }
                        formation.setDistanceVL(distanceVL);
                        formation.setDistanceHF(distanceHF);
                        formation.setDistanceVR(distanceVR);

                        // continue with next formation.
                        state = --numFormations > 0 // formations left?
                            ? State.LINE_2
                            : State.LINE_1;
                        break;
                }
            }
        }
        catch (IOException ioe) {
            log.error("HYK: Error reading file.", ioe);
            return false;
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ioe) {
                    log.error("HYK: Error closing file.", ioe);
                }
            }
        }
        return true;
    }

    protected ImportHYKFlowZoneType getFlowZoneType(String name) {
        name = name.toUpperCase();
        ImportHYKFlowZoneType fzt = flowZoneTypes.get(name);
        if (fzt == null) {
            log.info("New flow zone type: " + name);
            fzt = new ImportHYKFlowZoneType(name);
            flowZoneTypes.put(name, fzt);
        }
        return fzt;
    }

    protected void reset() {
        hyk = null;
    }

    public void parseHYKs(File root, final Callback callback) {

        FileTools.walkTree(root, new FileTools.FileVisitor() {
            @Override
            public boolean visit(File file) {
                if (file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(".hyk")
                && (callback == null || callback.hykAccept(file))) {
                    reset();
                    boolean success = parse(file);
                    log.info("parsing " + (success ? "succeeded" : "failed"));
                    if (success && callback != null) {
                        callback.hykParsed(HYKParser.this);
                    }
                }
                return true;
            }
        });
    }

    public static void main(String [] args) {

        HYKParser parser = new HYKParser();

        for (String arg: args) {
            parser.parseHYKs(new File(arg), null);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
