/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.math.BigDecimal;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Query;
import org.dive4elements.river.backend.SessionFactoryProvider;
import org.dive4elements.river.model.GrainFraction;
import org.dive4elements.river.model.MeasurementStation;
import org.dive4elements.river.model.WstColumnValue;
import org.dive4elements.river.model.WstColumn;
import org.dive4elements.river.model.DischargeTableValue;
import org.dive4elements.river.model.DischargeTable;
import org.dive4elements.river.model.Range;
import org.dive4elements.river.model.River;
import org.dive4elements.river.backend.utils.EpsilonComparator;
import org.dive4elements.artifacts.common.utils.LRUCache;

public class ImporterSession
{
    private static final ThreadLocal<ImporterSession> SESSION =
        new ThreadLocal<ImporterSession>() {
            @Override
            protected ImporterSession initialValue() {
                return new ImporterSession();
            }
        };

    protected Session databaseSession;

    private LRUCache<Integer, Map<ValueKey, WstColumnValue>>
        wstColumnValues;

    private LRUCache<Integer, Map<ValueKey, DischargeTableValue>>
        dischargeTableValues;

    private LRUCache<Integer, Map<ValueKey, Range>>
        ranges;

    private Map<String, GrainFraction> grainFractions;

    private Map<String, Map<Double, List<MeasurementStation>>>
        riversToMeasurementStations;

    public static ImporterSession getInstance() {
        return SESSION.get();
    }

    public ImporterSession() {
        SessionFactory sessionFactory =
            SessionFactoryProvider.createSessionFactory();
        databaseSession = sessionFactory.openSession();
        //databaseSession.setFlushMode(FlushMode.MANUAL);

        wstColumnValues =
            new LRUCache<Integer, Map<ValueKey, WstColumnValue>>();

        dischargeTableValues =
            new LRUCache<Integer, Map<ValueKey, DischargeTableValue>>();

        ranges = new LRUCache<Integer, Map<ValueKey, Range>>();
    }

    public Session getDatabaseSession() {
        return databaseSession;
    }

    public WstColumnValue getWstColumnValue(
        WstColumn  column,
        BigDecimal position,
        BigDecimal w
    ) {
        Integer c = column.getId();

        Map<ValueKey, WstColumnValue> map = wstColumnValues.get(c);

        if (map == null) {
            map = new TreeMap<ValueKey, WstColumnValue>(
                ValueKey.EPSILON_COMPARATOR);
            wstColumnValues.put(c, map);
            Query query = databaseSession.createQuery(
                "from WstColumnValue where wstColumn.id=:cid");
            query.setParameter("cid", c);
            for (Iterator iter = query.iterate(); iter.hasNext();) {
                WstColumnValue wcv = (WstColumnValue)iter.next();
                map.put(new ValueKey(wcv.getPosition(), wcv.getW()), wcv);
            }
        }

        ValueKey key = new ValueKey(position, w);

        WstColumnValue wcv = map.get(key);

        if (wcv != null) {
            return wcv;
        }

        wcv = new WstColumnValue(column, position, w);

        databaseSession.save(wcv);

        map.put(key, wcv);

        return wcv;
    }

    public DischargeTableValue getDischargeTableValue(
        DischargeTable table,
        BigDecimal     q,
        BigDecimal     w
    ) {
        Integer t = table.getId();

        Map<ValueKey, DischargeTableValue> map =
            dischargeTableValues.get(t);

        if (map == null) {
            map = new TreeMap<ValueKey, DischargeTableValue>(
                ValueKey.EPSILON_COMPARATOR);
            dischargeTableValues.put(t, map);
            Query query = databaseSession.createQuery(
                "from DischargeTableValue where dischargeTable.id=:tid");
            query.setParameter("tid", t);
            for (Iterator iter = query.iterate(); iter.hasNext();) {
                DischargeTableValue dctv = (DischargeTableValue)iter.next();
                map.put(new ValueKey(dctv.getQ(), dctv.getW()), dctv);
            }
        }

        ValueKey key = new ValueKey(q, w);

        DischargeTableValue dctv = map.get(key);

        if (dctv != null) {
            return dctv;
        }

        dctv = new DischargeTableValue(table, q, w);

        databaseSession.save(dctv);

        map.put(key, dctv);

        return dctv;
    }

    public GrainFraction getGrainFraction(String name) {
        if (grainFractions == null) {
            grainFractions = new HashMap<String, GrainFraction>();
            Query query = databaseSession.createQuery("from GrainFraction");
            for (Iterator iter = query.iterate(); iter.hasNext();) {
                GrainFraction gf = (GrainFraction)iter.next();
                grainFractions.put(gf.getName(), gf);
            }
        }
        return grainFractions.get(name);
    }

    public Range getRange(River river, BigDecimal a, BigDecimal b) {
        Integer r = river.getId();

        Map<ValueKey, Range> map = ranges.get(r);

        if (map == null) {
            map = new TreeMap<ValueKey, Range>(
                ValueKey.EPSILON_COMPARATOR);
            ranges.put(r, map);
            Query query = databaseSession.createQuery(
                "from Range where river.id=:rid");
            query.setParameter("rid", r);
            for (Iterator iter = query.iterate(); iter.hasNext();) {
                Range range = (Range)iter.next();
                map.put(new ValueKey(range.getA(), range.getB()), range);
            }
        }

        ValueKey key = new ValueKey(a, b);

        Range range = map.get(key);

        if (range != null) {
            return range;
        }

        range = new Range(a, b, river);

        databaseSession.save(range);

        map.put(key, range);

        return range;
    }

    public MeasurementStation getMeasurementStation(
        River river,
        double station,
        String measurementType
    ) {
        List<MeasurementStation> stations = getMeasurementStations(
            river, station);

        if (stations != null) {
            /* Assume there is only one MeasurementStation per type at
               any station. Should be enforced in database schema. */
            for (MeasurementStation m: stations) {
                if (m.getMeasurementType().equals(measurementType)) {
                    return m;
                }
            }
        }

        return null;
    }

    public List<MeasurementStation> getMeasurementStations(
        River river,
        double station
    ) {
        String rivername = river.getName();

        if (riversToMeasurementStations == null) {
            riversToMeasurementStations =
                new HashMap<String, Map<Double, List<MeasurementStation>>>();
        }

        Map<Double, List<MeasurementStation>> km2Stations =
            riversToMeasurementStations.get(rivername);
        if (km2Stations == null) {
            km2Stations =
                new TreeMap<Double, List<MeasurementStation>>(
                    EpsilonComparator.CMP);
            riversToMeasurementStations.put(rivername, km2Stations);
            Query query = databaseSession.createQuery(
                "from MeasurementStation where range.river = :river");
            query.setParameter("river", river);
            for (Iterator iter = query.iterate(); iter.hasNext();) {
                MeasurementStation st = (MeasurementStation)iter.next();

                // In case river is km_up, station is at larger value of range
                double stKm = river.getKmUp() && st.getRange().getB() != null
                    ? st.getRange().getB().doubleValue()
                    : st.getRange().getA().doubleValue();

                List<MeasurementStation> ms = km2Stations.get(stKm);
                if (ms == null) {
                    ms = new ArrayList<MeasurementStation>(2);
                    km2Stations.put(stKm, ms);
                }
                ms.add(st);
            }

        }
        return km2Stations.get(station);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
