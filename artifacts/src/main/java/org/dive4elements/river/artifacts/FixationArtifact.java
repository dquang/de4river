/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.geom.Lines;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.fixings.FixRealizingResult;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.model.FastCrossSectionLine;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * The default fixation analysis artifact.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixationArtifact
extends      D4EArtifact
implements   WaterLineArtifact
{
    /** The log for this class. */
    private static Logger log = LogManager.getLogger(FixationArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "fixanalysis";

    /* FacetActivity for this artifact is registered in FixAnalysisCompute . */

    /**
     * The default constructor.
     */
    public FixationArtifact() {
        log.debug("ctor()");
    }

    /**
     * Returns the name of the concrete artifact.
     *
     * @return the name of the concrete artifact.
     */
    @Override
    public String getName() {
        return ARTIFACT_NAME;
    }

    /** Calculate waterlines against a cross section. */
    @Override
    public Lines.LineData getWaterLines(
        int                  facetIdx,
        FastCrossSectionLine      csl,
        double                      d,
        double                      w,
        CallContext           context
    ) {
        FixRealizingResult result = (FixRealizingResult)
            ((CalculationResult)this.compute(
                context, ComputeType.ADVANCE, false)).getData();

        WKms wkms = result.getWQKms()[facetIdx];

        double km = csl.getKm();

        // Find W at km.
        double wAtKm;

        wAtKm = StaticWKmsArtifact.getWAtKm(wkms, km);

        if (wAtKm == -1 || Double.isNaN(wAtKm)) {
            log.warn("Waterlevel at km " + km + " unknown.");
            return new Lines.LineData(new double[][] {{}}, 0d, 0d);
        }

        // This should be FixRealizationResult, which can be getWQKms()ed
        return Lines.createWaterLines(csl.getPoints(), wAtKm);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
