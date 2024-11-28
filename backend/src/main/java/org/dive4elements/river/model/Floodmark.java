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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.Point;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "flood_marks")
public class Floodmark
implements   Serializable
{
    private Integer    id;
    private River      river;
    private Integer    z;
    private Integer    year;
    private BigDecimal km;
    private Point      geom;

    public Floodmark() {
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


    @Column(name = "z")
    public Integer getZ() {
        return z;
    }


    public void setZ(Integer z) {
        this.z = z;
    }


    @Column(name = "year")
    public Integer getYear() {
        return year;
    }


    public void setYear(Integer year) {
        this.year = year;
    }


    @Column(name = "km")
    public BigDecimal getKm() {
        return km;
    }


    public void setKm(BigDecimal km) {
        this.km = km;
    }


    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Point getGeom() {
        return geom;
    }


    public void setGeom(Point geom) {
        this.geom = geom;
    }

    public static List<Floodmark> getFloodmarks(int riverId) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Floodmark where river.id =:river_id");
        query.setParameter("river_id", riverId);

        return query.list();
    }

    public static List<Floodmark> getFloodmarks(int riverId, int year) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Floodmark where river.id =:river_id and year = :year");
        query.setParameter("river_id", riverId);
        query.setParameter("year", year);

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

