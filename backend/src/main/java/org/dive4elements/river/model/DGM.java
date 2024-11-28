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
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "dem")
public class DGM implements Serializable {

    private Integer      id;
    private Integer      srid;

    private River        river;

    private Range        range;
    private TimeInterval time_interval;

    private String       path;


    public DGM() {
    }


    public void setId(Integer id) {
        this.id = id;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_DEM_ID_SEQ",
        sequenceName   = "DEM_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_DEM_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setRiver(River river) {
        this.river = river;
    }

    @OneToOne
    @JoinColumn(name = "river_id")
    public River getRiver() {
        return river;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Column(name = "path")
    public String getPath() {
        return path;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    @Column(name = "srid")
    public int getSrid() {
        return srid;
    }

    public static DGM getDGM(int id) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from DGM where Id =:id");
        query.setParameter("id", id);

        List<DGM> result = query.list();

        return result.isEmpty() ? null : result.get(0);
    }


    public static DGM getDGM(String river, double lower, double upper) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from DGM where river.name =:river and " +
            "range.a <=:lower and range.b >=:lower and " +
            "range.a <=:upper and range.b >=:upper");
        query.setParameter("river", river);
        query.setParameter("lower", new BigDecimal(lower));
        query.setParameter("upper", new BigDecimal(upper));

        List<DGM> result = query.list();

        return result.isEmpty() ? null : result.get(0);
    }

    @OneToOne
    @JoinColumn(name = "range_id")
    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return time_interval;
    }

    public void setTimeInterval(TimeInterval time_interval) {
        this.time_interval = time_interval;
    }


}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
