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

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.minfo.SedimentDensity;

import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;


/** Process Sediment Density data. */
public class SedimentDensityProcessor extends DefaultProcessor {

    /** Private log. */
    private final static Logger log =
            LogManager.getLogger(SedimentDensityProcessor.class);

    public static final String I18N_AXIS_LABEL_DEFAULT =
        "Sedimentdichte [t/m^3]";
    public static final String I18N_AXIS_LABEL =
        "chart.yaxis.label.sedimentdensity";

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        XYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                theme);
        Object data = bundle.getData(context);
        String facetName = bundle.getFacetName();
        double [][] points;

        if (facetName.equals(FacetTypes.SEDIMENT_DENSITY)) {
            points =((SedimentDensity) data).getAllDensities();
        } else {
            log.error("Unknown facet name: " + facetName);
            return;
        }
        StyledSeriesBuilder.addPoints(series, points, true);

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        return facettype.equals(FacetTypes.SEDIMENT_DENSITY);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT);
    }
}
