/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ParentMovedEvent;
import com.smartgwt.client.widgets.events.ParentMovedHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.OutputParameterChangeEvent;
import org.dive4elements.river.client.client.event.OutputParameterChangeHandler;
import org.dive4elements.river.client.client.event.RedrawRequestEvent;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.services.LoadArtifactService;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.client.services.MapOutputService;
import org.dive4elements.river.client.client.services.MapOutputServiceAsync;
import org.dive4elements.river.client.client.services.StepForwardService;
import org.dive4elements.river.client.client.services.StepForwardServiceAsync;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.OutputTab;
import org.dive4elements.river.client.client.ui.ThemePanel;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.ArtifactDescription;
import org.dive4elements.river.client.shared.model.AttributedTheme;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.MapConfig;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Theme;
import org.dive4elements.river.client.shared.model.ThemeList;
import org.dive4elements.river.client.shared.model.WMSLayer;

import java.util.HashMap;
import java.util.List;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureAddedListener;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureRemovedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.GeoJSON;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.TransitionEffect;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.WMS;
import org.gwtopenmaps.openlayers.client.layer.WMSOptions;
import org.gwtopenmaps.openlayers.client.layer.WMSParams;


public class MapOutputTab
extends      OutputTab
implements   RedrawRequestHandler, ExternalWMSWindow.LayerLoader,
             TabSelectedHandler, OutputParameterChangeHandler {

    public static final String DEFAULT_SRID = "4326";

    public static final String BARRIERS_PARAMETER_KEY = "uesk.barriers";

    public static final String WSPLGEN_FACET = "floodmap.wsplgen";

    public static final String EXTERNAL_WMS_FACTORY = "externalwmsfactory";


    protected StepForwardServiceAsync feedService =
        GWT.create(StepForwardService.class);

    protected MapOutputServiceAsync mapService =
        GWT.create(MapOutputService.class);

    /** Service handle to clone and add artifacts to collection. */
    protected LoadArtifactServiceAsync loadArtifactService =
        GWT.create(LoadArtifactService.class);

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected MapToolbar controlPanel;
    protected ThemePanel themePanel;
    protected Canvas     themePanelCanvas;
    protected MapWidget     mapPanel;
    protected Canvas mapPanelCanvas;
    protected VLayout rootLayout = new VLayout();
    protected AbsolutePanel absPan = new AbsolutePanel();
    protected FloodMap floodMap;
    protected java.util.Map<String, String> wmsUrls =
        new HashMap<String, String>();


    public MapOutputTab(
        String         title,
        Collection     collection,
        OutputMode     mode,
        CollectionView collectionView
    ){
        super(title, collection, collectionView, mode);

        collectionView.registerTabHandler(this);

        mapService.doOut(collection, new AsyncCallback<MapConfig>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("MAP ERROR: " + caught.getMessage());
                }

                @Override
                public void onSuccess(MapConfig c) {
                    GWT.log("MAP SUCCESS!");

                    Bounds max     = boundsFromString(c.getMaxExtent());
                    Bounds initial = boundsFromString(c.getInitialExtent());

                    if (initial == null) {
                        GWT.log("Warning: No initial extent set.");
                        initial = max;
                    }

                    setFloodmap(new FloodMap(c.getSrid(), max, 640, 480));

                    initLayout();
                    initBarriers();

                    GWT.log("MAX EXTENT: " + max);
                    GWT.log("ZOOM TO: " + initial);
                    getMap().zoomToExtent(initial);
                }
            }
        );
    }


    protected void initLayout() {
        rootLayout.setHeight100();
        rootLayout.setWidth100();
        rootLayout.setMembersMargin(2);

        HLayout hlayout = new HLayout();
        hlayout.setMembersMargin(0);

        this.themePanelCanvas = createThemePanel();

        controlPanel = createControlPanel();
        mapPanel = floodMap.getMapWidget();

        rootLayout.addMember(controlPanel);
        rootLayout.addMember(absPan);
        absPan.setWidth("100%");
        absPan.setHeight("100%");
        absPan.add(themePanelCanvas);
        absPan.add(mapPanel);

        rootLayout.addResizedHandler(new ResizedHandler() {
            @Override
            public void onResized(ResizedEvent e) {
                doLayout();
            }
        });

        rootLayout.addParentMovedHandler(new ParentMovedHandler() {
            @Override
            public void onParentMoved(ParentMovedEvent event) {
                mapPanel.getMap().updateSize();
            }
        });

        setPane(rootLayout);
    }


    protected void doLayout() {
        if(!rootLayout.isVisible()) {
            return;
        }

        // Manually set the height of the AbsolutePanel,
        // somehow this is necessary
        absPan.setHeight(String.valueOf(
                rootLayout.getHeight() - controlPanel.getHeight() - 2) + "px");

        // Calculate bounds of Map
        int height = rootLayout.getHeight() -
                controlPanel.getHeight() - 6;
        int width  = controlPanel.getWidth() -
            (themePanelCanvas.isVisible()
                ? themePanelCanvas.getWidth() + 4
                : 2);

        // Set size and position of Map
        String w = String.valueOf(width) + "px";
        String h = String.valueOf(height) + "px";
        GWT.log("width=" + w);

        mapPanel.setSize(w, h);
        mapPanel.getMap().updateSize();
        if(themePanelCanvas.isVisible()) {
            absPan.setWidgetPosition(
                mapPanel, themePanelCanvas.getWidth() + 2, 0);
        }
        else {
            absPan.setWidgetPosition(mapPanel, 0, 0);
        }

        // Set bounds of ThemePanelCanvas
        themePanelCanvas.setSize(
            themePanelCanvas.getWidthAsString(),
            String.valueOf(height + 2) + "px");
    }


    protected void initBarriers() {
        Vector vector = floodMap.getBarrierLayer();
        vector.addVectorFeatureAddedListener(
            new VectorFeatureAddedListener() {
                @Override
                public void onFeatureAdded(FeatureAddedEvent e) {
                    saveBarriers();
                }
            }
        );

        vector.addVectorFeatureRemovedListener(
            new VectorFeatureRemovedListener() {
                @Override
                public void onFeatureRemoved(FeatureRemovedEvent e) {
                    saveBarriers();
                }
            }
        );


        Artifact artifact = getArtifact();

        if (artifact == null) {
            return;
        }

        ArtifactDescription desc = artifact.getArtifactDescription();

        String geojson = getGeoJSONFromStatic(desc);
        geojson = geojson != null ? geojson : getGeoJSONFromDynamic(desc);

        if (geojson == null || geojson.length() == 0) {
            GWT.log("No geojson string found -> no barriers existing.");
            return;
        }

        GeoJSON reader = new GeoJSON();
        VectorFeature[] features = reader.read(geojson);

        vector.addFeatures(features);
    }


    public void addLayer(Layer layer) {
        FloodMap map = getFloodmap();

        if (map != null) {
            GWT.log("Add new layer '" + layer.getName() + "' to map.");
            map.addLayer(layer);
            if (layer instanceof WMS) {
                wmsUrls.put(layer.getName(),
                    ((WMS)layer).getFullRequestString(new WMSParams(), null));
            }
        }
    }


    public void removeLayer(String name) {
        Map map = getMap();

        Layer[] layers = map.getLayers();

        for (Layer layer: layers) {
            if (name.equals(layer.getName())) {
                map.removeLayer(layer);
            }
        }
    }


    @Override
    public void onRedrawRequest(RedrawRequestEvent event) {
        mapService.doOut(collection, new AsyncCallback<MapConfig>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("MAP ERROR: " + caught.getMessage());
            }

            @Override
            public void onSuccess(MapConfig c) {
                GWT.log("We want to refresh the map now!");
                themePanel.updateCollection();
                getFloodmap().update();
            }
        });
    }


    @Override
    public void load(List<WMSLayer> toLoad) {
        GWT.log("The user wants to add " + toLoad.size() + " new WMS layers.");

        int len = toLoad.size();

        Recommendation[] recom = new Recommendation[len];

        for (int i = 0; i < len; i++) {
            WMSLayer w = toLoad.get(i);

            String ids = w.getServer() + ";" + w.getName() + ";" + w.getTitle();
            recom[i] = new Recommendation(EXTERNAL_WMS_FACTORY, ids);
        }

        Collection c = getCollection();

        Config config = Config.getInstance();
        String locale = config.getLocale();

        loadArtifactService.loadMany(c, recom, EXTERNAL_WMS_FACTORY, locale,
            new AsyncCallback<Artifact[]>() {

                @Override
                public void onFailure(Throwable throwable) {
                    SC.warn(MSG.getString(throwable.getMessage()));
                }

                @Override
                public void onSuccess(Artifact[] newArtifacts) {
                    getThemePanel().updateCollection();
                }
            }
        );
    }


    protected void setFloodmap(FloodMap floodMap) {
        this.floodMap = floodMap;
    }


    protected FloodMap getFloodmap() {
        return floodMap;
    }


    protected Map getMap() {
        return floodMap.getMap();
    }


    protected ThemePanel getThemePanel() {
        return themePanel;
    }


    protected String getGeoJSONFromDynamic(ArtifactDescription desc) {
        DataList list = desc.getCurrentData();

        if (list == null) {
            return null;
        }

        List<Data> datas = list.getAll();
        for (Data data: datas) {
            String key = data.getLabel();

            if (key != null && key.equals(BARRIERS_PARAMETER_KEY)) {
                DataItem def = data.getDefault();

                if (def != null) {
                    return def.getStringValue();
                }
            }
        }

        return null;
    }


    protected String getGeoJSONFromStatic(ArtifactDescription desc) {
        // TODO Implement this method, if there are reachable states right after
        // the floodmap state - which is currently not the case.
        return null;
    }


    public ThemeList getThemeList() {
        return collection.getThemeList(mode.getName());
    }


    public String getSrid() {
        ThemeList themeList = getThemeList();

        int num = themeList.getThemeCount();

        for (int i = 1; i <= num; i++) {
            AttributedTheme theme = (AttributedTheme) themeList.getThemeAt(i);

            if (theme == null) {
                continue;
            }

            String srid = theme.getAttr("srid");

            if (srid != null && srid.length() > 0) {
                return srid;
            }
        }

        return DEFAULT_SRID;
    }


    protected Bounds boundsFromString(String bounds) {
        GWT.log("Create Bounds from String: '" + bounds + "'");
        if (bounds == null || bounds.length() == 0) {
            return null;
        }

        String[] values = bounds.split(" ");

        if (values == null || values.length < 4) {
            return null;
        }

        try {
            return new Bounds(
                Double.valueOf(values[0]),
                Double.valueOf(values[1]),
                Double.valueOf(values[2]),
                Double.valueOf(values[3]));
        }
        catch (NumberFormatException nfe) {}

        return null;
    }


    public Layer createWMSLayer(Theme theme) {
        if (!(theme instanceof AttributedTheme)) {
            return null;
        }

        AttributedTheme at = (AttributedTheme) theme;

        String name      = at.getAttr("name");
        String desc      = at.getAttr("description");
        String url       = at.getAttr("url");
        String layers    = at.getAttr("layers");

        if (url == null || layers == null) {
            return null;
        }

        WMSParams params = new WMSParams();
        params.setLayers(layers);
        params.setFormat("image/png");
        params.setIsTransparent(true);

        WMSOptions opts = new WMSOptions();
        opts.setProjection("EPSG:" + getSrid());
        opts.setSingleTile(true);
        opts.setRatio(1);
        if (layers.equals("OSM-WMS-Dienst")) {
            opts.setAttribution(MSG.attribution());
            opts.setSingleTile(true);
            opts.setTransitionEffect(TransitionEffect.RESIZE);
        }
        WMS wms = new WMS(layers, url, params, opts);
        wms.setIsVisible(at.getActive() == 1);
        wms.setIsBaseLayer(false);
        // We can't set the full_url attribute here because map is not set
        // at.addAttr("full_url", wms.getFullRequestString(params, null));
        return wms;
    }


    public java.util.Map<String, String> wmsUrls() {
        return this.wmsUrls;
    }


    protected MapToolbar createControlPanel() {
        return new MapToolbar(this, floodMap, false);
    }


    protected Canvas createThemePanel() {
        Canvas c = new Canvas();
        c.setMinWidth(300);
        c.setWidth(200);
        c.setHeight100();
        c.setCanDragResize(true);
        c.setBorder("1px solid black");

        themePanel = new MapThemePanel(
            this.getCollectionView(),
            mode,
            this,
            new MapThemePanel.ActivateCallback() {
                @Override
                public void activate(Theme theme, boolean active) {
                    fireActivateTheme(theme, active);
                }
            },
            new MapThemePanel.ThemeMovedCallback() {
                @Override
                public void onThemeMoved(Theme theme, int oldIdx, int newIdx) {
                    // this code synchronizes the ThemePanel and the OpenLayers
                    // internal order of layers.
                    AttributedTheme at = (AttributedTheme) theme;

                    String    name = at.getAttr("layers");
                    Map        map = getMap();
                    Layer[] layers = map.getLayersByName(name);

                    if (layers == null || layers.length == 0) {
                        GWT.log("Error: Cannot find layer '" + name + "'");
                        return;
                    }

                    map.raiseLayer(layers[0], (newIdx-oldIdx)*-1);
                    map.zoomTo(map.getZoom()-1);
                    map.zoomTo(map.getZoom()+1);
                }
            },
            new MapThemePanel.LayerZoomCallback() {
                @Override
                public void onLayerZoom(Theme theme, String extent) {
                    Bounds zoomTo = boundsFromString(extent);

                    if (zoomTo == null) {
                        GWT.log("WARNING: No valid bounds for zooming found!");
                        return;
                    }

                    getMap().zoomToExtent(zoomTo);
                }
            }
        );
        themePanel.addRedrawRequestHandler(this);
        themePanel.addOutputParameterChangeHandler(this);
        c.addChild(themePanel);

        return c;
    }


    private void fireActivateTheme(Theme theme, boolean active) {
        activateTheme(theme, active);
    }


    protected void activateTheme(Theme theme, boolean active) {
        AttributedTheme at = (AttributedTheme) theme;

        String name = at.getAttr("layers");
        Layer layer = floodMap.getMap().getLayerByName(name);

        GWT.log("Set visibility of '" + name + "': " + active);

        if (layer != null) {
            layer.setIsVisible(active);
        }
    }


    protected void saveBarriers() {
        Vector layer = floodMap.getBarrierLayer();

        GeoJSON format   = new GeoJSON();
        String  features = format.write(layer.getFeatures());

        DataItem item = new DefaultDataItem(
            BARRIERS_PARAMETER_KEY, BARRIERS_PARAMETER_KEY, features);

        Data data = new DefaultData(
            BARRIERS_PARAMETER_KEY, BARRIERS_PARAMETER_KEY, "String",
            new DataItem[] {item} );

        Config config       = Config.getInstance();
        String locale = config.getLocale();

        feedService.go(locale, getArtifact(), new Data[] { data },
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not save barrier geometries: " +
                        caught.getMessage());
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully saved barrier geometries.");
                }
            }
        );
    }


    @Override
    public void onTabSelected(TabSelectedEvent tse) {
        if(floodMap == null) {
            return;
        }
        if(this.equals(tse.getTab())) {
            floodMap.activateScaleLine(true);
        }
        else {
            controlPanel.activateMeasureControl(false);
            floodMap.activateScaleLine(false);
        }
    }

    public void toogleThemePanel() {
        this.themePanelCanvas.setVisible(!themePanelCanvas.isVisible());

        // Trigger resize event handler
        doLayout();
    }


    @Override
    public void onOutputParameterChanged(OutputParameterChangeEvent evt) {
        GWT.log("OutputParameterChanged");
        controlPanel.updateThemes(getThemePanel().getThemeList());
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
