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
import javax.persistence.OneToOne;
import javax.persistence.JoinColumn;
import javax.persistence.GenerationType;

import java.math.BigDecimal;


/** A Main or Extreme value of a rivers gauge. */
@Entity
@Table(name = "main_values")
public class MainValue
implements   Serializable
{
    private Integer        id;

    private Gauge          gauge;

    private NamedMainValue mainValue;

    private BigDecimal     value;

    private TimeInterval   timeInterval;

    public MainValue() {
    }

    public MainValue(
        Gauge          gauge,
        NamedMainValue mainValue,
        BigDecimal     value,
        TimeInterval   timeInterval
    ) {
        this.gauge        = gauge;
        this.mainValue    = mainValue;
        this.value        = value;
        this.timeInterval = timeInterval;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_MAIN_VALUES_ID_SEQ",
        sequenceName   = "MAIN_VALUES_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_MAIN_VALUES_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "gauge_id")
    public Gauge getGauge() {
        return gauge;
    }

    public void setGauge(Gauge gauge) {
        this.gauge = gauge;
    }

    @OneToOne
    @JoinColumn(name = "named_value_id")
    public NamedMainValue getMainValue() {
        return mainValue;
    }

    public void setMainValue(NamedMainValue mainValue) {
        this.mainValue = mainValue;
    }

    @Column(name = "value") // FIXME: type mapping needed?
    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @OneToOne
    @JoinColumn(name = "time_interval_id")
    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
