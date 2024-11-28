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
@Table(name = "sediment_density_values")
public class SedimentDensityValue implements Serializable {

    private Integer id;

    private SedimentDensity sedimentDensity;

    private BigDecimal station;
    private BigDecimal shoreOffset;
    private BigDecimal density;
    private BigDecimal year;

    private String description;


    public SedimentDensityValue() {
    }


    public SedimentDensityValue(
        SedimentDensity sedimentDensity,
        BigDecimal      station,
        BigDecimal      shoreOffset,
        BigDecimal      density,
        BigDecimal      year,
        String          desc
    ) {
        this.sedimentDensity = sedimentDensity;
        this.station         = station;
        this.shoreOffset     = shoreOffset;
        this.density         = density;
        this.year            = year;
        this.description     = desc;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SEDIMENT_DENSITY_VALUES_ID_SEQ",
        sequenceName   = "SEDIMENT_DENSITY_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SEDIMENT_DENSITY_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "sediment_density_id")
    public SedimentDensity getSedimentDensity() {
        return sedimentDensity;
    }

    public void setSedimentDensity(SedimentDensity sedimentDensity) {
        this.sedimentDensity = sedimentDensity;
    }

    @Column(name = "station")
    public BigDecimal getStation() {
        return station;
    }

    public void setStation(BigDecimal station) {
        this.station = station;
    }

    @Column(name = "shore_offset")
    public BigDecimal getShoreOffset() {
        return shoreOffset;
    }

    public void setShoreOffset(BigDecimal shoreOffset) {
        this.shoreOffset = shoreOffset;
    }

    @Column(name = "density")
    public BigDecimal getDensity() {
        return density;
    }

    public void setDensity(BigDecimal density) {
        this.density = density;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "year")
    public BigDecimal getYear() {
        return year;
    }

    public void setYear(BigDecimal year) {
        this.year = year;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
