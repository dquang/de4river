/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbsolutePanel;

import org.dive4elements.river.client.shared.model.BBox;
import org.dive4elements.river.client.shared.model.MapInfo;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.MapWidget;

/**
 * Panel that contains a MapWidget and a MapToolbar.
 * This panel is used by the flood map calculation input helper.
 */
public class MapPanel extends AbsolutePanel {

    protected MapToolbar      toolbar;

    protected FloodMap  floodMap;
    protected MapWidget floodMapWidget;
    protected boolean   digitizeEnabled;

    public MapPanel(MapInfo mapInfo, boolean digitizeEnabled) {
        BBox bbox = mapInfo.getBBox();

        this.digitizeEnabled = digitizeEnabled;
        this.floodMap        = new FloodMap(
            String.valueOf(mapInfo.getSrid()),
            new Bounds(
                bbox.getLowerX(),
                bbox.getLowerY(),
                bbox.getUpperX(),
                bbox.getUpperY()),
                640, 480);

        initLayout();
    }


    private void initLayout() {
        setWidth("100%");
        setHeight("100%");

        floodMapWidget = floodMap.getMapWidget();
        toolbar = new MapToolbar(floodMap, digitizeEnabled);

        add(toolbar);
        add(floodMapWidget);
    }

    public void doLayout(int w, int h) {
        int width = w;
        int height = h;
        GWT.log("MapPanel.size: " + width + "x" + height);

        width -= 2; // minus black borders
        height -= toolbar.getHeight() + 4;

        if (width < 0 || height < 0) {
            GWT.log("MapPanel: Oops what a size!");
            return;
        }

        floodMapWidget.setSize(
            Integer.toString(width), Integer.toString(height));
        floodMapWidget.getMap().updateSize();
    }


    public FloodMap getFloodMap() {
        return floodMap;
    }

    public MapToolbar getMapToolbar () {
        return toolbar;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
