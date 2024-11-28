/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.access.BedHeightAccess;
import org.dive4elements.river.artifacts.model.BlackboardDataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.BedHeightValue;

public class BedHeightFacet
extends      BlackboardDataFacet
implements   FacetTypes {

    private static final Logger log = LogManager.getLogger(BedHeightFacet.class);

    public BedHeightFacet(String name, String description) {
        this.name = name;
        this.description = description;
        this.index = 0;
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.bedheight_middle.section.yaxis.label");
    }

    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext (ignored).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        BedHeightAccess access = new BedHeightAccess((D4EArtifact)artifact);
        BedHeight single = BedHeight.getBedHeightById(access.getHeightId());
        List<BedHeightValue> bedheightValues =
            BedHeightValue.getBedHeightValues(
                single,
                access.getFrom(true),
                access.getTo(true));
        double[][] values = new double[2][bedheightValues.size()];
        int i = 0;
        for (BedHeightValue bedheightValue : bedheightValues) {
            values[0][i] = bedheightValue.getStation();
            values[1][i] = bedheightValue.getHeight();
            i++;
        }

        this.addMetaData(Resources.getMsg(
                context.getMeta(),
                "meta.bedheight.cur.elevation"),
            single.getCurElevationModel().getName());
        if (single.getOldElevationModel() != null) {
            this.addMetaData(Resources.getMsg(
                    context.getMeta(),
                    "meta.bedheight.old.elevation"),
                single.getOldElevationModel().getName());
        }
        this.addMetaData(Resources.getMsg(
                context.getMeta(),
                "meta.bedheight.river.elevation"),
            access.getRiver().getWstUnit().getName());

        return values;
    }

    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public BedHeightFacet deepCopy() {
        BedHeightFacet copy = new BedHeightFacet(name, description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
