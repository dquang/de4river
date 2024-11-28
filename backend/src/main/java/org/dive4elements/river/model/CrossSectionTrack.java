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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.LineString;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "cross_section_tracks")
public class CrossSectionTrack
implements   Serializable
{
    private Integer    id;
    private River      river;
    private String     name;
    private LineString geom;
    private BigDecimal km;
    private BigDecimal z;

    public CrossSectionTrack() {
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


    @Column(name = "km")
    public BigDecimal getKm() {
        return km;
    }


    public void setKm(BigDecimal km) {
        this.km = km;
    }


    @Column(name = "z")
    public BigDecimal getZ() {
        return z;
    }


    public void setZ(BigDecimal z) {
        this.z = z;
    }


    public static List<CrossSectionTrack> getCrossSectionTrack(
        String river)
    {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from CrossSectionTrack where river.name =:river");
        query.setParameter("river", river);

        return query.list();
    }


    public static List<CrossSectionTrack> getCrossSectionTrack(
        String river,
        String name
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from CrossSectionTrack as cst " +
            "    where river.name =:river" +
            "      and cst.name=:name");
        query.setParameter("river", river);
        query.setParameter("name", name);

        return query.list();
    }

    public static List<CrossSectionTrack> getCrossSectionTrack(
        String river,
        int kind_id
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from CrossSectionTrack as cst " +
            "    where river.name =:river" +
            "      and kind_id=:kind_id");
        query.setParameter("river", river);
        query.setParameter("kind_id", kind_id);

        return query.list();
    }

    public static List<CrossSectionTrack> getCrossSectionTrack(
        String river,
        String name,
        int kind_id
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from CrossSectionTrack as cst " +
            "    where river.name =:river" +
            "      and cst.name=:name" +
            "      and kind_id=:kind_id");
        query.setParameter("river", river);
        query.setParameter("name", name);
        query.setParameter("kind_id", kind_id);

        return query.list();
    }


    /**
     * Returns the nearest CrossSectionTrack of <i>river</i> to a given
     * <i>km</i>.
     *
     * @param river The name of a river.
     * @param km The kilometer value.
     *
     * @return the nearest CrossSectionTrack to <i>km</i> of river <i>river</i>.
     */
    public static CrossSectionTrack getCrossSectionTrack(
        String river,
        double km
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from CrossSectionTrack where river.name =:river " +
            "and kind_id = 1 " +
            "order by abs( km - :mykm)");
        query.setParameter("river", river);
        query.setParameter("mykm", new BigDecimal(km));

        List<CrossSectionTrack> cst = query.list();

        return cst != null && !cst.isEmpty() ? cst.get(0) : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
