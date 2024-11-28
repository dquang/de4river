/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;
import java.math.BigDecimal;
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.hibernate.Query;
import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.backend.SessionHolder;


/** DB-mapped WST. */
@Entity
@Table(name = "wsts")
public class Wst
implements   Serializable
{
    private static Logger log = LogManager.getLogger(Wst.class);

    private Integer id;
    private River   river;
    private String  description;
    private Integer kind;

    private List<WstColumn> columns;


    public static final String SQL_SELECT_MINMAX =
        "select min(q) as minQ, max(q) as maxQ from wst_q_values " +
        "where wst_id = :wst and not (a > :km or b < :km)";

    public Wst() {
    }

    public Wst(River river, String description) {
        this(river, description, 0);
    }

    public Wst(River river, String description, Integer kind) {
        this.river       = river;
        this.description = description;
        this.kind        = kind;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_WSTS_ID_SEQ",
        sequenceName   = "WSTS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_WSTS_ID_SEQ")
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

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "kind")
    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    @OneToMany
    @JoinColumn(name="wst_id")
    public List<WstColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<WstColumn> columns) {
        this.columns = columns;
    }


    /**
     * Determines the min and max Q values of this WST. The min value is placed
     * in the first field of the resulting array - the max value is placed in
     * the second field.
     *
     * @return the min and max Q values of this WST.
     */
    public double[] determineMinMaxQ() {
        double[] ab = river.determineMinMaxDistance();
        return determineMinMaxQ(new Range(ab[0], ab[1], river));
    }


    /**
     * Determines the min and max Q values of this WST in the given range. The
     * min value is placed in the first field of the resulting array - the max
     * value is placed in the second field.
     *
     * @param range The range used for querying the Q values.
     *
     * @return the min and max Q values of this WST.
     */
    public double[] determineMinMaxQ(Range range) {
        if (range != null) {
            return determineMinMaxQ(
                range.getA().doubleValue(),
                range.getB().doubleValue());
        }

        return null;
    }


    /**
     * Determines the min and max Q values of this WST in the given range. The
     * min value is placed in the first field of the resulting array - the max
     * value is placed in the second field.
     *
     * @param fromKm the lower km value.
     * @param toKm the upper km value.
     *
     * @return the min and max Q values of this WST.
     */
    public double[] determineMinMaxQ(double fromKm, double toKm) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
          "select min(q), max(q) from WstQRange where "
          + "id in "
          + " (select wstQRange.id from WstColumnQRange where "
          + "  wstColumn.id in (select id from WstColumn where wst.id = :wst)) "
          + "and range.id in "
          + " (select id from Range where not (a > :end or b < :start))");

        query.setParameter("wst",   getId());
        query.setParameter("start", new BigDecimal(fromKm));
        query.setParameter("end",   new BigDecimal(toKm));

        List<Object []> results = query.list();

        if (results.isEmpty()) {
            return null;
        }

        if (results.get(0)[0] == null || results.get(0)[1] == null) {
            log.warn("Could not process result from min/maxQ query.");
            return null;
        }

        Object [] result = results.get(0);

        return new double [] {
            ((BigDecimal)result[0]).doubleValue(),
            ((BigDecimal)result[1]).doubleValue() };
    }


    public double[] determineMinMaxQFree(double km) {
        Session session = SessionHolder.HOLDER.get();

        SQLQuery sqlQuery = session.createSQLQuery(SQL_SELECT_MINMAX)
            .addScalar("minQ", StandardBasicTypes.DOUBLE)
            .addScalar("maxQ", StandardBasicTypes.DOUBLE);

        sqlQuery.setInteger("wst", getId());
        sqlQuery.setDouble("km", km);

        List<Object[]> minmaxQ = sqlQuery.list();


        if (minmaxQ.isEmpty()) {
            return null;
        }

        Object[] mm = minmaxQ.get(0);

        if (mm[0] == null || mm[1] == null) {
            log.warn("No min/max Q for km " + km + " found.");
            return null;
        }

        return new double[] { (Double) mm[0], (Double) mm[1] };
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
