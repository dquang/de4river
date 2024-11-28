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
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;

import org.dive4elements.river.artifacts.model.FlowVelocityData;

public class ShearStressProcessor extends DefaultProcessor {

    private final static Logger log =
            LogManager.getLogger(ShearStressProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.flow_velocity.section.yaxis.second.label";
    public static final String I18N_AXIS_LABEL_DEFAULT =
        "Schubspannung 1.3 Pau [N]";

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        XYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                theme);
        String facetName = bundle.getFacetName();
        FlowVelocityData data = (FlowVelocityData) bundle.getData(context);

        StyledSeriesBuilder.addPoints(series, data.getTauPoints(), true);

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        return facettype.equals(FacetTypes.FLOW_VELOCITY_TAU) ||
            facettype.equals(FacetTypes.FLOW_VELOCITY_TAU_FILTERED);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT);
    }
}

