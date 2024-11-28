/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Capabilities;
import org.dive4elements.river.client.client.services.GCService;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class GCServiceImpl
extends      RemoteServiceServlet
implements   GCService
{
    private static Logger log = LogManager.getLogger(GCServiceImpl.class);


    public Capabilities query(String path)
    throws ServerException
    {
        log.info("GCServiceImpl.query");

        return CapabilitiesParser.getCapabilities(path);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
