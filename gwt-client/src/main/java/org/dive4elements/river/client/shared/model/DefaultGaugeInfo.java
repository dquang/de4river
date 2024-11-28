/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class DefaultGaugeInfo implements GaugeInfo {

    private String name;
    private Double start;
    private Double end;
    private Double aeo;
    private Double datum;
    private Double minq;
    private Double maxq;
    private Double minw;
    private Double maxw;
    private boolean kmup;
    private Double station;
    private String wstunit;
    private Long officialnumber;
    private String rivername;

    public DefaultGaugeInfo() {
    }

    public DefaultGaugeInfo(
            String rivername,
            String name,
            boolean kmup,
            Double station,
            Double start,
            Double end,
            Double datum,
            Double aeo,
            Double minq,
            Double maxq,
            Double minw,
            Double maxw,
            String wstunit,
            Long official)
    {
        this.rivername      = rivername;
        this.name           = name;
        this.kmup           = kmup;
        this.station        = station;
        this.start          = start;
        this.end            = end;
        this.datum          = datum;
        this.aeo            = aeo;
        this.minq           = minq;
        this.maxq           = maxq;
        this.minw           = minw;
        this.maxw           = maxw;
        this.wstunit        = wstunit;
        this.officialnumber = official;
    }
    /**
     * Returns the name of the gauge
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the start KM of the gauge or null if not available
     */
    public Double getKmStart() {
        return this.start;
    }

    /**
     * Returns the end KM of the gauge or null if not available
     */
    public Double getKmEnd() {
        return this.end;
    }

    /**
     * Returns the mimimum Q value at this gauge or null if not available
     */
    public Double getMinQ() {
        return this.minq;
    }

    /**
     * Returns the maximum Q value at this gauge or null if not available
     */
    public Double getMaxQ() {
        return this.maxq;
    }

    /**
     * Returns the mimimum W value at this gauge or null if not available
     */
    public Double getMinW() {
        return this.minw;
    }

    /**
     * Returns the maximim W value at this gauge or null if not available
     */
    public Double getMaxW() {
        return this.maxw;
    }

    /**
     * Returns the datum value or null if not available
     */
    public Double getDatum() {
        return this.datum;
    }

    /**
     * Returns the aeo value or null if not available
     */
    public Double getAeo() {
        return this.aeo;
    }

    public boolean isKmUp() {
        return this.kmup;
    }

    /**
     * Returns the station km of the gauge or null if not available
     */
    public Double getStation() {
        return this.station;
    }

    /**
     * Returns the wst unit as a String
     */
    public String getWstUnit() {
        return this.wstunit;
    }

    /**
     * Returns the official number of this gauge
     */
    public Long getOfficialNumber() {
        return this.officialnumber;
    }

    /**
     * Returns the river to which this gauge belongs
     */
    public String getRiverName() {
        return this.rivername;
    }
}
