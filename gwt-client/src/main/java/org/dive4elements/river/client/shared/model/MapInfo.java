/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


public class MapInfo implements Serializable {

    private static final long serialVersionUID = 6691651180549280493L;

    protected String river;
    protected int    srid;
    protected BBox   bbox;
    protected String wmsUrl;
    protected String wmsLayers;
    protected String backgroundWmsUrl;
    protected String backgroundWmsLayers;


    public MapInfo() {
    }


    public MapInfo(
        String river,
        int    srid,
        BBox   bbox,
        String wmsUrl,
        String wmsLayers,
        String backgroundWmsUrl,
        String backgroundWmsLayers)
    {
        this.river               = river;
        this.srid                = srid;
        this.bbox                = bbox;
        this.wmsUrl              = wmsUrl;
        this.wmsLayers           = wmsLayers;
        this.backgroundWmsUrl    = backgroundWmsUrl;
        this.backgroundWmsLayers = backgroundWmsLayers;
    }


    public String getRiver() {
        return river;
    }


    public int getSrid() {
        return srid;
    }


    public String getProjection() {
        return "EPSG:" + srid;
    }


    public BBox getBBox() {
        return bbox;
    }


    public String getWmsUrl() {
        return wmsUrl;
    }


    public String getWmsLayers() {
        return this.wmsLayers;
    }


    public String getBackgroundWmsUrl() {
        return backgroundWmsUrl;
    }


    public String getBackgroundWmsLayers() {
        return backgroundWmsLayers;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
