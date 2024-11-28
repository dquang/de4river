/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class WMSLayer implements Serializable {

    protected String server;
    protected String name;
    protected String title;

    protected List<String>   srs;
    protected List<WMSLayer> layers;

    protected boolean queryable = true;


    public WMSLayer() {
        layers = new ArrayList<WMSLayer>();
    }


    /**
     * @param server
     * @param title
     * @param name
     * @param layers
     */
    public WMSLayer(
        String         server,
        String         title,
        String         name,
        List<String>   srs,
        List<WMSLayer> layers,
        boolean        queryable
    ) {
        this.server    = server;
        this.title     = title;
        this.name      = name;
        this.srs       = srs;
        this.layers    = layers;
        this.queryable = queryable;
    }


    public String getServer() {
        return server;
    }


    public String getName() {
        return name;
    }


    public String getTitle() {
        return title;
    }


    public List<String> getSrs() {
        return srs;
    }


    public List<WMSLayer> getLayers() {
        return layers;
    }


    public boolean isQueryable() {
        return queryable;
    }


    public boolean supportsSrs(String srs) {
        if (this.srs == null || this.srs.size() == 0) {
            return true;
        }

        if (!srs.startsWith("EPSG:")) {
            srs = "EPSG:" + srs;
        }

        return this.srs.contains(srs);
    }


    @Override
    public String toString() {
        return "WMS Layer: " + title + " (" + name + ") " + server;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
