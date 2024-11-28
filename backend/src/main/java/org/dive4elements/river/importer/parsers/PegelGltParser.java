/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.ImportGauge;
import org.dive4elements.river.importer.ImportRange;

public class PegelGltParser
{
    private static Logger log = LogManager.getLogger(PegelGltParser.class);

    public static final String ENCODING = "ISO-8859-1";

    public static final String KM = "km:";

    protected List<ImportGauge> gauges;

    public PegelGltParser() {
        gauges = new ArrayList<ImportGauge>();
    }

    public List<ImportGauge> getGauges() {
        return gauges;
    }

    public void parse(File file) throws IOException {

        File parent = file.getParentFile();

        log.info("parsing GLT file '" + file + "'");
        LineNumberReader in = null;
        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line = null;
            while ((line = in.readLine()) != null) {
                if ((line = line.trim()).length() == 0) {
                    continue;
                }

                int kmPos = line.indexOf(KM);
                if (kmPos < 0) {
                    log.warn("GLT: no gauge found in line "
                        + in.getLineNumber());
                    continue;
                }

                String gaugeName = line.substring(0, kmPos).trim();
                log.info("Found gauge '" + gaugeName + "'");

                line = line.substring(kmPos + KM.length()).trim();

                String [] parts = line.split("\\s+");
                if (parts.length < 4) {
                    log.warn("GLT: line " + in.getLineNumber()
                        + " has not enough columns.");
                    continue;
                }

                BigDecimal from = new BigDecimal(parts[0].replace(",", "."));
                BigDecimal to   = new BigDecimal(parts[1].replace(",", "."));
                if (from.compareTo(from) > 0) {
                    BigDecimal t = from; from = to; to = t;
                }
                ImportRange range = new ImportRange(from, to);
                File staFile = FileTools.repair(new File(parent, parts[2]));
                File atFile  = FileTools.repair(new File(parent, parts[3]));

                if (log.isDebugEnabled()) {
                    log.debug("\tfrom: " + from);
                    log.debug("\tto: " + to);
                    log.debug("\tsta: " + staFile);
                    log.debug("\tat: " + atFile);
                }

                if (staFile.exists() && atFile.exists()) {
                    gauges.add(new ImportGauge(range, staFile, atFile));
                }
                else {
                    if (!staFile.exists()) {
                        log.warn(staFile + " does not exist. Gauge ignored.");
                    }
                    if (!atFile.exists()) {
                        log.warn(atFile + " does not exist. Gauge ignored.");
                    }
                }
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
