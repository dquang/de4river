/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */

public class DefaultRiverInfo implements RiverInfo {

    private String name;
    private boolean kmup;
    private Double start;
    private Double end;
    private String wstunit;
    private Double minq;
    private Double maxq;
    private Long officialnumber;
    private String muuid;

    private List<GaugeInfo> gaugeinfo;
    private List<MeasurementStation> mstations;

    public DefaultRiverInfo() {
    }

    public DefaultRiverInfo(
            String name,
            boolean kmup,
            Double start,
            Double end,
            String wstunit,
            Double minq,
            Double maxq,
            Long official,
            String muuid)
    {
        this.name           = name;
        this.kmup           = kmup;
        this.start          = start;
        this.end            = end;
        this.wstunit        = wstunit;
        this.minq           = minq;
        this.maxq           = maxq;
        this.officialnumber = official;
        this.muuid          = muuid;
    }

    public boolean isKmUp() {
        return this.kmup;
    }

    /**
     * Start KM of the river
     */
    public Double getKmStart() {
        return this.start;
    }

    /**
     * End KM of the river
     */
    public Double getKmEnd() {
        return this.end;
    }

    /**
     * Returns the name of the river
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the name of the WST unit
     */
    public String getWstUnit() {
        return this.wstunit;
    }

    /**
     * Return all gauge info of the river or null if they aren't available.
     */
    public List<GaugeInfo> getGauges() {
        return this.gaugeinfo;
    }

    /**
     * Returns the min q value of the river
     */
    public Double getMinQ() {
        return this.minq;
    }

    /**
     * Returns the max q value of the river
     */
    public Double getMaxQ() {
        return maxq;
    }

    /**
     * Returns the official number of the river
     */
    public Long getOfficialNumber() {
        return this.officialnumber;
    }

    /**
     * Returns the model uuid of the river
     * @return
     */
    public String getModelUuid() {
        return muuid;
    }

    /**
     * Returns the MeasurementStations on this river or null if they aren't
     * available.
     */
    @Override
    public List<MeasurementStation> getMeasurementStations() {
        return this.mstations;
    }

    public void setGauges(List<GaugeInfo> gauges) {
        this.gaugeinfo = gauges;
    }

    public void setMeasurementStations(List<MeasurementStation> mstations) {
        this.mstations = mstations;
    }
}
