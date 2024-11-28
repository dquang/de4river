/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
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
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.exports.StyledSeriesBuilder;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;


/** Helper for data handling in discharge diagrams. */
public class DischargeProcessor
extends DefaultProcessor implements FacetTypes {

    private final static Logger log =
            LogManager.getLogger(DischargeProcessor.class);

    /** Station for which the diagram is shown. */
    private double km;

    private String yAxisLabel = "";

    private String I18N_AXIS_LABEL = "chart.discharge.curve.yaxis.label";


    /** This processor needs to be constructed with a given km. */
    public DischargeProcessor() {
        km = Double.NaN;
    }


    public DischargeProcessor(double km) {
        this.km = km;
    }


    /** Process data, add it to plot. */
    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument theme,
            boolean visible
    ) {
        CallContext context = generator.getCallContext();
        Object data = bundle.getData(context);
        if (data instanceof WQKms) {
            doDischargeOut(
                generator,
                bundle,
                theme,
                visible);
        }
        else {
            log.error("Can't process "
                + data.getClass().getName() + " objects of facet "
                + bundle.getFacetName());
        }
    }

    /** True if this processor knows how to deal with facetType. */
    @Override
    public boolean canHandle(String facetType) {
        return DISCHARGE_CURVE.equals(facetType)
            || GAUGE_DISCHARGE_CURVE.equals(facetType)
            || COMPUTED_DISCHARGE_Q.equals(facetType);
    }


    /** The station of the current calculation/view. */
    protected double getKm() {
        return km;
    }

    /**
     * Add series with discharge curve to diagram.
     */
    protected void doDischargeOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean       visible
    ) {
        CallContext context = generator.getCallContext();
        Map<String, String> metaData = bundle.getFacet().getMetaData(
            bundle.getArtifact(), context);
        WQKms data = (WQKms)bundle.getData(context);
        Double skm = data.sameKm();
        if (skm != null && Math.abs(skm-km) > 0.00001) {
            return;
        }
        StyledXYSeries series = new StyledXYSeries(
            bundle.getFacetDescription().trim(), theme);
        double[][] wqData = new double[2][data.size()];
        for (int i = 0, n = data.size(); i < n; i++) {
            wqData[0][i] = data.getQ(i);
            wqData[1][i] = data.getW(i);
        }
        StyledSeriesBuilder.addPoints(series, wqData, false);

        series.putMetaData(metaData, bundle.getArtifact(), context);
        yAxisLabel = metaData.get("Y");
        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        CallMeta meta = generator.getCallContext().getMeta();
        RiverAccess access =
            new RiverAccess((D4EArtifact)generator.getMaster());
        String unit = access.getRiver().getWstUnit().getName();

        if (yAxisLabel != null && !yAxisLabel.isEmpty()) {
            return Resources.getMsg(
                meta,
                yAxisLabel,
                new Object[] { unit });
        }
        return Resources.getMsg(
                meta,
                I18N_AXIS_LABEL,
                new Object[] { unit });
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
