/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.io.Serializable;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import org.hibernate.type.StandardBasicTypes;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.river.utils.BatchLoader;


/** Generate Fixings Table overview data structure to be stored in cache. */
public class FixingsOverview
implements   Serializable
{
    private static Logger log = LogManager.getLogger(FixingsOverview.class);

    public static final double EPSILON = 1e-2;

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    public static final String SQL_RIVER_ID =
        "SELECT" +
        "    id AS river_id," +
        "    km_up " +
        "FROM rivers " +
        "WHERE" +
        "    name = :name";

    /** All kind-2 wsts from given river. */
    public static final String SQL_FIXINGS =
        "SELECT" +
        "    id AS wst_id," +
        "    description " +
        "FROM wsts " +
        "WHERE" +
        "    river_id = :river_id AND kind = 2";

    public static final String SQL_FIXING_COLUMNS_BATCH =
        "SELECT " +
            "wc.wst_id     AS wst_id," +
            "wc.id         AS wst_column_id," +
            "ti.start_time AS start_time," +
            "wc.name       AS name " +
        "FROM wst_columns wc " +
            "JOIN time_intervals ti ON wc.time_interval_id = ti.id " +
        "WHERE " +
            "wc.wst_id IN ($IDS) " +
        "ORDER BY wc.wst_id, position";

    public static final String SQL_FIXING_COLUMN_Q_RANGES_BATCH =
        "SELECT " +
            "wcqr.wst_column_id AS wst_column_id," +
            "wqr.q              AS q," +
            "r.a                AS start_km," +
            "r.b                AS stop_km " +
        "FROM wst_column_q_ranges wcqr " +
            "JOIN wst_q_ranges wqr ON wcqr.wst_q_range_id = wqr.id " +
            "JOIN ranges       r   ON wqr.range_id        = r.id " +
        "WHERE " +
            "wcqr.wst_column_id IN ($IDS) " +
        "ORDER BY wcqr.wst_column_id, r.a";

    public static final String SQL_FIXING_COLUMN_KM_RANGE_BATCH =
        "SELECT " +
            "wst_column_id," +
            "MIN(position) AS start_km," +
            "MAX(position) AS stop_km " +
        "FROM " +
            "wst_column_values " +
        "WHERE " +
            "wst_column_id IN ($IDS) " +
        "GROUP BY wst_column_id";

    public static final class KMRangeLoader extends BatchLoader<double []> {

        public KMRangeLoader(List<Integer> columns, Session session) {
            super(columns, session, SQL_FIXING_COLUMN_KM_RANGE_BATCH);
        }

        @Override
        protected void fill(SQLQuery query) {
            query
                .addScalar("wst_column_id", StandardBasicTypes.INTEGER)
                .addScalar("start_km",      StandardBasicTypes.DOUBLE)
                .addScalar("stop_km",       StandardBasicTypes.DOUBLE);

            List<Object []> ranges = query.list();
            for (Object [] r: ranges) {
                Integer cid = (Integer)r[0];
                double [] vs = new double [] { (Double)r[1], (Double)r[2] };
                cache(cid, vs);
            }
        }
    } // class KMRangeLoader

    public static final class ColumnQRangeLoader
    extends                   BatchLoader<List<double []>>
    {
        public ColumnQRangeLoader(List<Integer> columns, Session session) {
            super(columns, session, SQL_FIXING_COLUMN_Q_RANGES_BATCH);
        }

        @Override
        protected void fill(SQLQuery query) {
            query
                .addScalar("wst_column_id", StandardBasicTypes.INTEGER)
                .addScalar("q",             StandardBasicTypes.DOUBLE)
                .addScalar("start_km",      StandardBasicTypes.DOUBLE)
                .addScalar("stop_km",       StandardBasicTypes.DOUBLE);

            int lastId = Integer.MIN_VALUE;
            List<double []> column = new ArrayList<double []>();

            List<Object []> ranges = query.list();
            for (Object [] r: ranges) {
                int cid = (Integer)r[0];

                if (cid != lastId && !column.isEmpty()) {
                    cache(lastId, column);
                    column = new ArrayList<double []>();
                }
                column.add(new double [] {
                    (Double)r[1],
                    (Double)r[2],
                    (Double)r[3]
                });

                lastId = cid;
            }

            if (!column.isEmpty()) {
                cache(lastId, column);
            }
        }
    } // class ColumnQRangeLoader

    /** Helper class to store data from batching fixing columns. */
    private static final class FixColumn {
        int    columnId;
        Date   startTime;
        String name;

        FixColumn(int columnId, Date startTime, String name) {
            this.columnId  = columnId;
            this.startTime = startTime;
            this.name      = name;
        }
    } // class FixColumn

    public static final class FixColumnLoader
    extends                   BatchLoader<List<FixColumn>>
    {
        public FixColumnLoader(List<Integer> columns, Session session) {
            super(columns, session, SQL_FIXING_COLUMNS_BATCH);
        }

        @Override
        protected void fill(SQLQuery query) {
            query
                .addScalar("wst_id",        StandardBasicTypes.INTEGER)
                .addScalar("wst_column_id", StandardBasicTypes.INTEGER)
                .addScalar("start_time",    StandardBasicTypes.TIMESTAMP)
                .addScalar("name",          StandardBasicTypes.STRING);

            int lastId = Integer.MIN_VALUE;
            List<FixColumn> cols = new ArrayList<FixColumn>();

            List<Object []> columns = query.list();
            for (Object [] c: columns) {
                int wid = (Integer)c[0];

                if (wid != lastId && !cols.isEmpty()) {
                    cache(lastId, cols);
                    cols = new ArrayList<FixColumn>();
                }
                cols.add(new FixColumn(
                    (Integer)c[1],
                    (Date)   c[2],
                    (String) c[3]));

                lastId = wid;
            }
            if (!cols.isEmpty()) {
                cache(lastId, cols);
            }
        }
    } // class FixColumnLoader

    public static class QRange extends Range {

        protected double q;

        public QRange() {
        }

        public QRange(double start, double end, double q) {
            super(start, end);
            this.q = q;
        }
    } // class QRange

    public static class SectorRange extends Range {

        protected int sector;

        public SectorRange() {
        }

        public SectorRange(SectorRange other) {
            start  = other.start;
            end    = other.end;
            sector = other.sector;
        }

        public SectorRange(Range range) {
            super(range);
        }

        public SectorRange(double start, double end, int sector) {
            super(start, end);
            this.sector = sector;
        }

        public int getSector() {
            return sector;
        }

        public void setSector(int sector) {
            this.sector = sector;
        }

        public boolean enlarge(SectorRange other) {
            if (sector == other.sector
            && Math.abs(end-other.start) < FixingsOverview.EPSILON) {
                end = other.end;
                return true;
            }
            return false;
        }
    } // class SectorRange

    public static class Fixing implements Serializable {

        public static final Comparator<Column> DATE_CMP =
            new Comparator<Column>() {
                @Override
                public int compare(Column a, Column b) {
                    return a.startTime.compareTo(b.startTime);
                }
            };

        public interface Filter {

            boolean accept(Column column);

        } // interface Filter

        public class Column extends Range {

            protected int    columnId;
            protected Date   startTime;
            protected String name;

            protected List<SectorRange> sectors;

            public Column() {
            }

            public Column(int columnId, Date startTime, String name) {
                this.columnId  = columnId;
                this.startTime = startTime;
                this.name      = name;

                sectors = new ArrayList<SectorRange>();
            }

            public int getId() {
                return columnId;
            }

            public Fixing getFixing() {
                return Fixing.this;
            }

            public Date getStartTime() {
                return startTime;
            }

            public String getName() {
                return name;
            }

            public String getDescription() {
                return Fixing.this.description + "/" + name;
            }

            public List<SectorRange> getSectors() {
                return sectors;
            }

            public boolean hasSectorsInRange(Range range) {
                for (SectorRange sector: sectors) {
                    if (sector.intersects(range)) {
                        return true;
                    }
                }
                return false;
            }

            public List<SectorRange> getSectors(Range range) {

                List<SectorRange> result =
                    new ArrayList<SectorRange>(sectors.size());

                for (SectorRange src: sectors) {
                    SectorRange dst = new SectorRange(src);
                    if (range == null || dst.clip(range)) {
                        result.add(dst);
                    }
                }

                return result;
            }

            public int findQSector(double km) {
                for (SectorRange sector: sectors) {
                    if (sector.inside(km)) {
                        return sector.getSector();
                    }
                }
                return -1;
            }

            public void buildSectors(
                GaugeFinder  gaugeFinder,
                List<QRange> qRanges
            ) {
                for (QRange qRange: qRanges) {
                    for (GaugeRange gRange: gaugeFinder.getGauges()) {
                        SectorRange sector = new SectorRange(qRange);
                        if (!sector.clip(gRange)) {
                            continue;
                        }
                        sector.setSector(gRange.classify(qRange.q));

                        if (sectors.isEmpty()
                        || !sectors.get(sectors.size()-1).enlarge(sector)) {
                            sectors.add(sector);
                        }
                    } // for all gauges
                } // for all Q ranges
            }

            public void loadKmRange(KMRangeLoader loader) {

                double [] range = loader.get(columnId);

                if (range == null) {
                    log.warn("No km range for column " + columnId + ".");
                    return;
                }
                start = range[0];
                end   = range[1];
            }

            public void loadQRanges(
                ColumnQRangeLoader loader,
                GaugeFinder        gaugeFinder
            ) {
                List<double []> qrs = loader.get(columnId);
                if (qrs == null) {
                    log.warn("No q ranges found for column " + columnId);
                    return;
                }

                List<QRange> qRanges = new ArrayList<QRange>(qrs.size());

                for (double [] qr: qrs) {
                    double q     = qr[0];
                    double start = qr[1];
                    double end   = qr[2];

                    QRange qRange = new QRange(start, end, q);
                    if (qRange.clip(this)) {
                        qRanges.add(qRange);
                    }
                }

                buildSectors(gaugeFinder, qRanges);
            }
        } // class Column

        protected int          wstId;
        protected String       description;
        protected List<Column> columns;

        public Fixing() {
        }

        public int getId() {
            return wstId;
        }

        public String getDescription() {
            return description;
        }

        public Fixing(int wstId, String description) {
            this.wstId       = wstId;
            this.description = description;
            columns = new ArrayList<Column>();
        }

        public void allColumnIds(List<Integer> cIds) {
            for (Column column: columns) {
                cIds.add(column.columnId);
            }
        }

        public void loadColumns(FixColumnLoader loader) {
            List<FixColumn> fcs = loader.get(wstId);
            if (fcs == null) {
                log.warn("No columns for wst " + wstId);
                return;
            }
            for (FixColumn fc: fcs) {
                columns.add(new Column(fc.columnId, fc.startTime, fc.name));
            }
        }

        public void loadColumnsKmRange(KMRangeLoader loader) {
            for (Column column: columns) {
                column.loadKmRange(loader);
            }
        }

        public void adjustExtent(Range extent) {
            for (Column column: columns) {
                extent.extend(column);
            }
        }

        public void loadColumnsQRanges(
            ColumnQRangeLoader loader,
            GaugeFinder        gaugeFinder
        ) {
            for (Column column: columns) {
                column.loadQRanges(loader, gaugeFinder);
            }
        }

        /**
         * @param allColumns[out] Columns will be put here.
         * @param range can be null.
         * @param filter filter to apply.
         */
        public void addAllColumns(
            List<Column> allColumns,
            Range        range,
            Filter       filter
        ) {
            for (Column column: columns) {
                if ((range == null || column.hasSectorsInRange(range))
                && (filter == null || filter.accept(column))) {
                    allColumns.add(column);
                }
            }
        }
    } // class Fixing


    protected String       riverName;
    protected int          riverId;
    protected boolean      isKmUp;
    protected List<Fixing> fixings;
    protected Range        extent;

    public FixingsOverview() {
        fixings = new ArrayList<Fixing>();
        extent  = new Range(Double.MAX_VALUE, -Double.MAX_VALUE);
    }

    public FixingsOverview(String riverName) {
        this();
        this.riverName = riverName;
    }

    protected boolean loadRiver(Session session) {
        SQLQuery query = session.createSQLQuery(SQL_RIVER_ID)
            .addScalar("river_id", StandardBasicTypes.INTEGER)
            .addScalar("km_up",    StandardBasicTypes.BOOLEAN);

        query.setString("name", riverName);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.warn("No river '" + riverName + "' found.");
            return false;
        }

        Object [] row = list.get(0);

        riverId = (Integer)row[0];
        isKmUp  = (Boolean)row[1];

        return true;
    }

    protected void loadFixings(Session session) {
        SQLQuery query = session.createSQLQuery(SQL_FIXINGS)
            .addScalar("wst_id",      StandardBasicTypes.INTEGER)
            .addScalar("description", StandardBasicTypes.STRING);

        query.setInteger("river_id", riverId);

        List<Object []> list = query.list();

        if (list.isEmpty()) {
            log.warn("River " + riverId + " has no fixings.");
            // Its pretty fine to have no fixings.
        }

        for (Object [] row: list) {
            int    wstId       = (Integer)row[0];
            String description = (String) row[1];
            Fixing fixing = new Fixing(wstId, description);
            fixings.add(fixing);
        }
    }

    protected void loadFixingsColumns(Session session) {

        FixColumnLoader loader = new FixColumnLoader(
            allFixingIds(),
            session);

        for (Fixing fixing: fixings) {
            fixing.loadColumns(loader);
        }
    }

    protected List<Integer> allFixingIds() {
        List<Integer> ids = new ArrayList<Integer>(fixings.size());
        for (Fixing fixing: fixings) {
            ids.add(fixing.getId());
        }
        return ids;
    }

    protected List<Integer> allColumnIds() {
        List<Integer> cIds = new ArrayList<Integer>();
        for (Fixing fixing: fixings) {
            fixing.allColumnIds(cIds);
        }
        return cIds;
    }

    protected void loadFixingsColumnsKmRange(Session session) {

        KMRangeLoader loader = new KMRangeLoader(
            allColumnIds(),
            session);

        for (Fixing fixing: fixings) {
            fixing.loadColumnsKmRange(loader);
        }
    }

    protected void loadFixingsColumnsQRanges(
        Session     session,
        GaugeFinder gaugeFinder
    ) {

        ColumnQRangeLoader loader = new ColumnQRangeLoader(
            allColumnIds(),
            session);

        for (Fixing fixing: fixings) {
            fixing.loadColumnsQRanges(loader, gaugeFinder);
        }
    }

    protected void adjustExtent() {
        for (Fixing fixing: fixings) {
            fixing.adjustExtent(extent);
        }
    }

    public boolean load(Session session) {

        if (!loadRiver(session)) {
            return false;
        }

        GaugeFinderFactory gff = GaugeFinderFactory.getInstance();

        GaugeFinder gaugeFinder = gff.getGaugeFinder(riverId, isKmUp);

        if (gaugeFinder == null) {
            return false;
        }

        loadFixings(session);
        loadFixingsColumns(session);
        loadFixingsColumnsKmRange(session);

        adjustExtent();

        loadFixingsColumnsQRanges(session, gaugeFinder);

        return true;
    }

    public static final Range FULL_EXTENT =
        new Range(-Double.MAX_VALUE, Double.MAX_VALUE);

    public static final Fixing.Filter ACCEPT = new Fixing.Filter() {
        @Override
        public boolean accept(Fixing.Column column) {
            return true;
        }
    };

    public static class NotFilter implements Fixing.Filter {
        protected Fixing.Filter child;

        public NotFilter(Fixing.Filter child) {
            this.child = child;
        }

        @Override
        public boolean accept(Fixing.Column column) {
            return !child.accept(column);
        }
    } // class NotFilter

    public static abstract class ComponentFilter implements Fixing.Filter {
        protected List<Fixing.Filter> children;

        public ComponentFilter() {
            children = new ArrayList<Fixing.Filter>();
        }

        public ComponentFilter(List<Fixing.Filter> children) {
            this.children = children;
        }

        public ComponentFilter add(Fixing.Filter filter) {
            children.add(filter);
            return this;
        }
    } // class ComponentFilter

    public static class OrFilter extends ComponentFilter {

        public OrFilter() {
        }

        public OrFilter(List<Fixing.Filter> children) {
            super(children);
        }

        @Override
        public boolean accept(Fixing.Column column) {
            for (Fixing.Filter child: children) {
                if (child.accept(column)) {
                    return true;
                }
            }
            return false;
        }
    } // class OrFilter

    public static class AndFilter extends ComponentFilter {

        public AndFilter() {
        }

        public AndFilter(List<Fixing.Filter> children) {
            super(children);
        }

        @Override
        public boolean accept(Fixing.Column column) {
            for (Fixing.Filter child: children) {
                if (!child.accept(column)) {
                    return false;
                }
            }
            return true;
        }
    } // class AndFilter

    public static class IdFilter implements Fixing.Filter {

        protected int columnId;

        public IdFilter(int columnId) {
            this.columnId = columnId;
        }

        @Override
        public boolean accept(Fixing.Column column) {
            return column.getId() == columnId;
        }
    } // class IdFilter

    /** Accept Fixing columns whose id is in id list. */
    public static class IdsFilter implements Fixing.Filter {

        protected int [] columnIds;

        public IdsFilter(int [] columnIds) {
            this.columnIds = columnIds;
        }

        @Override
        public boolean accept(Fixing.Column column) {
            int cid = column.getId();
            for (int i = columnIds.length-1; i >= 0; --i) {
                if (columnIds[i] == cid) {
                    return true;
                }
            }
            return false;
        }
    } // class IdFilter

    public static class DateFilter implements Fixing.Filter {

        protected Date date;

        public DateFilter(Date date) {
            this.date = date;
        }

        @Override
        public boolean accept(Fixing.Column column) {
            return date.equals(column.getStartTime());
        }
    } // class DateFilter

    public static class DateRangeFilter implements Fixing.Filter {

        protected Date start;
        protected Date end;

        public DateRangeFilter(Date start, Date end) {
            if (start.before(end)) {
                this.start = start;
                this.end   = end;
            }
            else {
                this.start = end;
                this.end   = start;
            }
        }

        @Override
        public boolean accept(Fixing.Column column) {
            Date date = column.getStartTime();
            // start <= date <= end
            return !(date.before(start) || date.after(end));
        }
    } // class DateRangeFilter

    public static class SectorFilter implements Fixing.Filter {

        protected int sector;

        public SectorFilter(int sector) {
            this.sector = sector;
        }

        @Override
        public boolean accept(Fixing.Column column) {
            for (SectorRange s: column.getSectors()) {
                if (s.getSector() == sector) {
                    return true;
                }
            }
            return false;
        }
    } // class SectorFilter

    public static class SectorRangeFilter implements Fixing.Filter {

        protected int min;
        protected int max;

        public SectorRangeFilter(int min, int max) {
            this.min = Math.min(min, max);
            this.max = Math.max(min, max);
        }

        @Override
        public boolean accept(Fixing.Column column) {
            for (SectorRange s: column.getSectors()) {
                int v = s.getSector();
                if (v < min || v > max) {
                    return false;
                }
            }
            return true;
        }
    } // class SectorRangeFilter

    public static class KmFilter implements Fixing.Filter {

        protected double km;

        public KmFilter(double km) {
            this.km = km;
        }

        @Override
        public boolean accept(Fixing.Column column) {
            for (SectorRange s: column.getSectors()) {
                if (s.inside(km)) {
                    return true;
                }
            }
            return false;
        }
    } // class KmFilter

    public void generateOverview(Document document) {
        generateOverview(document, FULL_EXTENT, ACCEPT);
    }

    /**
     * @param range can be null.
     */
    public List<Fixing.Column> filter(Range range, Fixing.Filter filter) {
        List<Fixing.Column> allColumns = new ArrayList<Fixing.Column>();

        for (Fixing fixing: fixings) {
            fixing.addAllColumns(allColumns, range, filter);
        }

        Collections.sort(allColumns, Fixing.DATE_CMP);

        return allColumns;
    }

    protected static Range realRange(List<Fixing.Column> columns) {
        Range range = null;
        for (Fixing.Column column: columns) {
            if (range == null) {
                range = new Range(column);
            }
            else {
                range.extend(column);
            }
        }
        return range;
    }

    protected Element intersectingGauges(Document document, Range range) {
        Element gauges = document.createElement("gauges");

        if (range == null) {
            return gauges;
        }

        GaugeFinderFactory gff = GaugeFinderFactory.getInstance();

        GaugeFinder gf = gff.getGaugeFinder(riverId, isKmUp);

        if (gf == null) {
            return gauges;
        }

        for (GaugeRange gr: gf.getGauges()) {
            if (gr.intersects(range)) {
                Element gauge = document.createElement("gauge");
                gauge.setAttribute("from", String.valueOf(gr.getStart()));
                gauge.setAttribute("to",   String.valueOf(gr.getEnd()));
                gauge.setAttribute("name", gr.getName());
                gauges.appendChild(gauge);
            }
        }

        return gauges;
    }

    /** Populate document with fixings, filtered by range and filter. */
    public void generateOverview(
        Document      document,
        Range         range,
        Fixing.Filter filter
    ) {
        List<Fixing.Column> allColumns = filter(range, filter);

        Element fixingsElement = document.createElement("fixings");

        Element riverElement = document.createElement("river");

        riverElement.setAttribute("from", String.valueOf(extent.start));
        riverElement.setAttribute("to",   String.valueOf(extent.end));
        riverElement.setAttribute("rid",  String.valueOf(riverId));
        riverElement.setAttribute("name", riverName);

        fixingsElement.appendChild(riverElement);

        fixingsElement.appendChild(
            intersectingGauges(
                document,
                realRange(allColumns)));

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

        Element esE = document.createElement("events");

        for (Fixing.Column column: allColumns) {

            List<SectorRange> sectors = column.getSectors(range);

            if (!sectors.isEmpty()) {
                Element eE = document.createElement("event");
                eE.setAttribute("description",
                    String.valueOf(column.getDescription()));
                eE.setAttribute("cid", String.valueOf(column.columnId));
                eE.setAttribute("date", df.format(column.startTime));

                for (SectorRange sector: sectors) {
                    Element sE = document.createElement("sector");

                    sE.setAttribute("from",  String.valueOf(sector.start));
                    sE.setAttribute("to",    String.valueOf(sector.end));
                    sE.setAttribute("class", String.valueOf(sector.sector));

                    eE.appendChild(sE);
                }

                esE.appendChild(eE);
            }
        }

        fixingsElement.appendChild(esE);

        document.appendChild(fixingsElement);
    }

    public Range getExtent() {
        return extent;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
