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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.annotations.Type;

import com.vividsolutions.jts.geom.MultiLineString;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "hydr_boundaries")
public class HydrBoundary
implements   Serializable
{
    private Integer    id;
    private SectieKind sectie;
    private SobekKind  sobek;
    private String     name;
    private River      river;
    private MultiLineString geom;
    private BoundaryKind kind;

    public HydrBoundary() {
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_HYDR_BOUNDARIES_ID_SEQ",
        sequenceName   = "HYDR_BOUNDARIES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_HYDR_BOUNDARIES_ID_SEQ")
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
    public MultiLineString getGeom() {
        return geom;
    }


    public void setGeom(MultiLineString geom) {
        this.geom = geom;
    }

    public static List<HydrBoundary> getHydrBoundaries(
        int riverId,
        String name,
        int kindId
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from HydrBoundary where river.id =:river_id and name=:name" +
            " and kind.id=:kind_id");
        query.setParameter("river_id", riverId);
        query.setParameter("name", name);
        query.setParameter("kind_id", kindId);

        return query.list();
    }

    public static List<HydrBoundary> getHydrBoundaries(
        int riverId,
        String name
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from HydrBoundary where river.id =:river_id and name=:name");
        query.setParameter("river_id", riverId);
        query.setParameter("name", name);

        return query.list();
    }

    /**
     * Get sectie.
     *
     * @return sectie as SectieKind.
     */
    @OneToOne
    @JoinColumn(name = "sectie")
    public SectieKind getSectie()
    {
        return sectie;
    }

    /**
     * Set sectie.
     *
     * @param sectie the value to set.
     */
    public void setSectie(SectieKind sectie)
    {
        this.sectie = sectie;
    }

    /**
     * Get sobek.
     *
     * @return sobek as SobekKind.
     */
    @OneToOne
    @JoinColumn(name = "sobek")
    public SobekKind getSobek()
    {
        return sobek;
    }

    /**
     * Set sobek.
     *
     * @param sobek the value to set.
     */
    public void setSobek(SobekKind sobek)
    {
        this.sobek = sobek;
    }

    /**
     * Get kind.
     *
     * @return kind as BoundaryKind.
     */
    @OneToOne
    @JoinColumn(name = "kind")
    public BoundaryKind getKind()
    {
        return kind;
    }

    /**
     * Set kind.
     *
     * @param kind the value to set.
     */
    public void setKind(BoundaryKind kind)
    {
        this.kind = kind;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
