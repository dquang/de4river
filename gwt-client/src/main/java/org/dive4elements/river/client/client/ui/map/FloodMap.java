/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.MapOptions;
import org.gwtopenmaps.openlayers.client.MapWidget;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.Attribution;
import org.gwtopenmaps.openlayers.client.control.ScaleLine;
import org.gwtopenmaps.openlayers.client.control.ScaleLineOptions;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureAddedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.format.GeoJSON;
import org.gwtopenmaps.openlayers.client.layer.Layer;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.util.Attributes;
import org.gwtopenmaps.openlayers.client.util.JObjectArray;
import org.gwtopenmaps.openlayers.client.util.JSObject;


public class FloodMap implements VectorFeatureAddedListener {

    public static final String LAYER_BARRIERS = "vector_layer_barriers";

    public static final String MARK_SELECTED = "mark.selected";

    public static final int SELECTED_STROKE_WIDTH = 2;

    protected MapWidget mapWidget;
    protected Map       map;
    protected Vector    barrierLayer;
    protected String    srid;
    protected Bounds    maxExtent;
    protected ScaleLine scaleLine;

    public FloodMap(String srid, Bounds maxExtent, int width, int height) {
        this.srid      = srid;
        this.maxExtent = maxExtent;
        recreateWidget(width, height);
        getBarrierLayer();
    }


    public void recreateWidget(int width, int height) {
        final MapOptions opts = new MapOptions();
        opts.setControls(new JObjectArray(new JSObject[] {}));
        opts.setNumZoomLevels(16);
        opts.setProjection(getRiverProjection());
        opts.setMaxExtent(maxExtent);
        opts.setUnits("m");
        opts.setMaxResolution(500); // TODO DO THIS ON THE FLY

        mapWidget = new MapWidget(
                Integer.toString(width - 4),
                Integer.toString(height),
                opts);
        map       = mapWidget.getMap();
        map.addControl(new Attribution());
    }


    @Override
    public void onFeatureAdded(FeatureAddedEvent evt) {
        final VectorFeature feature = evt.getVectorFeature();

        final Attributes attrs = feature.getAttributes();
        final String     type  = attrs.getAttributeAsString("typ");

        if (type == null || type.length() == 0) {
            return;
        }

        final Style style = getStyle(type);
        if (style != null) {
            feature.setStyle(style);
        }

        // necessary, otherwise the setStyle() has no effect
        getBarrierLayer().redraw();
    }


    /**
     * Returns an OpenLayers.Style based on a given type.
     *
     * @param type Type can be one of "pipe1", "pipe2", "ditch", "dam",
     * "ringdike".
     *
     * @return an OpenLayers.Style object.
     */
    public static Style getStyle(String type) {
        final Style style = new Style();

        if (type == null) {
            return null;
        }

        if (type.equals(DrawControl.BARRIER_PIPE1)
            || type.equals(DrawControl.BARRIER_PIPE1_VALUE)
        ) {
            style.setFillColor("#800080");
            style.setStrokeColor("#800080");
        }
        else if (type.equals(DrawControl.BARRIER_PIPE2)
              || type.equals(DrawControl.BARRIER_PIPE2_VALUE)
        ) {
            style.setFillColor("#808080");
            style.setStrokeColor("#808080");
        }
        else if (type.equals(DrawControl.BARRIER_DAM)
              || type.equals(DrawControl.BARRIER_DAM_VALUE)
        ) {
            style.setFillColor("#008000");
            style.setStrokeColor("#008000");
        }
        else if (type.equals(DrawControl.BARRIER_DITCH)
              || type.equals(DrawControl.BARRIER_DITCH_VALUE)
        ) {
            style.setFillColor("#800000");
            style.setStrokeColor("#800000");
        }
        else if (type.equals(DrawControl.BARRIER_RINGDIKE)
              || type.equals(DrawControl.BARRIER_RINGDIKE_VALUE)
        ) {
            style.setFill(false);
            style.setStrokeColor("#FF8000");
        }

        return style;
    }


    public MapWidget getMapWidget() {
        return mapWidget;
    }


    public Map getMap() {
        return map;
    }


    public String getRiverProjection() {
        return "EPSG:" + srid;
    }


