/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import org.dive4elements.river.artifacts.states.DefaultState.ComputeType;
import org.dive4elements.artifactdatabase.state.Facet;


public class WMSDBLayerFacet extends WMSLayerFacet {

    protected String data;
    protected String filter;
    protected String labelItem;
    protected String geometryType;
    protected String connection;
    protected String connectionType;


    public WMSDBLayerFacet() {
        super();
    }


    public WMSDBLayerFacet(int index, String name, String description) {
        this(index, name, description, ComputeType.FEED, null, null);
    }


    public WMSDBLayerFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash

    ) {
        super(index, name, description, type, stateId, hash);
    }


    public WMSDBLayerFacet(
        int         index,
        String      name,
        String      description,
        ComputeType type,
        String      stateId,
        String      hash,
        String      url
    ) {
        super(index, name, description, type, stateId, hash, url);
    }


    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
    }

    public String getGeometryType() {
        return geometryType;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setLabelItem(String labelItem) {
        this.labelItem = labelItem;
    }

    public String getLabelItem() {
        return labelItem;
    }


    @Override
    public boolean isQueryable() {
        return true;
    }

    /** Clone Facet-bound data. */
    protected void cloneData(WMSDBLayerFacet copy) {
        super.cloneData(copy);
        copy.setFilter(this.getFilter());
        copy.setData(this.getData());
        copy.setGeometryType(this.getGeometryType());
        copy.setConnection(this.getConnection());
        copy.setConnectionType(this.getConnectionType());
        copy.setLabelItem(this.getLabelItem());
    }

    /** Copy deeply. */
    @Override
    public Facet deepCopy() {
        WMSDBLayerFacet copy = new WMSDBLayerFacet();
        copy.set(this);

        cloneData(copy);

        return copy;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
