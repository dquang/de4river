/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.jfree.RiverAnnotation;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.jfree.CollisionFreeXYTextAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;

import org.dive4elements.river.jfree.JFreeUtil;

import org.dive4elements.river.artifacts.model.sq.SQ;
import org.dive4elements.river.artifacts.model.sq.SQFunction;

public class SQRelationProcessor extends DefaultProcessor {

    public static final String I18N_AXIS_LABEL =
        "chart.sq_relation.yaxis.label";
    public static final String I18N_AXIS_LABEL_DEFAULT =
        "";

    private final static Logger log =
            LogManager.getLogger(SQRelationProcessor.class);

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        CallContext context = generator.getCallContext();
        String facetName = bundle.getFacetName();
        StyledXYSeries series;
        Object data = bundle.getData(context);
        String desc = bundle.getFacetDescription();
        Map<String, String> metaData =
            bundle.getFacet().getMetaData(bundle.getArtifact(), context);
        if (data == null) {
            // Check has been here before so we keep it but
            // this should never happen.
            log.error("Data is null for facet: " + facetName);
            return;
        }

        if (FacetTypes.IS.SQ_CURVE(facetName)) {
            SQFunction func = (SQFunction) data;

            series = JFreeUtil.sampleFunction2DPositive(
                func.getFunction(),
                theme,
                desc,
                500,
                Math.max(func.getMinQ(), 0.01),
                Math.max(func.getMaxQ(), 0.02));

        } else if (FacetTypes.IS.SQ_MEASUREMENT(facetName) ||
               FacetTypes.IS.SQ_OUTLIER(facetName)) {

            SQ[] sqs = (SQ[]) data;
            series = new StyledXYSeries(desc, theme);
            List<XYTextAnnotation> xy = new ArrayList<XYTextAnnotation>();

            DateFormat dateFormat = DateFormat.getDateInstance(
                DateFormat.SHORT,
                Resources.getLocale(context.getMeta()));

            for (SQ sq: sqs) {
                double q = sq.getQ();
                double s = sq.getS();
                if (s > 0d && q > 0d) {
                    series.add(q, s, false);
                    // Annotate with measurement date
                    if (sq.getDate() != null) {
                        xy.add(new CollisionFreeXYTextAnnotation(
                                dateFormat.format(sq.getDate()), q, s));
                    }
                }
            }

            if (visible && theme.parseShowPointLabel()) {

                RiverAnnotation annotation = new RiverAnnotation(
                    "Messdatum", null, null, theme);
                annotation.setTextAnnotations(xy);
                generator.addAnnotations(annotation);
            }
        } else {
            log.error("Could not handle: " + facetName);
            return;
        }
        series.putMetaData(metaData, bundle.getArtifact(), context);

        if (log.isDebugEnabled()) {
            log.debug("Series '" + desc + "' has "
                + series.getItemCount() + " items.");

            log.debug("   -> min x = " + series.getMinX());
            log.debug("   -> max x = " + series.getMaxX());
            log.debug("   -> min y = " + series.getMinY());
            log.debug("   -> max y = " + series.getMaxY());
        }

        generator.addAxisSeries(series, axisName, visible);
    }

    @Override
    public boolean canHandle(String facettype) {
        return FacetTypes.IS.SQ_CURVE(facettype) ||
            FacetTypes.IS.SQ_MEASUREMENT(facettype) ||
            FacetTypes.IS.SQ_OUTLIER(facettype);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(
                I18N_AXIS_LABEL,
                I18N_AXIS_LABEL_DEFAULT);
    }
}
