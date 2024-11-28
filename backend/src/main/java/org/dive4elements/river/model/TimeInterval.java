/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.model;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;

@Entity
@Table(name = "time_intervals")
public class TimeInterval
implements   Serializable
{
    private Integer id;
    private Date    startTime;
    private Date    stopTime;

    public TimeInterval() {
    }

    public TimeInterval(Date startTime, Date stopTime) {
        this.startTime = startTime;
        this.stopTime  = stopTime;
    }

    @Id
    @SequenceGenerator(
        name           = "SEQUENCE_TIME_INTERVALS_ID_SEQ",
        sequenceName   = "TIME_INTERVALS_ID_SEQ",
        allocationSize = 1)
    @GeneratedValue(
        strategy  = GenerationType.SEQUENCE,
        generator = "SEQUENCE_TIME_INTERVALS_ID_SEQ")
    @Column(name = "id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(name = "start_time") // FIXME: type mapping needed?
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Column(name = "stop_time") // FIXME: type mapping needed?
    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
