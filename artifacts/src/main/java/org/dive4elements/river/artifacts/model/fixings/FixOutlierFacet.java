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
 * Facet to show the outliers in a fix calculation.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixOutlierFacet
extends      FixingsFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(FixOutlierFacet.class);

    /** Trivial Constructor. */
    public FixOutlierFacet() {
    }


    /**
     * @param name
     */
    public FixOutlierFacet(String name, String description) {
        super(0, name, description, ComputeType.ADVANCE, null, null);
    }

    public FixOutlierFacet(int index, String name, String description) {
        super(index, name, description, ComputeType.ADVANCE, null, null);
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact; needs to be a D4EArtifact.
     * @param context  the CallContext; required to retrieve the value of
     * <i>currentKm</i>.
     *
     * @return an array of QW objects or null.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("FixOutlierFacet.getData");

        if (artifact instanceof D4EArtifact) {
            D4EArtifact flys = (D4EArtifact)artifact;

            CalculationResult res =
                (CalculationResult) flys.compute(context,
                                                 ComputeType.ADVANCE,
                                                 false);

            FixResult result = (FixResult) res.getData();
            double currentKm = getCurrentKm(context);

            KMIndex<QWI []>       kmQWs    = result.getOutliers();
            KMIndex.Entry<QWI []> qwsEntry = kmQWs.binarySearch(currentKm);

            QWI [] qws = null;
            if (qwsEntry != null) {
                qws = qwsEntry.getValue();

                if (log.isDebugEnabled()) {
                    log.debug("Found " + (qws != null ? qws.length : 0)
                        + " KMIndex.Entry for km " + currentKm);
                }
            }
            else {
                log.debug("Found no KMIndex.Entry for km " + currentKm);
            }

            return qws;
        }

        log.warn("Not an instance of D4EArtifact.");
        return null;
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixOutlierFacet deepCopy() {
        FixOutlierFacet copy = new FixOutlierFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
