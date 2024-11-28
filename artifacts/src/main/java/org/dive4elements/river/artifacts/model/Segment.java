/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;

import org.dive4elements.river.utils.DoubleUtil;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/** A Range with values and a reference point. */
public class Segment
implements   Serializable
{
    private static Logger log = LogManager.getLogger(Segment.class);

    public static final Comparator<Segment> REF_CMP =
        new Comparator<Segment>() {
            @Override
            public int compare(Segment a, Segment b) {
                double d = a.referencePoint - b.referencePoint;
                if (d < 0d) return -1;
                return d > 0d ? +1 : 0;
            }
        };

    protected double    from;
    protected double    to;
    protected double [] values;
    protected double [] backup;
    protected double    referencePoint;

    public Segment() {
    }

    public Segment(double referencePoint) {
        this.referencePoint = referencePoint;
    }

    public Segment(double from, double to, double [] values) {
        this.from   = from;
        this.to     = to;
        this.values = values;
    }

    public boolean isUp() {
        return from < to;
    }

    /** Checks if given km lies inside the to/from bounds of this segment. */
    public boolean inside(double km) {
        return from < to
            ? km >= from && km <= to
            : km >= to   && km <= from;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Segment: [");
        sb.append("from: ").append(from).append("; to: ")
          .append(to)
          .append("; ref: ").append(referencePoint)
          .append("; values: (");
        for (int i = 0; i < values.length; ++i) {
            if (i > 0) sb.append(", ");
            sb.append(values[i]);
        }
        sb.append(")]");
        return sb.toString();
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public void backup() {
        backup = values != null
            ? (double [])values.clone()
            : null;
    }

    public double [] getBackup() {
        return backup;
    }

    public double getFrom() {
        return from;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public double getTo() {
        return to;
    }

    public void setValues(double [] values) {
        this.values = values;
    }

    public double [] getValues() {
        return values;
    }

    public int numValues() {
        return values.length;
    }

    public void setReferencePoint(double referencePoint) {
        this.referencePoint = referencePoint;
    }

    public double getReferencePoint() {
        return referencePoint;
    }

    /** Use DoubleUtil to parse Segments. */
    public static List<Segment> parseSegments(String input) {

        final List<Segment> segments = new ArrayList<Segment>();

        DoubleUtil.parseSegments(input, new DoubleUtil.SegmentCallback() {
            @Override
            public void newSegment(double from, double to, double [] values) {
                segments.add(new Segment(from, to, values));
            }
        });

        return segments;
    }

    public static boolean setReferencePointConvertQ(
        List<Segment> segments,
        River         river,
        boolean       isQ,
        Calculation   report
    ) {
        int numResults = -1;

        boolean success = true;

        // assign reference points
        for (Segment segment: segments) {
            Gauge gauge = river.maxOverlap(segment.getFrom(), segment.getTo());

            if (gauge == null) {
                log.warn("no gauge found. Defaults to mid point.");
                segment.setReferencePoint(
                    0.5*(segment.getFrom()+segment.getTo()));
            }
            else {
                double ref = gauge.getStation().doubleValue();
                log.debug(
                    "reference gauge: " + gauge.getName() +
                    " (km " + ref + ")");
                segment.setReferencePoint(ref);
            }

            double [] values = segment.values;

            if (numResults == -1) {
                numResults = values.length;
            }
            else if (numResults != values.length) {
                log.warn("wrong length of values");
                return false;
            }

            // convert to Q if needed
            if (!isQ && gauge != null) {

                DischargeTable dt = gauge.fetchMasterDischargeTable();

                double [][] table =
                    DischargeTables.loadDischargeTableValues(dt);

                // need the original values for naming
                segment.backup();

                for (int i = 0; i < values.length; ++i) {
                    double w = values[i] * 100.0;
                    double [] qs = DischargeTables.getQsForW(table, w);
                    if (qs.length == 0) {
                        log.warn("No Qs found for W = " + values[i]);
                        report.addProblem("cannot.find.q.for.w", values[i]);
                        values[i] = Double.NaN;
                        success = false;
                    }
                    else {
                        values[i] = qs[0];
                        if (qs.length > 1) {
                            log.warn(
                                "More than one Q found for W = " + values[i]);
                        }
                    }
                }
            }
        } // for all segments

        Collections.sort(segments, Segment.REF_CMP);

        return success;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
