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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "sediment_load")
public class SedimentLoad
implements   Serializable
{
    private Integer id;

    private GrainFraction grainFraction;

    private TimeInterval timeInterval;

    private TimeInterval sqTimeInterval;

    private String description;

    private Integer kind;

    private List<SedimentLoadValue> values;

    public SedimentLoad() {
    }

    public SedimentLoad(
        GrainFraction grainFraction,
        TimeInterval  timeInterval,
        TimeInterval  sqTimeInterval,
        String        description,
        Integer       kind
    ) {
        this.grainFraction  = grainFraction;
        this.timeInterval   = timeInterval;
        this.sqTimeInterval = sqTimeInterval;
        this.description    = description;
        this.kind           = kind;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SEDIMENT_LOAD_ID_SEQ",
        sequenceName   = "SEDIMENT_LOAD_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SEDIMENT_LOAD_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
    @JoinColumn(name="sediment_load_id")
    public List<SedimentLoadValue> getSedimentLoadValues() {
        return values;
    }

    public void setSedimentLoadValues(List<SedimentLoadValue> values) {
        this.values = values;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
