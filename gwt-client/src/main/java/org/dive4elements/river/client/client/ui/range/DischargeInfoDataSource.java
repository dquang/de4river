/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.range;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.FieldType;

public class DischargeInfoDataSource extends DataSource {

    public static final String XPATH_DISCHARGE_DEFAULT =
        "/discharges/discharge";


    public DischargeInfoDataSource(String url, long gauge, String river) {
        setDataFormat(DSDataFormat.XML);
        setRecordXPath(XPATH_DISCHARGE_DEFAULT);

        DataSourceField desc = new DataSourceField(
            "description", FieldType.TEXT, "description");

        DataSourceField bfgid = new DataSourceField(
            "bfg-id", FieldType.TEXT, "bfgid");

        DataSourceField start = new DataSourceField(
            "start", FieldType.TEXT, "start");

        DataSourceField end = new DataSourceField(
            "end", FieldType.TEXT, "end");

        setFields(desc, bfgid, start, end);
        setDataURL(getServiceURL(url, gauge, river));
    }


    protected String getServiceURL(String server, long gauge, String river) {
        String url = URL.encode(GWT.getModuleBaseURL()
            + "dischargeinfoxml"
            + "?server=" + server
            + "&gauge=" + String.valueOf(gauge)
            + "&river=" + river);

        return url;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
