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
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;


@Entity
@Table(name = "porosity")
public class Porosity implements Serializable {

    private Integer id;

    private River river;

    private Depth depth;

    private List<PorosityValue> values;

    private String description;

    private TimeInterval timeInterval;

    public Porosity() {
    }


    public Porosity(
        River river,
        Depth depth,
        String desc,
        TimeInterval timeInterval
    ) {
        this.river       = river;
        this.depth       = depth;
        this.description = desc;
        this.timeInterval = timeInterval;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_POROSITY_ID_SEQ",
        sequenceName   = "POROSITY_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_POROSITY_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "river_id" )
    public River getRiver() {
        return river;
    }

    public void setRiver(River river) {
        this.river = river;
    }

    @OneToOne
    @JoinColumn(name = "depth_id")
    public Depth getDepth() {
        return depth;
    }

    public void setDepth(Depth depth) {
        this.depth = depth;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    @OneToMany
    @JoinColumn(name="porosity_id")
    public List<PorosityValue> getValues() {
        return values;
    }

    public void setValues(List<PorosityValue> values) {
        this.values = values;
    }

    public void addValue(PorosityValue value) {
        this.values.add(value);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
