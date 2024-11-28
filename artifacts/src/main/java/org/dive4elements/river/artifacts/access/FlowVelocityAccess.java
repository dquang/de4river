/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;


/** Access to data that deals with flow velocity stuff. */
public class FlowVelocityAccess
extends      RangeAccess
{

    private int[] mainChannels;
    private int[] totalChannels;

    public FlowVelocityAccess(D4EArtifact artifact) {
        super(artifact);
    }


    public int[] getMainChannels() {
        if (mainChannels == null) {
            mainChannels = getIntArray("main_channel");
        }

        return mainChannels;
    }


    public int[] getTotalChannels() {
        if (totalChannels == null) {
            totalChannels = getIntArray("total_channel");
        }

        return totalChannels;
    }


    public Double getLowerKM() {
        // TODO update callers to getFrom
        return getFrom();
    }


    public Double getUpperKM() {
        // TODO update callers to getTo
        return getTo();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
