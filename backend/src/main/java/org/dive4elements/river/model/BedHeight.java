/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;

import org.hibernate.Session;
import org.hibernate.Query;

import org.dive4elements.river.backend.SessionHolder;


@Entity
@Table(name = "bed_height")
public class BedHeight implements Serializable {

    private Integer id;
    private Integer year;

    private String evaluationBy;
    private String description;

    private River river;

    private BedHeightType  type;

    private LocationSystem locationSystem;

    private ElevationModel curElevationModel;

    private ElevationModel oldElevationModel;

    private Range range;

    private List<BedHeightValue> values;


    public BedHeight() {
    }


    public BedHeight(
        River          river,
        Integer        year,
        BedHeightType  type,
        LocationSystem locationSystem,
        ElevationModel curElevationModel,
        Range          range
    ) {
        this(
            river,
            year,
            type,
            locationSystem,
            curElevationModel,
            null,
            range,
            null,
            null);
    }


    public BedHeight(
        River          river,
        Integer        year,
        BedHeightType  type,
        LocationSystem locationSystem,
        ElevationModel curElevationModel,
        ElevationModel oldElevationModel,
        Range          range,
        String         evaluationBy,
        String         description
    ) {
        this.river             = river;
        this.year              = year;
        this.type              = type;
        this.locationSystem    = locationSystem;
        this.curElevationModel = curElevationModel;
        this.oldElevationModel = oldElevationModel;
        this.range             = range;
        this.evaluationBy      = evaluationBy;
        this.description       = description;
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_BED_HEIGHT_ID_SEQ",
        sequenceName   = "BED_HEIGHT_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_BED_HEIGHT_ID_SEQ")
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

    @Column(name = "year")
    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @OneToOne
    @JoinColumn(name = "type_id")
    public BedHeightType getType() {
        return type;
    }

    public void setType(BedHeightType type) {
        this.type = type;
    }

    @OneToOne
    @JoinColumn(name = "location_system_id")
    public LocationSystem getLocationSystem() {
        return locationSystem;
    }

    public void setLocationSystem(LocationSystem locationSystem) {
        this.locationSystem = locationSystem;
    }

    @OneToOne
    @JoinColumn(name = "cur_elevation_model_id")
    public ElevationModel getCurElevationModel() {
        return curElevationModel;
    }

    public void setCurElevationModel(ElevationModel curElevationModel) {
        this.curElevationModel = curElevationModel;
    }

    @OneToOne
    @JoinColumn(name = "old_elevation_model_id")
    public ElevationModel getOldElevationModel() {
        return oldElevationModel;
    }

    public void setOldElevationModel(ElevationModel oldElevationModel) {
        this.oldElevationModel = oldElevationModel;
    }

    @OneToOne
    @JoinColumn(name = "range_id")
    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    @Column(name = "evaluation_by")
    public String getEvaluationBy() {
        return evaluationBy;
    }

    public void setEvaluationBy(String evaluationBy) {
        this.evaluationBy = evaluationBy;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany
    @JoinColumn(name = "bed_height_id")
    public List<BedHeightValue> getValues() {
        return values;
    }

    public void setValues(List<BedHeightValue> values) {
        this.values = values;
    }


    public static List<BedHeight> getBedHeights(
        River  river,
        double kmLo,
        double kmHi
    ) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from BedHeight where river=:river");

        query.setParameter("river", river);

        // TODO Do km range filtering in SQL statement

        List<BedHeight> singles = query.list();
        List<BedHeight> good    = new ArrayList<BedHeight>();

        for (BedHeight s: singles) {
            for (BedHeightValue value: s.getValues()) {
                double station = value.getStation().doubleValue();

                if (station >= kmLo && station <= kmHi) {
                    good.add(s);
                    break;
                }
            }
        }

        return good;
    }


    public static BedHeight getBedHeightById(int id) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from BedHeight where id=:id");

        query.setParameter("id", id);

        List<BedHeight> singles = query.list();

        return singles != null ? singles.get(0) : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
