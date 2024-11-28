/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.Date;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class DefaultMeasurementStation implements MeasurementStation {

    private String  name;
    private Double  start;
    private Double  end;
    private String  rivername;
    private String  measurementtype;
    private String  riverside;
    private Integer id;
    private String  moperator;
    private Date    starttime;
    private Date    stoptime;
    private String  gaugename;
    private String  comment;

    public DefaultMeasurementStation() {
    }

    public DefaultMeasurementStation(
            String  rivername,
            String  name,
            Integer id,
            Double  start,
            Double  end,
            String  riverside,
            String  measurementtype,
            String  moperator,
            Date    starttime,
            Date    stoptime,
            String  gaugename,
            String  comment
    ) {
        this.rivername       = rivername;
        this.name            = name;
        this.start           = start;
        this.end             = end;
        this.riverside       = riverside;
        this.measurementtype = measurementtype;
        this.id              = id;
        this.moperator       = moperator;
        this.starttime       = starttime;
        this.stoptime        = stoptime;
        this.gaugename       = gaugename;
        this.comment         = comment;
    }

    /**
     * Returns the name of the measurement station
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the start KM of the measurement station or null if not available
     */
    @Override
    public Double getKmStart() {
        return this.start;
    }

    /**
     * Returns the end KM of the measurement station or null if not available
     */
    @Override
    public Double getKmEnd() {
        return this.end;
    }

    /**
     * Returns the river to which this measurement station belongs
     */
    @Override
    public String getRiverName() {
        return this.rivername;
    }

    /**
     * Returns the type of the measurement station
     */
    @Override
    public String getMeasurementType() {
        return this.measurementtype;
    }

    /**
     * Returns the side of the river where this measurement station belongs
     */
    @Override
    public String getRiverSide() {
        return this.riverside;
    }

    /**
     * Returns the ID of the measurement station
     */
    @Override
    public Integer getID() {
        return this.id;
    }

    /**
     * Returns the operator of the measurement station
     */
    @Override
    public String getOperator() {
        return this.moperator;
    }

    /**
     * Returns the start time of the observation at this measurement station
     */
    @Override
    public Date getStartTime() {
        return this.starttime;
    }

    /**
     * Returns the end time of the observation at this measurement station
     */
    @Override
    public Date getStopTime() {
        return this.stoptime;
    }

    /**
     * Returns the name of the gauge in reference to this measurement station
     */
    @Override
    public String getGaugeName() {
        return this.gaugename;
    }

    /**
     * Returns the comment to this measurement station
     */
    @Override
    public String getComment() {
        return this.comment;
    }
}
