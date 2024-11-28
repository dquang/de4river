/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.model.CalculationMessage;
import org.dive4elements.river.artifacts.model.map.WSPLGENJob;


public class JobObserver extends Thread {

    private static Logger log = LogManager.getLogger(JobObserver.class);


    public static final String WSPLGEN_ENCODING =
        "ISO-8859-1";

    public static final String WSPLGEN_LOG_OUTPUT =
        System.getProperty("wsplgen.log.output", "false");

    public static final String[] STEPS = {
        ".*<-Auswertung der Kommandozeilen-Parameter beendet.*",
        ".*->Laden des DGM in Datei '.*' gestartet.*",
        ".*->Triangulierung der Knoten gestartet.*",
        ".*->Anpassung der Elemente an Dämme und Gräben gestartet.*",
        ".*<-WSPLGEN Version .* beendet.*"
    };


    protected WSPLGENJob job;

    protected InputStream in;

    protected Pattern[] patterns;

    protected int len;

    protected boolean copy;


    public JobObserver(WSPLGENJob job) {
        this.job  = job;
        this.len  = 0;
        this.copy = Boolean.parseBoolean(WSPLGEN_LOG_OUTPUT);

        patterns = new Pattern[STEPS.length];
    }


    protected void prepareRegexes() {
        for (int num = STEPS.length, i = 0; i < num; i++) {
            patterns[i] = Pattern.compile(STEPS[i], Pattern.DOTALL);
        }
    }


    public void setInputStream(InputStream in) {
        this.in = in;
    }


    public void run() {
        log.debug("Start observation...");
        prepareRegexes();

        try {
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(in, WSPLGEN_ENCODING));

            String line = null;

            while ((line = reader.readLine()) != null) {
                if (copy) {
                    log.debug(line);
                }

                update(line);
            }
        }
        catch (IOException ioe) {
            log.warn("Observation canceled: " + ioe.getMessage());
        }
    }


    protected void update(String s) {
        for (int num = patterns.length, i = 0; i < num; i++) {
            Matcher m = patterns[i].matcher(s);

            if (m.matches()) {
                job.getCallContext().addBackgroundMessage(
                    new CalculationMessage(num, i+1, s));

                log.info("Finished step " + (i+1) + " / " + num);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=5 fenc=utf-8 :
