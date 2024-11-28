/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.DataProvider;

import org.dive4elements.river.artifacts.AreaArtifact;


/**
 * Trival Facet for areas.
 * Note that this Facet comes in two "types" (names):
 *  <ul>
 *    <li>CROSS_SECTION_AREA (cross_section.area) and</li>
 *    <li>LONGITUDINAL_SECTION_AREA (longitudinal.area</li>
 *  </ul>
 * This is to support different diagram types without being painted in both
 * at the same time. The name has to be given when constructing.
 */
public class AreaFacet
extends      DefaultFacet
{
    private static Logger log = LogManager.getLogger(AreaFacet.class);

    /**
     * Constructor, set (maybe localized) description and name.
     * @param idx Index given when querying artifact for data.
     * @param name important to discern areas in different diagram types.
     */
    public AreaFacet(int idx, String name, String description) {
        super(idx, name, description);
    }


    /**
     * Gets Cross Section (profile).
     * @param art artifact to get data from.
     * @param context ignored
     */
    public Object getData(Artifact art, CallContext context) {
        log.debug("Get data for area.");

        // Get information from artifact about which
        // info to grab from blackboard.
        //
        // All compatible facets should provide their data
        // under the key (Artifact-UUID + Facet-Index).
        AreaArtifact artifact = (AreaArtifact) art;
        Object lowerData      = null;
        Object upperData      = null;
        String lowerFacetName = null;
        String upperFacetName = null;

        List<DataProvider> providers = context.
            getDataProvider(artifact.getLowerDPKey());
        if (providers.size() < 1) {
            log.warn("No 'lower' provider given for area [" +
                artifact.getLowerDPKey() + "]");
        }
        else {
            lowerData = providers.get(0).provideData(
                artifact.getLowerDPKey(), null, context);
            log.debug("'Lower' data provider key for area [" +
                artifact.getLowerDPKey() + "]");
            lowerFacetName = artifact.getLowerDPKey().split(":")[1];
        }

        providers = context.getDataProvider(artifact.getUpperDPKey());
        if (providers.size() < 1) {
            log.warn("No 'upper' provider given for area [" +
                artifact.getUpperDPKey() + "]");
        }
        else {
            upperData = providers.get(0).provideData(
                artifact.getUpperDPKey(), null, context);
            log.debug("'Upper' data provider key for area [" +
                artifact.getUpperDPKey() + "]");
            upperFacetName = artifact.getUpperDPKey().split(":")[1];
        }

        if (upperData == null && lowerData == null) {
            log.warn("Not given 'upper' and 'lower' for area");
        }

        return new Data(upperFacetName, lowerFacetName, lowerData, upperData,
            Boolean.valueOf(artifact.getPaintBetween()));
    }


    /** Do a deep copy. */
    @Override
    public Facet deepCopy() {
        AreaFacet copy = new AreaFacet(this.index, this.name, this.description);
        copy.set(this);
        return copy;
    }

    /** Result data bundle. */
    public static class Data {
        protected String  upperFacetName;
        protected String  lowerFacetName;
        protected Object  upperData;
        protected Object  lowerData;
        protected boolean doPaintBetween;

        /** Create a new result data bundle. */
        public Data(
            String upperFacetName,
            String lowerFacetName,
            Object low,
            Object up,
            boolean between
        ) {
            this.lowerData      = low;
            this.upperData      = up;
            this.doPaintBetween = between;
            this.lowerFacetName = lowerFacetName;
            this.upperFacetName = upperFacetName;
        }

        public String getLowerFacetName() {
            return this.lowerFacetName;
        }

        public String getUpperFacetName() {
            return this.upperFacetName;
        }

        /** Get data for 'upper' curve of area. */
        public Object getUpperData() {
            return this.upperData;
        }

        /** Get data for 'lower' curve of area. */
        public Object getLowerData() {
            return this.lowerData;
        }

        /** Whether to fill whole area between (in contrast to 'under'
         *  or 'over'). */
        public boolean doPaintBetween() {
            return this.doPaintBetween;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
