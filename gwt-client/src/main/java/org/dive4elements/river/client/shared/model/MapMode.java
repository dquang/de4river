/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.OutputTab;
import org.dive4elements.river.client.client.ui.map.MapOutputTab;

import java.util.List;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapMode extends DefaultOutputMode {

    /**
     * Default constructor required for serialization.
     */
    public MapMode() {
    }

    public MapMode(String name, String desc, String mimeType) {
        super(name, desc, mimeType);
    }


    public MapMode(
        String name,
        String descrition,
        String mimeType,
        List<Facet> facets)
    {
        super(name, descrition, mimeType, facets);
    }


    @Override
    public OutputTab createOutputTab(String t, Collection c, CollectionView p) {
        return new MapOutputTab(t, c, this, p);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
