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
import org.jfree.data.xy.XYSeries;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

// Base class for SedimantLoad$UNITProcessors
public class SedimentLoadLSProcessor extends DefaultProcessor
{
    private final static Logger log =
            LogManager.getLogger(SedimentLoadProcessor.class);

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        log.debug("doOut " + bundle.getFacetName());
        CallContext context = generator.getCallContext();
        XYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                false, // Handle NaN
                theme);
        Object data = bundle.getData(context);
        String facetName = bundle.getFacetName();
        double [][] points;

        log.debug("Do out for: " + facetName);
        if (facetName.startsWith("sedimentload.")) {
            points = (double[][]) data;
        } else {
            log.error("Unknown facet name: " + facetName);
            return;
        }

        StyledSeriesBuilder.addPoints(series, points, false); // Keep NaN

        generator.addAxisSeries(series, axisName, visible);
    }
}

