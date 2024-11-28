/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.model.MeasurementStation;

public class SQRelationAccess
extends      RiverAccess
{
    private static Logger log = LogManager.getLogger(SQRelationAccess.class);

    protected Double    location;

    protected DateRange period;

    protected Double    outliers;

    private String      method;

    protected MeasurementStation measurementStation;

    public SQRelationAccess() {
    }

    public SQRelationAccess(D4EArtifact artifact) {
        super(artifact);
    }

    public Double getLocation() {
        if (location == null) {
            // XXX: The parameter name suggests plural!?
            location = getDouble("ld_locations");
        }

        if (log.isDebugEnabled()) {
            log.debug("location: " + location);
        }

        return location;
    }

    public DateRange getPeriod() {
        if (period == null) {
            Long start = getLong("start");
            Long end   = getLong("end");

            if (start != null && end != null) {
                period = new DateRange(new Date(start), new Date(end));
            }
        }

        return period;
    }

    public Double getOutliers() {
        if (outliers == null) {
            outliers = getDouble("outliers");
        }
        if (log.isDebugEnabled()) {
            log.debug("outliers: " + outliers);
        }
        return outliers;
    }

    public String getOutlierMethod() {
        if (method == null) {
            method = getString("outlier-method");
        }
        if (log.isDebugEnabled()) {
            log.debug("outlier-method: " + method);
        }
        return method;
    }

    public String getMeasurementStationName() {
        MeasurementStation station = getMeasurementStation();
        return station == null ? null : station.getName();
    }

    public String getMeasurementStationGaugeName() {
        MeasurementStation station = getMeasurementStation();
        return station == null ? null : station.getGaugeName();
    }

    public MeasurementStation getMeasurementStation() {
        if (measurementStation != null) {
            return measurementStation;
        }
        List<MeasurementStation> candidates = MeasurementStation
            .getStationsAtKM(getRiver(), getLocation());
        if (candidates != null && !candidates.isEmpty()) {
            // Just take the first one as we only use the name
            // and that "should" be unique at the location
            measurementStation = candidates.get(0);
        }

        return measurementStation;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
