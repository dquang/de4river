/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

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


@Entity
@Table(name = "sediment_load_ls_values")
public class SedimentLoadLSValue
implements   Serializable
{
    private static Logger log = LogManager.getLogger(SedimentLoadLSValue.class);

    private Integer id;

    private SedimentLoadLS sedimentLoadLS;

    private Double station;
    private Double value;


    public SedimentLoadLSValue() {
    }

    public SedimentLoadLSValue(
        SedimentLoadLS sedimentLoadLS,
        Double        station,
        Double        value
    ) {
        this.sedimentLoadLS = sedimentLoadLS;
        this.station       = station;
        this.value         = value;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SEDIMENT_LOAD_LS_VALUES_ID_SEQ",
        sequenceName   = "SEDIMENT_LOAD_LS_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SEDIMENT_LOAD_LS_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "sediment_load_ls_id" )
    public SedimentLoadLS getSedimentLoadLS() {
        return sedimentLoadLS;
    }

    public void setSedimentLoadLS(SedimentLoadLS sedimentLoadLS) {
        this.sedimentLoadLS = sedimentLoadLS;
    }

    @Column(name="station")
    public Double getStation() {
        return station;
    }

    public void setStation(Double station) {
        this.station = station;
    }

    @Column(name = "value")
    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
