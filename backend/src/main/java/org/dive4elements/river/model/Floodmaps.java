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

import com.vividsolutions.jts.geom.Geometry;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "floodmaps")
public class Floodmaps
implements   Serializable
{
    private Integer      id;
    private River        river;
    private String       name;
    private Integer      kind;
    private Integer      count;
    private BigDecimal   diff;
    private BigDecimal   area;
    private BigDecimal   perimeter;
    private Geometry     geom;

    public Floodmaps() {
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


    @Column(name = "kind")
    public Integer getKind() {
        return kind;
    }


    public void setKind(Integer kind) {
        this.kind = kind;
    }


    @Column(name = "count")
    public Integer getCount() {
        return count;
    }


    public void setCount(Integer count) {
        this.count = count;
    }


    @Column(name = "diff")
    public BigDecimal getDiff() {
        return diff;
    }


    public void setDiff(BigDecimal diff) {
        this.diff = diff;
    }


    @Column(name = "area")
    public BigDecimal getArea() {
        return area;
    }


    public void setArea(BigDecimal area) {
        this.area = area;
    }


    @Column(name = "perimeter")
    public BigDecimal getPerimeter() {
        return perimeter;
    }


    public void setPerimeter(BigDecimal perimeter) {
        this.perimeter = perimeter;
    }


    @Column(name = "geom")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Geometry getGeom() {
        return geom;
    }


    public void setGeom(Geometry geom) {
        this.geom = geom;
    }


    public static List<Floodmaps> getFloodmaps(int riverId, String name) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from Floodmaps where river.id =:river_id AND name =:name");
        query.setParameter("river_id", riverId);
        query.setParameter("name", name);

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
