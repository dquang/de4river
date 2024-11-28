/* Copyright (C) 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports.process;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.river.exports.XYChartGenerator;
import org.dive4elements.river.exports.DiagramGenerator;
import org.dive4elements.river.themes.ThemeDocument;

/** Dummy implementation for the Processor interface.
 */
public class DefaultProcessor implements Processor {

    protected String axisName;

    public void setAxisName(String axisName) {
        this.axisName = axisName;
    }

    public String getAxisName() {
        return axisName;
    }

    /**
     * Processes data to generate e.g. a chart.
     *
     * @param generator XYChartGenerator to add output on.
     * @param bundle       The artifact and facet
     * @param theme      The theme that contains styling information.
     * @param visible       The visibility of the curve.
     * @param index        The index of the curve
     */
    @Override
    public void doOut(
            XYChartGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible,
            int              index) {
    }

    @Override
    public void doOut(
            DiagramGenerator generator,
            ArtifactAndFacet bundle,
            ThemeDocument    theme,
            boolean          visible) {
    }

    @Override
    public String getAxisLabel(DiagramGenerator generator) {
        return null;
    }


    /**
     * Returns true if Processor class is able to generate
     * output for facet type
     *
     * @param facettype Name of the facet type
     * @return true if the facettype can be processed
     */
    @Override
    public boolean canHandle(String facettype) {
        return false;
    }
}