    public Bounds getMaxExtent() {
        return maxExtent;
    }


    public Vector getBarrierLayer() {
        if (barrierLayer == null) {
            final VectorOptions opts = new VectorOptions();
            opts.setProjection(getRiverProjection());
            opts.setMaxExtent(getMaxExtent());

            barrierLayer = new Vector(LAYER_BARRIERS, opts);
            barrierLayer.setIsBaseLayer(true);

            map.addLayer(barrierLayer);
            map.setLayerZIndex(barrierLayer, 1000);

            barrierLayer.addVectorFeatureAddedListener(this);
        }

        return barrierLayer;
    }


    public String getFeaturesAsGeoJSON() {
        // disable features before exporting to GeoJSON
        disableFeatures();

        final VectorFeature[] features = barrierLayer.getFeatures();

        if (features == null || features.length == 0) {
            return null;
        }

        return new GeoJSON().write(features);
    }


    public void setSize(String width, String height) {
        mapWidget.setWidth(width);
        mapWidget.setHeight(height);
        final int currentZoom = map.getZoom();
        final LonLat currentCenter = map.getCenter();
        map.updateSize();
        map.zoomTo(currentZoom);
        map.setCenter(currentCenter);
    }


    public void addLayer(Layer layer) {
        if (layer != null) {
            map.addLayer(layer);

            final int index    = map.getLayerIndex(layer);
            final int newIndex = index * (-1) + 1;

            map.raiseLayer(layer, newIndex);

            update();
        }
    }


    public void hideBarrierLayer () {
        if (getBarrierLayer() != null) {
            barrierLayer.setIsVisible(false);
        }
    }

    public void showBarrierLayer () {
        if (getBarrierLayer() != null) {
            barrierLayer.setIsVisible(true);
        }
    }


    public void selectFeature(VectorFeature feature) {
        if (feature != null) {
            selectFeatures(new VectorFeature[] { feature } );
        }
    }


    public void selectFeatures(VectorFeature[] features) {
        if (features == null || features.length == 0) {
            return;
        }

        for (final VectorFeature feature: features) {
            final Attributes attr = feature.getAttributes();

            if (attr.getAttributeAsInt(MARK_SELECTED) == 1) {
                continue;
            }

            attr.setAttribute(MARK_SELECTED, 1);

            final Style style        = feature.getStyle();
            final double strokeWidth = style.getStrokeWidth();

            style.setStrokeWidth(strokeWidth+SELECTED_STROKE_WIDTH);
        }

        getBarrierLayer().redraw();
    }


    public void disableFeatures() {
        final Vector          barriers = getBarrierLayer();
        final VectorFeature[] features = barriers.getFeatures();

        if (features == null || features.length == 0) {
            return;
        }

        disableFeatures(features);
    }


    public void disableFeature(VectorFeature feature) {
        if (feature != null) {
            disableFeatures(new VectorFeature[] { feature });
        }
    }


    public void disableFeatures(VectorFeature[] features) {
        if (features == null || features.length == 0) {
            return;
        }

        for (final VectorFeature feature: features) {
            final Attributes attr = feature.getAttributes();

            if (attr.getAttributeAsInt(MARK_SELECTED) == 0) {
                continue;
            }

            attr.setAttribute(FloodMap.MARK_SELECTED, 0);

            final Style style        = feature.getStyle();
            final double strokeWidth = style.getStrokeWidth();

            style.setStrokeWidth(strokeWidth-SELECTED_STROKE_WIDTH);
        }

        getBarrierLayer().redraw();
    }


    public void update() {
        final Layer[] layers = map.getLayers();

        for (final Layer l: layers) {
            l.redraw();
        }
    }


    public void updateSize() {
        this.map.updateSize();
    }


    public void activateScaleLine(boolean activate) {
        if (activate) {
            final ScaleLineOptions slOpts = new ScaleLineOptions();
            slOpts.setBottomInUnits("m");
            slOpts.setBottomOutUnits("km");
            slOpts.setTopInUnits("");
            slOpts.setTopOutUnits("");

            scaleLine = new ScaleLine(slOpts);
            this.map.addControl(scaleLine);
        }
        else if (!activate && scaleLine != null){
            this.map.removeControl(scaleLine);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
