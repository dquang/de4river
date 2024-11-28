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

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortArrow;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.grid.events.RowOutEvent;
import com.smartgwt.client.widgets.grid.events.RowOutHandler;
import com.smartgwt.client.widgets.grid.events.RowOverEvent;
import com.smartgwt.client.widgets.grid.events.RowOverHandler;

import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Geometry;
import org.gwtopenmaps.openlayers.client.geometry.LinearRing;
import org.gwtopenmaps.openlayers.client.geometry.LineString;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.geometry.Polygon;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;
import org.gwtopenmaps.openlayers.client.util.Attributes;

import org.dive4elements.river.client.client.FLYSConstants;



public class ElevationWindow extends Window {

    public static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public static final int WINDOW_WIDTH  = 250;
    public static final int WINDOW_HEIGHT = 250;

    protected FloodMap      floodmap;
    protected Vector        layer;
    protected VectorFeature feature;

    protected ListGrid grid;


    private class PointRecord extends ListGridRecord {
        protected VectorFeature point;

        public PointRecord(VectorFeature point, double x, double y, String z) {
            super();

            this.point = point;
            setAttribute("x", x);
            setAttribute("y", y);
            setAttribute("z", z);
        }

        public VectorFeature getPoint() {
            return point;
        }
    }


    public ElevationWindow(FloodMap floodmap, VectorFeature feature) {
        super();
        this.feature  = feature;
        this.floodmap = floodmap;

        init();
    }


    protected void init() {
        addCloseClickHandler(new CloseClickHandler() {
            public void onCloseClick(CloseClickEvent evt) {
                doClose();
            }
        });

        initLayout();
        initEdit();
        updateGrid();
    }


    protected void initLayout() {
        setWidth(WINDOW_WIDTH);
        setHeight(WINDOW_HEIGHT);
        setTitle(MSG.ele_window_title());

        VLayout root = new VLayout();
        root.setMembersMargin(5);
        root.setPadding(5);

        root.addMember(getLabel());
        root.addMember(getGrid());
        root.addMember(getButtonBar());

        addItem(root);
        centerInPage();
    }


    protected void initEdit() {
        VectorOptions opts = new VectorOptions();
        opts.setProjection(floodmap.getRiverProjection());
        opts.setMaxExtent(floodmap.getMaxExtent());

        layer = new Vector("tmp", opts);
        layer.setIsBaseLayer(false);

        floodmap.getMap().addLayer(layer);
    }


    public Style getStyle() {
        Style style = new Style();
        style.setStrokeColor("#000000");
        style.setStrokeWidth(1);
        style.setFillColor("#FF0000");
        style.setFillOpacity(0.5);
        style.setPointRadius(5);
        style.setStrokeOpacity(1.0);
        return style;
    }


    public Style getHighStyle() {
        Style style = new Style();
        style.setStrokeColor("#000000");
        style.setStrokeWidth(1);
        style.setFillColor("#FFFF22");
        style.setFillOpacity(0.5);
        style.setPointRadius(5);
        style.setStrokeOpacity(1.0);
        return style;
    }


    protected Label getLabel() {
        Label label = new Label(MSG.ele_window_label());
        label.setHeight(25);

        return label;
    }


    protected ListGrid getGrid() {
        if (grid == null) {
            grid = new ListGrid();
            grid.setCanEdit(true);
            grid.setCanReorderFields(false);
            grid.setAutoFitMaxWidth(WINDOW_WIDTH);
            grid.setShowHeaderContextMenu(false);
            grid.setShowSortArrow(SortArrow.NONE);
            grid.setSortDirection(SortDirection.DESCENDING);
            grid.setSelectionType(SelectionStyle.NONE);

            ListGridField x = new ListGridField("x", MSG.ele_window_x_col());
            x.setCanEdit(false);

            ListGridField y = new ListGridField("y", MSG.ele_window_y_col());
            y.setCanEdit(false);

            ListGridField z = new ListGridField("z", MSG.ele_window_z_col());
            z.setCanEdit(true);

            grid.setFields(x, y, z);

            grid.addRowOverHandler(new RowOverHandler() {
                public void onRowOver(RowOverEvent evt) {
                    PointRecord   pr = (PointRecord) evt.getRecord();
                    VectorFeature p  = pr.getPoint();

                    p.setStyle(getHighStyle());
                    layer.redraw();
                }
            });

            grid.addRowOutHandler(new RowOutHandler() {
                public void onRowOut(RowOutEvent evt) {
                    PointRecord   pr = (PointRecord) evt.getRecord();
                    VectorFeature p  = pr.getPoint();

                    p.setStyle(getStyle());
                    layer.redraw();
                }
            });

            grid.addEditCompleteHandler(new EditCompleteHandler() {
                public void onEditComplete(EditCompleteEvent evt) {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    String        z = (String) evt.getNewValues().get("z");

                    try {
                        nf.parse(z);
                    }
                    catch (NumberFormatException nfe) {
                        SC.warn(MSG.ele_window_format_error() + " " + z);

                        PointRecord old = (PointRecord) evt.getOldValues();

                        ListGridRecord[] records = grid.getRecords();
                        records[evt.getRowNum()] = old;

                        grid.setRecords(records);
                    }

                }
            });
        }

        return grid;
    }


