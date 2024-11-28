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


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FeatureInfo implements Serializable {

    protected String layername;

    protected Map<String, String> attrs;


    public FeatureInfo() {
    }


    public FeatureInfo(String layername) {
        this.layername = layername;
        this.attrs     = new HashMap<String, String>();
    }


    public void setLayername(String layername) {
        this.layername = layername;
    }


    public String getLayername() {
        return layername;
    }


    public void addAttr(String key, String value) {
        if (key != null && key.length() > 0) {
            attrs.put(key, value);
        }
    }


    public Map<String, String> getAttrs() {
        return attrs;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
