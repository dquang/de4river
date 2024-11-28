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

import org.dive4elements.river.client.shared.model.FixingsOverviewInfo;

@RemoteServiceRelativePath("fixings-overview")
public interface FixingsOverviewService extends RemoteService {

    FixingsOverviewInfo generateOverview(
        String locale,
        String uuid,
        String filter,
        boolean checkbox,
        String callback
    ) throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :