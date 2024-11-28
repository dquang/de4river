/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dive4elements.river.artifacts.D4EArtifact;


public class MapAccess
extends RangeAccess
{

    public MapAccess(D4EArtifact artifact) {
        super(artifact);
    }

    public List<String> getHWS() {
        String param = getString("uesk.hws");
        if (param != null) {
            String[] split = param.split(";");
            return new ArrayList<String>(Arrays.asList(split));
        }
        return new ArrayList<String>();
    }
}
