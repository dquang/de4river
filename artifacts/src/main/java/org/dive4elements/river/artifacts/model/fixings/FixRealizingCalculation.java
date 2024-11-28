/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import org.dive4elements.river.artifacts.access.FixRealizingAccess;

import org.dive4elements.river.artifacts.math.fitting.Function;

import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.FixingsOverview;
import org.dive4elements.river.artifacts.model.RiverFactory;
import org.dive4elements.river.artifacts.model.Segment;
import org.dive4elements.river.artifacts.model.WQKms;
import org.dive4elements.river.artifacts.model.Parameters;

import org.dive4elements.river.model.River;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** Calculation for FixRealize (german: ausgel. WSPL). */
public class FixRealizingCalculation
extends      FixCalculation
{
    private static Logger log =
        LogManager.getLogger(FixRealizingCalculation.class);

    protected boolean       isQ;
    protected List<Segment> segments;

    public FixRealizingCalculation() {
    }

    public FixRealizingCalculation(FixRealizingAccess access) {
        super(access);

        Boolean       isQ      = access.isQ();
        List<Segment> segments = access.getSegments();

        if (isQ == null) {
            addProblem("fix.realize.missing.is.q");
        }

        if (segments == null || segments.isEmpty()) {
            addProblem("fix.realize.missing.segments");
        }

        River r = RiverFactory.getRiver(river);

        if (r == null) {
            addProblem("fix.no.such.river");
        }

        if (!hasProblems()) {
            this.isQ      = isQ;
            this.segments = segments;

            // Convert from W to Q
            Segment.setReferencePointConvertQ(segments, r, isQ, this);
        }
    }

    @Override
    protected CalculationResult innerCalculate(
        FixingsOverview overview,
        Function        func
    ) {
        ColumnCache cc = new ColumnCache();
        FitResult fitResult = doFitting(overview, cc, func);

        if (fitResult == null) {
            return new CalculationResult(this);
        }

        Segment segment = segments.get(0);
        int numResults = segment.numValues();

        WQKms [] results = new WQKms[numResults];
        for (int i = 0; i < results.length; ++i) {
            results[i] = new WQKms();
        }

        Parameters parameters = fitResult.getParameters();

        int kmIndex = parameters.columnIndex("km");
        int [] parameterIndices =
            parameters.columnIndices(func.getParameterNames());

        double [] parameterValues = new double[parameterIndices.length];

        for (int row = 0, R = parameters.size(); row < R; ++row) {
            double km = parameters.get(row, kmIndex);

            if (!segment.inside(km)) {
                Segment nextSeg = null;
                for (Segment seg: segments) {
                    if (seg.inside(km)) {
                        nextSeg = seg;
                        break;
                    }
                }
                if (nextSeg == null) {
                    addProblem(km, "fix.cannot.find.segment");
                    continue;
                }
                segment = nextSeg;
            }

            parameters.get(row, parameterIndices, parameterValues);

            org.dive4elements.river.artifacts.math.Function instance =
                func.instantiate(parameterValues);

            double [] values = segment.getValues();
            for (int i = 0; i < numResults; ++i) {
                double q = values[i];
                double w = instance.value(q);

                if (Double.isNaN(w)) {
                    addProblem(km, "fix.cannot.calculate.function", q);
                }
                else {
                    results[i].add(w, q, km);
                }
            }
        }

        // Name the curves.
        for (int i = 0; i < results.length; ++i) {
            results[i].setName(createName(i));
        }

        FixRealizingResult frr = new FixRealizingResult(
            parameters,
            fitResult.getReferenced(),
            fitResult.getOutliers(),
            results);

        return new CalculationResult(frr, this);
    }

    // TODO: issue1109/2
    protected String createName(int index) {
        // TODO: i18n
        StringBuilder sb = new StringBuilder(isQ ? "Q" : "W");
        sb.append(" benutzerdefiniert (");
        for (int i = 0, N = segments.size(); i < N; ++i) {
            if (i > 0) {
                sb.append("; ");
            }
            Segment segment = segments.get(i);
            double [] backup = segment.getBackup();
            double [] values = segment.getValues();
            sb.append((backup != null ? backup : values)[index]);
        }
        sb.append(')');
        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
