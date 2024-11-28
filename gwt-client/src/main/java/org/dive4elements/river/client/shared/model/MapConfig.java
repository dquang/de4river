/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class MapConfig implements Serializable {

    public static final String ATTR_SRID           = "srid";
    public static final String ATTR_MAX_EXTENT     = "max_extent";
    public static final String ATTR_INITIAL_EXTENT = "initial_extent";


    protected Map<String, String> attributes;


    public MapConfig() {
        attributes = new HashMap<String, String>();
    }


    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }


    public String getAttribute(String key) {
        return attributes.get(key);
    }


    public void setSrid(String srid) {
        setAttribute(ATTR_SRID, srid);
    }


    public String getSrid() {
        return getAttribute(ATTR_SRID);
    }


    public void setMaxExtent(String maxExtent) {
        setAttribute(ATTR_MAX_EXTENT, maxExtent);
    }


    public String getMaxExtent() {
        return getAttribute(ATTR_MAX_EXTENT);
    }


    public void setInitialExtent(String initialExtent) {
        setAttribute(ATTR_INITIAL_EXTENT, initialExtent);
    }


    public String getInitialExtent() {
        return getAttribute(ATTR_INITIAL_EXTENT);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
