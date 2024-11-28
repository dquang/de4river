/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.util.Map;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;


public interface XYMetaDataset
{

    /**
     * The meta data for this data set.
     */
    Map<String, String> getMetaData();

    /**
     * Add meta data for this data set.
     */
    void putMetaData(
        Map<String, String> metaData,
        Artifact artifact,
        CallContext context);
}
