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

import java.util.Map;

/**
 * Service that provides server configuration values relevant to the client.
 *
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 *
 */
@RemoteServiceRelativePath("server-info")
public interface ServerInfoService extends RemoteService {

    Map<String, String> getConfig(String locale);
}
