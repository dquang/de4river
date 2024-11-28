/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.fixings;

import java.util.IdentityHashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import gnu.trove.TIntIntHashMap;

public class RankRemapper {

    private static Logger log = LogManager.getLogger(RankRemapper.class);

    private TIntIntHashMap                index2rank;
    private IdentityHashMap<QWI, Boolean> visited;

    public RankRemapper() {
        index2rank = new TIntIntHashMap();
        visited    = new IdentityHashMap<QWI, Boolean>();
    }

    public void toMap(int index) {
        index2rank.put(index, index2rank.size());
    }

    public <I extends QWI> void remap(I qwi) {
        if (!visited.containsKey(qwi)) {
            int idx = qwi.index;
            if (index2rank.containsKey(idx)) {
                qwi.index = index2rank.get(idx);
            } else if (log.isDebugEnabled()) {
                log.debug("Cannot remap " + idx);
            }
            visited.put(qwi, true);
        }
    }
}
