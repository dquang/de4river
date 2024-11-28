/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * This FutureTask overrides the <i>cancel()</i> method. Before super.cancel()
 * is called, WSPLGENCallable.cancelWSPLGEN() is executed to kill a running
 * WSPLGEN process.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WSPLGENFuture extends FutureTask {

    private static final Logger log = LogManager.getLogger(WSPLGENFuture.class);

    protected WSPLGENCallable wsplgenCallable;


    public WSPLGENFuture(WSPLGENCallable callable) {
        super(callable);
        this.wsplgenCallable = callable;
    }


    public WSPLGENCallable getWSPLGENCallable() {
        return wsplgenCallable;
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        log.debug("WSPLGENFuture.cancel");

        wsplgenCallable.cancelWSPLGEN();
        return super.cancel(mayInterruptIfRunning);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
