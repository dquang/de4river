/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.river.artifacts.access.FlowVelocityAccess;

import org.dive4elements.river.model.DischargeZone;
import org.dive4elements.river.model.FlowVelocityModel;
import org.dive4elements.river.model.FlowVelocityModelValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/** Calculate flow velocity. */
public class FlowVelocityCalculation extends Calculation {

    /** Own log. */
    private static final Logger log =
        LogManager.getLogger(FlowVelocityCalculation.class);


    public CalculationResult calculate(FlowVelocityAccess access) {
        log.info("FlowVelocityCalculation.calculate");

        int[] mainIds  = access.getMainChannels();
        int[] totalIds = access.getTotalChannels();

        if (log.isDebugEnabled()) {
            Artifact a = access.getArtifact();
            log.debug("Artifact '" + a.identifier() + "' contains:");
            if (mainIds != null) {
                log.debug("   " + mainIds.length + " main channel ids");
            }

            if (totalIds != null) {
                log.debug("   " + totalIds.length + " total channel ids");
            }
        }

        List<DischargeZone>     zones  = getDischargeZones(mainIds, totalIds);
        List<FlowVelocityModel> models = getFlowVelocityModels(access, zones);

        return buildCalculationResult(access, models);
    }


    protected List<DischargeZone> getDischargeZones(
        int[] mainIds,
        int[] totalIds
    ) {
        List<DischargeZone> zones = new ArrayList<DischargeZone>();

        if (mainIds != null) {
            for (int id: mainIds) {
                DischargeZone zone = DischargeZone.getDischargeZoneById(id);
                zone.putType("main");

                if (zone != null) {
                    zones.add(zone);
                }
            }
        }

        if (totalIds != null) {
            for (int id: totalIds) {
                DischargeZone zone = DischargeZone.getDischargeZoneById(id);
                if (zone != null) {
                    int ndx = zones.indexOf(zone);
                    if (zones.contains(zone) &&
                        zones.get(ndx).fetchType().equals("main")) {
                        zone.putType("main_total");
                    }
                    else {
                        zone.putType("total");
                        zones.add(zone);
                    }
                }
            }
        }

        return zones;
    }


    protected List<FlowVelocityModel> getFlowVelocityModels(
        FlowVelocityAccess  access,
        List<DischargeZone> zones
    ) {
        String riverName = access.getRiverName();
        if (riverName == null) {
            log.warn("No river name found");
            return Collections.<FlowVelocityModel>emptyList();
        }

        List<FlowVelocityModel> models = new ArrayList<FlowVelocityModel>();

        for (DischargeZone zone: zones) {
            List<FlowVelocityModel> model = FlowVelocityModel.getModels(zone);
            models.addAll(model);
        }

        return models;
    }


    public static void prepareData(
        FlowVelocityData  data,
        FlowVelocityModel model,
        double kmLo,
        double kmHi
    ) {
        List<FlowVelocityModelValue> values =
            FlowVelocityModelValue.getValues(model, kmLo, kmHi);

        log.debug("Found " + values.size() + " values for model.");

        for (FlowVelocityModelValue value: values) {
            data.addKM(value.getStation().doubleValue());
            data.addQ(value.getQ().doubleValue());
            data.addVTotal(value.getTotalChannel().doubleValue());
            data.addVMain(value.getMainChannel().doubleValue());
            data.addTauMain(value.getShearStress().doubleValue());
        }

        DischargeZone zone = model.getDischargeZone();
        String        lo   = zone.getLowerDischarge();
        String        hi   = zone.getUpperDischarge();

        data.setType(zone.fetchType());
        if (lo.equals(hi)) {
            data.setZone(lo);
        }
        else {
            data.setZone(lo + " - " + hi);
        }
    }


    /**
     * From the given models and range restrictions from access,
     * create and return a calculationresult with flowvelocityDate.
     */
    protected CalculationResult buildCalculationResult(
        FlowVelocityAccess      access,
        List<FlowVelocityModel> models
    ) {
        double kmLo = access.getLowerKM();
        double kmHi = access.getUpperKM();

        log.debug("Prepare data for km range: " + kmLo + " - " + kmHi);

        FlowVelocityData[] data = new FlowVelocityData[models.size()];
        for (int i = 0, n = models.size(); i < n; i++) {
            FlowVelocityData d = new FlowVelocityData();

            prepareData(d, models.get(i), kmLo, kmHi);

            data[i] = d;
        }

        log.debug("Calculation contains " + data.length + " data items.");

        return new CalculationResult(data, this);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
