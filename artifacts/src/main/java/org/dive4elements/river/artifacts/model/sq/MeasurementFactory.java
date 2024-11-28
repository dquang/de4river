/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.hibernate.transform.BasicTransformerAdapter;

import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.artifacts.model.DateRange;

import org.dive4elements.river.backend.SedDBSessionHolder;
import org.dive4elements.river.artifacts.model.RiverFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class MeasurementFactory
{
    private static final Logger log =
        LogManager.getLogger(MeasurementFactory.class);

    public static final String MINFO_DUMP_SQ_SEDDB_PREFIX =
        "minfo.dump.sq.seddb.prefix";

    public static final String SQL_TOTALS =
        "SELECT " +
            "m.Q_BPEGEL AS Q_BPEGEL,"+
            "m.TSCHWEB  AS TSCHWEB," +
            "m.TSAND    AS TSAND, " +
            "m.DATUM    AS DATUM " +
        "FROM MESSUNG m " +
            "JOIN STATION   s ON m.STATIONID   = s.STATIONID " +
            "JOIN GEWAESSER r ON s.GEWAESSERID = r.GEWAESSERID " +
        "WHERE " +
            "r.NAME = :river_name " +
            "AND m.Q_BPEGEL IS NOT NULL " +
            "AND s.KM BETWEEN :location - 0.001 AND :location + 0.001 " +
            "AND m.DATUM BETWEEN :from AND :to " +
            "AND m.DATUM IS NOT NULL";

    public static final String SQL_FACTIONS =
        "SELECT " +
            "m.datum        AS DATUM," +
            "m.Q_BPEGEL     AS Q_BPEGEL,"+
            "g.GLOTRECHTEID AS GLOTRECHTEID," +
            "gp.LFDNR       AS LFDNR," +
            "g.UFERABST     AS UFERABST," +
            "g.UFERABLINKS  AS UFERABLINKS," +
            "COALESCE(m.TSCHWEB, 0)    AS TSCHWEB," +
            "COALESCE(m.TSAND, 0)      AS TSAND," +
            "COALESCE(gp.GTRIEB_F, 0)  AS GTRIEB," +
            "COALESCE(m.TGESCHIEBE, 0) AS TGESCHIEBE," +
            "si.SIEB01 AS SIEB01, si.SIEB02 AS SIEB02," +
            "si.SIEB03 AS SIEB03, si.SIEB04 AS SIEB04," +
            "si.SIEB05 AS SIEB05, si.SIEB06 AS SIEB06," +
            "si.SIEB07 AS SIEB07, si.SIEB08 AS SIEB08," +
            "si.SIEB09 AS SIEB09, si.SIEB10 AS SIEB10," +
            "si.SIEB11 AS SIEB11, si.SIEB12 AS SIEB12," +
            "si.SIEB13 AS SIEB13, si.SIEB14 AS SIEB14," +
            "si.SIEB15 AS SIEB15, si.SIEB16 AS SIEB16," +
            "si.SIEB17 AS SIEB17, si.SIEB18 AS SIEB18," +
            "si.SIEB19 AS SIEB19, si.SIEB20 AS SIEB20," +
            "si.SIEB21 AS SIEB21," +
            "gs.RSIEB01 AS RSIEB01, gs.RSIEB02 AS RSIEB02," +
            "gs.RSIEB03 AS RSIEB03, gs.RSIEB04 AS RSIEB04," +
            "gs.RSIEB05 AS RSIEB05, gs.RSIEB06 AS RSIEB06," +
            "gs.RSIEB07 AS RSIEB07, gs.RSIEB08 AS RSIEB08," +
            "gs.RSIEB09 AS RSIEB09, gs.RSIEB10 AS RSIEB10," +
            "gs.RSIEB11 AS RSIEB11, gs.RSIEB12 AS RSIEB12," +
            "gs.RSIEB13 AS RSIEB13, gs.RSIEB14 AS RSIEB14," +
            "gs.RSIEB15 AS RSIEB15, gs.RSIEB16 AS RSIEB16," +
            "gs.RSIEB17 AS RSIEB17, gs.RSIEB18 AS RSIEB18," +
            "gs.RSIEB19 AS RSIEB19, gs.RSIEB20 AS RSIEB20," +
            "gs.RSIEB21 AS RSIEB21, gs.REST    AS REST " +
        "FROM MESSUNG m " +
            "JOIN STATION    s ON m.STATIONID    = s.STATIONID " +
            "JOIN GEWAESSER  r ON s.GEWAESSERID  = r.GEWAESSERID " +
            "JOIN GLOTRECHTE g ON m.MESSUNGID    = g.MESSUNGID " +
            "JOIN GPROBE    gp ON g.GLOTRECHTEID = gp.GLOTRECHTEID " +
            "JOIN GSIEBUNG  gs ON g.GLOTRECHTEID = gs.GLOTRECHTEID " +
            "JOIN GSIEBSATZ si ON m.GSIEBSATZID  = si.GSIEBSATZID " +
        "WHERE " +
            "r.NAME = :river_name " +
            "AND m.Q_BPEGEL IS NOT NULL " +
            "AND s.KM BETWEEN :location - 0.001 AND :location + 0.001 " +
            "AND m.DATUM BETWEEN :from AND :to " +
            "AND m.TGESCHIEBE IS NOT NULL " +
            "AND m.DATUM IS NOT NULL " +
            "AND (" +
                "COALESCE(gs.RSIEB01, 0) + COALESCE(gs.RSIEB02, 0) +" +
                "COALESCE(gs.RSIEB03, 0) + COALESCE(gs.RSIEB04, 0) +" +
                "COALESCE(gs.RSIEB05, 0) + COALESCE(gs.RSIEB06, 0) +" +
                "COALESCE(gs.RSIEB07, 0) + COALESCE(gs.RSIEB08, 0) +" +
                "COALESCE(gs.RSIEB09, 0) + COALESCE(gs.RSIEB10, 0) +" +
                "COALESCE(gs.RSIEB11, 0) + COALESCE(gs.RSIEB12, 0) +" +
                "COALESCE(gs.RSIEB13, 0) + COALESCE(gs.RSIEB14, 0) +" +
                "COALESCE(gs.RSIEB15, 0) + COALESCE(gs.RSIEB16, 0) +" +
                "COALESCE(gs.RSIEB17, 0) + COALESCE(gs.RSIEB18, 0) +" +
                "COALESCE(gs.RSIEB19, 0) + COALESCE(gs.RSIEB20, 0) +" +
                "COALESCE(gs.RSIEB21, 0) + COALESCE(gs.REST, 0)) > 0 " +
        "ORDER BY " +
            "m.DATUM, g.UFERABST, g.GLOTRECHTEID, gp.LFDNR";

    private static final int index(String s) {
        return Integer.parseInt(s.substring(s.length()-2))-1;
    }

    public abstract static class CSVTransformer
        extends BasicTransformerAdapter
    {
        private static final long serialVersionUID = 1L;

        private CSVWriter rawWriter;
        private boolean   metaDataWritten;

        public CSVTransformer() {
            this(null);
        }

        public CSVTransformer(CSVWriter rawWriter) {
            this.rawWriter = rawWriter;
        }

        protected void writeRaw(Object [] tuple, String [] aliases) {
            if (rawWriter == null) {
                return;
            }

            if (!metaDataWritten) {
                rawWriter.writeNext(aliases);
                metaDataWritten = true;
            }

            String [] nextLine = new String[tuple.length];
            for (int i = 0; i < tuple.length; ++i) {
                nextLine[i] = tuple[i] != null ? tuple[i].toString() : "";
            }
            rawWriter.writeNext(nextLine);
        }
    } // class CSVTransformer

    public static class FractionsTransformer extends CSVTransformer {

        private static final long serialVersionUID = 1L;

        public FractionsTransformer() {
            this(null);
        }

        public FractionsTransformer(CSVWriter rawWriter) {
            super(rawWriter);
        }

        @Override
        public Object transformTuple(Object [] tuple, String [] aliases) {

            writeRaw(tuple, aliases);

            Map<String, Object> map = new HashMap<String, Object>();

            Sieve [] sieves = new Sieve[21];

            List<Sieve> validSieves = new ArrayList<Sieve>(21);

            for (int i = 0; i < tuple.length; ++i) {
                Object value = tuple[i];
                if (value == null) {
                    continue;
                }
                String alias = aliases[i];
                if (alias.startsWith("SIEB")
                ||  alias.startsWith("RSIEB")) {
                    int idx = index(alias);
                    Sieve s = sieves[idx];
                    double v = (Double)value;
                    if (s == null) {
                        s = new Sieve();
                        sieves[idx] = s;
                    }
                    if (alias.startsWith("SIEB")) {
                        s.setDiameter(v);
                    }
                    else {
                        s.setLoad(v);
                    }
                }
                else if (alias.equals("REST")) {
                    Sieve s = new Sieve(0d, (Double)value);
                    validSieves.add(s);
                }
                else {
                    map.put(alias, value);
                }

            }
            for (Sieve s: sieves) {
                if (s != null && s.isValid()) {
                    validSieves.add(s);
                }
            }
            return new Measurement(map, validSieves);
        }
    } // class FractionsTransformer

    public static class TotalsTransformer extends CSVTransformer {

        private static final long serialVersionUID = 1L;

        public TotalsTransformer() {
            this(null);
        }

        public TotalsTransformer(CSVWriter rawWriter) {
            super(rawWriter);
        }

        @Override
        public Object transformTuple(Object [] tuple, String [] aliases) {

            writeRaw(tuple, aliases);

            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < tuple.length; ++i) {
                Object value = tuple[i];
                if (value != null) {
                    map.put(aliases[i], value);
                }
            }
            return new Measurement(map, Collections.<Sieve>emptyList());
        }
    } // class TotalsTransformer

    private MeasurementFactory() {
    }

    public static Measurements getMeasurements(
        String     river,
        double     location,
        DateRange  dateRange,
        SQ.Factory sqFactory
    ) {
        Session session = SedDBSessionHolder.HOLDER.get();
        String seddbRiver = RiverFactory.getRiver(river).nameForSeddb();

        List<Measurement> totals = loadTotals(
            session, seddbRiver, location, dateRange);

        List<Measurement> accumulated = loadFractions(
            session, seddbRiver, location, dateRange);

        return new Measurements(totals, accumulated, sqFactory);
    }

    @SuppressWarnings("unchecked")
    protected static List<Measurement> loadTotals(
        Session   session,
        String    river,
        double    location,
        DateRange dateRange
    ) {
        SQLQuery query = session.createSQLQuery(SQL_TOTALS)
            .addScalar("Q_BPEGEL", StandardBasicTypes.DOUBLE)
            .addScalar("TSCHWEB",  StandardBasicTypes.DOUBLE)
            .addScalar("TSAND",    StandardBasicTypes.DOUBLE)
            .addScalar("DATUM",    StandardBasicTypes.DATE);

        query.setString("river_name", river);
        query.setDouble("location", location);
        query.setDate("from", dateRange.getFrom());
        query.setDate("to", dateRange.getTo());

        CSVWriter csvWriter =
            getCVSWriter("totals", river, location, dateRange);

        try {
            TotalsTransformer totalTransformer =
                new TotalsTransformer(csvWriter);
            query.setResultTransformer(totalTransformer);

            return (List<Measurement>)query.list();
        }
        finally {
            closeGraceful(csvWriter);
        }
    }

    private static CSVWriter getCVSWriter(
        String    type,
        String    river,
        double    location,
        DateRange dateRange
    ) {
        String dumpPrefix = System.getProperty(MINFO_DUMP_SQ_SEDDB_PREFIX);
        if (dumpPrefix == null) {
            return null;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-ddd");
        StringBuilder sb = new StringBuilder(dumpPrefix);
        Date from = dateRange.getFrom();
        Date to   = dateRange.getTo();

        sb.append(type)
          .append('-').append(df.format(new Date()))
          .append('-').append(river)
          .append('-').append(location)
          .append('-').append(from != null ? df.format(from) : "")
          .append('-').append(to != null ? df.format(to) : "")
          .append(".csv");

        String fileName = sb.toString();

        try {
            return new CSVWriter(new FileWriter(fileName), ';');
        }
        catch (IOException ioe) {
            log.error("Cannot open '" + fileName + "' for writing.", ioe);
        }

        return null;
    }

    private static void closeGraceful(CSVWriter writer) {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            }
            catch (IOException ioe) {
                log.error(ioe);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected static List<Measurement> loadFractions(
        Session   session,
        String    river,
        double    location,
        DateRange dateRange
    ) {
        boolean debug = log.isDebugEnabled();

        SQLQuery query = session.createSQLQuery(SQL_FACTIONS)
            .addScalar("Q_BPEGEL",     StandardBasicTypes.DOUBLE)
            .addScalar("DATUM",        StandardBasicTypes.DATE)
            .addScalar("GLOTRECHTEID", StandardBasicTypes.INTEGER)
            .addScalar("LFDNR",        StandardBasicTypes.INTEGER)
            .addScalar("UFERABST",     StandardBasicTypes.DOUBLE)
            .addScalar("UFERABLINKS",  StandardBasicTypes.DOUBLE)
            .addScalar("TSCHWEB",      StandardBasicTypes.DOUBLE)
            .addScalar("TSAND",        StandardBasicTypes.DOUBLE)
            .addScalar("GTRIEB",       StandardBasicTypes.DOUBLE)
            .addScalar("TGESCHIEBE",   StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB01",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB02",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB03",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB04",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB05",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB06",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB07",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB08",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB09",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB10",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB11",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB12",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB13",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB14",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB15",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB16",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB17",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB18",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB19",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB20",      StandardBasicTypes.DOUBLE)
            .addScalar("RSIEB21",      StandardBasicTypes.DOUBLE)
            .addScalar("REST",         StandardBasicTypes.DOUBLE)
            .addScalar("SIEB01",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB02",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB03",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB04",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB05",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB06",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB07",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB08",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB09",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB10",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB11",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB12",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB13",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB14",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB15",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB16",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB17",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB18",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB19",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB20",       StandardBasicTypes.DOUBLE)
            .addScalar("SIEB21",       StandardBasicTypes.DOUBLE);

        query.setString("river_name", river);
        query.setDouble("location", location);
        query.setDate("from", dateRange.getFrom());
        query.setDate("to", dateRange.getTo());

        List<Measurement> measuments;

        CSVWriter csvWriter =
            getCVSWriter("fractions", river, location, dateRange);

        try {
            FractionsTransformer fractionsTransformer =
                new FractionsTransformer(csvWriter);

            query.setResultTransformer(fractionsTransformer);

            measuments = (List<Measurement>)query.list();
        }
        finally {
            closeGraceful(csvWriter);
        }

        if (debug) {
            log.debug("num fraction results: " + measuments.size());
        }

        List<Measurement> same = new ArrayList<Measurement>();

        Integer lastLR = null;

        List<Measurement> accumulated = new ArrayList<Measurement>();

        for (Measurement m: measuments) {
            Integer currentLR = (Integer)m.getData("GLOTRECHTEID");

            boolean newDS = lastLR == null
                || (currentLR != null && !lastLR.equals(currentLR));

            if (newDS && !same.isEmpty()) {
                accumulated.add(accumulate(same));
                same.clear();
            }

            same.add(m);

            lastLR = currentLR;
        }

        if (!same.isEmpty()) {
            accumulated.add(accumulate(same));
        }

        if (debug) {
            log.debug("Before date separation: " + accumulated.size());
        }

        accumulated = separateByDate(accumulated);

        if (debug) {
            log.debug("After date separation: " + accumulated.size());
        }

        return accumulated;
    }

    protected static List<Measurement> separateByDate(
        List<Measurement> measurements
    ) {
        List<Measurement> result = new ArrayList<Measurement>();

        List<Measurement> same = new ArrayList<Measurement>();

        Date lastDate = null;

        for (Measurement m: measurements) {
            Date currentDate = (Date)m.getData("DATUM");
            if ((lastDate == null
            || !equalDate(currentDate, lastDate))
            && !same.isEmpty()
            ) {
                result.add(processSameDate(same));
                same.clear();
            }
            same.add(m);
            lastDate = currentDate;
        }

        if (!same.isEmpty()) {
            result.add(processSameDate(same));
        }

        return result;
    }


    protected static Measurement processSameDate(
        List<Measurement> measurements
    ) {
        int N = measurements.size();

        boolean debug = log.isDebugEnabled();
        if (debug && N > 0) {
            log.debug("process same date for Q: " + measurements.get(0).Q());
        }
        if (N == 1) {
            Measurement current = measurements.get(0);
            double left = current.get("UFERABLINKS");
            double right = current.get("UFERABST");
            current.set("EFFWIDTH", left + right);
        }
        else {
            for (int i = 0; i < N; ++i) {
                Measurement current = measurements.get(i);

                if (i == 0) {
                    Measurement next = measurements.get(i+1);
                    double distCurrent = current.get("UFERABST");
                    double distNext = next.get("UFERABST");
                    current.set("EFFWIDTH", distNext - distCurrent);
                }
                else if (i == N-1) {
                    Measurement prev = measurements.get(i-1);
                    double distCurrent = current.get("UFERABST");
                    double distPrev = prev.get("UFERABST");
                    current.set("EFFWIDTH", distCurrent - distPrev);
                }
                else {
                    Measurement prev = measurements.get(i-1);
                    Measurement next = measurements.get(i+1);
                    double distPrev = prev.get("UFERABST");
                    double distNext = next.get("UFERABST");
                    current.set("EFFWIDTH", 0.5*(distNext - distPrev));
                }
                if (debug) {
                    log.debug("effective width: " + current.get("EFFWIDTH"));
                }
            }
        }

        double sumSandF   = 0d;
        double sumCoarseF = 0d;
        double sumGravelF = 0d;
        double sumNorm    = 0d;

        for (Measurement m: measurements) {
            SieveArray sa   = m.getSieveArray();
            if (sa.totalLoad() < SieveArray.EPSILON) {
                continue;
            }
            double sandF    = sa.sandNormFraction();
            double coarseF  = sa.coarseNormFraction();
            double gravelF  = sa.gravelNormFraction();
            double effWidth = m.get("EFFWIDTH");
            double gt       = m.get("GTRIEB");
            double scale    = effWidth*gt;
            sumSandF   += scale*sandF;
            sumCoarseF += scale*coarseF;
            sumGravelF += scale*gravelF;
            sumNorm    += scale;
            if (debug) {
                log.debug("fractions - s: " +
                    sandF + " c: " +
                    coarseF + " g: " +
                    gravelF);
                log.debug("scale: " + scale + " = " + effWidth + " * " + gt);
            }
        }

        Map<String, Object> data =
            new HashMap<String, Object>(measurements.get(0).getData());

        Measurement m = new Measurement(data, Collections.<Sieve>emptyList());

        sumNorm = 1d/sumNorm;

        m.set("BL_S", sumNorm*sumSandF);
        m.set("BL_G", sumNorm*sumGravelF);
        m.set("BL_C", sumNorm*sumCoarseF);
        if (debug) {
            log.debug(
                "BL_S: " + m.get("BL_S") +
                " BL_G: " + m.get("BL_G") +
                " BL_C: " + m.get("BL_C"));
        }
        return m;
    }


    private static final boolean equalDate(Date a, Date b) {
        Calendar ca = Calendar.getInstance();
        Calendar cb = Calendar.getInstance();
        ca.setTime(a);
        cb.setTime(b);
        return ca.get(Calendar.YEAR)         == cb.get(Calendar.YEAR)
            && ca.get(Calendar.MONTH)        == cb.get(Calendar.MONTH)
            && ca.get(Calendar.DAY_OF_MONTH) == cb.get(Calendar.DAY_OF_MONTH);
    }


    protected static Measurement accumulate(List<Measurement> measuments) {

        int N = measuments.size();
        if (N == 1) {
            return measuments.get(0);
        }
        TreeMap<Double, double []> diameters =
            new TreeMap<Double, double []>(Sieve.DIAMETER_CMP);

        double sumGTrieb = 0d;
        for (Measurement m: measuments) {
            for (Sieve s: m.getSieves()) {
                Double key = s.getDiameter();
                double [] sum = diameters.get(key);
                if (sum == null) {
                    sum = new double[1];
                    diameters.put(key, sum);
                }
                sum[0] += s.getLoad();
            }
            // calculate 'Geschiebetrieb'
            sumGTrieb += m.get("GTRIEB");
        }
        List<Sieve> accumulatedSieves = new ArrayList<Sieve>(diameters.size());
        for (Map.Entry<Double, double []> entry: diameters.entrySet()) {
            accumulatedSieves.add(
                new Sieve(entry.getKey(),
                    entry.getValue()[0]/N));
        }
        Map<String, Object> data =
            new HashMap<String, Object>(measuments.get(0).getData());

        data.put("GTRIEB", sumGTrieb/N);

        return new Measurement(data, accumulatedSieves);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
