/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.Segment;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Fix-Realizing (Volmer/Ausgelagerte Wasserspiegellage) access. */
public class FixRealizingAccess
extends      FixAccess
{
    private static Logger log = LogManager.getLogger(FixRealizingAccess.class);

    protected Boolean isQ;

    protected List<Segment> segments;

    public FixRealizingAccess(D4EArtifact artifact) {
        super(artifact);
    }

    public Boolean isQ() {
        if (isQ == null) {
            isQ = getBoolean("wq_isq");
        }

        if (log.isDebugEnabled()) {
            log.debug("isQ: " + isQ);
        }

        return isQ;
    }

    public List<Segment> getSegments() {
        if (segments == null) {
            String segmentsS = getString("wq_values");
            if (segmentsS != null) {
                segments = Segment.parseSegments(segmentsS);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("segments: " + segments);
        }

        return segments;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
