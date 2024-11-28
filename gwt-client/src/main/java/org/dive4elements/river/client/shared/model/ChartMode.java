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
import org.dive4elements.river.client.client.ui.chart.ChartOutputTab;
import org.dive4elements.river.client.client.ui.chart.NaviChartOutputTab;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartMode extends DefaultOutputMode {

    public ChartMode() {
    }


    public ChartMode(String name, String desc, String mimeType) {
        super(name, desc, mimeType);
    }


    public ChartMode(
        String name,
        String descrition,
        String mimeType,
        List<Facet> facets,
        String type)
    {
        super(name, descrition, mimeType, facets);
        this.type = type;
    }


    /** Create output tab. Some outs feel better inside a specialized one. */
    @Override
    public OutputTab createOutputTab(String t, Collection c, CollectionView p) {
        if (this.getName().equals("fix_wq_curve") ||
            this.getName().equals("extreme_wq_curve") ||
            this.getName().equals("fix_deltawt_curve") ||
            this.getName().equals("fix_derivate_curve") ||
            this.getName().equals("fix_vollmer_wq_curve")){
            return new NaviChartOutputTab(t, c, this, p);
        }
        return new ChartOutputTab(t, c, this, p);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
