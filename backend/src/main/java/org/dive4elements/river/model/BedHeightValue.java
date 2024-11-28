/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.util.List;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "bed_height_values")
public class BedHeightValue
implements   Serializable
{
    private static Logger log =
        LogManager.getLogger(BedHeightValue.class);

    private Integer id;

    private BedHeight bedHeight;

    private Double station;
    private Double height;
    private Double uncertainty;
    private Double dataGap;
    private Double soundingWidth;


    public BedHeightValue() {
    }

    public BedHeightValue(
        BedHeight bedHeight,
        Double station,
        Double height,
        Double uncertainty,
        Double dataGap,
        Double soundingWidth
    ) {
        this.bedHeight     = bedHeight;
        this.station       = station;
        this.height        = height;
        this.uncertainty   = uncertainty;
        this.dataGap       = dataGap;
        this.soundingWidth = soundingWidth;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_BED_HEIGHT_VALUE_ID_SEQ",
        sequenceName   = "BED_HEIGHT_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_BED_HEIGHT_VALUE_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "bed_height_id")
    public BedHeight getBedHeight() {
        return bedHeight;
    }

    public void setBedHeight(BedHeight bedHeight) {
        this.bedHeight = bedHeight;
    }

    @Column(name = "station")
    public Double getStation() {
        return station;
    }

    public void setStation(Double station) {
        this.station = station;
    }

    @Column(name = "height")
    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    @Column(name="uncertainty")
    public Double getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(Double uncertainty) {
        this.uncertainty = uncertainty;
    }

    @Column(name="data_gap")
    public Double getDataGap() {
        return dataGap;
    }

    public void setDataGap(Double dataGap) {
        this.dataGap = dataGap;
    }

    @Column(name="sounding_width")
    public Double getSoundingWidth() {
        return soundingWidth;
    }

    public void setSoundingWidth(Double soundingWidth) {
        this.soundingWidth = soundingWidth;
    }

    public static List<BedHeightValue> getBedHeightValues(
        BedHeight single) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from BedHeightValue where bedHeight=:single");

        query.setParameter("single", single);
        return query.list();
    }


    public static List<BedHeightValue> getBedHeightValues(
        BedHeight single,
        double kmLo,
        double kmHi
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from BedHeightValue where bedHeight=:single " +
            "   and station >= :kmLo and station <= :kmHi");

        query.setParameter("single", single);
        query.setParameter("kmLo", new Double(kmLo));
        query.setParameter("kmHi", new Double(kmHi));

        return query.list();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
