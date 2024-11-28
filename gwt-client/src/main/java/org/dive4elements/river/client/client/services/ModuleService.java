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
import org.dive4elements.river.client.shared.model.Module;

@RemoteServiceRelativePath("modules")
public interface ModuleService extends RemoteService {

    /**
     * Returns the list of available modules of a user
     *
     * @param locale The locale used for the request
     * @return a String array of all available modules
     *
     */
    public Module[] list(String locale) throws ServerException;
}

// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 tw=80 :
