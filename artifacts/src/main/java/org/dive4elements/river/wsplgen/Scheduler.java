/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.map.WSPLGENJob;


/**
 * The Scheduler is used to retrieve new WSPLGENJob. The incoming jobs are added
 * to a ScheduledThreadPoolExecutor. This thread pool has a number of worker
 * threads that processes the WSPLGENJobs. The number of worker threads can be
 * set using a System property <i>wsplgen.max.threads</i> ; its default value is
 * 1.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class Scheduler {

    private class FutureJob {
        public Future     future;
        public WSPLGENJob job;

        public FutureJob(Future future, WSPLGENJob job) {
            this.future = future;
            this.job    = job;
        }
    }

    public static final int MAX_WSPLGEN_PROCESSES =
        Integer.getInteger("wsplgen.max.threads", 1);


    protected ScheduledThreadPoolExecutor pool;
    protected Map<String, FutureJob> jobs;


    private static Scheduler INSTANCE;

    private static final Logger log = LogManager.getLogger(Scheduler.class);



    private Scheduler() {
        jobs = new HashMap<String, FutureJob>();
        pool = new ScheduledThreadPoolExecutor(MAX_WSPLGEN_PROCESSES);
    }


    public static Scheduler getInstance() {
        if (INSTANCE == null) {
            log.info("Create new WSPLGEN Scheduler...");

            INSTANCE = new Scheduler();
        }

        return INSTANCE;
    }


    public void addJob(final WSPLGENJob job) {
        synchronized (jobs) {
            WSPLGENFuture f = new WSPLGENFuture(new WSPLGENCallable(this, job));
            pool.execute(f);

            jobs.put(job.getArtifact().identifier(), new FutureJob(f, job));

            log.info("New WSPLGEN job successfully added.");
        }
    }


    /**
     * Cancels a running (or queued) job.
     *
     * @param jobId The id of the job (which is the identifier of an Artifact).
     */
    public void cancelJob(String jobId) {
        log.debug("Search job in queue: " + jobId);

        synchronized (jobs) {
            FutureJob fj = jobs.get(jobId);

            if (fj != null) {
                log.info("Try to cancel job: " + jobId);

                fj.future.cancel(true);

                removeJob(jobId);

                fj.job.getCallContext().afterBackground(
                    CallContext.STORE);

                log.info("Canceled job: " + jobId);
            }
        }
    }


    protected void removeJob(String id) {
        synchronized (jobs) {
            jobs.remove(id);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
