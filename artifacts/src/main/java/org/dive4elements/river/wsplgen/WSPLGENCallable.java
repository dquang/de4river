/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.map.WSPLGENJob;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * A Callable that is used to start and observe an external Process for WSPLGEN.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WSPLGENCallable implements Callable {

    public static final String WSPLGEN_PARAMETER_FILE =
        "wsplgen.par";

    public static final String WSPLGEN_BIN_PATH =
        System.getProperty("wsplgen.bin.path");


    private Logger log = LogManager.getLogger(WSPLGENCallable.class);

    private Process process;

    protected Scheduler scheduler;

    protected WSPLGENJob job;

    protected JobObserver     logObserver;
    protected ProblemObserver errorObserver;


    public WSPLGENCallable(Scheduler scheduler, WSPLGENJob job) {
        this.scheduler     = scheduler;
        this.job           = job;
        this.logObserver   = new JobObserver(job);
        this.errorObserver = new ProblemObserver(job);
    }


    @Override
    public WSPLGENJob call() {
        File dir       = job.getWorkingDir();
        File parameter = new File(dir, WSPLGEN_PARAMETER_FILE);

        String[] args = new String[] {
            WSPLGEN_BIN_PATH,
            "-PAR=\"" + parameter.getAbsolutePath() + "\""
        };

        execute(args, dir);

        return job;
    }


    protected void execute(String[] args, File dir) {
        log.info("Start JobExecutor for artifact: " + dir.getName());

        try {
            synchronized (this) {
                process = Runtime.getRuntime().exec(args, null, dir);

                logObserver.setInputStream(process.getInputStream());
                errorObserver.setInputStream(process.getErrorStream());

                logObserver.start();
                errorObserver.start();

                try {
                    process.waitFor();
                }
                catch (InterruptedException ie) {
                    log.warn("WSPLGEN job interrupted: " + ie.getMessage());
                }

                try {
                    logObserver.join();
                    errorObserver.join();
                }
                catch (InterruptedException iee) { /* do nothing */ }

                int exitValue = process.exitValue();
                if (exitValue < 2) {
                    log.info("WSPLGEN exit value: " + exitValue);
                }
                else {
                    log.error("WSPLGEN exit value: " + exitValue);
                }
                log.info(
                    "WSPLGEN throw " +
                    errorObserver.numErrors() + " errors.");
                log.info(
                    "WSPLGEN throw " +
                    errorObserver.numWarnings() + " warnings.");

                if (exitValue < 2 && errorObserver.numErrors() == 0) {
                    FacetCreator fc = job.getFacetCreator();
                    fc.createWSPLGENFacet();
                    fc.finish();
                }

                job.getCallContext().afterBackground(CallContext.STORE);

                scheduler.removeJob(getJob().getArtifact().identifier());

                return;
            }
        }
        catch (SecurityException se) {
            log.error(se);
        }
        catch (IOException ioe) {
            log.error(ioe);
        }
        catch (NullPointerException npe) {
            log.error(npe, npe);
        }
        catch (IndexOutOfBoundsException ioobe) {
            log.error(ioobe, ioobe);
        }
    }


    public void cancelWSPLGEN() {
        if (process != null) {
            log.debug("Cancel running WSPLGEN process.");
            process.destroy();
        }
    }


    public WSPLGENJob getJob() {
        return job;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
