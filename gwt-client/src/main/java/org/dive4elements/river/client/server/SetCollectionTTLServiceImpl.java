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
import org.dive4elements.river.client.client.services.SetCollectionTTLService;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SetCollectionTTLServiceImpl
extends      DoCollectionAction
implements   SetCollectionTTLService
{
    private static final Logger log =
        LogManager.getLogger(SetCollectionTTLServiceImpl.class);


    public static final String XPATH_RESULT      = "/art:result/text()";
    public static final String OPERATION_FAILURE = "FAILED";

    public void setTTL(Collection c)
    throws ServerException
    {
        log.info("Set ttl of collection: " + c.identifier());

        String url = getServletContext().getInitParameter("server-url");

        long   ttl   = c.getTTL();
        String value = null;

        if (ttl == 0) {
            value = "INF";
        }
        else if (ttl < 0) {
            value = "DEFAULT";
        }
        else {
            value = String.valueOf(ttl);
        }

        Document set = ClientProtocolUtils.newSetCollectionTTLDocument(value);

        doAction(c, set, url);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
