/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import com.vividsolutions.jts.geom.MultiLineString;

import java.io.Serializable;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.dive4elements.river.backend.SessionHolder;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.hibernate.annotations.Type;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A river has one axis that is used for calculation.
 * Additional axes of a river can be used to be painted int maps etc.
 * which one is the main river axis can be determined over the axis kind.
 */
@Entity
@Table(name = "river_axes")
public class RiverAxis
implements   Serializable
{
    private static Logger log = LogManager.getLogger(RiverAxis.class);

    private Integer    id;
    private AxisKind   kind;
    private River      river;
    private String     name;
    private MultiLineString geom;

    public static final int KIND_UNKOWN = 0;
    public static final int KIND_CURRENT = 1;
    public static final int KIND_OTHER = 2;

    public RiverAxis() {
    }


    @Id
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

    @Column(name = "name")
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get kind.
     *
     * @return kind as AxisKind.
     */
    @OneToOne
    @JoinColumn(name = "kind_id")
    public AxisKind getKind() {
        return kind;
    }

    /**
     * Set kind.
     *
     * @param kind the value to set.
     */
    public void setKind(AxisKind kind) {
        this.kind = kind;
    }


    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public MultiLineString getGeom() {
        return geom;
    }


    public void setGeom(MultiLineString geom) {
        this.geom = geom;
    }


    public static List<RiverAxis> getRiverAxis(
        String river,
        String name,
        int kind
    ) throws HibernateException {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from RiverAxis as ax where river.name =:river" +
            " and kind.id =:kind" +
            " and ax.name=:name");
        query.setParameter("river", river);
        query.setParameter("kind", kind);
        query.setParameter("name", name);

        List<RiverAxis> list = query.list();
        return list.isEmpty() ? null : list;
    }

    public static RiverAxis getRiverAxis(String river)
    throws HibernateException {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from RiverAxis where river.name =:river AND kind.id =:kind");
        query.setParameter("river", river);
        query.setParameter("kind", KIND_CURRENT);

        List<RiverAxis> list = query.list();

        /* We expect that every river has only one RiverAxis of kind 1
           thow this is not currently enforced in database schema. */
        if (list.size() > 1) {
            log.error("River " + river + " has more than one current axis.");
        }

        return list.isEmpty() ? null : list.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
