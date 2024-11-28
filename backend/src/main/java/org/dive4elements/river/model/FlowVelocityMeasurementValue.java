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
import java.util.Date;

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


/** Measured Flow Velocities. */
@Entity
@Table(name = "flow_velocity_measure_values")
public class FlowVelocityMeasurementValue
implements   Serializable
{
    private static Logger log =
        LogManager.getLogger(FlowVelocityMeasurementValue.class);

    private Integer id;

    private FlowVelocityMeasurement measurement;

    private BigDecimal station;
    private BigDecimal w;
    private BigDecimal q;
    private BigDecimal v;

    private Date datetime;

    private String description;

    /** Non-mapped class holding same values. */
    public static class FastFlowVelocityMeasurementValue {
        protected double station;
        protected double w;
        protected double q;
        protected double v;
        protected Date   datetime;
        protected String description;

        public FastFlowVelocityMeasurementValue(double station,
            double w, double q, double v, Date datetime, String description) {
            this.station = station;
            this.w       = w;
            this.q       = q;
            this.v       = v;
            this.datetime = datetime;
            this.description = description;
        }

        public double getStation() {
            return station;
        }

        public double getW() {
            return w;
        }

        public double getQ() {
            return q;
        }

        public double getV() {
            return v;
        }

        public Date getDatetime() {
            return datetime;
        }

        public String getDescription() {
            return description;
        }
    }


    public FlowVelocityMeasurementValue() {
    }


    public FlowVelocityMeasurementValue(
        FlowVelocityMeasurement measurement,
        Date                    datetime,
        BigDecimal              station,
        BigDecimal              w,
        BigDecimal              q,
        BigDecimal              v,
        String                  description
    ) {
        this.measurement = measurement;
        this.datetime    = datetime;
        this.station     = station;
        this.w           = w;
        this.q           = q;
        this.v           = v;
        this.description = description;
    }

    public static FastFlowVelocityMeasurementValue getUnmapped(
            double station,
            double w,
            double q,
            double v,
            Date datetime,
            String description
    ) {
         return new FastFlowVelocityMeasurementValue(
             station, w, q, v, datetime, description);
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_FV_MEASURE_VALUES_ID_SEQ",
        sequenceName   = "FV_MEASURE_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_FV_MEASURE_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "measurements_id")
    public FlowVelocityMeasurement getMeasurement() {
        return measurement;
    }

    public void setMeasurement(FlowVelocityMeasurement measurement) {
        this.measurement = measurement;
    }

    @Column(name = "station")
    public BigDecimal getStation() {
        return station;
    }

    public void setStation(BigDecimal station) {
        this.station = station;
    }

    @Column(name = "datetime")
    public Date getDatetime() {
        return datetime;
    }

    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    @Column(name = "w")
    public BigDecimal getW() {
        return w;
    }

    public void setW(BigDecimal w) {
        this.w = w;
    }

    @Column(name = "q")
    public BigDecimal getQ() {
        return q;
    }

    public void setQ(BigDecimal q) {
        this.q = q;
    }

    @Column(name = "v")
    public BigDecimal getV() {
        return v;
    }

    public void setV(BigDecimal v) {
        this.v = v;
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
