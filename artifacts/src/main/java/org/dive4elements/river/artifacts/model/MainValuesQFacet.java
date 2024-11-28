/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.DataProvider;

import org.dive4elements.artifactdatabase.state.DefaultFacet;

import org.dive4elements.river.artifacts.MainValuesArtifact;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.jfree.StickyAxisAnnotation;
import org.dive4elements.river.jfree.StickyAxisAnnotation.SimpleAxis;

import org.dive4elements.river.exports.DurationCurveGenerator;
import org.dive4elements.river.exports.fixings.FixChartGenerator;

import static org.dive4elements.river.exports.injector.InjectorConstants.PNP;


/**
 * Facet to show Main Q Values.
 * TODO Join with W implementation.
 */
public class MainValuesQFacet
extends      DefaultFacet
implements   FacetTypes {

    /** Own log. */
    private static Logger log = LogManager.getLogger(MainValuesQFacet.class);

    /** Trivial Constructor. */
    public MainValuesQFacet(String name, String description) {
        this.description = description;
        this.name        = name;
        this.index       = 0;
    }

    /**
     * Set the hit-point in Q where a line drawn from the axis would hit the
     * curve in WQDay (if hit).
     * Employ linear interpolation.
     */
    protected static void setHitPoint(
        WQDay wqday,
        StickyAxisAnnotation annotation
    ) {

        float q = annotation.getPos();
        Double day = wqday.interpolateDayByQ(q);

        if (day != null) {
            annotation.setHitPoint(day.floatValue());
        }
        else if (log.isDebugEnabled()) {
            log.debug("StickyAnnotation does not hit wqday curve: " + q);
        }
    }


    /**
     * Returns the data this facet requires.
     *
     * @param artifact the owner artifact.
     * @param context  the CallContext (can be used to find out if in
     *                 navigable fixation-setting, or durationcurve).
     *
     * @return the data.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        MainValuesArtifact mvArtifact = (MainValuesArtifact) artifact;

        List<NamedDouble>          qs = mvArtifact.getMainValuesQ(
            context.getContextValue(PNP));
        List<StickyAxisAnnotation> xy = new ArrayList<StickyAxisAnnotation>();

        // Find whether a duration curve is on the blackboard.
        WQDay wqdays = null;
        List<DataProvider> providers = context.
            getDataProvider(DurationCurveFacet.BB_DURATIONCURVE);
        if (providers.size() < 1) {
            // Do we have a current km in context?
            // If so, we are likely fetching data for a navigable
            // diagram (i.e. in fixation branch).
            Object xkm = context.getContextValue(FixChartGenerator.CURRENT_KM);
            if (xkm != null) {
                Double ckm = (Double)xkm;
                qs = mvArtifact.getMainValuesQ(
                    new double[] {ckm},
                    context.getContextValue(PNP));
            }
        }
        else {
            wqdays = (WQDay) providers.get(0).provideData(
                DurationCurveFacet.BB_DURATIONCURVE,
                null,
                context);
        }

        // Rather specific case, Q-Annotations at a maybe second yaxis.
        StickyAxisAnnotation annotation = null;

        // defaults if not drawing a duration curve
        SimpleAxis axis = SimpleAxis.X_AXIS;
        int axisSymbol  = 0;

        if (providers.size() >= 1) {
            // for duration curve, overwrite previously given default
            axis       = SimpleAxis.Y_AXIS;
            axisSymbol = DurationCurveGenerator.YAXIS.Q.idx;
        }

        for (NamedDouble q: qs) {
            if (Double.isNaN(q.getValue())) {
                log.warn("NaN MainValue " + q.getName());
                continue;
            }
            annotation =
                new StickyAxisAnnotation(
                    q.getName(),
                    (float) q.getValue(),
                    axis,
                    axisSymbol);
            xy.add(annotation);
            if (wqdays != null) {
                setHitPoint(wqdays, annotation);
            }
        }

        return new RiverAnnotation(description, xy);
    }


    /**
     * Create a deep copy of this Facet.
     * @return a deep copy.
     */
    @Override
    public MainValuesQFacet deepCopy() {
        MainValuesQFacet copy = new MainValuesQFacet(this.name,
            description);
        copy.set(this);
        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
