/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.exports.DiagramGenerator;

/**
 * A processor is intended to generate an output e.g. curve in a
 * chart diagramm from
 * arbitrary data input which can be reused in several generators.
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public interface Processor {
    /**
     * Set the axis for this processor.
     * This should be done before doOut is called for the first time.
     *
     * @param axisName The name of the Axis this processor should use.
     */
    public void setAxisName(String axisName);

    /**
     * Get the axis for this processor.
     *
     * @return The name of the axis that is used.
     */
    public String getAxisName();

    /**
     * Get the axis label for this processor.
     *
     * @return The label of the axis.
     */
    public String getAxisLabel(DiagramGenerator generator);

    /**
     * Processes data to generate e.g. a chart.
     * Called for generators configured in the new-style way.
     * In contrast to other doOut, no axis is given, as its name
     * is in the given configuration.
     *
     * @param generator DiagramGenerator to add output on.
     * @param bundle    The artifact and facet
     * @param theme     The theme that contains styling information.
     * @param visible   The visibility of the curve.
     */
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible);

    /**
     * Processes data to generate e.g. a chart.
     * Called for 'unconconfigured' (old-style) generators.
     *
     * @param generator XYChartGenerator to add output on.
     * @param bundle    The artifact and facet.
     * @param theme     The theme that contains styling information.
     * @param visible   The visibility of the curve.
     * @param index     The index of the axis.
     */
    @Deprecated
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible,
            int              index);

    /**
     * Returns true if the Processor class is able to generate
     * output for a facet type
     *
     * @param facettype Name of the facet type
     * @return true if the facettype can be processed
     */
    public boolean canHandle(String facettype);
}
