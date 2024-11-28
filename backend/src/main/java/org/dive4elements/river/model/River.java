/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import org.dive4elements.river.backend.SessionHolder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Entity
@Table(name = "rivers")
public class River
implements   Serializable
{
    private static Logger log = LogManager.getLogger(River.class);

    public static final MathContext PRECISION = new MathContext(6);

    public static final double EPSILON = 1e-5;

    // Tolerance for determining whether we are at the station of a gauge
    public static final double GAUGE_EPSILON = 0.1;

    public static final Comparator<Double> KM_CMP = new Comparator<Double>() {
        @Override
        public int compare(Double a, Double b) {
            double diff = a - b;
            if (diff < -EPSILON) return -1;
            if (diff >  EPSILON) return +1;
            return 0;
        }
    };

    private Integer id;

    private Long    officialNumber;

    private String  name;

    private boolean kmUp;

    private String modelUuid;

    private List<Gauge> gauges;

    private Unit wstUnit;

    private SeddbName seddbName;

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_RIVERS_ID_SEQ",
        sequenceName   = "RIVERS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_RIVERS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "official_number")
    public Long getOfficialNumber() {
        return officialNumber;
    }

    public void setOfficialNumber(Long officialNumber) {
        this.officialNumber = officialNumber;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Type(type="numeric_boolean")
    @Column(name = "km_up")
    public boolean getKmUp() {
        return kmUp;
    }

    public void setKmUp(boolean kmUp) {
        this.kmUp = kmUp;
    }

    @Column(name = "model_uuid")
    public String getModelUuid() {
        return this.modelUuid;
    }

    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }

    public River() {
    }

    public River(String name, Unit wstUnit, String modelUuid) {
        this.name      = name;
        this.modelUuid = modelUuid;
        this.wstUnit   = wstUnit;
    }

    @OneToMany
    @JoinColumn(name="river_id")
    public List<Gauge> getGauges() {
        return gauges;
    }

    public void setGauges(List<Gauge> gauges) {
        this.gauges = gauges;
    }

    @OneToOne
    @JoinColumn(name = "wst_unit_id" )
    public Unit getWstUnit() {
        return wstUnit;
    }

    public void setWstUnit(Unit wstUnit) {
        this.wstUnit = wstUnit;
    }


    /**
     * Get alternative seddb name.
     *
     * This is the name should be used in seddb queries
     * and might differ from "our" backend db name.
     *
     * @return The name River in the seddb.
     */
    public String nameForSeddb() {
        SeddbName alt = getSeddbName();
        if (alt == null) {
            return getName();
        }
        return alt.getName();
    }


    @OneToOne
    @JoinColumn(name = "seddb_name_id" )
    public SeddbName getSeddbName() {
        return seddbName;
    }

    public void setSeddbName(SeddbName name) {
        this.seddbName = name;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }


    /**
     * This method returns the gauges that intersect with <i>a</i> and
     * <i>b</i>,
     *
     * @param a A start point.
     * @param b An end point.
     *
     * @return the intersecting gauges.
     */
    public List<Gauge> determineGauges(double a, double b) {
        Session session = SessionHolder.HOLDER.get();

        if (a > b) { double t = a; a = b; b = t; }

        Query query = session.createQuery(
            "from Gauge where river=:river " +
            "and not " +
            "((:b < least(range.a, range.b)) or" +
            " (:a > greatest(range.a, range.b)))" +
            "order by a");
        query.setParameter("river", this);
        query.setParameter("a", new BigDecimal(a, PRECISION));
        query.setParameter("b", new BigDecimal(b, PRECISION));

        return query.list();
    }

    public Gauge maxOverlap(double a, double b) {
        List<Gauge> gauges = determineGauges(a, b);
        if (gauges == null) {
            return null;
        }

        if (a > b) { double t = a; a = b; b = t; }

        double max = -Double.MAX_VALUE;

        Gauge result = null;

        for (Gauge gauge: gauges) {
            Range  r = gauge.getRange();
            double c = r.getA().doubleValue();
            double d = r.getB().doubleValue();

            if (c > d) { double t = c; c = d; d = t; }

            double start = c >= a ? c : a;
            double stop  = d <= b ? d : b;

            double length = stop - start;

            if (length > max) {
                max = length;
                result = gauge;
            }
        }

        return result;
    }

    public Gauge determineGaugeByName(String name) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from Gauge where river=:river and name=:name");
        query.setParameter("river", this);
        query.setParameter("name", name);
        List<Gauge> gauges = query.list();
        return gauges.isEmpty() ? null : gauges.get(0);
    }

    public Gauge determineGaugeByPosition(double p) {
        // Per default, we prefer the gauge downstream
        return determineGaugeByPosition(p, getKmUp());
    }

    /**
     * @param p Station on this river for which the gauge is searched
     * @param kmLower At boundary of two gauge ranges, should gauge at lower
     * km be returned?
     */
    public Gauge determineGaugeByPosition(double p, boolean kmLower) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from Gauge g where river=:river "  +
            "and :p between " +
            "least(g.range.a, g.range.b) and " +
            "greatest(g.range.a, g.range.b)");
        query.setParameter("river", this);
        query.setParameter("p", new BigDecimal(p, PRECISION));
        List<Gauge> gauges = query.list();
        if (gauges.isEmpty()) {
            return null;
        }
        if (gauges.size() == 1) {
            return gauges.get(0);
        }
        if (gauges.size() > 2) {
            // TODO: database schema should prevent this.
            log.warn("More than two gauge ranges overlap km " + p +
                ". Returning arbitrary result.");
        }
        Gauge g0 = gauges.get(0);
        Gauge g1 = gauges.get(1);
        if (kmLower) {
            return
                g0.getStation().doubleValue() < g1.getStation().doubleValue()
                ? g0
                : g1;
        }
        return g0.getStation().doubleValue() > g1.getStation().doubleValue()
            ? g0
            : g1;
    }


    /**
     * @param s station at which the gauge is requested.
     * @return Gauge within tolerance at given station. null if there is none.
     */
    public Gauge determineGaugeAtStation(double s) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Gauge where river.id=:river " +
            "and station between :a and :b");
        query.setParameter("river", getId());
        query.setParameter("a", new BigDecimal(s - GAUGE_EPSILON));
        query.setParameter("b", new BigDecimal(s + GAUGE_EPSILON));

        List<Gauge> gauges = query.list();
        if (gauges.size() > 1) {
            log.warn("More than one gauge found at km " + s +
                " within +-" + GAUGE_EPSILON +
                ". Returning arbitrary result.");
        }
        return gauges.isEmpty() ? null : gauges.get(0);
    }

    public double[] determineMinMaxQ() {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "select min(wqr.q) as min, max(wqr.q) as max " +
            "from Wst as w " +
            "join w.columns as wc " +
            "join wc.columnQRanges as wcqr " +
            "join wcqr.wstQRange as wqr " +
            "where w.kind = 0 and river_id = :river");

        query.setParameter("river", getId());

        double minmax[] = new double[] { Double.MAX_VALUE, -Double.MAX_VALUE };

        List<Object> results = query.list();

        if (!results.isEmpty()) {
            Object[] arr = (Object[]) results.get(0);
            BigDecimal minq = (BigDecimal)arr[0];
            BigDecimal maxq = (BigDecimal)arr[1];
            minmax[0] = minq.doubleValue();
            minmax[1] = maxq.doubleValue();
        }

        return minmax;
    }

    /**
     * Determine reference gauge dependent on direction of calculation
     * for a range calculation, otherwise dependent on flow direction.
     */
    public Gauge determineRefGauge(double[] range, boolean isRange) {
        if (isRange) {
            return determineGaugeByPosition(
                range[0],
                range[0] > range[1]);
        }
        else {
            return determineGaugeByPosition(range[0]);
        }
    }

    /**
     * Returns the min and max distance of this river. The first position in the
     * resulting array contains the min distance, the second position the max
     * distance.
     *
     * @return the min and max distance of this river.
     */
    public double[] determineMinMaxDistance() {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "select min(range.a), max(range.b) from Gauge "
            + "where river=:river "
            + "and range is not null");
        query.setParameter("river", this);

        List<Object[]> result = query.list();

        if (!result.isEmpty()) {
            Object[] minMax = result.get(0);
            if (minMax[0] != null && minMax[1] != null) {
                return new double[] { ((BigDecimal)minMax[0]).doubleValue(),
                    ((BigDecimal)minMax[1]).doubleValue() };
            }
        }

        return null;
    }

    public Map<Double, Double> queryGaugeDatumsKMs() {
        List<Gauge> gauges = getGauges();
        Map<Double, Double> result = new TreeMap<Double, Double>(KM_CMP);

        for (Gauge gauge: gauges) {
            BigDecimal km    = gauge.getStation();
            BigDecimal datum = gauge.getDatum();
            if (km != null && datum != null) {
                result.put(km.doubleValue(), datum.doubleValue());
            }
        }

        return result;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
