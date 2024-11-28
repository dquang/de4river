/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.fixings.AnalysisPeriod;
import org.dive4elements.river.artifacts.model.fixings.QWD;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.jfree.StyledXYSeries;
import org.dive4elements.river.jfree.StyledAreaSeriesCollection;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.KMIndex;

public class DeltaWProcessor extends DefaultProcessor {
    /* This is basically a collection of different processors. The
     * historic reason for this is that they have in common that they
     * work on deltaW data from the fixing analysis. */

    private static final Logger log = LogManager.getLogger(DeltaWProcessor.class);

    public static final String I18N_DW_YAXIS_LABEL_DEFAULT  =
            "delta W [cm]";

    public static final String I18N_DW_YAXIS_LABEL =
            "chart.fixings.longitudinalsection.yaxis.label";

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
        String facettype = bundle.getFacetName();
        if (!visible) {
            return;
        }
        log.debug("Doing out for: " + bundle.getFacetName());
        if (facettype.equals(FacetTypes.FIX_REFERENCE_EVENTS_LS)) {
            doReferenceEventsOut(generator, bundle, theme, visible);
        } else if (facettype.equals(FacetTypes.FIX_ANALYSIS_EVENTS_LS)) {
            doAnalysisEventsOut(generator, bundle, theme, visible);
        } else if (facettype.startsWith(
                FacetTypes.FIX_SECTOR_AVERAGE_LS_DEVIATION)) {
            doSectorAverageDeviationOut(generator, bundle, theme, visible);
        } else if (facettype.equals(FacetTypes.FIX_DEVIATION_LS)) {
            doReferenceDeviationOut(generator, bundle, theme, visible);
        } else if (facettype.startsWith(FacetTypes.FIX_SECTOR_AVERAGE_LS)) {
            doSectorAverageOut(generator, bundle, theme, visible);
        } else {
            log.error("Could not handle: " + facettype);
        }
    }

    @Override
    public boolean canHandle(String facettype) {
        if (facettype == null) {
            return false;
        }

        if (facettype.startsWith(FacetTypes.FIX_SECTOR_AVERAGE_LS)
                || facettype.equals(FacetTypes.FIX_REFERENCE_EVENTS_LS)
                || facettype.equals(FacetTypes.FIX_ANALYSIS_EVENTS_LS)
                || facettype.equals(FacetTypes.FIX_DEVIATION_LS)) {
            return true;
        }
        return false;
    }

    private void doSectorAverageOut(DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument doc, boolean visible) {
        CallContext context = generator.getCallContext();
        int index = bundle.getFacet().getIndex();
        int sectorNdx = index & 3;

        KMIndex<AnalysisPeriod> kms =
                (KMIndex<AnalysisPeriod>)bundle.getData(context);

        if(kms == null) {
            return;
        }

        XYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), doc);

        for (KMIndex.Entry<AnalysisPeriod> entry: kms) {
            double km = entry.getKm();
            AnalysisPeriod ap = entry.getValue();
            QWD qwd = ap.getQSectorAverages()[sectorNdx];
            if (qwd == null) {
                continue;
            }
            double deltaW = qwd.getDeltaW();
            series.add(km, deltaW);
        }

        generator.addAxisSeries(series, axisName, visible);
    }

    private void doReferenceEventsOut(DiagramGenerator generator,
            ArtifactAndFacet bundle, ThemeDocument doc, boolean visible) {
        CallContext context = generator.getCallContext();

        KMIndex<QWD> kms =
                (KMIndex<QWD>)bundle.getData(context);

        if(kms == null) {
            return;
        }

        XYSeriesCollection col = new XYSeriesCollection();

        StyledXYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), false, doc);

        for (KMIndex.Entry<QWD> entry: kms) {
            double km = entry.getKm();
            QWD qwd = entry.getValue();

            series.add(km, qwd.getDeltaW());
        }
        col.addSeries(series);

        generator.addAxisDataset(col, axisName, visible);
    }

    private void doAnalysisEventsOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument doc,
            boolean visible) {
        CallContext context = generator.getCallContext();

        KMIndex<QWD> kms =
                (KMIndex<QWD>)bundle.getData(context);

        if(kms == null) {
            return;
        }

        XYSeriesCollection col = new XYSeriesCollection();

        StyledXYSeries series = new StyledXYSeries(
            bundle.getFacetDescription(), false, doc);

        for (KMIndex.Entry<QWD> entry: kms) {
            double km = entry.getKm();
            QWD qwd = entry.getValue();

            series.add(km, qwd.getDeltaW());
        }
        col.addSeries(series);

        generator.addAxisDataset(col, axisName, visible);
    }

    protected void doSectorAverageDeviationOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument doc,
            boolean visible) {
        CallContext context = generator.getCallContext();

        int index = bundle.getFacet().getIndex();
        int sectorNdx = index & 3;

        KMIndex<AnalysisPeriod> kms =
                (KMIndex<AnalysisPeriod>)bundle.getData(context);

        if(kms == null) {
            return;
        }

        StyledAreaSeriesCollection area = new StyledAreaSeriesCollection(doc);
        XYSeries upper =
                new StyledXYSeries(bundle.getFacetDescription(), false, doc);
        XYSeries lower = new StyledXYSeries(
            bundle.getFacetDescription() + " ", false, doc);

        for (KMIndex.Entry<AnalysisPeriod> entry: kms) {
            double km = entry.getKm();
            AnalysisPeriod ap = entry.getValue();
            QWD qwd = ap.getQSectorAverages()[sectorNdx];
            double dev = ap.getQSectorStdDev(sectorNdx);
            if (qwd == null) {
                continue;
            }
            double deltaW = qwd.getDeltaW();
            double up = deltaW + dev;
            double lo = deltaW - dev;
            upper.add(km, up);
            lower.add(km, lo);
        }
        area.addSeries(upper);
        area.addSeries(lower);

        generator.addAreaSeries(area, axisName, visible);
    }

    protected void doReferenceDeviationOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument doc,
            boolean visible) {
        CallContext context = generator.getCallContext();

        KMIndex<double[]> kms =
                (KMIndex<double[]>)bundle.getData(context);

        if(kms == null) {
            return;
        }

        StyledAreaSeriesCollection area = new StyledAreaSeriesCollection(doc);
        XYSeries upper =
                new StyledXYSeries(bundle.getFacetDescription(), false, doc);
        XYSeries lower = new StyledXYSeries(
            bundle.getFacetDescription() + " ", false, doc);

        for (KMIndex.Entry<double[]> entry: kms) {
            double km = entry.getKm();
            double[] devArray = entry.getValue();
            if (devArray == null) {
                continue;
            }
            double dev = devArray[0];
            double up = dev;
            double lo = -dev;
            upper.add(km, up, false);
            lower.add(km, lo, false);
        }
        area.addSeries(upper);
        area.addSeries(lower);

        Marker marker = new ValueMarker(0);
        marker.setStroke(new BasicStroke(2));
        marker.setPaint(Color.BLACK);
        generator.addValueMarker(marker);
        generator.addAreaSeries(area, axisName, visible);
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return generator.msg(
            I18N_DW_YAXIS_LABEL, I18N_DW_YAXIS_LABEL_DEFAULT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
