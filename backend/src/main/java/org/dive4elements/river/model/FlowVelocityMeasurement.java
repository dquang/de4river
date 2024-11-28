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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


@Entity
@Table(name = "flow_velocity_measurements")
public class FlowVelocityMeasurement
implements   Serializable
{
    private static Logger log =
        LogManager.getLogger(FlowVelocityMeasurement.class);

    private Integer id;

    private River river;

    private String description;

    private List<FlowVelocityMeasurementValue> values;


    public FlowVelocityMeasurement() {
    }


    public FlowVelocityMeasurement(River river, String description) {
        this.river       = river;
        this.description = description;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_FV_MEASURE_ID_SEQ",
        sequenceName   = "FV_MEASURE_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_FV_MEASURE_ID_SEQ")
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

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToMany
    @JoinColumn(name = "measurements_id")
    public List<FlowVelocityMeasurementValue> getValues() {
        return values;
    }

    public void setValues(List<FlowVelocityMeasurementValue> values) {
        this.values = values;
    }

    public void addValue(FlowVelocityMeasurementValue value) {
        this.values.add(value);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
