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
@Table(name = "sediment_density")
public class SedimentDensity implements Serializable {

    private Integer id;

    private River river;

    private Depth depth;

    private List<SedimentDensityValue> values;

    private String description;


    public SedimentDensity() {
    }


    public SedimentDensity(River river, Depth depth, String desc) {
        this.river       = river;
        this.depth       = depth;
        this.description = desc;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SEDIMENT_DENSITY_ID_SEQ",
        sequenceName   = "SEDIMENT_DENSITY_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SEDIMENT_DENSITY_ID_SEQ")
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

    @OneToMany
    @JoinColumn(name="sediment_density_id")
    public List<SedimentDensityValue> getValues() {
        return values;
    }

    public void setValues(List<SedimentDensityValue> values) {
        this.values = values;
    }

    public void addValue(SedimentDensityValue value) {
        this.values.add(value);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
