/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.gwtopenmaps.openlayers.client.control.Measure;
import org.gwtopenmaps.openlayers.client.event.MeasureEvent;
import org.gwtopenmaps.openlayers.client.event.MeasureListener;
import org.gwtopenmaps.openlayers.client.event.MeasurePartialListener;
import org.gwtopenmaps.openlayers.client.handler.PathHandler;
import org.gwtopenmaps.openlayers.client.handler.PolygonHandler;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.utils.EnableDisableCmd;


public class MeasureControl extends HLayout {

    public static final String NUMBER_FORMAT_PATTERN = "#.##";

    public static final String AREA_UNIT      = "ha";
    public static final int    AREA_FACTOR_M  = 10000;
    public static final int    AREA_FACTOR_KM = 100;

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected FloodMap floodMap;

    protected Measure measureLine;
    protected Measure measurePolygon;

    protected ImgButton measureLineButton;
    protected ImgButton measurePolyButton;
    protected Label     label;

    protected NumberFormat formatter;


    public MeasureControl(FloodMap floodMap, EnableDisableCmd cmd) {
        this.floodMap = floodMap;

        measureLineButton = createMeasureLineControl(cmd);
        measurePolyButton = createMeasurePolyControl(cmd);

        formatter = NumberFormat.getFormat(NUMBER_FORMAT_PATTERN);

        label = new Label();

        initLayout();
    }


    protected void initLayout() {
        setWidth(100);
        setMembersMargin(2);

        label.setWidth(75);

        addMember(measureLineButton);
        addMember(measurePolyButton);
        addMember(label);
    }


    protected ImgButton createMeasureLineControl(final EnableDisableCmd cmd) {
        measureLine = new Measure(new PathHandler());
        measureLine.setPersist(true);
        measureLine.addMeasureListener(new MeasureListener() {
            public void onMeasure(MeasureEvent e) {
                updateMeasure(e.getMeasure(), e.getUnits());
            }
        });
        measureLine.addMeasurePartialListener(new MeasurePartialListener() {
            public void onMeasurePartial(MeasureEvent e) {
                updateMeasure(e.getMeasure(), e.getUnits());
            }
        });

        floodMap.getMap().addControl(measureLine);

        final ImgButton btn = new ImgButton();
        String baseUrl = GWT.getHostPageBaseURL();
        btn.setSrc(baseUrl + MSG.measureLine());
        btn.setActionType(SelectionType.CHECKBOX);
        btn.setSize(20);
        btn.setShowRollOver(false);
        btn.setShowRollOverIcon(false);
        btn.setSelected(false);
        btn.setTooltip(MSG.measureDistance());
        btn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                if (btn.isSelected()) {
                    cmd.enable();
                    activateMeasurePolygon(false);
                    activateMeasureLine(true);
                }
                else {
                    cmd.disable();
                    activateMeasureLine(false);
                }
            }
        });

        return btn;
    }


    protected ImgButton createMeasurePolyControl(final EnableDisableCmd cmd) {
        measurePolygon = new Measure(new PolygonHandler());
        measurePolygon.setPersist(true);
        measurePolygon.addMeasureListener(new MeasureListener() {
            public void onMeasure(MeasureEvent e) {
                updateMeasureArea(e.getMeasure(), e.getUnits());
            }
        });
        measurePolygon.addMeasurePartialListener(new MeasurePartialListener() {
            public void onMeasurePartial(MeasureEvent e) {
                updateMeasureArea(e.getMeasure(), e.getUnits());
            }
        });

        floodMap.getMap().addControl(measurePolygon);

        final ImgButton btn = new ImgButton();
        String baseUrl = GWT.getHostPageBaseURL();
        btn.setSrc(baseUrl + MSG.measurePolygon());
        btn.setActionType(SelectionType.CHECKBOX);
        btn.setSize(20);
        btn.setShowRollOver(false);
        btn.setShowRollOverIcon(false);
        btn.setSelected(false);
        btn.setTooltip(MSG.measureArea());
        btn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                if (btn.isSelected()) {
                    cmd.enable();
                    activateMeasureLine(false);
                    activateMeasurePolygon(true);
                }
                else {
                    cmd.disable();
                    activateMeasurePolygon(false);
                }
            }
        });

        return btn;
    }


    protected void clearMeasure() {
        label.setContents("");
    }


    protected void updateMeasure(float value, String unit) {
        label.setContents(formatter.format(value) + " " + unit);
    }


    protected void updateMeasureArea(float value, String unit) {
        float  ha      = value;
        String ha_unit = unit;

        if (unit.equals("m")) {
            ha      = (float) value / AREA_FACTOR_M;
            ha_unit = AREA_UNIT;
        }
        else if (unit.equals("km")) {
            ha      = (float) value * AREA_FACTOR_KM;
            ha_unit = AREA_UNIT;
        }

        label.setContents(formatter.format(ha) + " " + ha_unit);
    }


    public void activate(boolean activate) {
        if (!activate) {
            clearMeasure();
            activateMeasureLine(activate);
            activateMeasurePolygon(activate);
        }
    }


    protected void activateMeasureLine(boolean activate) {
        if (activate) {
            clearMeasure();
            measureLineButton.select();
            measureLine.activate();
        }
        else {
            measureLineButton.deselect();
            measureLine.deactivate();
        }
    }


    protected void activateMeasurePolygon(boolean activate) {
        if (activate) {
            clearMeasure();
            measurePolyButton.select();
            measurePolygon.activate();
        }
        else {
            measurePolyButton.deselect();
            measurePolygon.deactivate();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
