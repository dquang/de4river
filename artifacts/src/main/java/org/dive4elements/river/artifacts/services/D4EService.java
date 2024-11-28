/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.services;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifactdatabase.XMLService;

import org.dive4elements.river.backend.SessionHolder;


public abstract class D4EService extends XMLService {

    private static final Logger log = LogManager.getLogger(D4EService.class);


    @Override
    public Document processXML(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta
    ) {
        init();

        try {
            return doProcess(data, globalContext, callMeta);
        }
        finally {
            shutdown();
        }
    }


    /** Override to do the meat work (called in processXML). */
    protected abstract Document doProcess(
        Document      data,
        GlobalContext globalContext,
        CallMeta      callMeta);


    protected void init() {
        log.debug("init");
        SessionHolder.acquire();
    }


    /** Called when processing done, close session. */
    protected void shutdown() {
        log.debug("shutdown");
        SessionHolder.release();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
