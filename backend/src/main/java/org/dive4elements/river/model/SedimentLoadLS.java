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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import org.hibernate.Session;
import org.hibernate.Query;
import org.dive4elements.river.backend.SessionHolder;

/** SedimentLoadLS of a certain Fraction with possibly many values. */
@Entity
@Table(name = "sediment_load_ls")
public class SedimentLoadLS
implements   Serializable
{
    private Integer id;

    private River river;

    private GrainFraction grainFraction;

    private Unit unit;

    private TimeInterval timeInterval;

    private TimeInterval sqTimeInterval;

    private String description;

    private List<SedimentLoadLSValue> values;

    private Integer kind;


    public SedimentLoadLS() {
    }

    public SedimentLoadLS(River river, Unit unit, TimeInterval timeInterval) {
        this();

        this.river        = river;
        this.unit         = unit;
        this.timeInterval = timeInterval;
    }


    public SedimentLoadLS(
        River         river,
        Unit          unit,
        TimeInterval  timeInterval,
        GrainFraction grainFraction
    ) {
        this(river, unit, timeInterval);

        this.grainFraction = grainFraction;
    }


    public SedimentLoadLS(
        River         river,
        Unit          unit,
        TimeInterval  timeInterval,
        TimeInterval  sqTimeInterval,
        GrainFraction grainFraction,
        String        description
    ) {
        this(river, unit, timeInterval, grainFraction);

        this.sqTimeInterval = sqTimeInterval;
        this.description    = description;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SEDIMENT_LOAD_LS_ID_SEQ",
        sequenceName   = "SEDIMENT_LOAD_LS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SEDIMENT_LOAD_LS_ID_SEQ")
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

    @OneToOne
    @JoinColumn(name="grain_fraction_id")
    public GrainFraction getGrainFraction() {
        return grainFraction;
    }

    public void setGrainFraction(GrainFraction grainFraction) {
        this.grainFraction = grainFraction;
    }

    @OneToOne
    @JoinColumn(name = "unit_id")
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    @OneToOne
    @JoinColumn(name = "sq_time_interval_id")
    public TimeInterval getSqTimeInterval() {
        return sqTimeInterval;
    }

    public void setSqTimeInterval(TimeInterval sqTimeInterval) {
        this.sqTimeInterval = sqTimeInterval;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** kind == 0: "normal", kind == 1: "official epoch". */
    @Column(name = "kind")
    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer newKind) {
        this.kind = newKind;
    }

    @OneToMany
    @JoinColumn(name="sediment_load_ls_id")
    @OrderBy("station")
    public List<SedimentLoadLSValue> getSedimentLoadLSValues() {
        return values;
    }

    public void setSedimentLoadLSValues(List<SedimentLoadLSValue> values) {
        this.values = values;
    }

    public static SedimentLoadLS getSedimentLoadById(int id) {
        Session session = SessionHolder.HOLDER.get();

        Query query = session.createQuery(
            "from SedimentLoadLS where id=:db_id");

        query.setParameter("db_id", id);

        List<SedimentLoadLS> results = query.list();

        return results.isEmpty() ? null : results.get(0);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
