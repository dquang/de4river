/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.cache.CacheFactory;
import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.NamedMainValue;
import org.dive4elements.river.model.OfficialLine;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.TimeInterval;
import org.dive4elements.river.model.Wst;
import org.dive4elements.river.model.WstColumn;

public class OfficialLineFinder
{
    public static final String CACHE_NAME = "official-lines";

    private static Logger log = LogManager.getLogger(OfficialLineFinder.class);

    // We will only have one entry in this cache.
    public static final String CACHE_KEY = CACHE_NAME;

    public static final double EPSILON = 1e-4;


    public static class ValueRange extends Range {

        private double value;
        private int    wstId;
        private int    columnPos;
        private String name;
        private String source;
        private Date   date;

        public ValueRange(
            double start,
            double end,
            double value,
            int    wstId,
            int    columnPos,
            String name,
            String source,
            Date   date
        ) {
            super(start, end);
            this.value     = value;
            this.wstId     = wstId;
            this.columnPos = columnPos;
            this.name      = name;
            this.source    = source;
            this.date      = date;
        }

        public boolean sameValue(double value) {
            return Math.abs(value - this.value) < EPSILON;
        }

        public int getWstId() {
            return wstId;
        }

        public int getColumnPos() {
            return columnPos;
        }

        public boolean intersectsValueRange(Range r) {
            return r.inside(value);
        }

        public String getName() {
            return name;
        }

        public String getSource() {
            return source;
        }

