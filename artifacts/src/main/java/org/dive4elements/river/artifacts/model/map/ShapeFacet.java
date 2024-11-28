/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import java.io.File;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.utils.RiverUtils;


public class ShapeFacet
extends DefaultFacet
{
    private static Logger log = LogManager.getLogger(ShapeFacet.class);

    /**
     * Defaults to ADVANCE Compute type.
     * @param name Name of the facet.
     * @param description maybe localized description of the facet.
     */
    public ShapeFacet(String name, String description) {
        super(name, description);
    }

    /**
     * Return computation result.
     */
    @Override
    public Object getData(Artifact artifact, CallContext context) {
        D4EArtifact flys = (D4EArtifact)artifact;
        String baseDir = RiverUtils.getXPathString(
            RiverUtils.XPATH_MAPFILES_PATH);
        baseDir += "/" + flys.identifier();
        File shapeDir = new File(baseDir);
        if (shapeDir.exists()) {
            return shapeDir;
        }
        return null;
    }
}
