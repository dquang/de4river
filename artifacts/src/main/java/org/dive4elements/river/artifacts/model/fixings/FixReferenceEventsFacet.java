/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.river.utils.KMIndex;


/**
 * Facet to show W values for Q values at km for a date.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixReferenceEventsFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixReferenceEventsFacet.class);

    /** Trivial Constructor. */
    public FixReferenceEventsFacet() {
    }


    /**
     * @param name
     */
    public FixReferenceEventsFacet(int index, String name, String description) {
        super(index,
             name,
             description,
             ComputeType.ADVANCE,
             null,
             null);
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext (ignored).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("FixReferenceEventsFacet.getData");

        if (!(artifact instanceof D4EArtifact)) {
            log.debug("Not an instance of FixationArtifact.");
            return null;
        }

        D4EArtifact flys = (D4EArtifact)artifact;

        CalculationResult res =
            (CalculationResult) flys.compute(context,
                                             ComputeType.ADVANCE,
                                             false);

        FixResult result = (FixResult) res.getData();
        double currentKm = getCurrentKm(context);

        if (log.isDebugEnabled()) {
            log.debug("current km in FRE: " + currentKm);
        }

        KMIndex<QWD []> kmQWs = result.getReferenced();
        KMIndex.Entry<QWD []> kmQWsEntry = kmQWs.binarySearch(currentKm);
        if (kmQWsEntry != null) {
            int ndx = index & 255;
            for (QWD qwd: kmQWsEntry.getValue()) {
                if (qwd.getIndex() == ndx) {
                    return qwd;
                }
            }
        }
        return null;
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixReferenceEventsFacet deepCopy() {
        FixReferenceEventsFacet copy = new FixReferenceEventsFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