        public Date getDate() {
            return date;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ValueRange)) {
                return false;
            }
            ValueRange r = (ValueRange)o;
            return wstId == r.wstId && columnPos == r.columnPos;
        }

        @Override
        public String toString() {
            return "[" + name +
                " value: " + value +
                " wstId: " + wstId +
                " pos: " + columnPos +
                " source: " + source +
                " date: " + date +
                " from: " + start +
                " to: " + end + "]";
        }
    }

    public OfficialLineFinder() {
    }

    public static Map<String, List<ValueRange>> getAll() {
        Cache cache = CacheFactory.getCache(CACHE_NAME);

        if (cache == null) {
            return getAllUncached();
        }

        Element element  = cache.get(CACHE_KEY);

        if (element != null) {
            return (Map<String, List<ValueRange>>)element.getValue();
        }

        Map<String, List<ValueRange>> result = getAllUncached();
        if (result != null) {
            cache.put(new Element(CACHE_KEY, result));
        }
        return result;

    }

    public static Map<String, List<ValueRange>> getAllUncached() {

        boolean debug = log.isDebugEnabled();

        Map<String, List<ValueRange>> rivers2officialLines =
            new HashMap<String, List<ValueRange>>();

        for (OfficialLine line: OfficialLine.fetchAllOfficalLines()) {
            NamedMainValue nmv    = line.getNamedMainValue();
            Integer        mnvId  = nmv.getId();
            WstColumn      wc     = line.getWstColumn();
            Wst            wst    = wc.getWst();
            TimeInterval   ti     = wc.getTimeInterval();
            Date           date   = ti != null ? ti.getStartTime() : null;
            String         source = wc.getSource();

            List<ValueRange> ranges = new ArrayList<ValueRange>();

            River river = wst.getRiver();
            List<Gauge> gauges = river.getGauges();
            for (Gauge gauge: gauges) {
                List<MainValue> mainValues = gauge.getMainValues();
                for (MainValue mainValue: mainValues) {
                    NamedMainValue tnmv = mainValue.getMainValue();
                    if (tnmv.getId().equals(mnvId)) {
                        // found gauge with this main value
                        double from  = gauge.getRange().getA().doubleValue();
                        double to    = gauge.getRange().getB().doubleValue();
                        double value = mainValue.getValue().doubleValue();
                        int    wstId = wst.getId();
                        int    pos   = wc.getPosition();
                        ValueRange range = new ValueRange(
                            from, to, value, wstId, pos,
                            nmv.getName(), source, date);

                        if (debug) {
                            log.debug(
                                "river: " + river.getName() +
                                " gauge: " + gauge.getName() +
                                " ol: " + range);
                        }
                        ranges.add(range);
                        break;
                    }
                }
            }

            if (!ranges.isEmpty()) {
                String rname = river.getName();
                List<ValueRange> old = rivers2officialLines.get(rname);
                if (old != null) {
                    old.addAll(ranges);
                }
                else {
                    rivers2officialLines.put(rname, ranges);
                }
            }
        }

        return rivers2officialLines;
    }

    public static final Range MAX_RANGE =
        new Range(-Double.MAX_VALUE, +Double.MAX_VALUE);

    private static final String nn(String s) {
        return s != null ? s : "";
    }

    public static Range extractRange(D4EArtifact artifact) {

        String mode      = nn(artifact.getDataAsString("ld_mode"));
        String locations = nn(artifact.getDataAsString("ld_locations"));
        String from      = nn(artifact.getDataAsString("ld_from"));
        String to        = nn(artifact.getDataAsString("ld_to"));

        if (log.isDebugEnabled()) {
            log.debug("ld_mode: '" + mode + "'");
            log.debug("ld_locations: '" + locations + "'");
            log.debug("ld_from: '" + from + "'");
            log.debug("ld_to: '" + to + "'");
        }

        if (mode.equals("location")) {
            try {
                String loc = locations.replace(" ", "");
                String[] split = loc.split(",");
                if (split.length < 1) {
                    return MAX_RANGE;
                }
                double min = Double.parseDouble(split[0]);
                double max = min;
                for (int i = 1; i < split.length; ++i) {
                    double v = Double.parseDouble(split[i]);
                    if (v > max) max = v;
                    if (v < min) min = v;
                }
                return new Range(min, max);
            }
            catch (NumberFormatException nfe) {
                return MAX_RANGE;
            }
        }
        try {
            double f = Double.parseDouble(from);
            double t = Double.parseDouble(to);
            if (f > t) {
                return new Range(t, f);
            }
            return new Range(f, t);
        }
        catch (NumberFormatException nfe) {
            return MAX_RANGE;
        }
    }

    private static List<ValueRange> filterByRange(
        Range range,
        List<ValueRange> ranges
    ) {
        List<ValueRange> list = new ArrayList<ValueRange>(ranges.size());
        for (ValueRange r: ranges) {
            if (r.intersects(range)) {
                list.add(r);
            }
        }
        return list;
    }

    private static List<ValueRange> filterByQRange(
        Range range,
        List<ValueRange> ranges
    ) {
        List<ValueRange> list = new ArrayList<ValueRange>(ranges.size());
        for (ValueRange r: ranges) {
            if (r.intersectsValueRange(range) && !list.contains(r)) {
                list.add(r);
            }
        }
        return list;
    }

    private static List<ValueRange> filterByQValues(
        double[] values,
        List<ValueRange> ranges
    ) {
        List<ValueRange> list = new ArrayList<ValueRange>(ranges.size());
        for (ValueRange r: ranges) {
            for (double val: values) {
                if (r.sameValue(val) && !list.contains(r)) {
                    list.add(r);
                }
            }
        }
        return list;
    }

    private static boolean isQ(D4EArtifact artifact) {
        Boolean b = artifact.getDataAsBoolean("wq_isq");
        return b != null && b;
    }

    private static boolean isRange(D4EArtifact artifact) {
        Boolean b = artifact.getDataAsBoolean("wq_isrange");
        return b != null && b;
    }

    public static final Range Q_OUT_OF_RANGE = new Range(-10000, -9999);

    private static double[] singleQs(D4EArtifact artifact) {
        String singleData = nn(artifact.getDataAsString("wq_single"));
        String[] values = singleData.split(" ");
        double[] ret = new double[values.length];
        int i = 0;

        for (String value: values) {
            try {
                ret[i] = Double.parseDouble(value);
            }
            catch (NumberFormatException nfe) {
                ret[i] = -1; // INVALID_Q_VALUE
            }
            i++;
        }

        return ret;
    }

    private static Range qRange(D4EArtifact artifact) {
        try {
            Double from = artifact.getDataAsDouble("wq_from");
            Double to   = artifact.getDataAsDouble("wq_to");

            if (from == null || to == null) {
                return Q_OUT_OF_RANGE;
            }
            double f = from;
            double t = to;
            return new Range(Math.min(f, t), Math.max(f, t));
        }
        catch (NumberFormatException nfe) {
            return Q_OUT_OF_RANGE;
        }
    }

    private static Range tripleQRange(D4EArtifact artifact) {
        String rangesData = nn(artifact.getDataAsString("wq_values"));

        double min =  Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;

        for (String range: rangesData.split(":")) {
            String [] parts = range.split(";");
            if (parts.length < 3) {
                continue;
            }
            String [] values = parts[2].split(",");
            for (String value: values) {
                try {
                    double x = Double.parseDouble(value);
                    if (x < min) min = x;
                    if (x > max) max = x;
                }
                catch (NumberFormatException nfe) {
                }
            }
        }

        return min == Double.MAX_VALUE
            ? Q_OUT_OF_RANGE
            : new Range(min, max);
    }

    public static List<ValueRange> findOfficialLines(D4EArtifact artifact) {

        if (!isQ(artifact)) { // Only handle Q calculations
            return Collections.<ValueRange>emptyList();
        }

        Map<String, List<ValueRange>> rivers2officialLines = getAll();

        String riverName = nn(artifact.getDataAsString("river"));

        List<ValueRange> ranges = rivers2officialLines.get(riverName);

        if (ranges == null) {
            return Collections.<ValueRange>emptyList();
        }
        boolean debug = log.isDebugEnabled();

        if (debug) {
            log.debug("Before range filter:" + ranges);
        }

        ranges = filterByRange(extractRange(artifact), ranges);

        if (debug) {
            log.debug("After range filter:" + ranges);
        }

        if (ranges.isEmpty()) {
            return Collections.<ValueRange>emptyList();
        }

        if (isRange(artifact)) {
            Range qRange = qRange(artifact);
            if (qRange == Q_OUT_OF_RANGE) {
                qRange = tripleQRange(artifact);
            }
            ranges = filterByQRange(qRange, ranges);
            if (debug) {
                log.debug("Q range filter: " + qRange);
            }
        } else {
            ranges = filterByQValues(singleQs(artifact), ranges);
        }

        if (debug) {
            log.debug("After q range filter: " + ranges);
        }

        return ranges;
    }
}
