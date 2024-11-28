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

import com.vividsolutions.jts.geom.Geometry;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.annotations.Type;

import org.dive4elements.river.backend.SessionHolder;

@Entity
@Table(name = "jetties")
public class Jetty
implements   Serializable
{
    private Integer    id;
    private River      river;
    private Geometry   geom;

    public Jetty() {
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

    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Geometry getGeom() {
        return geom;
    }


    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    public static List<Jetty> getJetties(int riverId, int kindId) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Jetty where river.id =:river_id and kind_id=:kind_id");
        query.setParameter("kind_id", kindId);
        query.setParameter("river_id", riverId);

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
