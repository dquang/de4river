/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.artifacts.common.utils.StringUtils;

import org.dive4elements.river.artifacts.access.FixAccess;

import org.dive4elements.river.artifacts.math.fitting.Function;
import org.dive4elements.river.artifacts.math.fitting.FunctionFactory;

import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FixingsColumn;
import org.dive4elements.river.artifacts.model.FixingsColumnFactory;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing.Filter;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing;
import org.dive4elements.river.artifacts.model.FixingsOverview.IdsFilter;

import org.dive4elements.river.artifacts.model.FixingsOverview;
import org.dive4elements.river.artifacts.model.FixingsOverviewFactory;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.utils.DoubleUtil;
import org.dive4elements.river.utils.KMIndex;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Calculation base class for fix. */
public abstract class FixCalculation
extends               Calculation
{
    private static Logger log = LogManager.getLogger(FixCalculation.class);

    public static final double EPSILON = 1e-4;

    public static final String [] STANDARD_COLUMNS = {
        "km", "chi_sqr", "max_q", "std-dev"
    };

    protected static class FitResult {

        protected Parameters      parameters;
        protected KMIndex<QWD []> referenced;
        protected KMIndex<QWI []> outliers;

        public FitResult() {
        }

        public FitResult(
            Parameters      parameters,
            KMIndex<QWD []> referenced,
            KMIndex<QWI []> outliers
        ) {
            this.parameters = parameters;
            this.referenced = referenced;
            this.outliers   = outliers;
        }

        public Parameters getParameters() {
            return parameters;
        }

        public KMIndex<QWD []> getReferenced() {
            return referenced;
        }

        public KMIndex<QWI []> getOutliers() {
            return outliers;
        }
    } // class FitResult

    /** Helper class to bundle the meta information of a column
     *  and the real data.
     */
    protected static class Column {

        protected Fixing.Column meta;
        protected FixingsColumn data;
        protected int           index;

        public Column() {
        }

        public Column(Fixing.Column meta, FixingsColumn data, int index) {
            this.meta  = meta;
            this.data  = data;
            this.index = index;
        }

        public Date getDate() {
            return meta.getStartTime();
        }

        public String getDescription() {
            return meta.getDescription();
        }

        public int getIndex() {
            return index;
        }

        public int getId() {
            return meta.getId();
        }

        public boolean getQW(
            double    km,
            double [] qs,
            double [] ws,
            int       index
        ) {
            qs[index] = data.getQ(km);
            return data.getW(km, ws, index);
        }

        public boolean getQW(double km, double [] wq) {
            data.getW(km, wq, 0);
            if (Double.isNaN(wq[0])) return false;
            wq[1] = data.getQ(km);
            return !Double.isNaN(wq[1]);
        }
    } // class Column

    /**
     * Helper class to find the data belonging to meta info more quickly.
     */
    protected static class ColumnCache {

        protected Map<Integer, Column> columns;

        public ColumnCache() {
            columns = new HashMap<Integer, Column>();
        }

        public Column getColumn(Fixing.Column meta) {
            Integer key = meta.getId();
            Column column = columns.get(key);
            if (column == null) {
                FixingsColumn data = FixingsColumnFactory
                    .getInstance()
                    .getColumnData(meta);
                if (data != null) {
                    column = new Column(meta, data, columns.size());
                    columns.put(key, column);
                }
            }
            return column;
        }
    } // class ColumnCache


    protected String  river;
    protected double  from;
    protected double  to;
    protected double  step;
    protected boolean preprocessing;
    protected String  function;
    protected int []  events;
    protected int     qSectorStart;
    protected int     qSectorEnd;

    public FixCalculation() {
    }

    public FixCalculation(FixAccess access) {
        String  river         = access.getRiverName();
        Double  from          = access.getFrom();
        Double  to            = access.getTo();
        Double  step          = access.getStep();
        String  function      = access.getFunction();
        int []  events        = access.getEvents();
        Integer qSectorStart  = access.getQSectorStart();
        Integer qSectorEnd    = access.getQSectorEnd();
        Boolean preprocessing = access.getPreprocessing();

        if (river == null) {
            addProblem("fix.missing.river");
        }

        if (from == null) {
            addProblem("fix.missing.from");
        }

        if (to == null) {
            addProblem("fix.missing.to");
        }

        if (step == null) {
            addProblem("fix.missing.step");
        }

        if (function == null) {
            addProblem("fix.missing.function");
        }

        if (events == null || events.length < 1) {
            addProblem("fix.missing.events");
        }

        if (qSectorStart == null) {
            addProblem("fix.missing.qstart.sector");
        }

        if (qSectorEnd == null) {
            addProblem("fix.missing.qend.sector");
        }

        if (preprocessing == null) {
            addProblem("fix.missing.preprocessing");
        }

        if (!hasProblems()) {
            this.river         = river;
            this.from          = from;
            this.to            = to;
            this.step          = step;
            this.function      = function;
            this.events        = events;
            this.qSectorStart  = qSectorStart;
            this.qSectorEnd    = qSectorEnd;
            this.preprocessing = preprocessing;
        }
    }

    protected static String toString(
        String [] parameterNames,
        double [] values
    ) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameterNames.length; ++i) {
            if (i > 0) sb.append(", ");
            sb.append(parameterNames[i]).append(": ").append(values[i]);
        }
        return sb.toString();
    }


    /** Create filter to accept only the chosen events.
     *  This factored out out to be overwritten.
     */
    protected Filter createFilter() {
        return new IdsFilter(events);
    }

    protected List<Column> getEventColumns(
        FixingsOverview overview,
        ColumnCache     cc
    ) {
        FixingsColumnFactory fcf = FixingsColumnFactory.getInstance();

        Filter filter = createFilter();

        List<Fixing.Column> metas = overview.filter(null, filter);

        List<Column> columns = new ArrayList<Column>(metas.size());

        for (Fixing.Column meta: metas) {

            Column data = cc.getColumn(meta);
            if (data == null) {
                addProblem("fix.cannot.load.data");
            }
            else {
                columns.add(data);
            }
        }

        return columns;
    }

    // Fit a function to the given points from fixation.
    protected FitResult doFitting(
        FixingsOverview overview,
        ColumnCache     cc,
        Function        func
    ) {
        boolean debug = log.isDebugEnabled();

        final List<Column> eventColumns = getEventColumns(overview, cc);

        if (eventColumns.size() < 2) {
            addProblem("fix.too.less.data.columns");
            return null;
        }

        final double  [] qs = new double[eventColumns.size()];
        final double  [] ws = new double[qs.length];
        final boolean [] interpolated = new boolean[ws.length];

        Fitting.QWDFactory qwdFactory = new Fitting.QWDFactory() {
            @Override
            public QWD create(double q, double w) {
                // Check all the event columns for close match
                // and take the description and the date from meta.
                for (int i = 0; i < qs.length; ++i) {
                    if (Math.abs(qs[i]-q) < EPSILON
                    &&  Math.abs(ws[i]-w) < EPSILON) {
                        Column column = eventColumns.get(i);
                        return new QWD(
                            qs[i], ws[i],
                            column.getDescription(),
                            column.getDate(),
                            interpolated[i],
                            0d,
                            column.getId()); // Use database id here
                    }
                }
                log.warn("cannot find column for (" + q + ", " + w + ")");
                return new QWD(q, w);
            }
        };

        Fitting fitting = new Fitting(func, qwdFactory, preprocessing);

        String [] parameterNames = func.getParameterNames();

        Parameters results =
            new Parameters(
                StringUtils.join(STANDARD_COLUMNS, parameterNames));

        boolean invalid = false;

        double [] kms = DoubleUtil.explode(from, to, step / 1000.0);

        if (debug) {
            log.debug("number of kms: " + kms.length);
        }

        KMIndex<QWI []> outliers   = new KMIndex<QWI []>();
        KMIndex<QWD []> referenced = new KMIndex<QWD []>(kms.length);

        int kmIndex             = results.columnIndex("km");
        int chiSqrIndex         = results.columnIndex("chi_sqr");
        int maxQIndex           = results.columnIndex("max_q");
        int stdDevIndex         = results.columnIndex("std-dev");
        int [] parameterIndices = results.columnIndices(parameterNames);

        int numFailed = 0;

        for (int i = 0; i < kms.length; ++i) {
            double km = kms[i];

            // Fill Qs and Ws from event columns.
            for (int j = 0; j < ws.length; ++j) {
                interpolated[j] = !eventColumns.get(j).getQW(km, qs, ws, j);
            }

            fitting.reset();

            if (!fitting.fit(qs, ws)) {
                log.debug("Fitting for km: " + km + " failed");
                ++numFailed;
                addProblem(km, "fix.fitting.failed");
                continue;
            }

            QWD [] refs = fitting.referencedToArray();

            referenced.add(km, refs);

            if (fitting.hasOutliers()) {
                outliers.add(km, fitting.outliersToArray());
            }

            int row = results.newRow();
            double [] values = fitting.getParameters();

            results.set(row, kmIndex, km);
            results.set(row, chiSqrIndex, fitting.getChiSquare());
            results.set(row, stdDevIndex, fitting.getStandardDeviation());
            results.set(row, maxQIndex,   fitting.getMaxQ());
            invalid |= results.set(row, parameterIndices, values);

            if (debug) {
                log.debug("km: "+km+" " + toString(parameterNames, values));
            }
        }

        if (debug) {
            log.debug("success: " + (kms.length - numFailed));
            log.debug("failed: " + numFailed);
        }

        if (invalid) {
            addProblem("fix.invalid.values");
            results.removeNaNs();
        }

        outliers.sort();
        referenced.sort();

        return new FitResult(
            results,
            referenced,
            outliers);
    }

    public CalculationResult calculate() {
        FixingsOverview overview =
            FixingsOverviewFactory.getOverview(river);

        if (overview == null) {
            addProblem("fix.no.overview.available");
        }

        Function func = FunctionFactory.getInstance()
            .getFunction(function);

        if (func == null) {
            addProblem("fix.invalid.function.name");
        }

        if (hasProblems()) {
            return new CalculationResult(this);
        }
        CalculationResult result = innerCalculate(overview, func);

        if (result != null) {
            // Workaraound to deal with same dates in data set
            Object o = result.getData();
            if (o instanceof FixResult) {
                FixResult fr = (FixResult)o;
                fr.makeReferenceEventsDatesUnique();
                fr.remapReferenceIndicesToRank();
            }
        }

        return result;
    }

    protected abstract CalculationResult innerCalculate(
        FixingsOverview overview,
        Function        function
    );
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
