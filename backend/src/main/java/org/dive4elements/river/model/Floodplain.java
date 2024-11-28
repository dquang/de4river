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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Type;
import org.hibernate.HibernateException;

import com.vividsolutions.jts.geom.Polygon;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "floodplain")
public class Floodplain
implements   Serializable
{
    private Integer        id;

    private FloodplainKind kind;

    private River          river;

    private Polygon        geom;

    private String         name;


    public Floodplain() {
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

    @OneToOne
    @JoinColumn(name = "kind_id")
    public FloodplainKind getKind() {
        return kind;
    }

    public void setKind(FloodplainKind kind) {
        this.kind = kind;
    }

    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Polygon getGeom() {
        return geom;
    }

    public void setGeom(Polygon geom) {
        this.geom = geom;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public static List<Floodplain> getFloodplains(
        String river,
        String name,
        int kind
    ) throws HibernateException {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from Floodplain as fp where river.name =:river" +
            " and kind.id =:kind" +
            " and fp.name=:name");
        query.setParameter("river", river);
        query.setParameter("kind", kind);
        query.setParameter("name", name);

        List<Floodplain> list = query.list();
        return list.isEmpty() ? null : list;
    }

    public static List<Floodplain> getFloodplains(String river, int kind)
    throws HibernateException {
        Session session = SessionHolder.HOLDER.get();
        Query query = session.createQuery(
            "from Floodplain where river.name =:river AND kind.id =:kind");
        query.setParameter("river", river);
        query.setParameter("kind", kind);

        List<Floodplain> list = query.list();
        return list.isEmpty() ? null : list;
    }

    public static Floodplain getFloodplain(String river) {
        Session session = SessionHolder.HOLDER.get();

        // kind_id 0 -> Offical
        // kind_id 1 -> Misc.
        Query query = session.createQuery(
            "from Floodplain where river.name =:river and kind_id=1");
        query.setParameter("river", river);

        List<Floodplain> result = query.list();

        return result.isEmpty() ? null : result.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
