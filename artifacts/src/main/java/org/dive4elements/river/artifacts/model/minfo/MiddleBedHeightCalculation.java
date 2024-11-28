/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.BedHeightValue;
import org.dive4elements.river.artifacts.access.BedHeightAccess;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;


public class MiddleBedHeightCalculation extends Calculation {

    private static final Logger log =
        LogManager.getLogger(MiddleBedHeightCalculation.class);


    public CalculationResult calculate(BedHeightAccess access) {
        log.info("MiddleBedHeightCalculation.calculate");

        int[] singleIds = access.getBedHeightIDs();


        if (log.isDebugEnabled()) {
            Artifact artifact = access.getArtifact();

            log.debug("Artifact '" + artifact.identifier() + "' contains:");
            if (singleIds != null) {
                log.debug("   " + singleIds.length + " single bedheight ids");
            }
        }

        List<BedHeight> singles = getSingles(access, singleIds);

        return buildCalculationResult(access, singles);
    }


    protected List<BedHeight> getSingles(
        BedHeightAccess access,
        int[] ids
    ) {
        List<BedHeight> singles = new ArrayList<BedHeight>();

        for (int id: ids) {
            BedHeight s = BedHeight.getBedHeightById(id);

            if (s != null) {
                singles.add(s);
            }
            else {
                log.warn("Cannot find Single by id: " + id);
                // TODO ADD WARNING
            }
        }

        return singles;
    }


    protected CalculationResult buildCalculationResult(
        BedHeightAccess       access,
        List<BedHeight> singles
    ) {
        log.info("MiddleBedHeightCalculation.buildCalculationResult");

        double kmLo = access.getFrom();
        double kmHi = access.getTo();

        List<MiddleBedHeightData> data = new ArrayList<MiddleBedHeightData>();

        for (BedHeight single: singles) {
            MiddleBedHeightData d = prepareSingleData(single, kmLo, kmHi);

            if (d != null) {
                data.add(d);
            }
        }

        log.debug("Calculation results in " + data.size() + " data objects.");

        return new CalculationResult((MiddleBedHeightData[])
            data.toArray(new MiddleBedHeightData[data.size()]), this);
    }


    protected MiddleBedHeightData prepareSingleData(
        BedHeight single,
        double kmLo,
        double kmHi
    ) {
        log.debug("Prepare data for single: " + single.getDescription());

        List<BedHeightValue> values =
            BedHeightValue.getBedHeightValues(single, kmLo, kmHi);

        int year = single.getYear() != null ? single.getYear() : 0;

        String curElevModel = single.getCurElevationModel() != null ?
            single.getCurElevationModel().getName() : "";
        String oldElevModel = single.getOldElevationModel() != null ?
            single.getOldElevationModel().getName() : "";
        String riverElevModel = single.getRiver().getWstUnit() != null ?
            single.getRiver().getWstUnit().getName() : "";
        String type = single.getType() != null ?
            single.getType().getName() : "";
        String locationSystem = single.getLocationSystem() != null ?
            single.getLocationSystem().getName() : "";
        MiddleBedHeightData data = new MiddleBedHeightData(
            year,
            year,
            single.getEvaluationBy(),
            single.getDescription(),
            curElevModel,
            oldElevModel,
            riverElevModel,
            type,
            locationSystem);

        for (BedHeightValue value: values) {
            if (value.getHeight() != null) {
                double uncert = value.getUncertainty() != null ?
                    value.getUncertainty().doubleValue() : Double.NaN;
                double sounding = value.getSoundingWidth() != null ?
                    value.getSoundingWidth().doubleValue() : Double.NaN;
                double gap = value.getDataGap() != null ?
                    value.getDataGap().doubleValue() : Double.NaN;
                data.addAll(value.getStation().doubleValue(),
                    value.getHeight().doubleValue(),
                    uncert,
                    sounding,
                    gap,
                    false);
             }
            else {
                data.addAll(value.getStation().doubleValue(),
                    0,
                    0,
                    0,
                    0,
                    true);
            }
        }

        log.debug("Single contains " + values.size() + " values");

        return data;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
