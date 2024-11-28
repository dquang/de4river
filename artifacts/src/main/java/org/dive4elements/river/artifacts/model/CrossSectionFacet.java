/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.CrossSectionArtifact;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


/**
 * Trival Facet for Cross Sections (profiles).
 */
public class CrossSectionFacet
extends      BlackboardDataFacet
implements   FacetTypes {

    public static String BLACKBOARD_CS_MASTER_DATA
        = "crosssection.masterprofile.data";

    public static String BLACKBOARD_CS_PREV_KM
        = "crosssection.masterprofile.km.prev";

    public static String BLACKBOARD_CS_NEXT_KM
        = "crosssection.masterprofile.km.next";


    private static Logger log = LogManager.getLogger(CrossSectionFacet.class);

    protected ComputeType type;


    /** Trivial constructor, set (maybe localized) description. */
    public CrossSectionFacet(int idx, String description) {
        super(idx, CROSS_SECTION, description);
        type = ComputeType.ADVANCE;
    }


    /** Tell world we know about crosssection masters data and its index. */
    public List getStaticDataProviderKeys(Artifact art) {
        CrossSectionArtifact artifact = (CrossSectionArtifact) art;
        List keys = new ArrayList();
        if (artifact.isMaster()) {
            keys.add(BLACKBOARD_CS_MASTER_DATA);
            keys.add(BLACKBOARD_CS_PREV_KM);
            keys.add(BLACKBOARD_CS_NEXT_KM);
        }
        keys.add(artifact.identifier() + getIndex());
        keys.addAll(super.getStaticDataProviderKeys(art));
        return keys;
    }


    /**
     * Can provide the master cross section lines or its index.
     * @param artifact crosssection-artifact
     * @param key      will respond on BLACKBOARD_CS_MASTER_DATA
     * @param param    ignored
     * @param context  ignored
     * @return data from artifact (cross section master track).
     */
    public Object provideBlackboardData(Artifact artifact,
        Object key,
        Object param,
        CallContext context
    ) {
        CrossSectionArtifact crossSection = (CrossSectionArtifact) artifact;

        if (key.equals(BLACKBOARD_CS_MASTER_DATA)) {
            return crossSection.searchCrossSectionLine();
        }
        else if (key.equals(artifact.identifier() + getIndex())) {
            return getData(artifact, context);
        }
        else if (key.equals(BLACKBOARD_CS_NEXT_KM)) {
            return crossSection.getNextKm();
        }
        else if (key.equals(BLACKBOARD_CS_PREV_KM)) {
            return crossSection.getPrevKm();
        }
        else {
            Object obj = super.provideBlackboardData(artifact, key, param,
                context);
            if (obj == null) {
                log.warn("Cannot provide data for key: " + key);
            }
            return obj;
        }
    }


    /**
     * Gets Cross Section (profile).
     * @param art artifact to get data from.
     * @param context ignored
     */
    public Object getData(Artifact art, CallContext context) {
        log.debug("Get data for cross section");

        CrossSectionArtifact artifact = (CrossSectionArtifact)art;

        return artifact.getCrossSectionData();
    }


    /** Do a deep copy. */
    @Override
    public Facet deepCopy() {
        CrossSectionFacet copy = new CrossSectionFacet(
            this.index, this.description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
