/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.access.Calculation4Access;

import org.dive4elements.river.artifacts.math.BackJumpCorrector;
import org.dive4elements.river.artifacts.math.Function;
import org.dive4elements.river.artifacts.math.Identity;
import org.dive4elements.river.artifacts.math.Linear;

import org.dive4elements.river.artifacts.model.WstValueTable.QPosition;

import org.dive4elements.river.model.River;

import org.dive4elements.river.utils.DoubleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Calculation4
extends      Calculation
{
    private static Logger log = LogManager.getLogger(Calculation4.class);

    public static final double MINIMAL_STEP_WIDTH = 1e-5;

    protected List<Segment> segments;

    protected boolean isQ;
    protected double  from;
    protected double  to;
    protected double  step;
    protected String  river;

    public Calculation4() {
    }

    public Calculation4(Calculation4Access access) {
        log.debug("Calculation4Access.cnst");
        String        river    = access.getRiverName();
        List<Segment> segments = access.getSegments();
        double []     range    = access.getFromToStep();
        boolean       isQ      = access.isQ();

        if (river == null) {
            addProblem("no.river.selected");
        }

        if (range == null) {
            addProblem("no.range.found");
        }

        if (segments == null || segments.isEmpty()) {
            addProblem("cannot.create.segments");
        }

        if (!hasProblems()) {
            this.river    = river;
            this.segments = segments;
            this.from     = range[0];
            this.to       = range[1];
            this.step     = range[2];
            this.isQ      = isQ;
        }
    }

    public CalculationResult calculate() {
        if (hasProblems()) {
            return new CalculationResult(new WQKms[0], this);
        }

        WstValueTable table = null;
        River r = RiverFactory.getRiver(river);
        if (r == null) {
            addProblem("no.river.found");
        }
        else {
            table = WstValueTableFactory.getTable(r);
            if (table == null) {
                addProblem("no.wst.for.river");
            }
            else {
                Segment.setReferencePointConvertQ(segments, r, isQ, this);
            }
        }

        return hasProblems()
            ? new CalculationResult(new WQKms[0], this)
            : innerCalculate(table);
    }

    protected CalculationResult innerCalculate(WstValueTable table) {
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug(
                "calculate from " + from + " to " + to + " step " + step);
            log.debug("# segments: " + segments.size());
            for (Segment segment: segments) {
                log.debug("  " + segment);
            }
        }

        int numResults = segments.get(0).values.length;

        if (numResults < 1) {
            log.debug("no values given");
            addProblem("no.values.given");
            return new CalculationResult(new WQKms[0], this);
        }


        WQKms [] results = new WQKms[numResults];
        for (int i = 0; i < results.length; ++i) {
            results[i] = new WQKms();
        }

        if (Math.abs(step) < MINIMAL_STEP_WIDTH) {
            step = MINIMAL_STEP_WIDTH;
        }

        if (from > to) {
            step = -step;
        }

        QPosition [] qPositions = new QPosition[numResults];

        Function [] functions = new Function[numResults];

        double [] out = new double[2];

        Segment sentinel = new Segment(Double.MAX_VALUE);
        Segment s1 = sentinel, s2 = sentinel;

        for (double pos = from;
             from < to ? pos <= to : pos >= to;
             pos = DoubleUtil.round(pos + step)
        ) {
            if (pos < s1.referencePoint || pos > s2.referencePoint) {
                if (debug) {
                    log.debug("need to find new interval for " + pos);
                }
                // find new interval
                if (pos <= segments.get(0).referencePoint) {
                    // before first segment -> "gleichwertig"
                    if (debug) {
                        log.debug("before first segment -> gleichwertig");
                    }
                    Segment   first  = segments.get(0);
                    double [] values = first.values;
                    double    refPos = first.referencePoint;
                    for (int i = 0; i < qPositions.length; ++i) {
                        qPositions[i] = table.getQPosition(
                            refPos, values[i]);
                    }
                    sentinel.setReferencePoint(-Double.MAX_VALUE);
                    s1 = sentinel;
                    s2 = segments.get(0);
                    Arrays.fill(functions, Identity.IDENTITY);
                }
                else if (
                    pos >= segments.get(segments.size()-1).referencePoint
                ) {
                    // after last segment -> "gleichwertig"
                    if (debug) {
                        log.debug("after last segment -> gleichwertig");
                    }
                    Segment   last   = segments.get(segments.size()-1);
                    double [] values = last.values;
                    double    refPos = last.referencePoint;
                    for (int i = 0; i < qPositions.length; ++i) {
                        qPositions[i] = table.getQPosition(
                            refPos, values[i]);
                    }
                    sentinel.setReferencePoint(Double.MAX_VALUE);
                    s1 = last;
                    s2 = sentinel;
                    Arrays.fill(functions, Identity.IDENTITY);
                }
                else { // "ungleichwertig"
                    // find matching interval
                    if (debug) {
                        log.debug("in segments -> ungleichwertig");
                    }
                    s1 = s2 = null;
                    for (int i = 1, N = segments.size(); i < N; ++i) {
                        Segment si1 = segments.get(i-1);
                        Segment si  = segments.get(i);
                        if (debug) {
                            log.debug("check " + pos + " in "
                                + si1.referencePoint + " - "
                                + si.referencePoint);
                        }
                        if (pos >= si1.referencePoint
                        &&  pos <= si. referencePoint) {
                            s1 = si1;
                            s2 = si;
                            break;
                        }
                    }

                    if (s1 == null) {
                        throw new IllegalStateException("no interval found");
                    }

                    Segment anchor, free;

                    if (from > to) { anchor = s1; free = s2; }
                    else           { anchor = s2; free = s1; }

                    // build transforms based on "gleichwertiger" phase
                    for (int i = 0; i < qPositions.length; ++i) {
                        QPosition qi = table.getQPosition(
                            anchor.referencePoint,
                            anchor.values[i]);

                        if ((qPositions[i] = qi) == null) {
                            addProblem(pos, "cannot.find.q", anchor.values[i]);
                            functions[i] = Identity.IDENTITY;
                        }
                        else {
                            double qA = table.getQ(qi, anchor.referencePoint);
                            double qF = table.getQ(qi, free  .referencePoint);

                            functions[i] = Double.isNaN(qA) || Double.isNaN(qF)
                                ? Identity.IDENTITY
                                : new Linear(
                                    qA, qF,
                                    anchor.values[i], free.values[i]);

                            if (debug) {
                                log.debug(
                                    anchor.referencePoint + ": " +
                                    qA + " -> " + functions[i].value(qA) +
                                    " / " + free.referencePoint + ": " +
                                    qF + " -> " + functions[i].value(qF));
                            }
                        }
                    } // build transforms
                } // "ungleichwertiges" interval
            } // find matching interval

            for (int i = 0; i < qPositions.length; ++i) {
                QPosition qPosition = qPositions[i];

                if (qPosition == null) {
                    continue;
                }

                if (table.interpolate(pos, out, qPosition, functions[i])) {
                    results[i].add(out[0], out[1], pos);
                }
                else {
                    addProblem(pos, "cannot.interpolate.w.q");
                }
            }
        }

        // Backjump correction
        for (int i = 0; i < results.length; ++i) {
            BackJumpCorrector bjc = new BackJumpCorrector();

            double [] ws  = results[i].getWs();
            double [] kms = results[i].getKms();

            if (bjc.doCorrection(kms, ws, this)) {
                results[i] = new WQCKms(results[i], bjc.getCorrected());
            }
        }

        // Name the curves.
        for (int i = 0; i < results.length; ++i) {
            results[i].setName(createName(i));
        }

        // Generate the "Umhuellende".
        ConstantWQKms [] infoldings =
            generateInfolding(table, results, from, to, step);

        // TODO: Use qkms in a new result type.
        WQKms [] newResults = new WQKms[results.length + infoldings.length];
        System.arraycopy(
            results, 0, newResults, 0, results.length);
        System.arraycopy(
            infoldings, 0, newResults, results.length, infoldings.length);

        return new CalculationResult(newResults, this);
    }

    protected ConstantWQKms [] generateInfolding(
        WstValueTable wst,
        WQKms []      results,
        double        from,
        double        to,
        double        step
    ) {
        WstValueTable.Column [] columns = wst.getColumns();

        InfoldingColumns ic = new InfoldingColumns(columns);
        ic.markInfoldingColumns(results);

        List<ConstantWQKms> infoldings = new ArrayList<ConstantWQKms>();

        boolean [] infoldingColumns = ic.getInfoldingColumns();

        double [] kms = null;
        double [] ws  = null;

        for (int i = 0; i < infoldingColumns.length; ++i) {
            if (!infoldingColumns[i]) {
                continue;
            }

            if (kms == null) {
                kms = DoubleUtil.explode(from, to, step);
                ws  = new double[kms.length];
            }

            QRangeTree.QuickQFinder qf =
                columns[i].getQRangeTree().new QuickQFinder();

            int numProblemsBefore = numProblems();
            double [] qs = qf.findQs(kms, this);

            String name = columns[i].getName();
            ConstantWQKms infolding = new ConstantWQKms(kms, qs, ws, name);

            if (numProblems() > numProblemsBefore) {
                infolding.removeNaNs();
            }

            infoldings.add(infolding);
        }

        for (int i = 0, I = infoldings.size(); i < I; i++) {
            ConstantWQKms infolding = infoldings.get(i);
            String name = infolding.getName();
            // TODO: i18n
            if (i == 0) {
                infolding.setName("untere Umh\u00fcllende " + name);
            }
            else if (i ==  I-1) {
                infolding.setName("obere Umh\u00fcllende " + name);
            }
            else {
                infolding.setName("geschnitten " + name);
            }
        }

        return infoldings.toArray(new ConstantWQKms[infoldings.size()]);
    }

    // TODO: issue1109/2, merge with FixRealizingCalculation
    protected String createName(int index) {
        // TODO: i18n
        StringBuilder sb = new StringBuilder(isQ ? "Q" : "W");
        sb.append(" benutzerdefiniert (");
        for (int i = 0, N = segments.size(); i < N; ++i) {
            if (i > 0) {
                sb.append("; ");
            }
            Segment segment = segments.get(i);
            sb.append((segment.backup != null
                ? segment.backup
                : segment.values)[index]);
        }
        sb.append(')');
        return sb.toString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
