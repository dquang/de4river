/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.data.DefaultStateData;

import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.artifacts.model.GaugeDischargeCurveFacet;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.utils.RiverUtils;


/**
 * Artifact to calculate a discharge curve from a gauge overview info
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class GaugeDischargeCurveArtifact
extends      AbstractStaticStateArtifact
{
    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(GaugeDischargeCurveArtifact.class);

    public static final String XPATH_RIVER = "/art:action/art:river/@art:name";
    public static final String XPATH_GAUGE =
        "/art:action/art:gauge/@art:reference";
    public static final String NAME = "gaugedischargecurve";
    public static final String STATIC_STATE_NAME =
        "state.gaugedischargecurve.static";
    public static final String UIPROVIDER = "gauge_discharge_curve";
    public static final String GAUGE_DISCHARGE_CURVE_FACET =
        "gauge_discharge_curve";
    public static final String GAUGE_DISCHARGE_CURVE_AT_EXPORT_FACET =
        "at";
    public static final String GAUGE_DISCHARGE_CURVE_OUT =
        "discharge_curve";
    public static final String GAUGE_DISCHARGE_CURVE_AT_EXPORT_OUT =
        "computed_dischargecurve_at_export";
    public static final String GAUGE_DISCHARGE_CURVE_EXPORT_OUT =
        "computed_dischargecurve_export";
    public static final String GAUGE_DISCHARGE_CURVE_CSV_FACET =
        "csv";
    public static final String GAUGE_DISCHARGE_CURVE_PDF_FACET =
        "pdf";

    private Facet atexportfacet;
    private Facet curvefacet;
    private Facet csvfacet;
    private Facet pdffacet;

    /**
     * Setup initializes the data by extracting the river and gauge from
     * the XML Document.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callmeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("GaugeDischargeCurveArtifact.setup");

        if (log.isDebugEnabled()) {
            log.debug("GaugeDischargeCurveArtifact.setup"
                + XMLUtils.toString(data));
        }
        String gaugeref = XMLUtils.xpathString(data, XPATH_GAUGE,
                ArtifactNamespaceContext.INSTANCE);
        String rivername = XMLUtils.xpathString(data, XPATH_RIVER,
                ArtifactNamespaceContext.INSTANCE);

        addData("river", new DefaultStateData("river",
                    Resources.getMsg(callmeta,
                        "facet.gauge_discharge_curve.river",
                        "Name of the river"),
                    "String", rivername));
        addData("reference_gauge", new DefaultStateData("reference_gauge",
                    Resources.getMsg(callmeta,
                        "facet.gauge_discharge_curve.reference_gauge",
                        "Gauge official number"),
                    "Long", gaugeref));

        Gauge gauge = RiverUtils.getReferenceGauge(this);
        String gaugename = "";
        Double gaugelocation = null;
        if (gauge != null) {
            gaugename = gauge.getName();
            BigDecimal station = gauge.getStation();
            if (station != null) {
                gaugelocation = station.doubleValue();
            }
        }

        addData("gauge_name", new DefaultStateData("gauge_name",
                    Resources.getMsg(callmeta,
                        "facet.gauge_discharge_curve.gauge_name",
                        "Name of the gauge"),
                    "String", gaugename));

        if (gaugelocation != null) {
            addData("ld_locations", new DefaultStateData("ld_locations",
                        Resources.getMsg(callmeta,
                            "facet.gauge_discharge_curve.gauge_location",
                            "Location of the gauge"),
                        "Double", gaugelocation.toString()));
        }

        String description = Resources.format(callmeta,
                "facet.gauge_discharge_curve.description",
                "Discharge curve on gauge",
                rivername,
                gaugename);

        List<Facet> fs = new ArrayList<Facet>(4);
        curvefacet = new GaugeDischargeCurveFacet(
                GAUGE_DISCHARGE_CURVE_FACET, description);
        fs.add(curvefacet);

        description = Resources.format(callmeta,
                "facet.gauge_discharge_curve_at_export.description",
                "Discharge curve AT export on gauge",
                rivername,
                gaugename);
        atexportfacet = new GaugeDischargeCurveFacet(
                GAUGE_DISCHARGE_CURVE_AT_EXPORT_FACET, description);
        fs.add(atexportfacet);

        description = Resources.format(callmeta,
                "facet.computed_dischargecurve_export.csv",
                "Discharge curve CSV export on gauge",
                rivername,
                gaugename);
        csvfacet = new GaugeDischargeCurveFacet(
                GAUGE_DISCHARGE_CURVE_CSV_FACET, description);
        fs.add(csvfacet);

        description = Resources.format(callmeta,
                "facet.computed_dischargecurve_export.pdf",
                "Discharge curve PDF export on gauge",
                rivername,
                gaugename);
        pdffacet = new GaugeDischargeCurveFacet(
                GAUGE_DISCHARGE_CURVE_PDF_FACET, description);
        fs.add(pdffacet);

        addFacets(STATIC_STATE_NAME, fs);

        super.setup(identifier, factory, context, callmeta, data, loadFacets);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void initStaticState() {
        StaticState state = new StaticState(STATIC_STATE_NAME);

        List<Facet> fs = new ArrayList<Facet>(1);
        fs.add(curvefacet);

        DefaultOutput output = new DefaultOutput(
            GAUGE_DISCHARGE_CURVE_OUT,
            "output.discharge_curve",
            "image/png",
            fs,
            "chart");
        state.addOutput(output);

        fs = new ArrayList<Facet>(1);
        fs.add(atexportfacet);
        output = new DefaultOutput(
            GAUGE_DISCHARGE_CURVE_AT_EXPORT_OUT,
            "output.computed_dischargecurve_at_export",
            "text/plain",
            fs,
            "export");
        state.addOutput(output);

        fs = new ArrayList<Facet>(2);
        fs.add(csvfacet);
        fs.add(pdffacet);
        output = new DefaultOutput(
            GAUGE_DISCHARGE_CURVE_EXPORT_OUT,
            "output.computed_dischargecurve_export",
            "text/plain",
            fs,
            "export");
        state.addOutput(output);

        state.setUIProvider(UIPROVIDER);
        setStaticState(state);
    }
}
