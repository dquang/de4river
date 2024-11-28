/* Copyright (C) 2011, 2012, 2013, 2015 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import java.util.List;

import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.states.DistanceSelect;

import org.dive4elements.river.artifacts.model.minfo.QualityMeasurementFactory;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.data.StateData;

/** Extended Distance Select with default values appropiate for BedQuality */
public class BedQualityDistanceSelect extends DistanceSelect {

    private static Logger log = LogManager.getLogger(
        BedQualityDistanceSelect.class);

    /**
     * The default constructor that initializes an empty State object.
     */
    public BedQualityDistanceSelect() {
    }

    @Override
    protected Element createData(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        StateData   data,
        CallContext context)
    {
        Element ele = super.createData(cr, artifact, data, context);
        if (!data.getName().equals("ld_from")
            && !data.getName().equals("ld_to")) {
            return ele;
        }

        RiverAccess access = new RiverAccess((D4EArtifact)artifact);
        String river = access.getRiverName();
        double [] minMax = access.getRiver().determineMinMaxDistance();

        List<Double> bKms = QualityMeasurementFactory.getBedMeasurements(
            river,
            minMax[0],
            minMax[1]
        ).getKms();

        List<Double> blKms = QualityMeasurementFactory.getBedloadMeasurements(
            river,
            minMax[0],
            minMax[1]
        ).getKms();

        if (bKms.isEmpty() || blKms.isEmpty()) {
            log.warn("Not all data found for river '" + river + "'");
            return ele;
        }

        double start = Math.min(bKms.get(0), blKms.get(0));
        double end = Math.max(
            bKms.get(bKms.size()-1), blKms.get(blKms.size()-1));

        if (data.getName().equals("ld_from")) {
            cr.addAttr(ele, "defaultLabel", data.getName(), true);
            cr.addAttr(ele, "defaultValue", Double.toString(start), true);
        } else if (data.getName().equals("ld_to")) {
            cr.addAttr(ele, "defaultLabel", data.getName(), true);
            cr.addAttr(ele, "defaultValue", Double.toString(end), true);
        }

        return ele;
    }
}
