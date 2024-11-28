/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.utils.KMIndex;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Facet to show average W values for Q sectors.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixLongitudinalReferenceFacet
extends      DataFacet
implements   FacetTypes {

    /** House log. */
    private static Logger log = LogManager.getLogger(
        FixLongitudinalReferenceFacet.class);

    /** Trivial Constructor. */
    public FixLongitudinalReferenceFacet() {
    }


    public FixLongitudinalReferenceFacet(
        int ndx,
        String name,
        String description)
    {
        super(
            ndx,
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
     * @param context  the CallContext.
     *
     * @return the data as KMIndex.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("FixLongitudinalReferenceFacet.getData");

        if (artifact instanceof D4EArtifact) {
            D4EArtifact flys = (D4EArtifact)artifact;

            CalculationResult res =
                (CalculationResult) flys.compute(context,
                                                 ComputeType.ADVANCE,
                                                 false);

            FixAnalysisResult result = (FixAnalysisResult) res.getData();

            KMIndex<QWD []> kmReference = result.getReferenced();

            if (kmReference == null) {
                log.warn("No references found.");
                return null;
            }

            int qwdNdx = index & 255;
            KMIndex<QWD> resReference =
                    new KMIndex<QWD>();
            for (KMIndex.Entry<QWD[]> entry: kmReference) {
                QWD[] qwds = entry.getValue();
                for(int i = 0; i < qwds.length; i++) {
                    if(qwds[i].getIndex() == qwdNdx) {
                        resReference.add(entry.getKm(), qwds[i]);
                    }
                }
            }
            return resReference;
        }
        else {
            log.warn("Artifact is no instance of D4EArtifact.");
            return null;
        }
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public FixLongitudinalReferenceFacet deepCopy() {
        FixLongitudinalReferenceFacet copy =
            new FixLongitudinalReferenceFacet();
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
