/* Copyright (C) 2014 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.artifacts.model.minfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.backend.utils.EpsilonComparator;

public class SedimentLoadData implements Serializable
{
    private static Logger log = LogManager.getLogger(SedimentLoadData.class);

    public static final int GF_UNKNOWN            = -1;
    public static final int GF_COARSE             =  0;
    public static final int GF_FINE_MIDDLE        =  1;
    public static final int GF_SAND               =  2;
    public static final int GF_SUSP_SAND          =  3;
    public static final int GF_SUSP_SAND_BED      =  4;
    public static final int GF_SUSP_SEDIMENT      =  5;
    public static final int GF_TOTAL              =  6;
    public static final int GF_BED_LOAD           =  7;
    public static final int GF_BED_LOAD_SUSP_SAND =  8;
    public static final int GF_SUSP_LOAD          =  9;
    public static final int GF_MAX                =  9;

    public static final String [] GF_NAMES = {
        "coarse",
        "fine_middle",
        "sand",
        "susp_sand",
        "susp_sand_bed",
        "suspended_sediment",
        "total",
        "bed_load",
        "bed_load_susp_sand",
        "suspended_load"
    };

    public static final int [] MEASUREMENT_STATION_GF = {
        /* GF_COARSE             */ Station.BED_LOAD,
        /* GF_FINE_MIDDLE        */ Station.BED_LOAD,
        /* GF_SAND               */ Station.BED_LOAD,
        /* GF_SUSP_SAND          */ Station.BED_LOAD,
        /* GF_SUSP_SAND_BED      */ Station.BED_LOAD,
        /* GF_SUSP_SEDIMENT      */ Station.SUSPENDED,
        /* GF_TOTAL              */ Station.BED_LOAD|Station.SUSPENDED,
        /* GF_BED_LOAD           */ Station.BED_LOAD,
        /* GF_BED_LOAD_SUSP_SAND */ Station.BED_LOAD,
        /* GF_SUSP_LOAD          */ Station.SUSPENDED
    };

    public static final int measurementStationType(int grainFraction) {
        return grainFraction < 0
            || grainFraction >= MEASUREMENT_STATION_GF.length
            ? Station.UNKNOWN
            : MEASUREMENT_STATION_GF[grainFraction];
    }

    public static final int grainFractionIndex(String name) {
        for (int i = 0; i < GF_NAMES.length; ++i) {
            if (GF_NAMES[i].equals(name)) {
                return i;
            }
        }
        return GF_UNKNOWN;
    }

    public static final String grainFractionName(int index) {
        return index >= 0 && index < GF_NAMES.length
            ? GF_NAMES[index]
            : "unknown";
    }


    public interface Visitor {
        void visit(Station station);
    }

    public static class Value implements Serializable {

        public interface Filter {
            boolean accept(Value value);
        }

        public interface Visitor {
            void visit(Value value);
        }

        private double value;

        private Load load;

        public Value() {
        }

        public Value(Load load, double value) {
            this.load = load;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public Load getLoad() {
            return load;
        }
    } // class Value


    public static class Load implements Serializable {

        private int id;
        private int kind;
        private Integer sqTiId;

        private String description;

        private Date startTime;
        private Date stopTime;
        private Date sqStartTime;
        private Date sqStopTime;

        public Load() {
        }

        public Load(
            int     id,
            int     kind,
            String  description,
            Date    startTime,
            Date    stopTime,
            Integer sqTiId,
            Date    sqStartTime,
            Date    sqStopTime
        ) {
            this.id          = id;
            this.kind        = kind;
            this.description = description;
            this.startTime   = startTime;
            this.stopTime    = stopTime;
            this.sqStartTime = sqStartTime;
            this.sqStopTime  = sqStopTime;
            this.sqTiId      = sqTiId;
        }

        public int getId() {
            return id;
        }

        public Integer getSQRelationTimeIntervalId() {
            return sqTiId;
        }

        public int getKind() {
            return kind;
        }

        public String getDescription() {
            return description;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getStopTime() {
            return stopTime;
        }

        public Date getSQStartTime() {
            return sqStartTime;
        }

        public Date getSQStopTime() {
            return sqStopTime;
        }

        public boolean isEpoch() {
            return startTime != null && stopTime != null;
        }
    } // class Load

    public static class Station implements Serializable {

        public static final int UNKNOWN   = 0;
        public static final int BED_LOAD  = 1;
        public static final int SUSPENDED = 2;

        private double station;

        private int type;

        private List<List<Value>> grainFractions;

        private Station prev;

        public Station() {
            this(BED_LOAD, 0.0);
        }

        public Station(int type, double station) {
            grainFractions = new ArrayList<List<Value>>(GF_MAX+1);
            for (int i = 0; i < GF_MAX+1; ++i) {
                grainFractions.add(null);
            }
            this.type = type;
            this.station = station;
        }

        public void allLoadsWithValue(Collection<Load> loads) {
            for (List<Value> values: grainFractions) {
                if (values != null) {
                    for (Value value: values) {
                        if (!Double.isNaN(value.getValue())) {
                            loads.add(value.getLoad());
                        }
                    }
                }
            }
        }

        public void allNonEpochLoadsWithValue(Collection<Load> loads) {
            for (List<Value> values: grainFractions) {
                if (values != null) {
                    for (Value value: values) {
                        Load load = value.getLoad();
                        if (load.isEpoch() || Double.isNaN(value.getValue())) {
                            continue;
                        }
                        loads.add(value.getLoad());
                    }
                }
            }
        }

        public void allLoadsWithValue(
            Collection<Load> loads,
            Integer sqRelationTimeInterval
        ) {
            for (List<Value> values: grainFractions) {
                if (values == null) {
                    continue;
                }
                for (Value value: values) {
                    if (Double.isNaN(value.getValue())) {
                        continue;
                    }
                    Load load = value.getLoad();
                    Integer sqId = load.getSQRelationTimeIntervalId();
                    if ((sqRelationTimeInterval == null)
                        || sqId != null
                        && sqId.equals(sqRelationTimeInterval)
                    ) {
                        loads.add(load);
                    }
                }
            }
        }

        public double getStation() {
            return station;
        }

        public int getType() {
            return type;
        }

        public boolean isType(int type) {
            return (this.type & type) != 0;
        }

        public void setPrev(Station prev) {
            this.prev = prev;
        }

        public Station getPrev() {
            return prev;
        }

        public void merge(Station other) {
            this.type |= other.type;
            for (int i = 0, N = grainFractions.size(); i < N; ++i) {
                grainFractions.set(i,
                    mergeValues(
                        grainFractions.get(i), other.grainFractions.get(i)));
            }
        }

        private static final Comparator<Value> ID_CMP =
            new Comparator<Value>() {
            @Override
            public int compare(Value a, Value b) {
                return a.getLoad().getId() - b.getLoad().getId();
            }
        };

        private static List<Value> mergeValues(List<Value> a, List<Value> b) {
            if (a == null) return b;
            if (b == null) return a;
            a.addAll(b);
            // re-establish id order.
            Collections.sort(a, ID_CMP);
            return a;
        }

        public Station prevByType(int type) {
            for (Station curr = this; curr != null; curr = curr.getPrev()) {
                if (curr.isType(type)) {
                    return curr;
                }
            }
            return null;
        }

        public void addValue(int grainFraction, Value value) {
            List<Value> values = grainFractions.get(grainFraction);
            if (values == null) {
                values = new ArrayList<Value>();
                grainFractions.set(grainFraction, values);
            }
            values.add(value);
        }

        public boolean hasGrainFraction(String grainFraction) {
            return hasGrainFraction(grainFractionIndex(grainFraction));
        }

        public boolean hasGrainFraction(int grainFraction) {
            List<Value> values = grainFractions.get(grainFraction);
            return values != null && !values.isEmpty();
        }

        public void filterGrainFraction(
            int           grainFraction,
            Value.Filter  filter,
            Value.Visitor visitor
        ) {
            List<Value> values = grainFractions.get(grainFraction);
            if (values != null && !values.isEmpty()) {
                for (Value value: values) {
                    if (filter.accept(value)) {
                        visitor.visit(value);
                    }
                }
            }
        }

        public List<Value> filterGrainFraction(
            int grainFraction,
            Value.Filter filter
        ) {
            final List<Value> result = new ArrayList<Value>();
            filterGrainFraction(grainFraction, filter, new Value.Visitor() {
                @Override
                public void visit(Value value) {
                    result.add(value);
                }
            });
            return result;
        }

        public double findValueByLoadId(int id) {
            for (List<Value> values: grainFractions) {
                double value = findValueByLoadId(values, id);
                if (!Double.isNaN(value)) {
                    return value;
                }
            }
            return Double.NaN;
        }

        private static final double findValueByLoadId(
            List<Value> values,
            int         id
        ) {
            if (values == null) {
                return Double.NaN;
            }
            // List is ordered by station id -> binary search.
            int lo = 0, hi = values.size()-1;
            while (lo <= hi) {
                int mid = (lo + hi)/2;
                Value v = values.get(mid);
                int xid = v.getLoad().getId();
                if      (xid > id) hi = mid-1;
                else if (xid < id) lo = mid+1;
                else return v.getValue();
            }

            return Double.NaN;
        }
    } // class Station


    private Station [] stations;

    public SedimentLoadData() {
    }

    public SedimentLoadData(Collection<Station> stations, boolean kmUp) {
        setStations(stations, kmUp);
    }

    public Station[] getStations() {
        return stations;
    }

    public void setStations(Collection<Station> stations, boolean kmUp) {
        TreeMap<Double, Station> same =
            new TreeMap<Double, Station>(EpsilonComparator.CMP);

        for (Station station: stations) {
            Double key = station.getStation();
            Station st = same.get(key);
            if (st == null) {
                same.put(key, station);
            } else {
                st.merge(station);
            }
        }
        this.stations = new Station[same.size()];
        int i = 0;
        for (Station station: same.values()) {
            this.stations[i++] = station;
        }
        wireNeighbors(kmUp);
    }

    private void wireNeighbors(boolean kmUp) {
        if (kmUp) {
            for (int i = stations.length - 1; i > 0; --i) {
                stations[i-1].setPrev(stations[i]);
            }
        }
        else {
            for (int i = 1; i < stations.length; ++i) {
                stations[i].setPrev(stations[i-1]);
            }
        }
    }

    private void recursiveFindStations(
        double a, double b,
        int lo, int hi,
        Visitor visitor
    ) {
        while (lo <= hi) {
            int mid = (lo+hi)/2;
            Station st = stations[mid];
            double station = st.getStation();
            if (station < a) {
                lo = mid+1;
            } else if (station > b) {
                hi = mid-1;
            } else {
                recursiveFindStations(a, b, lo, mid-1, visitor);
                visitor.visit(st);
                lo = mid+1;
            }
        }
    }

    public static final Comparator<Load> LOAD_ID_CMP = new Comparator<Load>() {
        @Override
        public int compare(Load a, Load b) {
            return a.getId() - b.getId();
        }
    };

    public static final Comparator<Load> LOAD_TI_CMP = new Comparator<Load>() {
        @Override
        public int compare(Load a, Load b) {
            Date a_start = a.getStartTime();
            Date a_stop = a.getStopTime();
            Date b_start = b.getStartTime();
            Date b_stop = b.getStopTime();
            if (a_start == null && b_start == null) {
                return 0;
            } else if (a_start != null) {
                return a_start.compareTo(b_start);
            } else if (a_stop != null) {
                return a_stop.compareTo(b_stop);
            } else {
                return 1;
            }
        }
    };

    public static final Comparator<Load> LOAD_SQ_TI_CMP =
        new Comparator<Load>() {
        @Override
        public int compare(Load a, Load b) {
            Integer a_id = a.getSQRelationTimeIntervalId();
            Integer b_id = b.getSQRelationTimeIntervalId();
            if (a_id == null && b_id == null) {
                return 0;
            }
            if (a_id == null) {
                return -1;
            }
            if (b_id == null) {
                return 1;
            }
            return a_id - b_id;
        }
    };

    /** Find all loads in the range a/b with the according sq_time_interval */
    public Collection<Load> findLoadsWithValue(
        double a,
        double b,
        final Integer sqRelationTimeInterval
    ) {
        final TreeSet<Load> loads = new TreeSet<Load>(LOAD_ID_CMP);

        findStations(a, b, new Visitor() {
            @Override
            public void visit(Station station) {
                station.allLoadsWithValue(loads, sqRelationTimeInterval);
            }
        });

        return loads;
    }

    /** Get a list of loads with unique sq_time_intervals.
     *
     * This is mainly a convenience function for the SedimentLoadInfoService.
     */
    public Collection<Load> findDistinctSQTimeIntervalNonEpochLoadsWithValue(
        double a,
        double b
    ) {
        final TreeSet<Load> loads = new TreeSet<Load>(LOAD_SQ_TI_CMP);

        findStations(a, b, new Visitor() {
            @Override
            public void visit(Station station) {
                station.allNonEpochLoadsWithValue(loads);
            }
        });

        return loads;
    }

    public Collection<Load> findLoadsWithValue(double a, double b) {
        final TreeSet<Load> loads = new TreeSet<Load>(LOAD_ID_CMP);

        findStations(a, b, new Visitor() {
            @Override
            public void visit(Station station) {
                station.allLoadsWithValue(loads);
            }
        });

        return loads;
    }

    public void findStations(double a, double b, Visitor visitor) {
        if (a > b) {
            double t = a; a = b; b = t;
        }
        recursiveFindStations(a, b, 0, stations.length-1, visitor);
    }

    public List<Station> findStations(double a, double b) {
        final List<Station> result = new ArrayList<Station>();
        findStations(a, b, new Visitor() {
            @Override
            public void visit(Station station) {
                result.add(station);
            }
        });
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
