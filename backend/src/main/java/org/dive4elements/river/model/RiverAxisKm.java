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
@Table(name = "river_axes_km")
public class RiverAxisKm
implements   Serializable
{
    private Integer    id;
    private River      river;
    private BigDecimal km;
    private Point      geom;


    public RiverAxisKm() {
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


    /**
     * Returns a list of RiverAxisKm objects for a given river.
     *
     * @param riverid The ID of a river in the database.
     *
     * @return a list of RiverAxisKm objects.
     */
    public static List<RiverAxisKm> getRiverAxisKms(int riverid) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from RiverAxisKm where river.id =:riverid");
        query.setParameter("riverid", riverid);

        List<RiverAxisKm> list = query.list();

        return list;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
