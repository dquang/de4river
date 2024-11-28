/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dive4elements.river.client.shared.exceptions.ServerException;

import org.dive4elements.river.client.shared.model.FeatureInfoResponse;
import org.dive4elements.river.client.shared.model.Theme;

@RemoteServiceRelativePath("getfeatureinfo")
public interface GFIService extends RemoteService {

    public FeatureInfoResponse query(
        Theme       theme,
        String      format,
        String      bbox,
        String      projection,
        int         height,
        int         width,
        int         x,
        int         y
    ) throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
