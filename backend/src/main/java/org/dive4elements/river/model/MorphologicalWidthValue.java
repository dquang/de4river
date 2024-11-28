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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;


@Entity
@Table(name = "morphologic_width_values")
public class MorphologicalWidthValue implements Serializable {

    private Integer id;

    private MorphologicalWidth morphologicalWidth;

    private BigDecimal station;
    private BigDecimal width;

    private String description;


    public MorphologicalWidthValue() {
    }


    public MorphologicalWidthValue(
        MorphologicalWidth morphologicalWidth,
        BigDecimal         station,
        BigDecimal         width,
        String             description
    ) {
        this.morphologicalWidth = morphologicalWidth;
        this.station            = station;
        this.width              = width;
        this.description        = description;
    }


    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_MORPH_WIDTH_VALUES_ID_SEQ",
        sequenceName   = "MORPH_WIDTH_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_MORPH_WIDTH_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    @OneToOne
    @JoinColumn(name = "morphologic_width_id")
    public MorphologicalWidth getMorphologicalWidth() {
        return morphologicalWidth;
    }

    public void setMorphologicalWidth(MorphologicalWidth width) {
        this.morphologicalWidth = width;
    }

    @Column(name = "station")
    public BigDecimal getStation() {
        return station;
    }

    public void setStation(BigDecimal station) {
        this.station = station;
    }

    @Column(name = "width")
    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
