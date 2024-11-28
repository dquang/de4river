/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.jfree.Bounds;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.ZoomScale;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.resources.Resources;

import org.jfree.data.Range;

import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class LongitudinalSectionGenerator2 extends DiagramGenerator
{
    private static Logger log = LogManager.getLogger(
        LongitudinalSectionGenerator2.class);

    /** Wrapper around the doOut info for postprocessing. */
    protected static class SuperBundle
    {
        public ArtifactAndFacet bundle;
        public ThemeDocument theme;
        boolean visible;
        public SuperBundle(
            ArtifactAndFacet bundle,
            ThemeDocument theme,
            boolean visible
        ) {
            this.bundle = bundle;
            this.theme = theme;
            this.visible = visible;
        }
    };

    protected List<SuperBundle> postOutAF;

    public static final String I18N_CHART_SHORT_SUBTITLE =
        "chart.longitudinal.section.shortsubtitle";

    public static final String I18N_CHART_LOCATION_SUBTITLE =
        "chart.longitudinal.section.locsubtitle";

    public static final String I18N_CHART_DISTANCE_SUBTITLE =
        "chart.longitudinal.section.subtitle";

    public static final String I18N_SUBTITLE_RADIUS =
        "chart.subtitle.radius";

    @Override
    public String getDefaultChartSubtitle() {
        double[] dist = getRange();

        String parts = "";
        if (subTitleParts != null && !subTitleParts.isEmpty()) {
             for (String p : subTitleParts) {
                 parts += " " + p;
             }
        }
        if (dist == null || dist.length != 2 ||
                Double.isNaN(dist[0]) || Double.isNaN(dist[1])) {
            Object [] args = new Object[] {getRiverName()};
            return msg(I18N_CHART_SHORT_SUBTITLE, "", args) + parts;
        }

        if (Math.abs(dist[0] - dist[1]) < 1E-5) {
            Object [] args = new Object[] {getRiverName(), dist[1]};
            return msg(I18N_CHART_LOCATION_SUBTITLE, "", args) + parts;
        }
        Object [] args = new Object[] {getRiverName(), dist[0], dist[1]};
        return msg(I18N_CHART_DISTANCE_SUBTITLE, "", args) + parts;
    }

    protected void calculateRadius() {
        // Fixed range in settings is preferred
        Range candidate = getRangeForAxisFromSettings("X");
        Bounds dataBounds = getXBounds(0);
        if (candidate == null) {
            candidate = getDomainAxisRange(); // Diagram is zoomed
            if (candidate != null && dataBounds == null) {
                log.debug("Can't calculate the zoom without any X bounds.");
                candidate = null;
            } else if (candidate != null) {
                // domainAxisRange is relative so we have to take
                // this into account.
                Bounds bounds =
                    calculateZoom(dataBounds, candidate);
                candidate = new Range(bounds.getLower().doubleValue(),
                                      bounds.getUpper().doubleValue());
                log.debug("Using X Range from zoom.");
            }
        } else {
            log.debug("Using X Range from settings.");
        }

        if (candidate == null) {
            if (dataBounds == null) {
                // Diagram is empty.
                candidate = new Range(0d, 0d);
                log.debug("Empty diagram using fake Range");
            } else {
                // Diagram is not zoomed
                candidate = new Range(dataBounds.getLower().doubleValue(),
                                      dataBounds.getUpper().doubleValue());
                log.debug("Using Full X Range.");
            }
        }
        log.debug("startkm for Radius is: " + candidate.getLowerBound() +
                  " endkm: " + candidate.getUpperBound());

        // This might not be neccessary if every facet uses only the
        // radius and does not do its own zoomscale calculation.
        context.putContextValue("startkm", candidate.getLowerBound());
        context.putContextValue("endkm", candidate.getUpperBound());
        context.putContextValue("bounds_defined", true);

        RiverContext fc = (RiverContext)context.globalContext();
        ZoomScale scales = (ZoomScale)fc.get("zoomscale");
        RiverAccess access = new RiverAccess((D4EArtifact)getMaster());
        String river = access.getRiverName();

        double radius = scales.getRadius(river, candidate.getLowerBound(),
                                         candidate.getUpperBound());
        context.putContextValue("radius", radius);
    }

    @Override
    protected void postProcess() {
        if (postOutAF == null) {
            log.debug("PostProcess without bundles to process");
            return;
        }

        // fake startkm and endkm for the dry run
        context.putContextValue("startkm", 0d);
        context.putContextValue("endkm", 42d);
        for (SuperBundle superbundle: postOutAF) {
            // Dry run with fake start /end
            // to get the filtered facets also included
            // in the x axis range.
            super.doOut(superbundle.bundle, superbundle.theme, false);
        }

        calculateRadius(); // This calculates the real start and end km's

        for (SuperBundle superbundle: postOutAF) {
            super.doOut(
                superbundle.bundle,
                superbundle.theme,
                superbundle.visible);
        }
    }

    /* We override doOut here to save the startkm and endkm in the
     * context. Some facets will deliver different data because of
     * that setting. It is mainly used in MINFO where it causes
     * adaptive smoothing on the data if you are zoomed out do
     * reduce the static in the curve. */
    @Override
    public void doOut(
        ArtifactAndFacet bundle,
        ThemeDocument    theme,
        boolean          visible
    ) {
        String facetName = bundle.getFacetName();
        if (FacetTypes.IS.FILTERED(facetName)) {
            // We can only process the filtered (smoothed) facets
            // after we know the diagram's extend to correctly calculate
            // the radius of the filter / smoothing operation. So
            // we postprocess them.

            SuperBundle superbundle = new SuperBundle(bundle, theme, visible);
            if (postOutAF == null) {
                postOutAF = new ArrayList<SuperBundle>();
            }
            postOutAF.add(superbundle);
            if (visible) {
                log.debug("Adding radius subtitle.");

                addSubtitle(Resources.getMsg(
                            getCallContext().getMeta(),
                            I18N_SUBTITLE_RADIUS) + ": $RADIUS");
            }
            return;
        }
        super.doOut(bundle, theme, visible);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
