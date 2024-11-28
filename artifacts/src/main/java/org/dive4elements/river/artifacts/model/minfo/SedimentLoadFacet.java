/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.StaticFacet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.artifacts.model.DataFacet;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;


/** Facet to access various sediment loads. */
public class SedimentLoadFacet
extends DataFacet
implements StaticFacet
{
    private static Logger log = LogManager.getLogger(SedimentLoadFacet.class);

    private static final String NAME = "sedimentload.ta";

    public SedimentLoadFacet() {
    }

    public SedimentLoadFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "");
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("get Data");
        D4EArtifact arti = (D4EArtifact) artifact;

        RiverAccess access = new RiverAccess(arti);
        String idStr = arti.getDataAsString("load_id");
        int id = Integer.valueOf(idStr);

        /* Get all the data from the river. This will be nicely cached. */
        SedimentLoadData allLoadData =
            SedimentLoadDataFactory.INSTANCE.getSedimentLoadData(
                access.getRiverName());

        /* Now lets get what we want */
        TDoubleArrayList xPos = new TDoubleArrayList();
        TDoubleArrayList yPos = new TDoubleArrayList();

        for (SedimentLoadData.Station sta: allLoadData.getStations()) {
            double value = sta.findValueByLoadId(id);
            xPos.add(sta.getStation());
            yPos.add(value);
        }

        return new double[][] {xPos.toNativeArray(), yPos.toNativeArray()};
    }

    @Override
    public void setup(Artifact artifact, Document data, CallMeta callMeta) {
        log.debug("setup");
        String code = D4EArtifact.getDatacageIDValue(data);
        String[] split = code.split(";");
        String idStr = split[0];

        String fraction = split.length >= 2 ? split[1] : "";
        String years    = split.length >= 3 ? split[2] : "";
        String kind     = split.length >= 4 ? split[3] : "";

        // Name has the pattern sedimentload.ta.<grainfraction_name>
        name = NAME + "." + fraction;
        log.debug("Created facet: " + name);

        description = Resources.getMsg(
                callMeta,
                "facet.sedimentload." + fraction,
                new Object[] { years, "t/a" });

        if (kind.equals("official")) {
            String descPrefix = Resources.getMsg(
                    callMeta,
                    "facet.sedimentload.prefix.offcial");
            description = descPrefix + " " + description;
        }

        ((D4EArtifact)artifact).addStringData("load_id", idStr);
    }


    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        SedimentLoadFacet copy = new SedimentLoadFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
