/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */
package org.dive4elements.river.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "sediment_load_values")
public class SedimentLoadValue
implements   Serializable
{
    private Integer id;

    private SedimentLoad sedimentLoad;

    private MeasurementStation measurementStation;

    private Double value;

    public SedimentLoadValue() {
    }

    public SedimentLoadValue(
        SedimentLoad       sedimentLoad,
        MeasurementStation measurementStation,
        Double             value
    ) {
        this.sedimentLoad       = sedimentLoad;
        this.measurementStation = measurementStation;
        this.value              = value;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_SEDIMENT_LOAD_VALUES_ID_SEQ",
        sequenceName   = "SEDIMENT_LOAD_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_SEDIMENT_LOAD_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "sediment_load_id")
    public SedimentLoad getSedimentLoad() {
        return sedimentLoad;
    }

    public void setSedimentLoad(SedimentLoad sedimentLoad) {
        this.sedimentLoad = sedimentLoad;
    }

    @OneToOne
    @JoinColumn(name = "measurement_station_id")
    public MeasurementStation getMeasurementStation() {
        return measurementStation;
    }

    public void setMeasurementStation(MeasurementStation measurementStation) {
        this.measurementStation = measurementStation;
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
