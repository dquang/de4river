/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.access.BedDifferencesAccess;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;

/**
 * Perform calculation of differences of bed height (german Sohlhoehe).
 * The input are either single year data or epochs.
 */
public class BedDiffCalculation
extends Calculation
{
    private static final Logger log =
        LogManager.getLogger(BedDiffCalculation.class);

    protected String   river;
    protected int [][] heightIds;

    public BedDiffCalculation() {
    }

    public CalculationResult calculate(
        BedDifferencesAccess access,
        CallContext context
    ) {
        log.info("BedDiffCalculation.calculate");

        this.river     = access.getRiverName();
        this.heightIds = access.extractHeightIds(context);
        double from    = access.getFrom(true);
        double to      = access.getTo(true);

        BedDiffYearResult [] results = new BedDiffYearResult[heightIds.length];
        for (int i = 0; i < heightIds.length; i++) {
            BedHeightData [] pair = getHeightPair(heightIds[i], from, to);
            if (pair[0].getYear() == null || pair[1].getYear() == null) {
                addProblem("beddiff.missing.year");
            }
            results[i] = calculateYearDifference(pair, heightIds[i]);
        }

        return new CalculationResult(results, this);
    }

    /** Get two BedHeights from factory. */
    private static BedHeightData [] getHeightPair(
        int [] ids,
        double from,
        double to
    ) {
        return new BedHeightData [] {
            (BedHeightData)BedHeightFactory.getHeight(
                "single", ids[0], from, to),
            (BedHeightData)BedHeightFactory.getHeight(
                "single", ids[1], from, to)
        };
    }

    private BedDiffYearResult calculateYearDifference(
        BedHeightData[] pair,
        int[] ids
        ) {
        log.debug("BedDiffCalculation.calculateYearDifference");
        BedHeightData s1 = pair[0];
        BedHeightData s2 = pair[1];

        TDoubleArrayList stations = s1.getStations();
        int size = stations.size();

        TDoubleArrayList diffRes    = new TDoubleArrayList(size);
        TDoubleArrayList kms        = new TDoubleArrayList(size);
        TDoubleArrayList soundings1 = new TDoubleArrayList(size);
        TDoubleArrayList soundings2 = new TDoubleArrayList(size);
        TDoubleArrayList absolute   = new TDoubleArrayList(size);
        TDoubleArrayList gap1       = new TDoubleArrayList(size);
        TDoubleArrayList gap2       = new TDoubleArrayList(size);
        TDoubleArrayList heights1   = new TDoubleArrayList(size);
        TDoubleArrayList heights2   = new TDoubleArrayList(size);

        Integer range = null;
        if (s1.getYear() != null && s2.getYear() != null) {
            range = Math.abs(s1.getYear() - s2.getYear());
        }

        for (int i = 0; i < size; i++) {
            double station = stations.getQuick(i);
            double h1      = s1.getHeight(station);
            double h2      = s2.getHeight(station);
            double hDiff   = h1 - h2;

            if (!Double.isNaN(hDiff)) {
                diffRes.add(hDiff);
                kms.add(station);

                soundings1.add(s1.getSoundingWidth(station));
                soundings2.add(s2.getSoundingWidth(station));

                gap1.add(s1.getDataGap(station));
                gap2.add(s2.getDataGap(station));

                if (range != null) {
                    absolute.add((hDiff / range) * 100d);
                }
                heights1.add(h1);
                heights2.add(h2);
            }
        }
        return new BedDiffYearResult(
            kms,
            diffRes,
            heights1,
            heights2,
            soundings1,
            soundings2,
            absolute,
            gap1,
            gap2,
            s1.getYear(),
            s2.getYear(),
            s1.getName(),
            s2.getName(),
            ids[0],
            ids[1]);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
