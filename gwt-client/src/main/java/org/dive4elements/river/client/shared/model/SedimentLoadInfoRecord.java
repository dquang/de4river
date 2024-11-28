/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import com.smartgwt.client.widgets.grid.ListGridRecord;


public class SedimentLoadInfoRecord
extends ListGridRecord
{
    protected SedimentLoadInfoObject sedimentLoadInfo;

    public SedimentLoadInfoRecord(SedimentLoadInfoObject info) {
        this.sedimentLoadInfo = info;
        setDescription(info.getDescription());
        setDate(info.getDate());
        setSQTiDate(info.getSQTiDate());
        setSQTiId(info.getSQTiId());
    }

    public void setDescription(String description) {
        setAttribute("description", description);
    }

    public void setDate(String date) {
        setAttribute("date", date);
    }

    public void setSQTiDate(String date) {
        setAttribute("sq_ti_date", date);
    }

    public void setSQTiId(String id) {
        setAttribute("sq_ti_id", id);
    }

    public String getDescription() {
        return getAttribute("description");
    }

    public String getDate() {
        return getAttribute("date");
    }

    public String getSQTiId() {
        return getAttribute("sq_ti_id");
    }

    public String getSQTiDate() {
        return getAttribute("sq_ti_date");
    }
}
