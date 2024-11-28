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
import org.dive4elements.artifactdatabase.state.StaticFacet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.DataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.w3c.dom.Document;


public class PorosityFacet
extends DataFacet
implements   FacetTypes, StaticFacet
{
    private static Logger log = LogManager.getLogger(PorosityFacet.class);

    private static final String NAME = "porosity";

    public PorosityFacet() {
    }

    public PorosityFacet(String facetName, String description) {
        super(facetName, description);
    }

    public PorosityFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.bedquality.yaxis.label.porosity");
    }

    public Object getData(Artifact artifact, CallContext context) {

        D4EArtifact flys = (D4EArtifact) artifact;
        String porosity_id = flys.getDataAsString("porosity_id");

        Porosity porosity =
            PorosityFactory.getPorosity(Integer.valueOf(porosity_id));

        return porosity.getAsArray();
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        PorosityFacet copy = new PorosityFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }

    @Override
    public void setup(Artifact artifact, Document data, CallMeta callMeta) {
        log.debug("setup");

        if (log.isDebugEnabled()) {
            log.debug(XMLUtils.toString(data));
        }

        String code = D4EArtifact.getDatacageIDValue(data);

        String[] split = code.split(";");
        String id = split[0];
        String desc = "";
        if (split.length >= 2) {
            desc = split[1];
        }

        if (code != null) {
            this.name = NAME;
            this.description = Resources.getMsg(
                callMeta,
                "facet.porosity",
                "Porosity",
                new Object[] { desc });
            D4EArtifact d4e = (D4EArtifact) artifact;
            d4e.addStringData("porosity_id", id);
        }
    }
}
