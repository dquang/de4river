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

public class DataCageNode implements Serializable
{
    protected String             name;
    protected String             description;
    protected List<DataCageNode> children;
    protected AttrList           attrs;

    public DataCageNode() {
    }

    public DataCageNode(String name) {
        this(name, null);
    }

    public DataCageNode(String name, AttrList attrs) {
        this(name, name, attrs);
    }

    public DataCageNode(String name, String description, AttrList attrs) {
        this.name        = name;
        this.description = description;
        this.attrs       = attrs;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addChild(DataCageNode child) {
        if (children == null) {
            children = new ArrayList<DataCageNode>();
        }
        children.add(child);
    }

    public List<DataCageNode> getChildren() {
        return children;
    }

    public AttrList getAttributes() {
        return attrs;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
