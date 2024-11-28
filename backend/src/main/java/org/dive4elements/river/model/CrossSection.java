/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.util.List;
import java.util.ArrayList;

import java.awt.geom.Point2D;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.JoinColumn;

import java.math.MathContext;
import java.math.BigDecimal;

import org.hibernate.Session;
import org.hibernate.SQLQuery;
import org.hibernate.Query;

import org.hibernate.type.StandardBasicTypes;

import org.dive4elements.river.backend.SessionHolder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Entity
@Table(name = "cross_sections")
public class CrossSection
implements   Serializable
{
    private static Logger log =
        LogManager.getLogger(CrossSection.class);

    public static final MathContext PRECISION = new MathContext(6);

    public static final String SQL_FAST_CROSS_SECTION_LINES =
        "SELECT km, x, y, csl.id AS csl_id " +
        "FROM cross_section_lines csl JOIN cross_section_points csp " +
        "ON csp.cross_section_line_id = csl.id " +
        "WHERE csl.cross_section_id = :cs_id AND " +
        "km between :from_km AND :to_km " +
        "ORDER BY csl.km, csl.id, csp.col_pos";

    public static final String SQL_MIN_MAX =
        "SELECT * FROM ( "+
            "SELECT cross_section_id, MIN(km) AS minkm, MAX(km) AS maxkm " +
            "FROM cross_section_lines " +
            "WHERE cross_section_id IN " +
            " (SELECT id FROM cross_sections WHERE river_id = :river_id) " +
            "  GROUP BY cross_section_id" +
        ") cs_ranges " +
        "JOIN cross_sections cs ON cs_ranges.cross_section_id = cs.id " +
        "LEFT OUTER JOIN time_intervals " +
        "    ON cs.time_interval_id = time_intervals.id " +
        "WHERE :km BETWEEN minkm AND maxkm " +
        "ORDER BY stop_time desc, start_time desc, :km - minkm";
    // Order by time interval missing.

    private Integer                id;
    private River                  river;
    private TimeInterval           timeInterval;
    private String                 description;
    private List<CrossSectionLine> lines;

    public CrossSection() {
    }

    public CrossSection(
        River        river,
        TimeInterval timeInterval,
        String       description
    ) {
        this.river        = river;
        this.timeInterval = timeInterval;
        this.description  = description;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_CROSS_SECTIONS_ID_SEQ",
        sequenceName   = "CROSS_SECTIONS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_CROSS_SECTIONS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "river_id")
    public River getRiver() {
        return river;
    }

    public void setRiver(River river) {
        this.river = river;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany
    @OrderBy("km")
    @JoinColumn(name="cross_section_id")
    public List<CrossSectionLine> getLines() {
        return lines;
    }

    public void setLines(List<CrossSectionLine> lines) {
        this.lines = lines;
    }

    public List<CrossSectionLine> getLines(double startKm, double endKm) {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from CrossSectionLine where crossSection=:crossSection " +
            "and km between :startKm and :endKm order by km");
        query.setParameter("crossSection", this);
        query.setParameter("startKm", new BigDecimal(startKm, PRECISION));
        query.setParameter("endKm", new BigDecimal(endKm, PRECISION));

        return query.list();
    }

    /** Get Lines from startkm to endkm, fast because direct usage of sql. */
    public List<FastCrossSectionLine> getFastLines(
        double startKm,
        double endKm
    ) {
        Session session = SessionHolder.HOLDER.get();

        SQLQuery sqlQuery = session.createSQLQuery(
            SQL_FAST_CROSS_SECTION_LINES)
            .addScalar("km",     StandardBasicTypes.DOUBLE)
            .addScalar("x",      StandardBasicTypes.DOUBLE)
            .addScalar("y",      StandardBasicTypes.DOUBLE)
            .addScalar("csl_id", StandardBasicTypes.INTEGER);

        sqlQuery
            .setInteger("cs_id",  getId())
            .setDouble("from_km", startKm)
            .setDouble("to_km",   endKm);

        List<Object []> results = sqlQuery.list();

        ArrayList<Point2D> points = new ArrayList<Point2D>(500);
        ArrayList<FastCrossSectionLine> lines =
            new ArrayList<FastCrossSectionLine>();

        Integer lastId = null;
        Double  lastKm = null;

        for (Object [] result: results) {
            Double  km = (Double)result[0];
            Double  x  = (Double)result[1];
            Double  y  = (Double)result[2];
            Integer id = (Integer)result[3];

            if (lastId != null && !lastId.equals(id)) {
                points.trimToSize();
                FastCrossSectionLine line =
                    new FastCrossSectionLine(lastKm, points);
                lines.add(line);
                points = new ArrayList<Point2D>(500);
            }

            points.add(new Point2D.Double(x, y));

            lastKm = km;
            lastId = id;
        }

        if (lastId != null) {
            points.trimToSize();
            FastCrossSectionLine line =
                new FastCrossSectionLine(lastKm, points);
            lines.add(line);
        }

        lines.trimToSize();

        return lines;
    }

    /**
     * True if the given section is the "newest" for that river
     * and has values at km.
     * @param km Given station.
     * @return true if the section has the most advanced end of its validity
     *         interval or the most advanced start of its validity interval.
     */
    public boolean shouldBeMaster(double km) {
        Session session = SessionHolder.HOLDER.get();

        SQLQuery sqlQuery = session.createSQLQuery(SQL_MIN_MAX)
            .addScalar("cross_section_id", StandardBasicTypes.INTEGER);

        sqlQuery
            .setInteger("river_id", getRiver().getId())
            .setDouble("km", km);

        List<Integer> results = sqlQuery.list();

        if (results.size() >= 1) {
            Integer result = results.get(0);
            if (result == getId()) {
                return true;
            }
        }
        else {
            log.warn("No CS found that could be master.");
        }

        // TODO If there is none, might need a fallback.
        // Formerly this was the most current CS (issue1157).

        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
