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


public class DistanceInfoDataSource extends DataSource {

    public static final String XPATH_DISTANCE_DEFAULT = "/distances/distance";


    public DistanceInfoDataSource(String url, String river, String filter) {
        setDataFormat(DSDataFormat.XML);
        setRecordXPath(XPATH_DISTANCE_DEFAULT);

        DataSourceField desc = new DataSourceField(
            "description", FieldType.TEXT, "description");

        DataSourceField from = new DataSourceField(
            "from", FieldType.TEXT, "from");

        DataSourceField to = new DataSourceField(
            "to", FieldType.TEXT, "to");

        DataSourceField side = new DataSourceField(
            "riverside", FieldType.TEXT, "riverside");

        DataSourceField top = new DataSourceField(
            "top", FieldType.TEXT, "top");

        DataSourceField bottom = new DataSourceField(
            "bottom", FieldType.TEXT, "bottom");

        setFields(desc, from, to, side, top, bottom);
        setDataURL(getServiceURL(url, river, filter));
    }


    protected String getServiceURL(
        String server,
        String river,
        String filter
    ) {
        String url = URL.encode(GWT.getModuleBaseURL()
            + "distanceinfoxml"
            + "?server=" + server
            + "&river=" + river
            + "&filter=" + filter);

        return url;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
