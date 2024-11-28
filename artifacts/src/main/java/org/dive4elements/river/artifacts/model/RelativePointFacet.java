/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.List;
import java.awt.geom.Point2D;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.DataProvider;

import org.dive4elements.river.artifacts.StaticWKmsArtifact;
import org.dive4elements.river.artifacts.StaticWQKmsArtifact;
import org.dive4elements.river.artifacts.math.Linear;

/**
 * Facet to access a point.
 */
public class RelativePointFacet
extends      BlackboardDataFacet
implements   FacetTypes {

    /** Own log. */
    private static Logger log = LogManager.getLogger(RelativePointFacet.class);

    /** Trivial Constructor. */
    public RelativePointFacet(String description) {
        this(RELATIVE_POINT, description);
    }


    public RelativePointFacet(String name, String description) {
        this.name        = name;
        this.description = description;
        this.index       = 0;
    }


    protected Point2D calculateDurationCurvePoint(CallContext context,
        WKms wKms)
    {
        // TODO here and in reference curve calc: Do warn if more than 1
        // provider found or (way better) handle it.
        Object wqdays = null;
        double km     = 0d;
        List<DataProvider> providers = context.
            getDataProvider(DurationCurveFacet.BB_DURATIONCURVE);
        if (providers.size() < 1) {
            log.warn("Could not find durationcurve data provider.");
        }
        else {
            wqdays = providers.get(0).provideData(
                DurationCurveFacet.BB_DURATIONCURVE,
                null,
                context);
        }
        List<DataProvider> kmproviders = context.
            getDataProvider(DurationCurveFacet.BB_DURATIONCURVE_KM);
        if (kmproviders.size() < 1) {
            log.warn("Could not find durationcurve.km data provider.");
        }
        else {
            log.debug("Found durationcurve.km data provider.");
            String dckm = providers.get(0).provideData(
                DurationCurveFacet.BB_DURATIONCURVE_KM,
                null,
                context).toString();
            km = Double.valueOf(dckm);
        }

        if (wqdays != null) {
            // Which W at this km?
            double w = StaticWKmsArtifact.getWAtKmLin(wKms, km);
            if (w == -1) {
                log.warn("w is -1, already bad sign!");
            }
            // Where is this W passed by in the wq-curve?
            WQDay wqday = (WQDay) wqdays;
            // Doing a linear Day Of KM.
            int idx = 0;
            boolean wIncreases = wqday.getW(0) < wqday.getW(wqday.size()-1);
            if (wIncreases) {
                while (idx < wqday.size() && wqday.getW(idx) < w) {
                    idx++;
                }
            }
            else {
                idx = wqday.size() -1;
                while (idx > 0 && wqday.getW(idx) > w) {
                    idx--;
                }
            }

            double day = 0d;
            int mod = (wIncreases) ? -1 : +1;
            if (idx != 0 && idx <= wqday.size()-1) {
                day = Linear.linear(w, wqday.getW(idx+mod), wqday.getW(idx),
                    wqday.getDay(idx+mod), wqday.getDay(idx));
            }

            return new Point2D.Double((double) day, w);
        }
        log.warn("not wqkms / w / day found");
        // TODO better signal failure.
        return new Point2D.Double(0d, 0d);
    }


    /**
     * Calculate a reference curve point, that is, a point made of
     * the Ws from start and end km param of the reference curve.
     */
    public Point2D calculateReferenceCurvePoint(CallContext context,
        WKms wKms) {

        List<DataProvider> providers = context.
            getDataProvider(ReferenceCurveFacet.BB_REFERENCECURVE_STARTKM);
        if (providers.size() < 1) {
            log.warn("Could not find reference curve startkm data provider.");
        }

        Double start = (Double) providers.get(0).
            provideData(
                ReferenceCurveFacet.BB_REFERENCECURVE_STARTKM, null, context);

        providers = context.
            getDataProvider(ReferenceCurveFacet.BB_REFERENCECURVE_ENDKMS);
        if (providers.size() < 1) {
            log.warn("Could not find reference curve endkms data provider.");
        }
        double[] ends = (double[]) providers.get(0).
            provideData(
                ReferenceCurveFacet.BB_REFERENCECURVE_ENDKMS, null, context);

        log.debug("Got s " + start + " e " + ends);

        double startW = StaticWKmsArtifact.getWAtKmLin(
            wKms, start.doubleValue());
        // TODO handle multiple ends.
        double endW = StaticWKmsArtifact.getWAtKmLin(wKms, ends[0]);
        log.debug("Gotw s " + startW + " e " + endW);
        return new Point2D.Double(startW, endW);
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
        WKms wKms = null;
        if (artifact instanceof StaticWKmsArtifact) {
            wKms = ((StaticWKmsArtifact) artifact).getWKms(0);
        }
        else if (artifact instanceof StaticWQKmsArtifact) {
            wKms = ((StaticWQKmsArtifact) artifact).getWQKms();
        }
        else {
            log.error("Cannot handle Artifact to create relative point.");
            return null;
        }

        // Find out whether we live in a duration curve context, there we would
        // provide only a single point.

        if (context.getDataProvider(
            DurationCurveFacet.BB_DURATIONCURVE_KM).size() > 0) {
            return calculateDurationCurvePoint(context, wKms);
        }
        else if (context.getDataProvider(
            ReferenceCurveFacet.BB_REFERENCECURVE_STARTKM).size() > 0) {
            return calculateReferenceCurvePoint(context, wKms);
        }

        // TODO better signal failure.
        return new Point2D.Double(0d, 0d);
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public RelativePointFacet deepCopy() {
        RelativePointFacet copy = new RelativePointFacet(description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
