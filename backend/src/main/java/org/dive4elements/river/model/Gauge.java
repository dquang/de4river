/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.math.BigDecimal;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.backend.SessionHolder;

/** Database-mapped Gauge with all info about it. */
@Entity
@Table(name = "gauges")
public class Gauge
implements   Serializable, Comparable<Gauge>
{
    private static final Logger log = LogManager.getLogger(Gauge.class);

    public static final int MASTER_DISCHARGE_TABLE = 0;

    private Integer    id;
    private String     name;
    private River      river;
    private BigDecimal station;
    private BigDecimal aeo;
    private BigDecimal datum;
    private Long       officialNumber;
    private Range      range;

    private List<DischargeTable> dischargeTables;

    /** MainValues at this Gauge. */
    protected List<MainValue> mainValues;

    public Gauge() {
    }

    public Gauge(
        String     name,
        River      river,
        BigDecimal station,
        BigDecimal aeo,
        BigDecimal datum,
        Long       officialNumber,
        Range      range
    ) {
        this.name            = name;
        this.river           = river;
        this.station         = station;
        this.aeo             = aeo;
        this.datum           = datum;
        this.officialNumber  = officialNumber;
        this.range           = range;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_GAUGES_ID_SEQ",
        sequenceName   = "GAUGES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_GAUGES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "river_id" )
    public River getRiver() {
        return river;
    }

    public void setRiver(River river) {
        this.river = river;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "station") // FIXME: type mapping needed
    public BigDecimal getStation() {
        return station;
    }

    public void setStation(BigDecimal station) {
        this.station = station;
    }

    @Column(name = "aeo") // FIXME: type mapping needed
    public BigDecimal getAeo() {
        return aeo;
    }

    public void setAeo(BigDecimal aeo) {
        this.aeo = aeo;
    }

    @Column(name = "datum") // FIXME: type mapping needed
    public BigDecimal getDatum() {
        return datum;
    }

    public void setDatum(BigDecimal datum) {
        this.datum = datum;
    }

    @Column(name = "official_number")
    public Long getOfficialNumber() {
        return officialNumber;
    }

    public void setOfficialNumber(Long officialNumber) {
        this.officialNumber = officialNumber;
    }

    @OneToOne
    @JoinColumn(name = "range_id" )
    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @OneToMany
    @JoinColumn(name = "gauge_id")
    public List<DischargeTable> getDischargeTables() {
        return dischargeTables;
    }

    public void setDischargeTables(List<DischargeTable> dischargeTables) {
        this.dischargeTables = dischargeTables;
    }


    /**
     * Returns min and max W values of this gauge.
     *
     * @return the min and max W value of this gauge [min,max].
     */
    public double[] determineMinMaxW() {
        Session session = SessionHolder.HOLDER.get();

        DischargeTable dischargeTable = fetchMasterDischargeTable();

        if (dischargeTable == null) {
            return null;
        }

        Query query  = session.createQuery(
            "select min(w) as min, max(w) as max from DischargeTableValue " +
            "where table_id =:table");
        query.setParameter("table", dischargeTable.getId());

        List<?> results = query.list();
        if (results.isEmpty()) {
            log.error("No values in discharge table found.");
            return null;
        }

        Object[] result  = (Object[])results.get(0);

        BigDecimal a = (BigDecimal)result[0];
        BigDecimal b = (BigDecimal)result[1];

        return a != null && b != null
            ? new double [] { a.doubleValue(), b.doubleValue() }
            : null;
    }

    @OneToMany
    @JoinColumn(name = "gauge_id")
    public List<MainValue> getMainValues() {
        return mainValues;
    }

    public void setMainValues(List<MainValue> mainValues) {
        this.mainValues = mainValues;
    }

    public static Gauge getGaugeByOfficialNumber(long number) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Gauge where officialNumber=:number");

        query.setParameter("number", number);

        List<Gauge> results = query.list();

        return results.isEmpty() ? null : results.get(0);
    }

    public static Gauge getGaugeByOfficialNumber(
        long number,
        String river_name
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Gauge as gau " +
            "where gau.officialNumber=:number and gau.river.name=:river_name");

        query.setParameter("number", number);
        query.setParameter("river_name", river_name);

        List<Gauge> results = query.list();

        return results.isEmpty() ? null : results.get(0);
    }


    public DischargeTable fetchMasterDischargeTable() {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from DischargeTable " +
            "where kind = 0 " +
            "and gauge = :gauge");

        query.setParameter("gauge", this);

        List<Object> results = query.list();

        return results.isEmpty()
            ? null
            : (DischargeTable)results.get(0);
    }

    /**
     * Returns an array of [days, qs] necessary to create duration curves.
     *
     * @return a 2dim array of [days, qs] where days is an int[] and qs is
     * an double[].
     */
    public Object[] fetchDurationCurveData() {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "select cast(nmv.name as integer) as days, mv.value as q " +
            "from MainValue as mv " +
            "join mv.mainValue as nmv " +
            "join nmv.type mvt " +
            "where mvt.name = 'D' and mv.gauge.id = :gauge_id " +
            "order by days");

        query.setParameter("gauge_id", getId());

        List<Object> results = query.list();
        int[]        days    = new int[results.size()];
        double[]     qs      = new double[results.size()];

        int idx = 0;

        for (Object obj: results) {
            Object[] arr = (Object[]) obj;

            try {
                int  day = ((Integer)    arr[0]).intValue();
                double q = ((BigDecimal) arr[1]).doubleValue();

                days[idx] = day;
                qs[idx++] = q;
            }
            catch (NumberFormatException nfe) {
            }
        }

        return new Object[] { days, qs };
    }

    /**
     * Calculates the maximum and minimum W and Q values
     *
     * @return the MaxMinWQ object representing the calculated values
     */
    public MinMaxWQ fetchMaxMinWQ() {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "select max(mv.value) as max, min(mv.value) as min " +
            "from MainValue as mv " +
            "join mv.mainValue as nmv " +
            "join nmv.type mvt " +
            "where mvt.name in ('W', 'Q') " +
            "and mv.gauge.id = :gauge_id " +
            "group by mvt.name order by mvt.name"
            );

        query.setParameter("gauge_id", getId());

        List<Object> results = query.list();
        if (results.isEmpty()) {
            // No values found
            return new MinMaxWQ();
        }

        Object[] arr = (Object[]) results.get(0);
        BigDecimal maxw = (BigDecimal)arr[0];
        BigDecimal minw = (BigDecimal)arr[1];
        BigDecimal maxq = null;
        BigDecimal minq = null;


        if (results.size() > 1) {
            arr = (Object[]) results.get(1);
            maxq = (BigDecimal)arr[0];
            minq = (BigDecimal)arr[1];
        }

        return new MinMaxWQ(minw, maxw, minq, maxq);
    }

    @Override
    public int compareTo(Gauge o) {
        return getName().compareTo(o.getName());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
