/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.map;

import org.dive4elements.artifactdatabase.LifetimeListener;
import org.dive4elements.artifacts.GlobalContext;
import org.dive4elements.river.utils.RiverMapfileGenerator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

/**
 * LifetimeListener that creates a Mapfile containing river axis layers.
 * The listener is called when the server has completed its startup.
 *
 * @author <a href="mailto:christian.lins@intevation.de">Christian Lins</a>
 */
public class RiverMapfileGeneratorStarter implements LifetimeListener {

    private static Logger log = LogManager.getLogger(
        RiverMapfileGeneratorStarter.class);

    @Override
    public void setup(Document document) {
        // Nothing to setup here
    }

    /**
     * Calls RiverMapfileGenerator.generate().
     */
    @Override
    public void systemUp(GlobalContext globalContext) {
        log.debug("systemUp()");

        RiverMapfileGenerator fmfg = new RiverMapfileGenerator();
        fmfg.generate();
    }

    @Override
    public void systemDown(GlobalContext globalContext) {
        // No, we're not cleaning up our generated mapfile
    }

}
