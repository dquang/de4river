/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;
import java.util.ArrayList;

import java.io.Serializable;

public class AttrList implements Serializable
{
    protected List<String> keyValues;

    public AttrList() {
        this(5);
    }

    public AttrList(int size) {
        keyValues = new ArrayList<String>(size*2);
    }

    public int size() {
        return keyValues != null ? keyValues.size()/2 : null;
    }

    public String getKey(int index) {
        return keyValues.get(index*2);
    }

    public String getValue(int index) {
        return keyValues.get(index*2 + 1);
    }

    public void add(String key, String value) {
        keyValues.add(key);
        keyValues.add(value);
    }

    public boolean hasAttribute(String key) {
        for (int i = 0, N = keyValues.size(); i < N; i += 2) {
            if (keyValues.get(i).equals(key)) {
                return true;
            }
        }
        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
