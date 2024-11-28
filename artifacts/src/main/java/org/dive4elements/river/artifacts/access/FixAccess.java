/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Access for Fixation related data. */
public class FixAccess
extends      RangeAccess
{
    private static Logger log = LogManager.getLogger(FixAccess.class);

    protected Long start;
    protected Long end;

    protected Integer qSectorStart;
    protected Integer qSectorEnd;

    protected int [] events;

    protected Boolean preprocessing;

    protected String  function;

    public FixAccess(D4EArtifact artifact) {
        super(artifact);
    }

    public Long getStart() {

        if (start == null) {
            start = getLong("start");
        }

        if (log.isDebugEnabled()) {
            log.debug("start: '" + start + "'");
        }

        return start;
    }

    public Long getEnd() {

        if (end == null) {
            end = getLong("end");
        }

        if (log.isDebugEnabled()) {
            log.debug("end: '" + end + "'");
        }

        return end;
    }

    public Integer getQSectorStart() {

        if (qSectorStart == null) {
            qSectorStart = getInteger("q1");
        }

        if (log.isDebugEnabled()) {
            log.debug("q1: '" + qSectorStart + "'");
        }

        return qSectorStart;
    }

    public Integer getQSectorEnd() {

        if (qSectorEnd == null) {
            qSectorEnd = getInteger("q2");
        }

        if (log.isDebugEnabled()) {
            log.debug("q2: '" + qSectorEnd + "'");
        }

        return qSectorEnd;
    }

    public int [] getEvents() {
        if (events == null) {
            events = getIntArray("events");
        }
        if (log.isDebugEnabled() && events != null) {
            log.debug("events: " + Arrays.toString(events));
        }
        return events;
    }

    public Boolean getPreprocessing() {
        if (preprocessing == null) {
            preprocessing = getBoolean("preprocessing");
        }
        if (log.isDebugEnabled()) {
            log.debug("preprocessing: " + preprocessing);
        }
        return preprocessing;
    }

    public String getFunction() {
        if (function == null) {
            function = getString("function");
        }
        if (log.isDebugEnabled()) {
            log.debug("function: " + function);
        }
        return function;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
