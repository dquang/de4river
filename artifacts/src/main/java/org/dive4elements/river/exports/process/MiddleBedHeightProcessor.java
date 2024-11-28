/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;

public class MiddleBedHeightProcessor extends DefaultProcessor {

    /** Private log. */
    private static final Logger log =
            LogManager.getLogger(MiddleBedHeightProcessor.class);

    public static final String I18N_AXIS_LABEL =
        "chart.bedheight_middle.section.yaxis.label";

    public static final String I18N_AXIS_LABEL_DEFAULT =
        "mittlere Sohlhöhen [müNN]";

    protected String yAxisLabel;

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        Map<String, String> metaData = bundle.getFacet().getMetaData();
        yAxisLabel = metaData.get("Y");

        Object raw = bundle.getData(context);
        if (raw == null) {
            return;
        }
        if (!(raw instanceof double[][])) {
            log.error("Unkonwn data type: " + raw.getClass().getName());
            return;
        }

        double[][] data = (double[][])raw;
        StyledXYSeries series = new StyledXYSeries(bundle.getFacetDescription(),
                theme);
        series.putMetaData(metaData, bundle.getArtifact(), context);

        StyledSeriesBuilder.addPoints(series, data,
                false, 0.110d);

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        D4EArtifact flys = (D4EArtifact) generator.getMaster();

        RiverAccess access = new RiverAccess(flys);
        String unit = access.getRiver().getWstUnit().getName();

        if (yAxisLabel != null && !yAxisLabel.isEmpty()) {
            return generator.msg(
                yAxisLabel,
                I18N_AXIS_LABEL_DEFAULT,
                new Object[] { unit });
        }
        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT,
                new Object[] { unit });
    }

    @Override
    public boolean canHandle(String facetType) {
        if (facetType == null) {
            return false;
        }
        return facetType.equals(FacetTypes.MIDDLE_BED_HEIGHT_SINGLE);
    }
}
