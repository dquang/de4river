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
@Table(name = "porosity_values")
public class PorosityValue implements Serializable {

    private Integer id;

    private Porosity porosity;

    private BigDecimal station;
    private BigDecimal shoreOffset;
    private BigDecimal porosityValue;

    private String description;


    public PorosityValue() {
    }


    public PorosityValue(
        Porosity        porosity,
        BigDecimal      station,
        BigDecimal      shoreOffset,
        BigDecimal      porosityValue,
        String          desc
    ) {
        this.porosity        = porosity;
        this.station         = station;
        this.shoreOffset     = shoreOffset;
        this.porosityValue   = porosityValue;
        this.description     = desc;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_POROSITY_VALUES_ID_SEQ",
        sequenceName   = "POROSITY_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_POROSITY_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "porosity_id")
    public Porosity getPorosity() {
        return porosity;
    }

    public void setPorosity(Porosity porosity) {
        this.porosity = porosity;
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

    @Column(name = "porosity")
    public BigDecimal getPorosityValue() {
        return porosityValue;
    }

    public void setPorosityValue(BigDecimal porosityValue) {
        this.porosityValue = porosityValue;
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
