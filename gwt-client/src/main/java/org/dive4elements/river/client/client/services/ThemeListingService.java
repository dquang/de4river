/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.services;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Style;

/**
 * This interface provides a method to list themes filtered by name.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund.Renkert</a>
 */
@RemoteServiceRelativePath("themelisting")
public interface ThemeListingService extends RemoteService {

    /**
     * This method returns a list of themes filtered by name.
     *
     * @param locale The locale used for the request.
     *
     * @return a list of themes provided by the artifact server.
     */
    public Map<String, Style> list(String locale, String name)
    throws ServerException;
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
