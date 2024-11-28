/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import gnu.trove.TDoubleArrayList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.access.BedQualityAccess;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.backend.SedDBSessionHolder;


public class BedQualityCalculation extends Calculation {

    private static final Logger log = LogManager
        .getLogger(BedQualityCalculation.class);

    protected String river;
    protected double from;
    protected double to;
    protected List<String> bedDiameter;
    protected List<String> bedloadDiameter;
    protected List<DateRange> ranges;

    public BedQualityCalculation() {
    }

    public CalculationResult calculate(BedQualityAccess access) {
        log.info("BedQualityCalculation.calculate");

        String river = access.getRiverName();
        Double from = access.getFrom();
        Double to = access.getTo();
        List<String> bedDiameter = access.getBedDiameter();
        List<String> bedloadDiameter = access.getBedloadDiameter();
        List<DateRange> ranges = access.getDateRanges();

        if (river == null) {
            // TODO: i18n
            addProblem("minfo.missing.river");
        }

        if (from == null) {
            // TODO: i18n
            addProblem("minfo.missing.from");
        }

        if (to == null) {
            // TODO: i18n
            addProblem("minfo.missing.to");
        }

        if (ranges == null) {
            // TODO: i18n
            addProblem("minfo.missing.periods");
        }

        if (!hasProblems()) {
            this.river = river;
            this.from = from;
            this.to = to;
            this.ranges = ranges;
            this.bedDiameter = bedDiameter;
            this.bedloadDiameter = bedloadDiameter;

            SedDBSessionHolder.acquire();
            try {
                return internalCalculate();
            }
            finally {
                SedDBSessionHolder.release();
            }
        }

        return new CalculationResult();
    }

    /** Adds non empty values to a result and adds Problems for empty ones.*/
    protected void addValuesToResult(BedQualityResult result,
                                     BedQualityResultValue[] values) {
        for (BedQualityResultValue value: values) {
            if (!value.isInterpolateable()) {
                if (value.isDiameterResult()) {
                    addProblem("bedquality.missing.diameter." +
                            value.getType(), value.getName().toUpperCase(),
                            result.getDateRange().getFrom(),
                            result.getDateRange().getTo());
                } else {
                    addProblem("bedquality.missing." + value.getName() + "." +
                            value.getType(), result.getDateRange().getFrom(),
                            result.getDateRange().getTo());
                }
                if (!value.isEmpty() && !value.isNaN()) {
                    // we want to keep single point results
                    result.add(value);
                }
            } else {
                result.add(value);
            }
        }
    }


    protected CalculationResult internalCalculate() {

        List<BedQualityResult> results = new LinkedList<BedQualityResult>();
        // Calculate for all time periods.
        for (DateRange dr : ranges) {
            QualityMeasurements loadMeasurements =
                QualityMeasurementFactory.getBedloadMeasurements(
                    river,
                    from,
                    to,
                    dr.getFrom(),
                    dr.getTo());
            QualityMeasurements bedMeasurements =
                QualityMeasurementFactory.getBedMeasurements(
                    river,
                    from,
                    to,
                    dr.getFrom(),
                    dr.getTo());
            BedQualityResult result = new BedQualityResult();
            result.setDateRange(dr);
            if (!bedDiameter.isEmpty()) {
                log.debug("Bed diameter is not empty + " + bedDiameter);
                addValuesToResult(
                    result, calculateBedParameter(bedMeasurements));
                for (String bd : bedDiameter) {
                    addValuesToResult(
                        result, calculateBed(bedMeasurements, bd));
                }
            }
            if (!bedloadDiameter.isEmpty()) {
                for (String bld : bedloadDiameter) {
                    addValuesToResult(
                        result, calculateBedload(loadMeasurements, bld));
                }
            }
            results.add(result);
        }

        return new CalculationResult(
            results.toArray(new BedQualityResult[results.size()]), this);
    }

    private BedQualityResultValue[] calculateBedParameter(
        QualityMeasurements qm
    ) {
        List<Double> kms = qm.getKms();
        TDoubleArrayList location = new TDoubleArrayList();
        QualityMeasurements capFiltered = filterCapMeasurements(qm);
        QualityMeasurements subFiltered = filterSubMeasurements(qm);
        TDoubleArrayList porosityCap = new TDoubleArrayList();
        TDoubleArrayList porositySub = new TDoubleArrayList();
        TDoubleArrayList densityCap = new TDoubleArrayList();
        TDoubleArrayList densitySub = new TDoubleArrayList();

        for(double km : kms) {
            double[] pCap = calculatePorosity(capFiltered, km);
            double[] pSub = calculatePorosity(subFiltered, km);
            double[] dCap = calculateDensity(capFiltered, pCap);
            double[] dSub = calculateDensity(subFiltered, pSub);

            double pCapRes = 0d;
            double pSubRes = 0d;
            double dCapRes = 0d;
            double dSubRes = 0d;
            for (int i = 0; i < pCap.length; i++) {
                pCapRes += pCap[i];
                dCapRes += dCap[i];
            }
            for (int i = 0; i < pSub.length; i++) {
                pSubRes += pSub[i];
                dSubRes += dSub[i];
            }
            location.add(km);
            porosityCap.add((pCapRes / pCap.length) * 100 );
            porositySub.add((pSubRes / pSub.length) * 100);
            densityCap.add((dCapRes / dCap.length) / 1000);
            densitySub.add((dSubRes / dSub.length) / 1000);

        }
        return new BedQualityResultValue[] {
                new BedQualityResultValue("porosity",
                        new double[][] {location.toNativeArray(),
                                        porositySub.toNativeArray()},
                        "sublayer"),
                new BedQualityResultValue("porosity",
                        new double[][] {location.toNativeArray(),
                                        porosityCap.toNativeArray()},
                        "toplayer"),
                new BedQualityResultValue("density",
                        new double[][] {location.toNativeArray(),
                                        densitySub.toNativeArray()},
                        "sublayer"),
                new BedQualityResultValue("density",
                        new double[][] {location.toNativeArray(),
                                        densityCap.toNativeArray()},
                        "toplayer")};
    }

