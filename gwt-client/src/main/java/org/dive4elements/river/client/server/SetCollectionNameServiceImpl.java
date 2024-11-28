/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.server;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.ClientProtocolUtils;

import org.dive4elements.river.client.shared.exceptions.ServerException;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.client.services.SetCollectionNameService;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SetCollectionNameServiceImpl
extends      DoCollectionAction
implements   SetCollectionNameService
{
    private static final Logger log =
        LogManager.getLogger(SetCollectionNameServiceImpl.class);


    public void setName(Collection c)
    throws ServerException
    {
        log.info("Set name of collection: " + c.identifier());

        String url = getServletContext().getInitParameter("server-url");

        String   name = c.getName();
        Document set  = ClientProtocolUtils.newSetCollectionNameDocument(name);

        doAction(c, set, url);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
