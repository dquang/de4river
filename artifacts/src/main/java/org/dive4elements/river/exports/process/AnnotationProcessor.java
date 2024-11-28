/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.themes.ThemeDocument;

/**
 * Add data to chart/generator.
 *
 */
public class AnnotationProcessor extends DefaultProcessor {

    /** Private log. */
    private static final Logger log =
            LogManager.getLogger(AnnotationProcessor.class);

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        if (!visible) {
            // Nothing to do
            return;
        }
        CallContext context = generator.getCallContext();
        if (!(bundle.getData(context) instanceof RiverAnnotation)) {
            // Just a bit defensive should not happen
            log.error("Incompatible facet in doOut");
            return;
        }
        RiverAnnotation ra = (RiverAnnotation)bundle.getData(context);
        ra.setTheme(theme);
        ra.setLabel(bundle.getFacetDescription());
        generator.addAnnotations(ra);
    }

    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible,
            int              index)
    {
        if (!visible) {
            // Nothing to do
            return;
        }
        CallContext context = generator.getCallContext();
        if (!(bundle.getData(context) instanceof RiverAnnotation)) {
            // Just a bit defensive should not happen
            log.error("Incompatible facet in doOut");
            return;
        }
        RiverAnnotation ra = (RiverAnnotation)bundle.getData(context);
        ra.setTheme(theme);
        ra.setLabel(bundle.getFacetDescription());
        generator.addAnnotations(ra);
    }

    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }
        return facetType.equals(FacetTypes.LONGITUDINAL_ANNOTATION) ||
            facetType.equals(FacetTypes.MIDDLE_BED_HEIGHT_ANNOTATION) ||
            facetType.equals(FacetTypes.FLOW_VELOCITY_ANNOTATION);
    }
}
