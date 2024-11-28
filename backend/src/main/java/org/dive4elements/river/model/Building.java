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

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.LineString;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "buildings")
public class Building
implements   Serializable
{
    private Integer    id;
    private River      river;
    private String     name;
    private LineString geom;

    public Building() {
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


    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public LineString getGeom() {
        return geom;
    }


    public void setGeom(LineString geom) {
        this.geom = geom;
    }

    public static List<Building> getBuildings(int riverId, int kindId) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Building where river.id =:river_id and kind_id=:kind_id");
        query.setParameter("kind_id", kindId);
        query.setParameter("river_id", riverId);

        return query.list();
    }

    public static List<Building> getBuildings(int riverId, String name) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Building where river.id =:river_id and name=:name");
        query.setParameter("river_id", riverId);
        query.setParameter("name", name);

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
