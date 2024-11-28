/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.Arrays;
import java.util.Map;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.model.Gauge;

import org.dive4elements.river.utils.RiverUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.dive4elements.river.exports.injector.InjectorConstants.PNP;

/**
 * A Facet that returns discharge curve data at a gauge
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class GaugeDischargeCurveFacet
extends      DataFacet
implements FacetTypes
{
    private static final Logger log =
        LogManager.getLogger(GaugeDischargeCurveFacet.class);

    public GaugeDischargeCurveFacet() {
    }

    public GaugeDischargeCurveFacet(String name, String description) {
        super(name, description);
    }

    @Override
    public Object getData(Artifact art, CallContext context) {
        return getWQKms(art, context);
    }

    protected WQKms getWQKms(Artifact art, CallContext context) {
        if (!(art instanceof D4EArtifact)) {
            log.warn("Invalid artifact type");
            return null;
        }

        D4EArtifact flys = (D4EArtifact)art;

        String river = flys.getDataAsString("river");

        Gauge gauge = RiverUtils.getReferenceGauge(flys);

        if (river == null || gauge == null) {
            log.warn("Unknown river or gauge");
            return null;
        }

        String name = gauge.getName();

        DischargeTables dt = new DischargeTables(river, name);

        Map<String, double [][]> map = dt.getValues();

        double [][] values = map.get(name);
        if (values == null) {
            return null;
        }
        double [] kms = new double[values[0].length];
        Arrays.fill(kms, gauge.getStation().doubleValue());

        Object pnpObject = context.getContextValue(PNP);
        if (!(pnpObject instanceof Number)) {
            RangeAccess access = new RangeAccess(flys);
            double km = Double.NaN;
            if (access.getLocations() != null &&
                access.getLocations().length > 0) {
                km = access.getLocations()[0];
            }
            Gauge g = access.getRiver().determineGaugeAtStation(km);
            if (g != null) {
                return new WQKms(
                    kms,
                    values[0],
                    transformToM(values[1], g.getDatum().doubleValue()));
            }
            return new WQKms(kms, values[0], values[1], name);
        }
        double[] ws = transformToM(
            values[1], ((Number)pnpObject).doubleValue());

        return new WQKms(kms, values[0], ws, name);
    }

    private double[] transformToM(double[] ws, double pnp) {
        double[] retVals = new double[ws.length];
        for (int i = 0; i < ws.length; i++) {
            retVals[i] = ws[i]/100 + pnp;
        }
        return retVals;
    }

    @Override
    public Facet deepCopy() {
        GaugeDischargeCurveFacet copy = new GaugeDischargeCurveFacet(
                this.name,
                this.description);
        copy.set(this);
        return copy;
    }
}
