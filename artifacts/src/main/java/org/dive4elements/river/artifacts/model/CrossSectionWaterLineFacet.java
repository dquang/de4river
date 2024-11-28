/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.DataProvider;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.WaterLineArtifact;

import org.dive4elements.river.model.FastCrossSectionLine;

import org.dive4elements.river.artifacts.geom.Lines;


/**
 * Facet for Waterlines in Cross Sections.
 */
public class CrossSectionWaterLineFacet
extends      BlackboardDataFacet
implements   FacetTypes {

    /** Private log to use. */
    private static Logger log =
        LogManager.getLogger(CrossSectionWaterLineFacet.class);


    /** Trivial constructor, set (maybe localized) description. */
    public CrossSectionWaterLineFacet(int idx, String description) {
        super(idx, CROSS_SECTION_WATER_LINE, description);
    }


    /**
     * Trivial constructor, set (maybe localized) description.
     * @param idx Index of this facet.
     * @param name 'type' of this facet.
     * @param description (maybe) localized user-visible description.
     */
    public CrossSectionWaterLineFacet(
        int idx,
        String name,
        String description
    ) {
        super(idx, name, description);
    }


    /**
     * Gets waterline (crossed with cross section) of waterlevel.
     */
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("Get data for cross section water line");

        List<DataProvider> providers = context.
            getDataProvider(CrossSectionFacet.BLACKBOARD_CS_MASTER_DATA);
        if (providers.size() < 1) {
            log.warn("Could not find Cross-Section data provider.");
            return new Lines.LineData(new double[][] {}, 0d, 0d);
        }

        Object crossSection = providers.get(0)
            .provideData(CrossSectionFacet.BLACKBOARD_CS_MASTER_DATA,
                null, context);
        Object nextKm = providers.get(0).
            provideData(CrossSectionFacet.BLACKBOARD_CS_NEXT_KM, null, context);
        Object prevKm = providers.get(0).
            provideData(CrossSectionFacet.BLACKBOARD_CS_PREV_KM, null, context);
        if (prevKm == null)
            prevKm = new Double(-1d);
        if (nextKm == null)
            nextKm = new Double(-1d);

        if (!(artifact instanceof WaterLineArtifact)) {
            log.error("CrossSectionWaterLineFacet needs WaterLineArtifact");
            return new Lines.LineData(new double[][] {}, 0d,0d);
        }
        WaterLineArtifact lineArtifact = (WaterLineArtifact) artifact;

        if (crossSection != null) {
            return lineArtifact.getWaterLines(this.getIndex(),
                (FastCrossSectionLine) crossSection, (Double) nextKm,
                (Double) prevKm, context);
        }
        else {
            return new Lines.LineData(new double[][] {}, 0d,0d);
        }
    }


    /** Do a deep copy. */
    @Override
    public Facet deepCopy() {
        CrossSectionWaterLineFacet copy = new CrossSectionWaterLineFacet(
            this.getIndex(),
            this.description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
