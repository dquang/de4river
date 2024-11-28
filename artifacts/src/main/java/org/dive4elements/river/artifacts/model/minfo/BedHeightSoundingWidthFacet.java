/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.List;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.BedHeightAccess;
import org.dive4elements.river.artifacts.model.BlackboardDataFacet;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.BedHeightValue;


public class BedHeightSoundingWidthFacet
extends BlackboardDataFacet
implements FacetTypes
{
    public BedHeightSoundingWidthFacet(String name, String description) {
        this.name = name;
        this.description = description;
        this.index = 0;
        this.metaData.put("X", "chart.longitudinal.section.xaxis.label");
        this.metaData.put("Y", "chart.bedheight_middle.sounding.yaxis.label");
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
        /* Former doc (from BedHeightAccess):
         * Return a {@link List} of {@link BedHeightValue}s
         * at the range of the artifact
         * @return List of {@link BedHeightValue}s */
        BedHeight single = BedHeight.getBedHeightById(
                access.getHeightId());
        List<BedHeightValue> bedheightValues =
            BedHeightValue.getBedHeightValues(
                single);
        double[][] values = new double[2][bedheightValues.size()];
        int i = 0;
        for (BedHeightValue bedheightValue : bedheightValues) {
            values[0][i] = bedheightValue.getStation();
            values[1][i] = bedheightValue.getSoundingWidth() != null
                ? bedheightValue.getSoundingWidth() : Double.NaN;
            i++;
        }
        return values;
    }

    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public BedHeightSoundingWidthFacet deepCopy() {
        BedHeightSoundingWidthFacet copy =
            new BedHeightSoundingWidthFacet(name, description);
        copy.set(this);
        return copy;
    }
}
