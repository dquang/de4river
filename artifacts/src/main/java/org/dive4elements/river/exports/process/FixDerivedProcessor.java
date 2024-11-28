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
import org.dive4elements.river.artifacts.model.fixings.FixDerivateFacet;
import org.dive4elements.river.artifacts.model.fixings.FixFunction;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.JFreeUtil;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.themes.ThemeDocument;


public class FixDerivedProcessor
extends DefaultProcessor
implements FacetTypes
{

    private static Logger log = LogManager.getLogger(FixDerivedProcessor.class);

    private String yAxisLabel = "";

    private String I18N_AXIS_LABEL = "chart.discharge.curve.yaxis.label";


    public FixDerivedProcessor() {
    }

    @Override
    public void doOut(
        DiagramGenerator generator,
        ArtifactAndFacet bundle,
        ThemeDocument theme,
        boolean visible
    ) {
        CallContext context = generator.getCallContext();
        Map<String, String> metaData = bundle.getFacet().getMetaData(
            bundle.getArtifact(), context);
        FixDerivateFacet facet = (FixDerivateFacet)bundle.getFacet();
        FixFunction func = (FixFunction)facet.getData(
                bundle.getArtifact(), generator.getCallContext());

        yAxisLabel = metaData.get("Y");


        if (func == null) {
            log.warn("doOut: Facet does not contain FixFunction");
            return;
        }

        double maxQ = func.getMaxQ();

        if (maxQ > 0) {
            StyledXYSeries series = JFreeUtil.sampleFunction2D(
                    func.getFunction(),
                    theme,
                    bundle.getFacetDescription(),
                    500,   // number of samples
                    0.0 ,  // start
                    maxQ); // end
            generator.addAxisSeries(series, axisName, visible);
            series.putMetaData(metaData, bundle.getArtifact(), context);
        }

    }

    @Override
    public boolean canHandle(String facettype) {
        return FIX_DERIVATE_CURVE.equals(facettype);
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
