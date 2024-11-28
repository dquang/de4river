/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQKms;

import org.dive4elements.river.artifacts.states.DefaultState;

import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DischargeTables;

import org.dive4elements.river.artifacts.access.RiverAccess;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.DischargeTable;


/**
 * Artifact to get discharge curves at gauges.
 */
public class GaugeDischargeArtifact
extends      WINFOArtifact
implements   FacetTypes
{
    /** The log for this class. */
    private static Logger log = LogManager.getLogger(GaugeDischargeArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "gaugedischarge";

    /** The name a facet should have */
    protected String facetWishName;

    /**
     * Trivial Constructor.
     */
    public GaugeDischargeArtifact() {
        log.debug("GaugeDischargeArtifact.GaugeDischargeArtifact()");
    }


    /**
     * Gets called from factory, to set things up.
     * Especially, when loaded via datacage mechanisms, provide the
     * data document.
     * @param data filled with stuff from dc, if any.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("GaugeDischargeArtifact.setup");
        String ids = StaticD4EArtifact.getDatacageIDValue(data);
        addStringData("ids", ids);
        log.debug("id for gaugedischarge: " + ids);
        String[] splitIds = ids.split(";");
       /* We assume that if an id's string with a ; is given that the
        * format is <gauge_name>;<discharge_table_id>;<facet_desc>
        * so that a specific discharge table can be selected */
        if (splitIds.length > 2) {
            facetWishName = splitIds[2];
        }
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    /** Return the name of this artifact. */
    public String getName() {
        return ARTIFACT_NAME;
    }


    /**
     * Setup state and facet, copy from master artifact.
     */
    @Override
    protected void initialize(Artifact art, Object context, CallMeta meta) {
        log.debug("GaugeDischargeArtifact.initialize");
        List<Facet> fs = new ArrayList<Facet>();
        D4EArtifact artifact = (D4EArtifact) art;
        importData(artifact, "river");

        // Get the location(s)
        //importData(artifact, "ld_mode", ld_from, ld_to, ld_locations
        addStringData("ld_from", "0");
        addStringData("ld_to", "1000");
        addStringData("ld_mode", "distance");

        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);
        if (!fs.isEmpty()) {
            log.debug("Facets to add in GaugeDischargeArtifact.initialize. ("
                + state.getID() + "/ " + getCurrentStateId() + ").");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in GaugeDischargeArtifact.initialize ("
                + state.getID() + "/ "+getCurrentStateId()+").");
        }
    }


    /** Get the Gauges name which came with datacage data-document. */
    public String getGaugeName() {
        if (getDataAsString("ids") == null) {
            return null;
        }
        return getDataAsString("ids").split(";")[0];
    }


    /** Get the Gauges which came with datacage data-document. */
    public Gauge getGauge() {
        return new RiverAccess((D4EArtifact)this).getRiver()
            .determineGaugeByName(getGaugeName());
    }


    /**
     * Returns the data that is used to create discharge curves.
     * @return CalculationResult with WQKms.
     */
    public CalculationResult getDischargeCurveData() {

        River river = new RiverAccess((D4EArtifact)this).getRiver();
        if (river == null) {
            return error(new WQKms[0], "no.river.selected");
        }
        /*
        // This one would allow to automatically pick the right Gauge.
        double [] distance = RiverUtils.getKmRange(this);
        log.debug("getDischargeCurveData: get range");

        if (distance == null) {
            return error(new WQKms[0], "no.range.found");
        }

        List<Gauge> gauges = river.determineGauges(distance[0], distance[1]);
        log.debug("getDischargeCurveData: got " + gauges.size() + " gauges");

        if (gauges.isEmpty()) {
            return error(new WQKms[0], "no.gauge.selected");
        }

        String [] names = new String[gauges.size()];

        for (int i = 0; i < names.length; ++i) {
            names[i] = gauges.get(i).getName();
            log.debug("getDischargeCurveData: name " + names[i]);
        }
        */

        Map<String, double [][]> map;

        String[] ids = getDataAsString("ids").split(";");
        if (ids.length > 1) {
           /* We assume that if an id's string with a ; is given that the
            * format is <gauge_name>;<discharge_table_id>;<facet_desc>
            * so that a specific discharge table can be selected */
            int tableId = 0;
            try {
                tableId = Integer.parseInt(ids[1]);
            } catch (NumberFormatException e) {
                log.error("Discharge tables ids string is wrong. "
                    + "Format is "
                    + "<gauge_name>;<discharge_table_id>;<facet_desc>"
                    + " Fix your Datacage!");
                // Let's rather break down completly then show the wrong data.
                return null;
            }
            DischargeTable table = DischargeTable.getDischargeTableById(
                tableId);
            map = new HashMap<String, double [][]>();
            map.put(getGaugeName(), DischargeTables.loadDischargeTableValues(
                    table));
        } else {
            DischargeTables dt = new DischargeTables(
                river.getName(), getGaugeName());
            map = dt.getValues();
        }

        ArrayList<WQKms> res = new ArrayList<WQKms>();

        Gauge gauge = river.determineGaugeByName(getGaugeName());

        String name = getGaugeName();
        double [][] values = map.get(name);
        if (values == null) {
            log.error("No values for this gauge / discharge found.");
            return error(new WQKms[0], "no.gauge.found");
        }
        for (int i = 0 ; i < values[1].length; i++) {
            values[1][i] = values[1][i]/100d + gauge.getDatum().doubleValue();
        }
        double [] kms = new double[values[1].length];
        Arrays.fill(kms, gauge.getStation().doubleValue());
        res.add(new WQKms(kms, values[0], values[1], name));

        return new CalculationResult(
            res.toArray(new WQKms[res.size()]),
            new Calculation());
    }

    /** Gets the facet wish name.
     *
     * This is a hack to enable setting the name of the facet / theme in the
     * UI from the datacage setting. */
    public String getFacetWishName() {
        return facetWishName;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
