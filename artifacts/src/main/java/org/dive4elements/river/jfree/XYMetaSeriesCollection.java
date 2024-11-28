/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.jfree;

import java.util.Map;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class XYMetaSeriesCollection
extends XYSeriesCollection
{

    protected Map<String, String> metaData;

    public XYMetaSeriesCollection(XYSeries series) {
        super(series);
        if (series instanceof XYMetaDataset) {
            this.metaData = ((XYMetaDataset) series).getMetaData();
        }
        else {
            metaData = null;
        }
    }

    public Map<String, String> getMetaData() {
        return this.metaData;
    }
}
