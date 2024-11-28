/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionType;

import com.smartgwt.client.util.SC;

import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.FLYSConstants;

import org.dive4elements.river.client.client.ui.Toolbar;

import org.dive4elements.river.client.client.utils.EnableDisableCmd;

import org.dive4elements.river.client.shared.model.ThemeList;

import org.gwtopenmaps.openlayers.client.Map;

import org.gwtopenmaps.openlayers.client.control.DragPan;
import org.gwtopenmaps.openlayers.client.control.SelectFeature;
import org.gwtopenmaps.openlayers.client.control.SelectFeatureOptions;
import org.gwtopenmaps.openlayers.client.control.ZoomBox;

import org.gwtopenmaps.openlayers.client.event.MapZoomListener;

import org.gwtopenmaps.openlayers.client.feature.VectorFeature;

import org.gwtopenmaps.openlayers.client.layer.Vector;

import org.gwtopenmaps.openlayers.client.util.Attributes;

/**
 * Toolbar for the Map views.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapToolbar
extends      Toolbar
implements   MapZoomListener
{
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected FloodMap       floodMap;
    protected DragPan        pan;
    protected ZoomBox        zoomBox;
    protected SelectFeature  selectFeature;
    protected GetFeatureInfo getFeatureInfo;

    protected Button manageThemesButton;
    protected Button datacageButton;
    protected Button legendButton;

    protected ImgButton addWMSButton;
    protected ImgButton zoomToMaxButton;
    protected ImgButton zoomBoxButton;
    protected ImgButton zoomOutButton;
    protected ImgButton panButton;
    protected ImgButton selectButton;
    protected ImgButton infoButton;
    protected ImgButton removeButton;
    protected ImgButton elevationButton;
    protected ImgButton printMap;

    protected Label epsgLabel;

    protected DrawControl    drawControl;
    protected MeasureControl measureControl;

    protected LegendWindow legendWindow;

    protected Canvas position;


    public MapToolbar(FloodMap floodMap, boolean digitize) {
        this(null, floodMap, digitize);
    }


    public MapToolbar(
        MapOutputTab   mapTab,
        FloodMap       floodMap,
        boolean        digitize)
    {
        super(mapTab);

        setWidth100();
        setHeight(38);
        setMembersMargin(10);
        setPadding(5);
        setBorder("1px solid black");
        this.floodMap = floodMap;

        zoomToMaxButton = createMaxExtentControl();
        zoomBoxButton   = createZoomBoxControl();
        zoomOutButton   = createZoomOutControl();
        panButton       = createPanControl();
        drawControl     = createDrawControl();
        selectButton    = createSelectFeatureControl();
        infoButton      = createGetFeatureInfo();
        measureControl  = createMeasureControl();
        position        = createMousePosition();
        removeButton    = createRemoveFeatureControl();
        elevationButton = createElevationControl();
        epsgLabel       = createEPSGLabel();

        if (mapTab != null) {
            manageThemesButton = createManageThemesControl();
            addMember(manageThemesButton);

            datacageButton = createDatacageControl();
            addMember(datacageButton);

            legendButton = createLegendControl();
            addMember(legendButton);

            addWMSButton = createWMSControl();
            addMember(addWMSButton);

            printMap = createMapPrintControl();
            addMember(printMap);
        }

        addMember(zoomToMaxButton);
        addMember(zoomBoxButton);
        addMember(zoomOutButton);
        addMember(panButton);

        if (digitize) {
            addMember(drawControl);
            addMember(selectButton);
            addMember(removeButton);
            addMember(elevationButton);
        }

        if (infoButton != null) {
            addMember(infoButton);
        }

        addMember(measureControl);
        addMember(createRightPanel());
    }


    protected HLayout createRightPanel() {
        HLayout right = new HLayout();
        right.setAlign(Alignment.RIGHT);

        right.addMember(epsgLabel);
        right.addMember(position);

        return right;
    }


    protected Map getMap() {
        return floodMap.getMap();
    }


    protected void activatePan(boolean activate) {
        if (activate) {
            panButton.select();
            pan.activate();
        }
        else {
            panButton.deselect();
            pan.deactivate();
        }
    }


    protected void activateZoomBox(boolean activate) {
        if (activate) {
            zoomBoxButton.select();
            zoomBox.activate();
        }
        else {
            zoomBoxButton.deselect();
            zoomBox.deactivate();
        }
    }


    public void activateDrawFeature(boolean activate) {
        drawControl.activate(activate);
    }


    protected void activateSelectFeature(boolean activate) {
        if (activate) {
            selectButton.select();
            selectFeature.activate();
        }
        else {
            selectButton.deselect();
            selectFeature.deactivate();
        }
    }


    protected void activateMeasureControl(boolean activate) {
        measureControl.activate(activate);
    }


    protected void activateGetFeatureInfo(boolean activate) {
        if (infoButton == null) {
            return;
        }

        if (activate) {
            infoButton.select();
        }
        else {
            infoButton.deselect();
        }

        getFeatureInfo.activate(activate);
    }


    protected ImgButton createButton(String img, ClickHandler handler) {
        ImgButton btn = new ImgButton();

        String baseUrl = GWT.getHostPageBaseURL();
        btn.setSrc(baseUrl + img);
        btn.setWidth(20);
        btn.setHeight(20);
        btn.setShowDown(false);
        btn.setShowRollOver(false);
        btn.setShowRollOverIcon(false);
        btn.setShowDisabled(false);
        btn.setShowDisabledIcon(true);
        btn.setShowDownIcon(false);
        btn.setShowFocusedIcon(false);

        if (handler != null) {
            btn.addClickHandler(handler);
        }

        return btn;
    }


    protected ImgButton createToggleButton(
        String img,
        final EnableDisableCmd cmd
    ) {
        final ImgButton btn = new ImgButton();

        String baseUrl = GWT.getHostPageBaseURL();
        btn.setSrc(baseUrl + img);
        btn.setActionType(SelectionType.CHECKBOX);
        btn.setSize(20);
        btn.setShowRollOver(false);
        btn.setShowRollOverIcon(false);
        btn.setSelected(false);
        btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                if (btn.isSelected()) {
                    cmd.enable();
                }
                else {
                    cmd.disable();
                }
            }
        });

        return btn;
    }


    protected ImgButton createMaxExtentControl() {
        ImgButton zoomToMax = createButton(MSG.zoom_all(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                floodMap.getMap().zoomToMaxExtent();
            }
        });

        zoomToMax.setTooltip(MSG.zoomMaxExtent());

        return zoomToMax;
    }


    protected ImgButton createZoomBoxControl() {
        zoomBox = new ZoomBox();

        EnableDisableCmd cmd = new EnableDisableCmd() {
            @Override
            public void enable() {
                activatePan(false);
                activateDrawFeature(false);
                activateSelectFeature(false);
                activateMeasureControl(false);
                activateGetFeatureInfo(false);
                activateZoomBox(true);
            }

            @Override
            public void disable() {
                activateZoomBox(false);
            }
        };

        ImgButton button = createToggleButton(MSG.zoom_in(), cmd);
        button.setTooltip(MSG.zoomIn());

        Map map = getMap();
        map.addControl(zoomBox);

        return button;
    }


    protected ImgButton createZoomOutControl() {
        ImgButton zoomOut = createButton(MSG.zoom_out(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Map map   = floodMap.getMap();
                int level = map.getZoom();

                if (level > 1) {
                    map.zoomTo(level-1);
                }
            }
        });

        zoomOut.setTooltip(MSG.zoomOut());

        return zoomOut;
    }


    protected ImgButton createPanControl() {
        pan = new DragPan();
        getMap().addControl(pan);

        EnableDisableCmd cmd = new EnableDisableCmd() {
            @Override
            public void enable() {
                activateZoomBox(false);
                activateDrawFeature(false);
                activateSelectFeature(false);
                activateMeasureControl(false);
                activateGetFeatureInfo(false);
                activatePan(true);
            }

            @Override
            public void disable() {
                activatePan(false);
            }
        };

        final ImgButton button = createToggleButton(MSG.pan(), cmd);
        button.setTooltip(MSG.moveMap());

        return button;
    }


    protected DrawControl createDrawControl() {
        EnableDisableCmd cmd = new EnableDisableCmd() {
            @Override
            public void enable() {
                activateZoomBox(false);
                activatePan(false);
                activateDrawFeature(true);
                activateSelectFeature(false);
                activateMeasureControl(false);
            }

            @Override
            public void disable() {
                activateDrawFeature(false);
            }
        };
        return new DrawControl(getMap(), floodMap.getBarrierLayer(), cmd);
    }


    protected ImgButton createSelectFeatureControl() {
        SelectFeatureOptions opts = new SelectFeatureOptions();
        opts.setBox(true);

        // VectorFeatures selected by the SelectFeature control are manually
        // marked with the string "mark.delete". The control to remove selected
        // features makes use of this string to determine if the feature should
        // be deleted (is marked) or not. Actually, we would like to use the
        // OpenLayers native mechanism to select features, but for some reason
        // this doesn't work here. After a feature has been selected, the layer
        // still has no selected features.
        opts.onSelect(new SelectFeature.SelectFeatureListener() {
            @Override
            public void onFeatureSelected(VectorFeature feature) {
                floodMap.selectFeature(feature);
            }
        });

        opts.onUnSelect(new SelectFeature.UnselectFeatureListener() {
            @Override
            public void onFeatureUnselected(VectorFeature feature) {
                floodMap.disableFeature(feature);
            }
        });

        selectFeature = new SelectFeature(floodMap.getBarrierLayer(), opts);
        getMap().addControl(selectFeature);

        EnableDisableCmd cmd = new EnableDisableCmd() {
            @Override
            public void enable() {
                activateDrawFeature(false);
                activatePan(false);
                activateZoomBox(false);
                activateSelectFeature(true);
                activateMeasureControl(false);
            }

            @Override
            public void disable() {
                activateSelectFeature(false);
                floodMap.disableFeatures();
            }
        };

        ImgButton button = createToggleButton(MSG.selectFeature(), cmd);
        button.setTooltip(MSG.selectObject());

        return button;
    }


    protected ImgButton createRemoveFeatureControl() {
        ImgButton remove = createButton(MSG.removeFeature(),new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Vector          barriers = floodMap.getBarrierLayer();
                VectorFeature[] features = barriers.getFeatures();

                if (features == null || features.length == 0) {
                    return;
                }

                for (int i = features.length-1; i >= 0; i--) {
                    VectorFeature feature = features[i];

                    Attributes attr = feature.getAttributes();
                    int del = attr.getAttributeAsInt(FloodMap.MARK_SELECTED);

                    if (del == 1) {
                        barriers.removeFeature(feature);
                        feature.destroy();
                    }
                }
            }
        });

        remove.setTooltip(MSG.removeObject());

        return remove;
    }


    protected ImgButton createElevationControl() {
        ImgButton btn = createButton(MSG.adjustElevation(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent evt) {
                Vector          barriers = floodMap.getBarrierLayer();
                VectorFeature[] features = barriers.getFeatures();

                VectorFeature feature = null;

                if (features == null || features.length == 0) {
                    SC.warn(MSG.error_no_feature_selected());
                    return;
                }

                boolean multipleFeatures = false;

                for (VectorFeature f: features) {
                    Attributes attr = f.getAttributes();
                    if (attr.getAttributeAsInt(FloodMap.MARK_SELECTED) == 1) {
                        if (feature == null) {
                            feature = f;
                        }
                        else {
                            multipleFeatures = true;
                        }
                    }
                }

                if (feature == null) {
                    SC.warn(MSG.error_no_feature_selected());
                    return;
                }

                new ElevationWindow(floodMap, feature).show();

                if (multipleFeatures) {
                    SC.warn(MSG.warning_use_first_feature());
                }
            }
        });

        btn.setTooltip(MSG.adjustElevationTooltip());

        return btn;
    }


    protected Canvas createMousePosition() {
        return new MapPositionPanel(floodMap.getMapWidget());
    }


    protected MeasureControl createMeasureControl() {
        EnableDisableCmd cmd = new EnableDisableCmd() {
            @Override
            public void enable() {
                activateDrawFeature(false);
                activatePan(false);
                activateZoomBox(false);
                activateSelectFeature(false);
                activateGetFeatureInfo(false);
            }

            @Override
            public void disable() {
                // do nothing
            }
        };

        return new MeasureControl(floodMap, cmd);
    }


    protected Button createDatacageControl() {
        Button btn = new Button(MSG.databasket());
        btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent evt) {
                openDatacageWindow((MapOutputTab) getOutputTab());
            }
        });

        return btn;
    }


    protected Button createLegendControl() {
        Button btn = new Button(MSG.legend());
        btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openLegendWindow();
            }
        });

        return btn;
    }


    protected void openLegendWindow() {
        if (legendWindow == null) {
            MapOutputTab tab = (MapOutputTab) getOutputTab();
            legendWindow = new LegendWindow(tab.getThemePanel().getThemeList());
        }

        legendWindow.show();
    }


    protected ImgButton createGetFeatureInfo() {
        MapOutputTab ot = (MapOutputTab) getOutputTab();
        if (ot == null) {
            return null;
        }

        //ThemeList tl = ot.getCollection().getThemeList("floodmap");

        getFeatureInfo = new GetFeatureInfo(
            getMap(),
            ot.getThemePanel(),
            "gml");

        EnableDisableCmd cmd = new EnableDisableCmd() {
            @Override
            public void enable() {
                activateDrawFeature(false);
                activatePan(false);
                activateZoomBox(false);
                activateSelectFeature(false);
                activateMeasureControl(false);
                activateGetFeatureInfo(true);
            }

            @Override
            public void disable() {
                activateGetFeatureInfo(false);
            }
        };

        ImgButton button = createToggleButton(MSG.getFeatureInfo(), cmd);
        button.setTooltip(MSG.getFeatureInfoTooltip());

        return button;
    }


    protected Button createManageThemesControl() {
        Button btn = new Button(MSG.manageThemes());
        btn.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                ((MapOutputTab)getOutputTab()).toogleThemePanel();
            }
        });
        return btn;
    }


    protected ImgButton createMapPrintControl() {
        final MapToolbar mtb = this;
        ImgButton btn = createButton(
            MSG.printMapSettings(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MapPrintWindow mpsw =
                        new MapPrintWindow(outputTab.getCollection(), mtb);
                outputTab.getCollectionView().addChild(mpsw);
            }
        });
        btn.setTooltip(MSG.printTooltip());

        return btn;
    }


    protected ImgButton createWMSControl() {
        final String srs = floodMap.getRiverProjection();

        ImgButton add = createButton(MSG.addWMS(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                MapOutputTab ot = (MapOutputTab) getOutputTab();
                new ExternalWMSWindow(ot, srs).start();
            }
        });

        add.setTooltip(MSG.addWMSTooltip());

        return add;
    }


    protected Label createEPSGLabel() {
        Label epsgLabel = new Label(floodMap.getRiverProjection());

        epsgLabel.setAlign(Alignment.RIGHT);
        epsgLabel.setWidth(75);

        return epsgLabel;
    }

    @Override
    public void onMapZoom(MapZoomListener.MapZoomEvent e) {
//        updatePrintUrl();
    }

    public void updateThemes(ThemeList themeList) {
        if (legendWindow != null) {
            legendWindow.update(themeList);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
