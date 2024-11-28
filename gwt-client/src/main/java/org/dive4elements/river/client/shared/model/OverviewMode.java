/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.OutputTab;
import org.dive4elements.river.client.client.ui.chart.OverviewOutputTab;


/**
 * Output mode for chart overviews.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class OverviewMode
extends
DefaultOutputMode {

    public OverviewMode() {
    }


    public OverviewMode(String name, String desc, String mimeType) {
        super(name, desc, mimeType);
    }


    public OverviewMode(
        String name,
        String descrition,
        String mimeType,
        List<Facet> facets,
        String type)
    {
        super(name, descrition, mimeType, facets);
        this.type = type;
    }


    @Override
    public OutputTab createOutputTab(String t, Collection c, CollectionView p) {
        return new OverviewOutputTab(t, c, this, p);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
