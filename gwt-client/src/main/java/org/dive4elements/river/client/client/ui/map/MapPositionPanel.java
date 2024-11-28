/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;

import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Pixel;
import org.gwtopenmaps.openlayers.client.event.EventHandler;
import org.gwtopenmaps.openlayers.client.event.EventObject;
import org.gwtopenmaps.openlayers.client.util.JSObject;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapPositionPanel extends HLayout {

    protected MapWidget mapWidget;
    protected final Map map;

    protected Label  x;
    protected Label  y;


    public MapPositionPanel(MapWidget mapWidget) {
        this.mapWidget = mapWidget;
        this.map       = mapWidget.getMap();

        this.x  = new Label();
        this.y  = new Label();
        Label d = new Label("|");

        setAlign(Alignment.RIGHT);
        setMembersMargin(2);

        setWidth(150);
        x.setWidth(25);
        y.setWidth(25);
        d.setWidth(5);

        addMember(x);
        addMember(d);
        addMember(y);

        // TODO This is not an optimal way to get the mouse position but makes
        // the wrapper canvas superfluous.
       this.map.getEvents().register("mousemove", map, new EventHandler() {

            @Override
            public void onHandle(EventObject eventObject) {
                JSObject xy = eventObject.getJSObject().getProperty("xy");
                Pixel px = Pixel.narrowToPixel(xy);
                LonLat lonlat = map.getLonLatFromPixel(px);

                setX(lonlat.lon());
                setY(lonlat.lat());
            }
        });
    }


    protected void setX(double x) {
        NumberFormat f = NumberFormat.getFormat("#0.0000");
        this.x.setContents(f.format(x).toString());
    }


    protected void setY(double y) {
        NumberFormat f = NumberFormat.getFormat("#0.0000");
        this.y.setContents(f.format(y).toString());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
