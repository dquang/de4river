/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.geom.Lines;
import org.dive4elements.river.model.FastCrossSectionLine;


/**
 * Interface, Artifact can create WaterLines (Water against Cross-Profile).
 */
public interface WaterLineArtifact {

    /** Get points that define a line of a (water)facet against a cross-
     * section. */
    public Lines.LineData getWaterLines(
        int                  facetIdx,
        FastCrossSectionLine      csl,
        double                      d,
        double                      w,
        CallContext           context);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
