/* Copyright (C) 2012, 2012, 2013 by Bundesanstalt für Gewässerkunde
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
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.artifacts.model.DataFacet;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.model.SedimentLoadLS;
import org.dive4elements.river.model.SedimentLoadLSValue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;


/** Facet to access sediment loads for longitudinal sections.
 * This facet differs from the sedimentloadfacet in that it
 * handles values from the sedimentload_ls table in the backend db.
 *
 * The sedimentload facet uses the internal sedimentload data model
 * to work with measurement stations instead.
 */
public class SedimentLoadLSFacet
extends DataFacet
implements StaticFacet
{
    private static Logger log = LogManager.getLogger(SedimentLoadLSFacet.class);

    /* Aheinecke we probably need to get the kind and split this up here
     * in some way */
    private static final String NAME = "sedimentload.ls";

    public SedimentLoadLSFacet() {
    }

    public SedimentLoadLSFacet(int idx, String name, String description,
        ComputeType type, String stateId, String hash) {
        super(idx, name, description, type, hash, stateId);
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "");
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {
        log.debug("get Data");
        D4EArtifact arti = (D4EArtifact) artifact;

        String idStr = arti.getDataAsString("load_id");
        int id = Integer.valueOf(idStr);

        SedimentLoadLS theLoad = SedimentLoadLS.getSedimentLoadById(id);

        if (theLoad == null) {
            log.error("No load found for id: " + idStr);
            return null;
        }

        /* Now lets get what we want */
        TDoubleArrayList xPos = new TDoubleArrayList();
        TDoubleArrayList yPos = new TDoubleArrayList();

        for (SedimentLoadLSValue val: theLoad.getSedimentLoadLSValues()) {
            double value = val.getValue();
            if (Double.isNaN(value)) {
                continue;
            }
            xPos.add(val.getStation());
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
        String years = split.length >= 2 ? split[1] : "";
        String kind  = split.length >= 3 ? split[2] : "";

        int id = Integer.valueOf(idStr);
        SedimentLoadLS theLoad = SedimentLoadLS.getSedimentLoadById(id);
        if (theLoad == null) {
            log.error("No load found for id: " + idStr);
            return;
        }
        log.debug("Setting up SedimentLoadLSFacet for id: " + id);
        if (theLoad.getGrainFraction() != null) {
            log.debug("GrainFraction: " + theLoad.getGrainFraction().getName());
        }
        log.debug("Kind: " + theLoad.getKind());
        log.debug("Unit: " + theLoad.getUnit().getName());

        // Name has the pattern sedimentload.ls.<unit>.<grainfraction_name>
        name = NAME;

        String i18nUnit = "";

        if (theLoad.getUnit().getName().equals("m3/a")) {
            /* unit_id is NOT NULL */
            name += ".m3a";
            i18nUnit = "m\u00b3/a";
        } else {
            name += ".ta";
            i18nUnit = "t/a";
        }

        String gfName;
        if (theLoad.getGrainFraction() != null) {
            gfName =  theLoad.getGrainFraction().getName();
        } else {
            gfName = "unknown";
        }
        name += "." + gfName;

        log.debug("Created facet: " + name);

        description = Resources.getMsg(
                callMeta,
                "facet.sedimentload." + gfName,
                new Object[] { years, i18nUnit });

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
        SedimentLoadLSFacet copy = new SedimentLoadLSFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
