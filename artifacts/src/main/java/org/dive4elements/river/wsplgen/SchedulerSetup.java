/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.wsplgen;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.GlobalContext;

import org.dive4elements.artifactdatabase.LifetimeListener;

import org.dive4elements.river.artifacts.context.RiverContext;


/**
 * A LifetimeListener that is used to create an instance of Scheduler. This
 * instance is put into the GlobalContext using RiverContext.SCHEDULER.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class SchedulerSetup implements LifetimeListener {

    @Override
    public void setup(Document document) {
    }


    @Override
    public void systemUp(GlobalContext globalContext) {
        Scheduler scheduler = Scheduler.getInstance();
        globalContext.put(RiverContext.SCHEDULER, scheduler);
    }


    @Override
    public void systemDown(GlobalContext globalContext) {
        // TODO IMPLEMENT ME!
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
