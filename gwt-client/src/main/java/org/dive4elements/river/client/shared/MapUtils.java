/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared;

import java.util.Date;


public class MapUtils {

    public static final String GET_LEGEND_GRAPHIC_TEMPLATE =
        "${SERVER}SERVICE=WMS&VERSION=1.1.1&layer=${LAYER}" +
        "&REQUEST=getLegendGraphic&FORMAT=image/png";


    private MapUtils() {
    }

    public static String getLegendGraphicUrl(String server, String layer) {
        return getLegendGraphicUrl(server, layer, -1);
    }

    public static String getLegendGraphicUrl(
        String server,
        String layer,
        int dpi
    ) {
        if (server == null || layer == null) {
            return null;
        }

        if (server.contains("osm.intevation.de")) {
            // GetLegend is not implemented at osm.intevation.de
            // This avoids an error in the print log
            return null;
        }
        server = server.indexOf("?") >= 0 ? server : server + "?";

        String url = GET_LEGEND_GRAPHIC_TEMPLATE;
        url = url.replace("${SERVER}", server);
        url = url.replace("${LAYER}", layer);
        url = url + "&timestamp=" + new Date().getTime();
        if (dpi != -1) {
            url+="&legend_options=dpi:" + dpi;
        }

        return url;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
