/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.river.artifacts.access.Calculation4Access;
import org.dive4elements.river.artifacts.access.RangeAccess;

import org.dive4elements.river.artifacts.geom.Lines;

import org.dive4elements.river.artifacts.model.Calculation1;
import org.dive4elements.river.artifacts.model.Calculation2;
import org.dive4elements.river.artifacts.model.Calculation3;
import org.dive4elements.river.artifacts.model.Calculation4;
import org.dive4elements.river.artifacts.model.Calculation5;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.DischargeTables;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.WQCKms;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.WW;
import org.dive4elements.river.artifacts.model.WstValueTable;
import org.dive4elements.river.artifacts.model.WstValueTableFactory;

import org.dive4elements.river.artifacts.model.extreme.ExtremeResult;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;

import org.dive4elements.river.artifacts.states.LocationDistanceSelect;

import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.FastCrossSectionLine;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;

import org.dive4elements.river.utils.DoubleUtil;
import org.dive4elements.river.utils.RiverUtils;

import gnu.trove.TDoubleArrayList;

import java.awt.geom.Point2D;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * The default WINFO artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WINFOArtifact
extends      D4EArtifact
implements   FacetTypes, WaterLineArtifact {

    /** The log for this class. */
    private static Logger log = LogManager.getLogger(WINFOArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "winfo";

    /** XPath */
    public static final String XPATH_STATIC_UI =
        "/art:result/art:ui/art:static";

    /** The default number of steps between the start end end of a selected Q
     * range. */
    public static final int DEFAULT_Q_STEPS = 30;

    private static final String [] INACTIVES = new String[] {
        LONGITUDINAL_Q,
        DURATION_Q,
        STATIC_WQKMS_Q
    };

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance().register(
            ARTIFACT_NAME,
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   outputName
                ) {
                    String fname = facet.getName();
                    if ((fname.equals(MAINVALUES_Q)
                        || fname.equals(MAINVALUES_W))
                        && outputName.equals("computed_discharge_curve"))
                    {
                        return Boolean.FALSE;
                    }
                    return !StringUtils.contains(fname, INACTIVES);
                }
            });
    }

    /**
     * The default constructor.
     */
    public WINFOArtifact() {
    }



    /**
     * Returns the name of the concrete artifact.
     *
     * @return the name of the concrete artifact.
     */
    @Override
    public String getName() {
        return ARTIFACT_NAME;
    }

    protected static boolean reportGeneratedWs(
        Calculation report,
        double []   ws
    ) {
        if (ws == null || ws.length < 2) {
            return false;
        }

        double  lastW = ws[0];
        boolean alreadyReported = false;

        for (int i = 1; i < ws.length; ++i) {
            if (Math.abs(lastW - ws[i]) < 1e-5) {
                if (!alreadyReported) {
                    alreadyReported = true;
                    report.addProblem("more.than.one.q.for.w", ws[i]);
                }
            }
            else {
                alreadyReported = false;
            }
            lastW = ws[i];
        }

        return true;
    }


    //
    // METHODS FOR RETRIEVING COMPUTED DATA FOR DIFFERENT CHART TYPES
    //
    //
    /**
     * Returns the data that is computed by a waterlevel computation.
     *
     * @return an array of data triples that consist of W, Q and Kms.
     */
    public CalculationResult getWaterlevelData() {
        return this.getWaterlevelData(null);
    }

    protected CalculationResult getDischargeLongitudinalSectionData() {
        // TODO: This caluclation should be cached as it is quite expensive.
        return new Calculation4(new Calculation4Access(this)).calculate();
    }

    /**
     * Returns the data that is computed by a waterlevel computation.
     *
     * @return an array of data triples that consist of W, Q and Kms.
     */
    public CalculationResult getWaterlevelData(CallContext context)
    {
        log.debug("WINFOArtifact.getWaterlevelData");

        String calculationMode = getDataAsString("calculation_mode");

        // If this WINFO-Artifact has a calculation trait.
        if (calculationMode != null) {
            if (calculationMode.equals("calc.discharge.longitudinal.section")
            ) {
                return getDischargeLongitudinalSectionData();
            }
            else if (calculationMode.equals("calc.extreme.curve")) {
                return (CalculationResult)
                    this.compute(context, ComputeType.ADVANCE, false);
            }
            else if (calculationMode.equals("calc.w.differences")) {
                return (CalculationResult)
                    this.compute(context, ComputeType.ADVANCE, true);
            }
            else {
                log.warn("Unhandled calculation_mode " + calculationMode);
            }
        }

        // Otherwise get it from parameterization.
        River river = RiverUtils.getRiver(this);
        if (river == null) {
            return error(new WQKms[0], "no.river.selected");
        }

        double[] kms = getKms();
        if (kms == null) {
            return error(new WQKms[0], "no.kms.selected");
        }

        double[] qs   = getQs();
        double[] ws   = null;
        boolean  qSel = true;

        Calculation report = new Calculation();

        if (qs == null) {
            log.debug("Determine Q values based on a set of W values.");
            qSel = false;
            ws   = getWs();
            double [][] qws = getQsForWs(ws, report);
            if (qws == null || qws.length == 0) {
                return error(new WQKms[0], "converting.ws.to.qs.failed");
            }
            qs = qws[0];

            if (reportGeneratedWs(report, qws[1])) {
                ws = qws[1];
            }
        }

        WstValueTable wst = WstValueTableFactory.getTable(river);
        if (wst == null) {
            return error(new WQKms[0], "no.wst.for.selected.river");
        }

        RangeAccess rangeAccess = new RangeAccess(this);
        double [] range = rangeAccess.getKmRange();
        if (range == null) {
            return error(new WQKms[0], "no.range.found");
        }

        double refKm;

        if (isFreeQ() || isFreeW()) {
            refKm = range[0];
            log.debug("'free' calculation (km " + refKm + ")");
        }
        else {
            Gauge gauge = river.determineRefGauge(
                range, rangeAccess.isRange());

            if (gauge == null) {
                return error(
                    new WQKms[0], "no.gauge.found.for.km", range[0]);
            }

            refKm = gauge.getStation().doubleValue();

            log.debug(
                "reference gauge: " + gauge.getName() + " (km " + refKm + ")");
        }

        return computeWaterlevelData(kms, qs, ws, wst, refKm, report);
    }


    /**
     * Computes the data of a waterlevel computation based on the interpolation
     * in WstValueTable.
     *
     * @param kms The kilometer values.
     * @param qs The discharge values.
     * @param wst The WstValueTable used for the interpolation.
     *
     * @return an array of data triples that consist of W, Q and Kms.
     */
    public static CalculationResult computeWaterlevelData(
        double []     kms,
        double []     qs,
        double []     ws,
        WstValueTable wst,
        double        refKm,
        Calculation   report
    ) {
        log.info("WINFOArtifact.computeWaterlevelData");

        Calculation1 calc1 = new Calculation1(kms, qs, ws, refKm);

        if (report != null) {
            calc1.addProblems(report);
        }

        return calc1.calculate(wst);
    }


    /**
     * Returns the data that is computed by a duration curve computation.
     *
     * @return the data computed by a duration curve computation.
     */
    public CalculationResult getDurationCurveData() {
        log.debug("WINFOArtifact.getDurationCurveData");

        RangeAccess rangeAccess = new RangeAccess(this);

        River r = rangeAccess.getRiver();
        if (r == null) {
            return error(null, "no.river.selected");
        }

        double[] locations = rangeAccess.getLocations();
        if (locations == null) {
            return error(null, "no.locations.selected");
        }

        Gauge g = r.determineGaugeByPosition(locations[0]);
        if (g == null) {
           return error(null, "no.gauge.selected");
        }

        WstValueTable wst = WstValueTableFactory.getTable(r);
        if (wst == null) {
            return error(null, "no.wst.for.river");
        }

        return computeDurationCurveData(g, wst, locations[0]);
    }


    /**
     * Computes the data used to create duration curves.
     *
     * @param gauge The selected gauge.
     * @param location The selected location.
     *
     * @return the computed data.
     */
    public static CalculationResult computeDurationCurveData(
        Gauge         gauge,
        WstValueTable wst,
        double        location)
    {
        log.info("WINFOArtifact.computeDurationCurveData");

        Object[] obj = gauge.fetchDurationCurveData();

        int[]    days = (int[]) obj[0];
        double[] qs   = (double[]) obj[1];

        Calculation3 calculation = new Calculation3(location, days, qs);

        return calculation.calculate(wst);
    }


    /**
     * Returns the data that is computed by a discharge curve computation.
     *
     * @return the data computed by a discharge curve computation.
     */
    public CalculationResult getComputedDischargeCurveData()
    throws NullPointerException
    {
        log.debug("WINFOArtifact.getComputedDischargeCurveData");

        River r = RiverUtils.getRiver(this);

        if (r == null) {
            return error(new WQKms[0], "no.river.selected");
        }

        RangeAccess rangeAccess = new RangeAccess(this);
        double[] locations = rangeAccess.getLocations();

        if (locations == null) {
            return error(new WQKms[0], "no.locations.selected");
        }

        WstValueTable wst = WstValueTableFactory.getTable(r);
        if (wst == null) {
            return error(new WQKms[0], "no.wst.for.river");
        }

        return computeDischargeCurveData(wst, locations[0]);
    }


    /**
     * Computes the data used to create computed discharge curves.
     *
     * @param wst The WstValueTable that is used for the interpolation (river-
     *            bound).
     * @param location The location where the computation should be based on.
     *
     * @return an object that contains tuples of W/Q values at the specified
     * location.
     */
    public static CalculationResult computeDischargeCurveData(
        WstValueTable wst,
        double location)
    {
        log.info("WINFOArtifact.computeDischargeCurveData");

        Calculation2 calculation = new Calculation2(location);

        return calculation.calculate(wst);
    }


    /** Create CalculationResult with data and message. */
    protected static final CalculationResult error(Object data, String msg) {
        return new CalculationResult(data, new Calculation(msg));
    }

    /** Create CalculationResult with data and message with args. */
    protected static final CalculationResult error(
        Object data,
        String msg,
        Object ... args
    ) {
        return new CalculationResult(data, new Calculation(msg, args));
    }


    /**
     * Returns the data that is computed by a reference curve computation.
     *
     * @return the data computed by a reference curve computation.
     */
    public CalculationResult getReferenceCurveData(CallContext context) {

        Double startKm = getReferenceStartKm();

        if (startKm == null) {
            return error(new WW[0], "no.reference.start.km");
        }

        double [] endKms = getReferenceEndKms();

        if (endKms == null || endKms.length == 0) {
            return error(new WW[0], "no.reference.end.kms");
        }

        Calculation5 calc5 = new Calculation5(startKm, endKms);

        River r = RiverUtils.getRiver(this);
        if (r == null) {
            return error(new WW[0], "no.river.found");
        }

        WstValueTable wst = WstValueTableFactory.getTable(r);
        if (wst == null) {
            return error(new WW[0], "no.wst.for.river");
        }

        Map<Double, Double> kms2gaugeDatums = r.queryGaugeDatumsKMs();

        return calc5.calculate(wst, kms2gaugeDatums, context);
    }


    /** Get reference (start) km. */
    public Double getReferenceStartKm() {
        StateData sd = getData("reference_startpoint");

        if (sd == null) {
            log.warn("no reference start given.");
            return null;
        }

        log.debug("Reference start km given: " + sd.getValue());

        String input = (String) sd.getValue();

        if (input == null || (input = input.trim()).length() == 0) {
            log.warn("reference start string is empty.");
            return null;
        }

        try {
            return Double.valueOf(input);
        }
        catch (NumberFormatException nfe) {
            log.warn("reference start string is not numeric.");
        }

        return null;
    }


    /**
     * Get end kms for reference curve (null if none).
     */
    public double [] getReferenceEndKms() {
        StateData sd = getData("reference_endpoint");

        if (sd == null) {
            log.warn("no reference end given.");
            return null;
        }
        else {
            log.debug("Reference end km : " + sd.getValue());
        }

        String input = (String) sd.getValue();

        if (input == null || (input = input.trim()).length() == 0) {
            log.warn("reference end string is empty.");
            return null;
        }

        TDoubleArrayList endKms = new TDoubleArrayList();

        for (String part: input.split("\\s+")) {
            try {
                double km = Double.parseDouble(part);
                if (!endKms.contains(km)) {
                    endKms.add(km);
                }
            }
            catch (NumberFormatException nfe) {
                log.warn("reference end string is not numeric.");
            }
        }

        return endKms.toNativeArray();
    }


    /**
     * Get corrected waterline against surface/profile.
     */
    public Lines.LineData waterLineC(int idx, FastCrossSectionLine csl) {
        List<Point2D> points = csl.getPoints();

        WQKms[] wqckms = (WQKms[])
            getDischargeLongitudinalSectionData().getData();

        // Find index of km.
        double wishKM = csl.getKm();

        // Find W/C at km, linear naive approach.
        WQCKms triple = (WQCKms) wqckms[idx-1];

        if (triple.size() == 0) {
            log.warn("Calculation of c/waterline is empty.");
            return Lines.createWaterLines(points, 0.0f);
        }

        // Linear seach in WQKms for closest km.
        double old_dist_wish = Math.abs(wishKM - triple.getKm(0));
        double last_c = triple.getC(0);

        for (int i = 0, T = triple.size(); i < T; i++) {
            double diff = Math.abs(wishKM - triple.getKm(i));
            if (diff > old_dist_wish) {
                break;
            }
            last_c = triple.getC(i);
            old_dist_wish = diff;
        }

        return Lines.createWaterLines(points, last_c);
    }


    /**
     * Get points of line describing the surface of water at cross section.
     *
     * @param idx Index for getWaterlevelData.
     * @param csl The profile/surface to fill with water.
     * @param nextIgnored Ignored in this implementation of WaterLineArtifact.
     * @param prevIgnored Ignored in this implementation of WaterLineArtifact.
     *
     * @return an array holding coordinates of points of surface of water (
     *         in the form {{x1, x2} {y1, y2}} ).
     */
    @Override
    public Lines.LineData getWaterLines(int idx, FastCrossSectionLine csl,
        double nextIgnored, double prevIgnored, CallContext context) {
        log.debug("getWaterLines(" + idx + ")");

        List<Point2D> points = csl.getPoints();

        // Need W at km
        Object waterlevelResult = getWaterlevelData(context).getData();
        WQKms [] wqkms;

        if (waterlevelResult instanceof ExtremeResult) {
            wqkms = ((ExtremeResult) waterlevelResult).getWQKms();
        }
        else {
            wqkms = (WQKms[]) waterlevelResult;
        }

        if (wqkms.length == 0) {
            log.error("No WQKms found.");
            return Lines.createWaterLines(points, 0.0f);
        }

        if (wqkms.length <= idx) {
            log.error("getWaterLines() requested index ("
                         + idx + " not found.");
            return waterLineC(idx, csl);
        }

        // Find W at km, linear naive approach.
        WQKms triple = wqkms[idx];

        // Find index of km.
        double wishKM = csl.getKm();

        if (triple.size() == 0) {
            log.warn("Calculation of waterline is empty.");
            return Lines.createWaterLines(points, 0.0f);
        }

        // Early abort if we would need to extrapolate.
        int T = triple.size();
        double max_km = triple.getKm(T-1), min_km = triple.getKm(0);
        if (wishKM < min_km || wishKM > max_km) {
            // TODO Does this have to be done in the other WaterlineArtifact
            //      implementations, too?
            log.warn("Will not extrapolate waterlevels.");
            return Lines.createWaterLines(points, 0.0f);
        }

        int old_idx = 0;

        // Linear seach in WQKms for closest km.
        double old_dist_wish = Math.abs(wishKM - triple.getKm(0));
        double last_w = triple.getW(0);

        for (int i = 0; i < T; i++) {
            double diff = Math.abs(wishKM - triple.getKm(i));
            if (diff > old_dist_wish) {
                break;
            }
            last_w = triple.getW(i);
            old_dist_wish = diff;
        }

        return Lines.createWaterLines(points, last_w);
    }


    /**
     * Returns the Qs for a number of Ws.
     *
     * @param ws An array of W values.
     *
     * @return an array of Q values.
     */
    public double [][] getQsForWs(double[] ws, Calculation report) {

        if (ws == null) {
            log.error("getQsForWs: ws == null");
            return null;
        }

        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("D4EArtifact.getQsForWs");
        }

        River r = RiverUtils.getRiver(this);
        if (r == null) {
            log.warn("no river found");
            return null;
        }

        RangeAccess rangeAccess = new RangeAccess(this);
        double [] range = rangeAccess.getKmRange();
        if (range == null) {
            log.warn("no ranges found");
            return null;
        }

        if (isFreeW()) {
            log.debug("Bezugslinienverfahren I: W auf freier Strecke");
            // The simple case of the "Bezugslinienverfahren"
            // "W auf freier Strecke".
            WstValueTable wst = WstValueTableFactory.getTable(r);
            if (wst == null) {
                log.warn("no wst value table found");
                return null;
            }
            double km = range[0];

            TDoubleArrayList outQs = new TDoubleArrayList(ws.length);
            TDoubleArrayList outWs = new TDoubleArrayList(ws.length);

            boolean generatedWs = false;

            for (int i = 0; i < ws.length; ++i) {
                double w = ws[i];
                if (debug) {
                    log.debug("getQsForWs: lookup Q for W: " + w);
                }
                // There could be more than one Q per W.
                double [] qs = wst.findQsForW(km, w, report);
                for (int j = 0; j < qs.length; ++j) {
                    outWs.add(ws[i]);
                    outQs.add(qs[j]);
                }
                generatedWs |= qs.length != 1;
            }

            if (debug) {
                log.debug("getQsForWs: number of Qs: " + outQs.size());
            }

            return new double [][] {
                outQs.toNativeArray(),
                generatedWs ? outWs.toNativeArray() : null };
        }

        if (debug) {
            log.debug("range: " + Arrays.toString(range));
        }

        Gauge g = rangeAccess.getRiver().determineRefGauge(
            range, rangeAccess.isRange());
        if (g == null) {
            log.warn("no gauge found for km: " + range[0]);
            return null;
        }

        if (debug) {
            log.debug("convert w->q with gauge '" + g.getName() + "'");
        }

        DischargeTable dt = g.fetchMasterDischargeTable();

        if (dt == null) {
            log.warn("No master discharge table found for gauge '"
                + g.getName() + "'");
            return null;
        }

        double [][] values = DischargeTables.loadDischargeTableValues(dt);

        TDoubleArrayList wsOut = new TDoubleArrayList(ws.length);
        TDoubleArrayList qsOut = new TDoubleArrayList(ws.length);

        boolean generatedWs = false;

        for (int i = 0; i < ws.length; i++) {
            if (Double.isNaN(ws[i])) {
                log.warn("W is NaN: ignored");
                continue;
            }
            double [] qs = DischargeTables.getQsForW(values, ws[i]);

            if (qs.length == 0) {
                log.warn("No Qs found for W = " + ws[i]);
            }
            else {
                for (double q: qs) {
                    wsOut.add(ws[i]);
                    qsOut.add(q);
                }
            }
            generatedWs |= qs.length != 1;
        }

        return new double [][] {
            qsOut.toNativeArray(),
            generatedWs ? wsOut.toNativeArray() : null
        };
    }


    /**
     * Returns the selected distance based on a given range (from, to).
     *
     * @param dFrom The StateData that contains the lower value.
     * @param dTo The StateData that contains the upper value.
     *
     * @return the selected distance.
     */
    protected double[] getDistanceByRange(StateData dFrom, StateData dTo) {
        double from = Double.parseDouble((String) dFrom.getValue());
        double to   = Double.parseDouble((String) dTo.getValue());

        return new double[] { from, to };
    }


    /**
     * Returns the selected Kms.
     *
     * @return the selected kms.
     */
    public double[] getKms() {
        RangeAccess rangeAccess = new RangeAccess(this);
        if (rangeAccess.isRange()) {
            return rangeAccess.getKmSteps();
        }
        else {
            return LocationDistanceSelect.getLocations(this);
        }
    }


    public double [] getFromToStep() {
        RangeAccess rangeAccess = new RangeAccess(this);
        if (!rangeAccess.isRange()) {
            return null;
        }
        double [] fromTo = rangeAccess.getKmRange();

        if (fromTo == null) {
            return null;
        }

        StateData dStep = getData("ld_step");
        if (dStep == null) {
            return null;
        }

        double [] result = new double[3];
        result[0] = fromTo[0];
        result[1] = fromTo[1];

        try {
            String step = (String)dStep.getValue();
            result[2] = DoubleUtil.round(Double.parseDouble(step) / 1000d);
        }
        catch (NumberFormatException nfe) {
            return null;
        }

        return result;
    }


    /**
     * This method returns the Q values.
     *
     * @return the selected Q values or null, if no Q values are selected.
     */
    public double[] getQs() {
        StateData dMode      = getData("wq_isq");
        StateData dSelection = getData("wq_isrange");

        boolean isRange = dSelection != null
            ? Boolean.valueOf((String)dSelection.getValue())
            : false;

        if (isQ()) {
            if (!isRange) {
                return getSingleWQValues();
            }
            else {
                return getWQTriple();
            }
        }
        else {
            log.warn("You try to get Qs, but W has been inserted.");
            return null;
        }
    }


    public boolean isQ() {
        StateData mode = getData("wq_isq");
        String value = (mode != null) ? (String) mode.getValue() : null;
        return value != null ? Boolean.valueOf(value) : false;
    }

    public boolean isW() {
        StateData mode = getData("wq_isq");
        String value = (mode != null) ? (String) mode.getValue() : null;
        return value != null ? !Boolean.valueOf(value) : false;
    }

    public boolean isFreeW() {
        if(!isW()) {
            return false;
        }
        StateData mode = getData("wq_isfree");
        String value =  (mode != null) ? (String) mode.getValue() : null;

        return value != null ? Boolean.valueOf(value) : false;
    }


    /**
     * Returns true, if the parameter is set to compute data on a free range.
     * Otherwise it returns false, which tells the calculation that it is bound
     * to a gauge.
     *
     * @return true, if the calculation should compute on a free range otherwise
     * false and the calculation is bound to a gauge.
     */
    public boolean isFreeQ() {
        if(!isQ()) {
            return false;
        }
        StateData mode  = getData("wq_isfree");
        String    value = (mode != null) ? (String) mode.getValue() : null;

        log.debug("isFreeQ: " + value);

        return value != null && Boolean.valueOf(value);
    }


    /**
     * Returns the Q values based on a specified kilometer range.
     *
     * @param range A 2dim array with lower and upper kilometer range.
     *
     * @return an array of Q values.
     */
    public double[] getQs(double[] range) {
        StateData dMode   = getData("wq_isq");

        if (isQ()) {
            return getWQForDist(range);
        }

        log.warn("You try to get Qs, but Ws has been inserted.");
        return null;
    }


    /**
     * Returns the W values based on a specified kilometer range.
     *
     * @param range A 2dim array with lower and upper kilometer range.
     *
     * @return an array of W values.
     */
    public double[] getWs(double[] range) {
        if (isW()) {
            return getWQForDist(range);
        }

        log.warn("You try to get Ws, but Qs has been inserted.");
        return null;
    }


    /**
     * This method returns the W values.
     *
     * @return the selected W values or null, if no W values are selected.
     */
    public double[] getWs() {
        if (isW()) {
            StateData dSingle = getData("wq_single");
            if (dSingle != null) {
                return getSingleWQValues();
            }
            else {
                return getWQTriple();
            }
        }
        else {
            log.warn("You try to get Ws, but Q has been inserted.");
            return null;
        }
    }

    /**
     * This method returns the given W or Q values for a specific range
     * (inserted in the WQ input panel for discharge longitudinal sections).
     *
     * @param dist A 2dim array with lower und upper kilometer values.
     *
     * @return an array of W or Q values.
     */
    protected double[] getWQForDist(double[] dist) {
        log.debug("Search wq values for range: " + dist[0] + " - " + dist[1]);
        StateData data = getData("wq_values");

        if (data == null) {
            log.warn("Missing wq values!");
            return null;
        }

        String dataString = (String) data.getValue();
        String[]   ranges = dataString.split(":");

        for (String range: ranges) {
            String[] parts = range.split(";");

            double lower = Double.parseDouble(parts[0]);
            double upper = Double.parseDouble(parts[1]);

            if (lower <= dist[0] && upper >= dist[1]) {
                String[] values = parts[2].split(",");

                int      num = values.length;
                double[] res = new double[num];

                for (int i = 0; i < num; i++) {
                    try {
                        res[i] = Double.parseDouble(values[i]);
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(nfe, nfe);
                    }
                }

                return res;
            }
        }

        log.warn("Specified range for WQ not found!");

        return null;
    }


    /**
     * This method returns an array of inserted WQ triples that consist of from,
     * to and the step width.
     *
     * @return an array of from, to and step width.
     */
    protected double[] getWQTriple() {
        StateData dFrom = getData("wq_from");
        StateData dTo   = getData("wq_to");

        if (dFrom == null || dTo == null) {
            log.warn("Missing start or end value for range.");
            return null;
        }

        double from = Double.parseDouble((String) dFrom.getValue());
        double to   = Double.parseDouble((String) dTo.getValue());

        StateData dStep = getData("wq_step");

        if (dStep == null) {
            log.warn("No step width given. Cannot compute Qs.");
            return null;
        }

        double step  = Double.parseDouble((String) dStep.getValue());

        // if no width is given, the DEFAULT_Q_STEPS is used to compute the step
        // width. Maybe, we should round the value to a number of digits.
        if (step == 0d) {
            double diff = to - from;
            step = diff / DEFAULT_Q_STEPS;
        }

        return DoubleUtil.explode(from, to, step);
    }


    /**
     * Returns an array of inserted WQ double values stored as whitespace
     * separated list.
     *
     * @return an array of W or Q values.
     */
    protected double[] getSingleWQValues() {
        StateData dSingle = getData("wq_single");

        if (dSingle == null) {
            log.warn("Cannot determine single WQ values. No data given.");
            return null;
        }

        String   tmp       = (String) dSingle.getValue();
        String[] strValues = tmp.split(" ");

        TDoubleArrayList values = new TDoubleArrayList();

        for (String strValue: strValues) {
            try {
                values.add(Double.parseDouble(strValue));
            }
            catch (NumberFormatException nfe) {
                log.warn(nfe, nfe);
            }
        }

        values.sort();

        return values.toNativeArray();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
