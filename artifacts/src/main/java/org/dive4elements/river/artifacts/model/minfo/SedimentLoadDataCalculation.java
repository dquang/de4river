/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.model.minfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dive4elements.river.artifacts.access.SedimentLoadAccess;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData.Value;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadData.Station;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataValueFilter.And;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataValueFilter.IsEpoch;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataValueFilter.Not;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataValueFilter.SQTimeInterval;
import org.dive4elements.river.artifacts.model.minfo.SedimentLoadDataValueFilter.TimeRangeIntersects;
import org.dive4elements.river.utils.DoubleUtil;

public class SedimentLoadDataCalculation
extends      Calculation
{
    private static final Logger log = LogManager
        .getLogger(SedimentLoadDataCalculation.class);

    public static final int [] TOTAL_LOAD = {
        SedimentLoadData.GF_COARSE,
        SedimentLoadData.GF_FINE_MIDDLE,
        SedimentLoadData.GF_SAND,
        SedimentLoadData.GF_SUSP_SAND,
        SedimentLoadData.GF_SUSP_SEDIMENT
    };

    public static final int [] BED_LOAD = {
        SedimentLoadData.GF_COARSE,
        SedimentLoadData.GF_FINE_MIDDLE,
        SedimentLoadData.GF_SAND
    };

    public static final int [] BED_LOAD_SUSP_SAND = {
        SedimentLoadData.GF_COARSE,
        SedimentLoadData.GF_FINE_MIDDLE,
        SedimentLoadData.GF_SAND,
        SedimentLoadData.GF_SUSP_SAND
    };

    public static final int [] COARSE = {
        SedimentLoadData.GF_COARSE
    };

    public static final int [] FINE_MIDDLE = {
        SedimentLoadData.GF_FINE_MIDDLE
    };

    public static final int [] SAND = {
        SedimentLoadData.GF_SAND
    };

    public static final int [] SUSP_SAND = {
        SedimentLoadData.GF_SUSP_SAND
    };

    public static final int [] SUSP_SAND_BED = {
        SedimentLoadData.GF_SUSP_SAND_BED
    };

    public static final int [] SUSP_SEDIMENT = {
        SedimentLoadData.GF_SUSP_SEDIMENT
    };

    public static final class LoadSum {
        private String description;
        private int [] grainFractions;

        public LoadSum(String description, int [] grainFractions) {
            this.description = description;
            this.grainFractions = grainFractions;
        }
        public static final LoadSum make(
            String description,
            int [] grainFractions
        ) {
            return new LoadSum(description, grainFractions);
        }

        public String getDescription() {
            return description;
        }

        public int [] getGrainFractions() {
            return grainFractions;
        }

        public int getStationType() {
            return SedimentLoadData.measurementStationType(
                SedimentLoadData.grainFractionIndex(this.description));
        }
    } // class LoadSum

    public static final LoadSum [] LOAD_SUMS = {
        // Names are alignt to the grain_fractions table
        LoadSum.make("total",              TOTAL_LOAD),
        LoadSum.make("bed_load",           BED_LOAD),
        LoadSum.make("bed_load_susp_sand", BED_LOAD_SUSP_SAND),
        LoadSum.make("coarse",             COARSE),
        LoadSum.make("fine_middle",        FINE_MIDDLE),
        LoadSum.make("sand",               SAND) ,
        LoadSum.make("susp_sand",          SUSP_SAND),
        LoadSum.make("susp_sand_bed",      SUSP_SAND_BED),
        LoadSum.make("suspended_sediment", SUSP_SEDIMENT),
    };

    public static class Sum implements Value.Visitor {

        protected int    n;
        protected double sum;

        public Sum() {
        }

        public double getSum() {
            return sum;
        }

        public int getN() {
            return n;
        }

        public void reset() {
            n   = 0;
            sum = 0.0;
        }

        @Override
        public void visit(Value value) {
            sum += value.getValue();
            ++n;
        }
    } // class Sum

    private String   river;
    private String   yearEpoch;
    private String   unit;
    private int [][] epochs;
    private int []   years;
    private double   from;
    private double   to;
    /* The sq time interval to use. 0 means this is ignored. */
    private int      sqTiId;


    public SedimentLoadDataCalculation() {
    }

    public CalculationResult calculate(SedimentLoadAccess access) {
        log.info("SedimentLoadDataCalculation.calculate");

        String river     = access.getRiverName();
        String yearEpoch = access.getYearEpoch();
        String unit      = access.getUnit();

        int [] years  = null;
        int [][] epochs = null;

        double from = access.getLowerKM();
        double to   = access.getUpperKM();

        Integer sqTiId = access.getSQTiId();

        if (yearEpoch.equals("year")) {
            years = access.getYears();
        }
        else if (yearEpoch.equals("epoch")) {
            epochs = access.getEpochs();
        }
        else {
            addProblem("minfo.missing.year_epoch");
        }

        if (river == null) {
            // TODO: i18n
            addProblem("minfo.missing.river");
        }

        if (years == null && epochs == null) {
            addProblem("minfo.missing.time");
        }

        if (!hasProblems()) {
            this.river     = river;
            this.yearEpoch = yearEpoch;
            this.unit      = unit;
            this.years     = years;
            this.epochs    = epochs;
            this.from      = from;
            this.to        = to;
            this.sqTiId    = sqTiId;
            return internalCalculate();
        }

        return error(null);
    }

    private CalculationResult error(String msg) {
        if (msg != null) addProblem(msg);
        return new CalculationResult(this);
    }

    private CalculationResult internalCalculate() {
        if ("year".equals(yearEpoch))      return calculateYears();
        if ("epoch".equals(yearEpoch))     return calculateEpochs();

        // TODO: i18n
        return error("minfo.sedimentload.unknown.calc.mode");
    }

    private CalculationResult calculateYears() {
        SedimentLoadData sld =
            SedimentLoadDataFactory.INSTANCE.getSedimentLoadData(river);
        if (sld == null) {
            return error("minfo.sedimentload.no.data");
        }

        SedimentLoadDataResult sldr = new SedimentLoadDataResult();

        Not notEpochs = new Not(IsEpoch.INSTANCE);

        SQTimeInterval sqTiFilter = new SQTimeInterval(sqTiId);

        Sum sum = new Sum();

        SedimentDensity sd = getSedimentDensity();

        for (int i = 0; i < years.length; i++) {
            int year = years[i];
            Value.Filter filter = new And(notEpochs)
                .add(new TimeRangeIntersects(year)).add(sqTiFilter);
            String period = Integer.toString(year);

            for (LoadSum ls: LOAD_SUMS) {

                double [][] result = sum(
                    sld, ls.getGrainFractions(), ls.getStationType(),
                    filter, sum);

                if (result[0].length == 0 || DoubleUtil.isNaN(result[1])) {
                    addProblem("sedimentload.missing.fraction." +
                               ls.getDescription(), period);
                    continue;
                }

                transformT2M3(sd, year, result);

                SedimentLoadDataResult.Fraction sldrf =
                    new SedimentLoadDataResult.Fraction(
                        ls.getDescription(), result, period);

                sldr.addFraction(sldrf);
            }
        }
        return new CalculationResult(sldr, this);
    }

    private CalculationResult calculateEpochs() {
        SedimentLoadData sld =
            SedimentLoadDataFactory.INSTANCE.getSedimentLoadData(river);
        if (sld == null) {
            return error("minfo.sedimentload.no.data");
        }

        SedimentLoadDataResult sldr = new SedimentLoadDataResult();

        Sum sum = new Sum();

        SedimentDensity sd = getSedimentDensity();

        // They are not epochs, they are single years!
        Not notEpochs = new Not(IsEpoch.INSTANCE);

        SQTimeInterval sqTiFilter = new SQTimeInterval(sqTiId);

        for (int [] epoch: epochs) {
            int min = Math.min(epoch[0], epoch[1]);
            int max = Math.max(epoch[0], epoch[1]);

            String period = Integer.toString(epoch[0]) + " - " +
                Integer.toString(epoch[1]);

            for (LoadSum ls: LOAD_SUMS) {

                List<double [][]> results = new ArrayList<double [][]>();

                for (int year = min; year <= max; ++year) {
                    Value.Filter filter = new And(notEpochs)
                        .add(new TimeRangeIntersects(year)).add(sqTiFilter);

                    double [][] result = sum(
                        sld, ls.getGrainFractions(), ls.getStationType(),
                        filter, sum);

                    if (result[0].length == 0 || DoubleUtil.isNaN(result[1])) {
                        addProblem("sedimentload.missing.fraction." +
                            ls.getDescription(), ((Integer)year).toString());
                    }

                    transformT2M3(sd, year, result);
                    results.add(result);
                }

                if (results.size() == 0) {
                    addProblem("sedimentload.missing.fraction." +
                               ls.getDescription(), period);
                    continue;
                }

                double [][] result = average(results);

                if (!DoubleUtil.isNaN(result[1])) {
                    SedimentLoadDataResult.Fraction sldrf =
                        new SedimentLoadDataResult.Fraction(
                            ls.getDescription(), result, period);
                    sldr.addFraction(sldrf);
                }
                else {
                    addProblem("sedimentload.missing.fraction." +
                               ls.getDescription(), period);
                }
            }

        }
        return new CalculationResult(sldr, this);
    }

    private final boolean inM3() {
        return unit.equals("m3_per_a");
    }

    private SedimentDensity getSedimentDensity() {
        return inM3()
            ? SedimentDensityFactory.getSedimentDensity(river, from, to)
            : null;
    }

    private static void transformT2M3(
        SedimentDensity sd,
        int year,
        double [][] data
    ) {
        if (sd == null) {
            return;
        }
        double [] kms = data[0];
        double [] values = data[1];
        for (int i = 0; i < kms.length; ++i) {
            if (Double.isNaN(kms[i]) || Double.isNaN(kms[i])) {
                continue;
            }
            double density = sd.getDensity(kms[i], year);
            values[i] /= density;
        }
    }

    public double[][] sum(
        SedimentLoadData sld,
        int []           grainFractions,
        int              lsSType,
        Value.Filter     filter,
        Sum              sum
    ) {
        List<Station> stations = sld.findStations(from, to);

        double [] values = new double[grainFractions.length];

        double [][] result = new double[2][stations.size()];

        for (int j = 0, S = stations.size(); j < S; ++j) {
            Station station = stations.get(j);
            int sType = station.getType();

            for (int i = 0; i < grainFractions.length; ++i) {
                int gf = grainFractions[i];
                int gfSType = SedimentLoadData.measurementStationType(gf);

                sum.reset();

                // Add current single fraction at current station
                station.filterGrainFraction(gf, filter, sum);

                if (gfSType == Station.UNKNOWN) {
                    log.error("No measurement station type defined for " +
                              "fraction-index" + gf);
                }
                else {
                    if (lsSType != Station.BED_LOAD &&
                        lsSType != Station.SUSPENDED &&
                        (sType == Station.BED_LOAD ||
                         sType == Station.SUSPENDED) &&
                        sum.getN() == 0) {
                        /* In case the station-type of the load sum is
                           a combined type and we are at non-combined station:
                           we need to add values from previous station of
                           the other type for fractions not given here. */
                        int otherType = sType == Station.BED_LOAD ?
                            Station.SUSPENDED : Station.BED_LOAD;
                        Station prev = station.prevByType(otherType);
                        if (prev != null) {
                            prev.filterGrainFraction(gf, filter, sum);
                        }
                    }
                }

                if (sum.getN() == 0) {
                    values[i] = Double.NaN;
                } else {
                    values[i] = sum.getSum();
                }
            }
            result[0][j] = station.getStation();
            result[1][j] = DoubleUtil.sum(values);
        }

        return result;
    }

    private static final class XSum {
        private double sum;
        private int n;
        public XSum() {
        }
        public void add(double v) {
            sum += v;
            ++n;
        }
        public double avg() {
            return sum/n;
        }
    }

    private static double [][] average(List<double [][]> data) {

        TreeMap<Double, XSum> map = new TreeMap<Double, XSum>();

        for (double [][] pair: data) {
            double [] kms = pair[0];
            double [] vs = pair[1];
            for (int i = 0; i < kms.length; ++i) {
                double km = kms[i];
                double v = vs[i];
                if (Double.isNaN(km)) {
                    continue;
                }
                XSum xsum = map.get(km);
                if (xsum == null) {
                    map.put(km, xsum = new XSum());
                }
                xsum.add(v);
            }
        }

        double [][] result = new double[2][map.size()];
        int i = 0;
        for (Map.Entry<Double, XSum> entry: map.entrySet()) {
            result[0][i] = entry.getKey();
            result[1][i] = entry.getValue().avg();
            ++i;
        }

        return result;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
