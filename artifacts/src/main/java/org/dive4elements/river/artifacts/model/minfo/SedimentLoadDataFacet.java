/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

// TODO Aheinecke 15.8.2014
// This class was formerly known as SedimentLoadFacet.
// This class could be a base for the calculated sediment loads.
// If there is another facet for them remove this
// class altogether.
//
// The new SedimentLoadFacet is a new StaticFacet which only wraps
// static data from the database.

package org.dive4elements.river.artifacts.model.minfo;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DataFacet;

import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataResult.Fraction;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Facet to access various sediment loads. */
public class SedimentLoadDataFacet
extends DataFacet
{
    /** Very own log. */
    private static Logger log = LogManager.getLogger(SedimentLoadDataFacet.class);

    private static final String BASE_NAME = "sedimentload";

    public String fraction;

    public String period;

    public SedimentLoadDataFacet() {
    }

    public SedimentLoadDataFacet(int idx, String fraction_name, String unit,
        String periodString,
        ComputeType type, String stateId, String hash, CallContext context) {
        super(idx, /*name*/"", ""/*description*/, type, hash, stateId);

        this.fraction = fraction_name;
        this.period   = periodString;

        String typeUnit; /* Gnah someone should unify unit strings,... */
        String i18nUnit;
        if (unit != null && unit.equals("m3/a")) {
            typeUnit = "m3a";
            i18nUnit = "m\u00b3/a";
        } else {
            typeUnit = "ta";
            i18nUnit = "t/a";
        }

        name = BASE_NAME + "." + typeUnit + "." + fraction_name;

        CallMeta meta = context.getMeta();
        /* descriptions of real calculation results have to be distinguished
           from simple fractions from the database */
        String isCalculated = fraction_name.equals("total") ||
            fraction_name.equals("bed_load") ||
            fraction_name.equals("bed_load_susp_sand") ? "calc." : "";
        description = Resources.getMsg(
                meta,
                "facet.sedimentload." + isCalculated + fraction_name,
                new Object[] { periodString, i18nUnit });

        /* Is this necessary? */
        metaData.put("X", "chart.longitudinal.section.xaxis.label");
        metaData.put("Y", "");
    }

    @Override
    public Object getData(Artifact artifact, CallContext context) {

        D4EArtifact flys = (D4EArtifact) artifact;

        CalculationResult res = (CalculationResult)flys.compute(
            context, hash, stateId, type, false);
        Object payload = res.getData();

        if (!(payload instanceof SedimentLoadDataResult)) {
            log.error("Invalid result!");
        }

        SedimentLoadDataResult sdResult = (SedimentLoadDataResult) payload;

        List<Fraction> fractions = sdResult.getFractions();

        if (fractions.size() < index + 1) {
            log.error("No result for fraction '" + fraction +
                        "' and period '" + period + "'");
            return null;
        }

        return fractions.get(index).getData();

    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        SedimentLoadDataFacet copy = new SedimentLoadDataFacet();
        copy.set(this);
        copy.type = type;
        copy.hash = hash;
        copy.stateId = stateId;
        copy.fraction = fraction;
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
