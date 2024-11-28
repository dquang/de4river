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
@Table(name = "fixpoints")
public class Fixpoint
implements   Serializable
{
    private Integer    id;
    private River      river;
    private Integer    x;
    private Integer    y;
    private BigDecimal km;
    private String     hpgp;
    private Point      geom;

    public Fixpoint() {
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


    @Column(name = "x")
    public Integer getX() {
        return x;
    }


    public void setX(Integer x) {
        this.x = x;
    }


    @Column(name = "y")
    public Integer getY() {
        return y;
    }


    public void setY(Integer y) {
        this.y = y;
    }


    @Column(name = "km")
    public BigDecimal getKm() {
        return km;
    }


    public void setKm(BigDecimal km) {
        this.km = km;
    }


    @Column(name = "hpgp")
    public String getHpgp() {
        return hpgp;
    }


    public void setHpgp(String hpgp) {
        this.hpgp = hpgp;
    }


    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Point getGeom() {
        return geom;
    }


    public void setGeom(Point geom) {
        this.geom = geom;
    }


    public static List<Fixpoint> getFixpoints(int riverId) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Fixpoint where river.id =:river_id");
        query.setParameter("river_id", riverId);

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
