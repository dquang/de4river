/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;


public class MorphologicWidthFacet
extends DataFacet
implements   FacetTypes
{
    private static Logger log = LogManager.getLogger(MorphologicWidthFacet.class);


    public MorphologicWidthFacet() {
    }

    public MorphologicWidthFacet(String facetName, String description) {
        super(facetName, description);
    }

    public MorphologicWidthFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.beddifference.yaxis.label.morph");
    }

    public Object getData(Artifact artifact, CallContext context) {

        D4EArtifact flys = (D4EArtifact) artifact;
        String width_id = flys.getDataAsString("width_id");

        MorphologicWidth width =
            MorphologicWidthFactory.getWidth(Integer.valueOf(width_id));

        return width;
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        MorphologicWidthFacet copy = new MorphologicWidthFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