    protected HLayout getButtonBar() {
        HLayout bar = new HLayout();
        bar.setAlign(Alignment.CENTER);
        bar.setHeight(25);
        bar.setMembersMargin(15);

        bar.addMember(getOKButton());
        bar.addMember(getCancelButton());

        return bar;
    }


    protected IButton getOKButton() {
        IButton btn = new IButton(MSG.ele_window_ok_button());

        btn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent evt) {
                if (saveElevation()) {
                    doClose();
                }

            }
        });

        return btn;
    }


    protected IButton getCancelButton() {
        IButton btn = new IButton(MSG.ele_window_cancel_button());

        btn.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent evt) {
                doClose();
            }
        });

        return btn;
    }


    public void updateGrid() {
        Attributes attr = feature.getAttributes();
        Geometry   geom = feature.getGeometry();

        String barrierType = null;

        if (attr != null) {
            barrierType = attr.getAttributeAsString("typ");
        }
        else {
            barrierType = DrawControl.BARRIER_DAM_VALUE;
        }

        GWT.log("Geometry is from type: " + geom.getClassName());
        GWT.log("Barrier  is from type: " + barrierType);

        double[][] points = getPointsFromGeometry(geom);
        double[]   ele    = extractElevations(attr);

        if (points == null) {
            return;
        }

        NumberFormat nf = NumberFormat.getDecimalFormat();

        ListGrid grid = getGrid();

        int zLen = ele != null ? ele.length : 0;

        for (int i = 0, len = points.length; i < len; i++) {
            double[]  point = points[i];
            VectorFeature p = addPoint(point[0], point[1]);

            String value = null;

            if (zLen > i) {
                value = nf.format(ele[i]);
            }
            else if (barrierType.equals(DrawControl.BARRIER_DITCH_VALUE)) {
                value = "-9999";
            }
            else {
                value = "9999";
            }

            grid.addData(new PointRecord(p, point[0], point[1], value));
        }

        grid.redraw();
    }


    public static double[] extractElevations(Attributes attr) {
        if (attr == null) {
            return null;
        }

        String   elevationStr = attr.getAttributeAsString("elevation");

        if (elevationStr == null || elevationStr.length() == 0) {
            return null;
        }

        String[] elevations   = elevationStr.split(" ");

        int len = elevations != null ? elevations.length : 0;

        if (len == 0) {
            return null;
        }

        double[] res = new double[len];

        for (int i = 0; i < len; i++) {
            try {
                res[i] = Double.valueOf(elevations[i]);
            }
            catch (NumberFormatException nfe) {
                // go on
            }
        }

        return res;
    }


    public static double[][] getPointsFromGeometry(Geometry geom) {
        String clazz = geom.getClassName();

        if (clazz != null && clazz.equals(Geometry.LINESTRING_CLASS_NAME)) {
            return getPointsFromLineString(
                LineString.narrowToLineString(geom.getJSObject()));
        }
        else if (clazz != null && clazz.equals(Geometry.POLYGON_CLASS_NAME)) {
            return getPointsFromPolygon(
                Polygon.narrowToPolygon(geom.getJSObject()));
        }
        else {
            SC.warn(MSG.ele_window_geometry_error() + " " + clazz);
        }

        return null;
    }


    public static double[][] getPointsFromLineString(LineString line) {
        return line.getCoordinateArray();
    }


    public static double[][] getPointsFromPolygon(Polygon polygon) {
        LinearRing[] rings = polygon.getComponents();

        return getPointsFromLineString(rings[0]);
    }


    protected VectorFeature addPoint(double x, double y) {
        VectorFeature point = new VectorFeature(new Point(x, y), getStyle());
        layer.addFeature(point);

        return point;
    }


    protected boolean saveElevation() {
        ListGridRecord[] records = grid.getRecords();

        NumberFormat nf = NumberFormat.getDecimalFormat();

        StringBuilder sb = new StringBuilder();

        for (ListGridRecord record: records) {
            PointRecord pr = (PointRecord) record;
            String value   = pr.getAttributeAsString("z");

            try {
                double z = nf.parse(value);
                sb.append(String.valueOf(z));
            }
            catch (NumberFormatException nfe) {
                SC.warn(MSG.ele_window_save_error());
                return false;
            }

            sb.append(" ");
        }

        Attributes attr = feature.getAttributes();
        attr.setAttribute("elevation", sb.toString());

        return true;
    }


    protected void doClose() {
        floodmap.getMap().removeLayer(layer);
        destroy();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
