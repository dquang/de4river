/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ParentMovedEvent;
import com.smartgwt.client.widgets.events.ParentMovedHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.events.VisibilityChangedEvent;
import com.smartgwt.client.widgets.events.VisibilityChangedHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.MapInfoService;
import org.dive4elements.river.client.client.services.MapInfoServiceAsync;
import org.dive4elements.river.client.client.ui.map.FloodMap;
import org.dive4elements.river.client.client.ui.map.MapPanel;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.MapInfo;

import java.util.List;

import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.control.Attribution;
import org.gwtopenmaps.openlayers.client.layer.TransitionEffect;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;


public class DigitizePanel
extends SelectProvider
implements TabSelectedHandler, VisibilityChangedHandler {

    private static final long serialVersionUID = 3529775660871273314L;

    protected MapInfoServiceAsync mapInfo = GWT.create(MapInfoService.class);

    protected FloodMap floodMap;

    protected MapPanel mapPanel;

    public static final String UESK_BARRIERS = "uesk.barriers";

    /** The message class that provides i18n strings. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public DigitizePanel() {
    }


    @Override
    public Canvas create(DataList list) {
        helperContainer.addVisibilityChangedHandler(this);

        DataList clone = (DataList) list.clone();
        List<Data> all = clone.getAll();
        all.remove(UESK_BARRIERS);

        Canvas widget = createWidget(list);

        final Config cfg    = Config.getInstance();
        final String locale = cfg.getLocale();

        String river = getDataValue("state.winfo.river", "river");
        mapInfo.getMapInfo(locale, river, new AsyncCallback<MapInfo>() {
            @Override
            public void onFailure(Throwable caught) {
                String msg = caught.getMessage();

                GWT.log("Error while fetching MapInfo: " + msg);
                SC.warn(FLYS.getExceptionString(MSG, caught));
            }

            @Override
            public void onSuccess(MapInfo info) {
                createMapWidget(info);
            }
        });

        return widget;
    }


    /**
     * This method creates the content of the widget.
     *
     * @param data The {@link DataList} object.
     *
     * @return a combobox.
     */
    @Override
    protected Canvas createWidget(DataList data) {
        GWT.log("DigitizePanel - createWidget()");

        VLayout layout = new VLayout();
        layout.setAlign(VerticalAlignment.TOP);
        layout.setHeight(25);

        int size = data.size();

        for (int i = 0; i < size; i++) {
            Data d = data.get(i);

            Label label = new Label(d.getDescription());
            label.setValign(VerticalAlignment.TOP);
            label.setHeight(20);
            label.setWidth(400);

            layout.addMember(label);
            layout.addMember(getNextButton());
        }

        layout.setAlign(VerticalAlignment.TOP);

        return layout;
    }


    @Override
    protected Data[] getData() {
        final Data[] total = new Data[1];

        if (floodMap != null) {
            DataItem item = new DefaultDataItem(
                UESK_BARRIERS, UESK_BARRIERS, floodMap.getFeaturesAsGeoJSON());
            total[0] = new DefaultData(
                UESK_BARRIERS, null, null, new DataItem[] { item });
        }
        else {
            // Happens when OpenLayers is missing
            GWT.log("floodMap is null -> OpenLayers missing?");
        }

        return total;
    }


    public void createMapWidget(MapInfo mapInfo) {
        mapPanel = new MapPanel(mapInfo, true);

        floodMap = mapPanel.getFloodMap();
        Map map  = floodMap.getMap();

        helperContainer.addResizedHandler(new ResizedHandler() {
            @Override
            public void onResized(ResizedEvent event) {
                mapPanel.doLayout(
                    helperContainer.getWidth(), helperContainer.getHeight());
            }
        });
        helperContainer.addParentMovedHandler(new ParentMovedHandler() {
            @Override
            public void onParentMoved(ParentMovedEvent event) {
                mapPanel.getFloodMap().updateSize();
            }
        });
        helperContainer.addVisibilityChangedHandler(
            new VisibilityChangedHandler() {
            @Override
            public void onVisibilityChanged(VisibilityChangedEvent event) {
                mapPanel.doLayout(
                    helperContainer.getWidth(), helperContainer.getHeight());
            }
        });
        helperContainer.addMember(mapPanel);

        parameterList.registerCollectionViewTabHandler(this);

        WMS axis = getLayer(
            mapInfo.getWmsUrl(), mapInfo.getWmsLayers(),
            mapInfo.getProjection(), false, true);
        WMS back = getLayer(
            mapInfo.getBackgroundWmsUrl(), mapInfo.getBackgroundWmsLayers(),
            mapInfo.getProjection(), false, false);

        map.addLayer(back);
        map.addLayer(axis);

        String hws = getDataValue("state.winfo.uesk.dc-hws", "uesk.hws");
        if (hws != null && hws.length() > 0) {
            WMS hwsLayer = getLayer(
            //TODO: Use Mapinfo to get hws layer infos.
                mapInfo.getWmsUrl().replace("river", "user"),
                "ms_layer-hws-lines" + artifact.getUuid(),
                mapInfo.getProjection(),
                false, true);
            map.addLayer(hwsLayer);
        }
        String userRgd = getDataValue(
            "state.winfo.uesk.user-rgd", "uesk.user-rgd");
        if (userRgd != null && userRgd.length() > 0) {
            WMS userLayer = getLayer(
            //TODO: Use Mapinfo to get hws layer infos.
                mapInfo.getWmsUrl().replace("river", "user"),
                "ms_layer-user-rgd" + artifact.getUuid(),
                mapInfo.getProjection(),
                false, true);
            map.addLayer(userLayer);
        }
        map.addControl(new Attribution());
        map.zoomToMaxExtent();

        mapPanel.doLayout(
            helperContainer.getWidth(), helperContainer.getHeight());
    }


    protected WMS getLayer(
        String url,
        String layers,
        String proj,
        boolean isBaseLayer,
        boolean singleTiled
    ) {
        final WMSParams params = new WMSParams();
        params.setLayers(layers);
        params.setFormat("image/png");
        params.setIsTransparent(!isBaseLayer);

        final WMSOptions opts = new WMSOptions();
        opts.setProjection(proj);
        opts.setSingleTile(false); // FIXME: Make working...
        opts.setTransitionEffect(TransitionEffect.RESIZE);
        opts.setRatio(1);
        opts.setBuffer(0);
        if (layers.equals("OSM-WMS-Dienst")) {
            opts.setAttribution(MSG.attribution());
        }
        final WMS wms = new WMS(layers, url, params, opts);
        wms.setIsVisible(true);
        wms.setIsBaseLayer(isBaseLayer);

        return wms;
    }


    @Override
    public void onTabSelected(TabSelectedEvent tse) {
        if (tse.getTabNum () != 0) {
            floodMap.hideBarrierLayer();
        }
        else {
            floodMap.showBarrierLayer();
        }
    }

    @Override
    public void onVisibilityChanged(VisibilityChangedEvent vce) {
        if (!vce.getIsVisible()) {
            floodMap.hideBarrierLayer();
            mapPanel.getMapToolbar().activateDrawFeature(false);
        }
        else {
            floodMap.showBarrierLayer();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