    protected BedQualityResultValue[] calculateBed(
        QualityMeasurements qm,
        String diameter
    ) {
        List<Double> kms = qm.getKms();
        TDoubleArrayList location = new TDoubleArrayList();
        TDoubleArrayList avDiameterCap = new TDoubleArrayList();
        TDoubleArrayList avDiameterSub = new TDoubleArrayList();
        QualityMeasurements capFiltered = filterCapMeasurements(qm);
        QualityMeasurements subFiltered = filterSubMeasurements(qm);

        for (double km : kms) {
            //Filter cap and sub measurements.
            List<QualityMeasurement> cm = capFiltered.getMeasurements(km);
            List<QualityMeasurement> sm = subFiltered.getMeasurements(km);

            double avCap = calculateAverage(cm, diameter);
            double avSub = calculateAverage(sm, diameter);
            location.add(km);
            avDiameterCap.add(avCap * 1000);// bring to mm.
            avDiameterSub.add(avSub * 1000);
        }
        return new BedQualityResultValue[] {
                new BedQualityResultValue(diameter,
                        new double[][] {location.toNativeArray(),
                                        avDiameterSub.toNativeArray()},
                        "sublayer"),
                new BedQualityResultValue(diameter,
                        new double[][] {location.toNativeArray(),
                                        avDiameterCap.toNativeArray()},
                        "toplayer")};
    }

    private double[] calculateDensity(
        QualityMeasurements capFiltered,
        double[] porosity
    ) {
        double[] density = new double[porosity.length];
        for (int i = 0; i < porosity.length; i++) {
            density[i] = (1 - porosity[i]) * 2650;
        }
        return density;
    }

    private double[] calculatePorosity(
        QualityMeasurements capFiltered,
        double km
    ) {
        List<QualityMeasurement> list = capFiltered.getMeasurements(km);
        double[] results = new double[list.size()];
        int i = 0;
        for (QualityMeasurement qm : list) {
            double deviation = calculateDeviation(qm);
            double p = calculateP(qm);
            double porosity = 0.362 - 0.088 * deviation + 0.219 * p;
            results[i] = porosity;
            i++;
        }

        return results;
    }

    protected BedQualityResultValue[] calculateBedload(
        QualityMeasurements qm,
        String diameter
    ) {
        List<Double> kms = qm.getKms();
        TDoubleArrayList location = new TDoubleArrayList();
        TDoubleArrayList avDiameter = new TDoubleArrayList();
        for (double km : kms) {
            List<QualityMeasurement> measurements = qm.getMeasurements(km);
            double mid = calculateAverage(measurements, diameter);
            location.add(km);
            avDiameter.add(mid * 1000);
        }
        return new BedQualityResultValue[] {
            new BedQualityResultValue(diameter,
                new double[][] {location.toNativeArray(),
                                avDiameter.toNativeArray()},
                "bedload")};
    }

    protected double calculateAverage(
        List<QualityMeasurement> list,
        String diameter
    ) {
        double av = 0;
        for (QualityMeasurement qm : list) {
            av += qm.getDiameter(diameter);
        }
        return av/list.size();
    }

    protected QualityMeasurements filterCapMeasurements(
        QualityMeasurements qms
    ) {
        List<QualityMeasurement> result = new LinkedList<QualityMeasurement>();
        for (QualityMeasurement qm : qms.getMeasurements()) {
            if (qm.getDepth1() == 0d && qm.getDepth2() <= 0.3) {
                result.add(qm);
            }
        }
        return new QualityMeasurements(result);
    }

    protected QualityMeasurements filterSubMeasurements(
        QualityMeasurements qms
    ) {
        List<QualityMeasurement> result = new LinkedList<QualityMeasurement>();
        for (QualityMeasurement qm : qms.getMeasurements()) {
            if (qm.getDepth1() > 0d && qm.getDepth2() <= 0.5) {
                result.add(qm);
            }
        }
        return new QualityMeasurements(result);
    }

    public double calculateDeviation(QualityMeasurement qm) {
        Map<String, Double> dm = qm.getAllDiameter();
        int size = dm.size();

        double phiM    = 0;
        double [] phis = new double[size];
        double [] ps   = new double[size];
        double scale   = -1d/Math.log(2d);

        int i = 0;
        for (Map.Entry<String, Double> entry: dm.entrySet()) {
            double phi = scale*Math.log(entry.getValue());
            double p   = calculateWeight(qm, entry.getKey());
            phiM += phi * p;
            ps[i]   = p;
            phis[i] = phi;
            i++;
        }

        double sig = 0d;
        for (i = 0; i < size; i++) {
            sig += ps[i] * Math.pow((phis[i] - phiM), 2);
        }
        double deviation = Math.sqrt(sig);
        return deviation;
    }

    protected double calculateP(QualityMeasurement qm) {
        return calculateWeight(qm, "dmin");
    }

    public double calculateWeight(QualityMeasurement qm, String diameter) {
        Map<String, Double> dm = qm.getAllDiameter();
        double value = qm.getDiameter(diameter);

        double sum = 0d;
        for (Double d : dm.values()) {
            sum += d.doubleValue();
        }
        double weight = sum/100*value;
        return weight;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
