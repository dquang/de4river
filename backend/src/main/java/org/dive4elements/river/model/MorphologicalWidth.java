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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


@Entity
@Table(name = "morphologic_width")
public class MorphologicalWidth implements Serializable {

    private Integer id;

    private River river;

    private Unit unit;

    private List<MorphologicalWidthValue> values;


    public MorphologicalWidth() {
    }


    public MorphologicalWidth(River river, Unit unit) {
        this.river = river;
        this.unit  = unit;
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_MORPHOLOGIC_WIDTH_ID_SEQ",
        sequenceName   = "MORPHOLOGIC_WIDTH_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_MORPHOLOGIC_WIDTH_ID_SEQ")
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
    @JoinColumn(name = "unit_id")
    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @OneToMany
    @JoinColumn(name = "morphologic_width_id")
    public List<MorphologicalWidthValue> getValues() {
        return values;
    }

    public void setValues(List<MorphologicalWidthValue> values) {
        this.values = values;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
