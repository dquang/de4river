/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.states.DefaultState;


public class WMSBackgroundArtifact extends StaticD4EArtifact {

    public static final String NAME = "wmsbackground";

    private static final Logger log =
        LogManager.getLogger(WMSBackgroundArtifact.class);


    @Override
    public String getName() {
        return NAME;
    }


    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta
    ) {
        log.debug("Initialize internal state with: "+ artifact.identifier());

        D4EArtifact flys = (D4EArtifact) artifact;
        addData("river", flys.getData("river"));

        List<Facet> fs = new ArrayList<Facet>();

        // TODO Add CallMeta
        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);

        if (!fs.isEmpty()) {
            addFacets(getCurrentStateId(), fs);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
