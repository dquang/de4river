/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.model.map.WSPLGENCalculation;
import org.dive4elements.river.artifacts.model.map.WSPLGENJob;


public class ProblemObserver extends JobObserver {

    private static Logger log = LogManager.getLogger(ProblemObserver.class);


    public static final Pattern WSPLGEN_ERROR_START = Pattern.compile(
        ".*->(.*Fehler)(\\s*\\((\\d+)\\).*)*",
        Pattern.DOTALL);

    public static final Pattern WSPLGEN_ERROR_END = Pattern.compile(
        ".*<-(.*Fehler).*",
        Pattern.DOTALL);

    public static final Pattern WSPLGEN_WARNING_START = Pattern.compile(
        ".*->Warnung\\s*\\((\\d+)\\).*",
        Pattern.DOTALL);

    public static final Pattern WSPLGEN_WARNING_END = Pattern.compile(
        ".*<-Warnung .*",
        Pattern.DOTALL);


    protected int error;
    protected int warning;

    protected WSPLGENCalculation calculation;


    public ProblemObserver(WSPLGENJob job) {
        super(job);
        error       = -1;
        warning     = -1;
        calculation = job.getCalculation();
    }


    public void run() {
        log.debug("Start observation...");

        super.run();
    }


    @Override
    protected void prepareRegexes() {
        // do nothing
    }


    @Override
    protected void update(String log) {
        Matcher startError = WSPLGEN_ERROR_START.matcher(log);
        if (startError.matches()) {
            if (startError.groupCount() >= 2) {
                String tmp = startError.group(3);

                if (tmp != null && tmp.length() > 0) {
                    error = Integer.parseInt(tmp);
                }
                else error = 1;
            }
            else {
                error = 1;
            }

            return;
        }

        Matcher endError = WSPLGEN_ERROR_END.matcher(log);
        if (endError.matches()) {
            error = -1;
        }

        if (error > 0) {
            calculation.addError(new Integer(error), log);
        }

        Matcher startWarning = WSPLGEN_WARNING_START.matcher(log);
        if (startWarning.matches()) {
            warning = Integer.parseInt(startWarning.group(1));
            return;
        }

        Matcher endWarning = WSPLGEN_WARNING_END.matcher(log);
        if (endWarning.matches()) {
            warning = -1;
        }

        if (warning > 0) {
            calculation.addWarning(new Integer(warning), log);
        }
    }


    public int numErrors() {
        return calculation.numErrors();
    }


    public int numWarnings() {
        return calculation.numWarnings();
    }
}
// vim:set ts=4 sw=4 si et sta sts=5 fenc=utf-8 :
