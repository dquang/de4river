/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.event.OutputParameterChangeEvent;
import org.dive4elements.river.client.client.event.OutputParameterChangeHandler;
import org.dive4elements.river.client.client.event.PanEvent;
import org.dive4elements.river.client.client.event.PanHandler;
import org.dive4elements.river.client.client.event.RedrawRequestEvent;
import org.dive4elements.river.client.client.event.RedrawRequestEvent.Type;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.event.ZoomEvent;
import org.dive4elements.river.client.client.event.ZoomHandler;
import org.dive4elements.river.client.client.services.ChartInfoService;
import org.dive4elements.river.client.client.services.ChartInfoServiceAsync;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.OutputTab;
import org.dive4elements.river.client.shared.Transform2D;
import org.dive4elements.river.client.shared.model.Axis;
import org.dive4elements.river.client.shared.model.ChartInfo;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.ZoomObj;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Tab representing and showing one Chart-output (diagram).
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartOutputTab
extends      OutputTab
implements   ResizedHandler,
             OutputParameterChangeHandler,
             ZoomHandler,
             PanHandler,
             RedrawRequestHandler
{
    public static final int DEFAULT_CHART_WIDTH  = 600;
    public static final int DEFAULT_CHART_HEIGHT = 500;

    public static final int THEMEPANEL_MIN_WIDTH = 250;

    /** The service that is used to fetch chart information. */
    protected ChartInfoServiceAsync info = GWT.create(ChartInfoService.class);

    /** The ChartInfo object that provides information about the current
     * chart. */
    protected ChartInfo chartInfo;

    /** Transformer used to transform image pixels into chart (data)
     * coordinates. */
    protected Transform2D[] transformer;

    /** The collection view.*/
    protected CollectionView view;

    /** The ThemePanel to expose control over themes (facettes). */
    protected ChartThemePanel ctp;

    /** The canvas that wraps the chart toolbar. */
    protected ChartToolbar tbarPanel;

    /** The canvas that wraps the theme editor. */
    protected Canvas left;

    /** The canvas that wraps the chart. */
    protected Canvas right;

    protected Img chart;

    /** Chart zoom options. */
    protected int[] xrange;
    protected int[] yrange;

    /** Stack of ZoomObj to allow 'redo last zoom'-kind of actions. */
    protected Stack<ZoomObj> zoomStack;
    protected Number[]       zoom;


    /**
     * The default constructor to create a new ChartOutputTab.
     *
     * @param title The title of this tab.
     * @param collection The Collection which this chart belongs to.
     * @param mode The OutputMode.
     * @param collectionView The shown collection.
     */
    public ChartOutputTab(
        String         title,
        Collection     collection,
        OutputMode     mode,
        CollectionView collectionView
    ){
        super(title, collection, collectionView, mode);

        view      = collectionView;
        left      = new Canvas();
        right     = new Canvas();
        xrange    = new int[2];
        yrange    = new int[2];
        zoomStack = new Stack<ZoomObj>();

        zoom = new Number[] {
            new Double(0), new Double(1),
            new Double(0), new Double(1) };

        left.setBorder("1px solid gray");
        left.setWidth(THEMEPANEL_MIN_WIDTH);
        left.setMinWidth(THEMEPANEL_MIN_WIDTH);
        right.setWidth("*");

        VLayout vLayout = new VLayout();
        vLayout.setMembersMargin(2);

        HLayout hLayout = new HLayout();
        hLayout.setWidth100();
        hLayout.setHeight100();
        hLayout.setMembersMargin(10);

        hLayout.addMember(left);
        hLayout.addMember(right);

        ctp = createThemePanel(mode, collectionView);
        if (ctp != null) {
            ctp.addRedrawRequestHandler(this);
            ctp.addOutputParameterChangeHandler(this);
            left.addChild(ctp);
        }
        else {
            left.setVisible(false);
        }

        chart = createChartImg();
        right.addChild(chart);
        right.setOverflow(Overflow.HIDDEN);

        left.setShowResizeBar(true);

        tbarPanel = createChartToolbar(this);
        vLayout.addMember(tbarPanel);
        vLayout.addMember(hLayout);
        vLayout.setOverflow(Overflow.HIDDEN);

        setPane(vLayout);

        right.addResizedHandler(this);
    }


    public ChartThemePanel createThemePanel(
        OutputMode mode, CollectionView view
    ) {
        // Output "cross_section" needs slightly modified ThemePanel
        // (with action buttons).
        if (mode.getName().equals("cross_section")) {
            return new CrossSectionChartThemePanel(mode, view);
        }
        else {
            return new ChartThemePanel(mode, view);
        }
    }


    public ChartToolbar createChartToolbar(ChartOutputTab tab) {
        return new ChartToolbar(tab);
    }


    public void toggleThemePanel() {
        this.left.setVisible(!left.isVisible());
    }


    /**
     * This method is called after the chart panel has resized. It removes the
     * chart - if existing - and requests a new one with adjusted size.
     *
     * @param event The resize event.
     */
    @Override
    public void onResized(ResizedEvent event) {
        updateChartPanel();
        updateChartInfo();
    }


    /** For RESET type of events, just reset the ranges, otherwise do a
     * complete refresh of panel, info and collection. */
    @Override
    public void onRedrawRequest(RedrawRequestEvent event) {
        if (event.getType() == Type.RESET) {
            resetRanges();
        }
        else {
            ctp.updateCollection();
            updateChartPanel();
            updateChartInfo();
        }
    }


    /**
     * Listens to change event in the chart them panel and updates chart after
     * receiving such an event.
     *
     * @param event The OutputParameterChangeEvent.
     */
    @Override
    public void onOutputParameterChanged(OutputParameterChangeEvent event) {
        updateChartInfo();
        updateChartPanel();
    }


    /**
     * Listens to zoom events and refreshes the current chart in such case.
     *
     * @param evt The ZoomEvent that stores the coordinates for zooming.
     */
    @Override
    public void onZoom(ZoomEvent evt) {
        zoomStack.push(new ZoomObj(zoom[0], zoom[1], zoom[2], zoom[3]));

        xrange[0] = evt.getStartX();
        xrange[1] = evt.getEndX();
        yrange[0] = evt.getStartY();
        yrange[1] = evt.getEndY();

        xrange[0] = xrange[0] < xrange[1] ? xrange[0] : xrange[1];
        yrange[0] = yrange[0] < yrange[1] ? yrange[0] : yrange[1];

        translateCoordinates();

        updateChartInfo();
        updateChartPanel();
    }


    protected Number[] translateCoordinates() {
        if (xrange == null || (xrange[0] == 0 && xrange[1] == 0)) {
            zoom[0] = 0d;
            zoom[1] = 1d;
        }
        else {
            translateXCoordinates();
        }

        if (yrange == null || (yrange[0] == 0 && yrange[1] == 0)) {
            zoom[2] = 0d;
            zoom[3] = 1d;
        }
        else {
            translateYCoordinates();
        }

        return zoom;
    }


    protected void translateXCoordinates() {
        Axis xAxis = chartInfo.getXAxis(0);

        Number xmin   = xAxis.getMin();
        Number xmax   = xAxis.getMax();
        Number xRange = subtract(xmax, xmin);

        Transform2D transformer = getTransformer(0);

        double[] start = transformer.transform(xrange[0], yrange[0]);
        double[] end   = transformer.transform(xrange[1], yrange[1]);

        zoom[0] = divide(subtract(start[0], xmin), xRange);
        zoom[1] = divide(subtract(end[0], xmin), xRange);
    }


    protected void translateYCoordinates() {
        Axis yAxis = chartInfo.getYAxis(0);

        Number ymin   = yAxis.getMin();
        Number ymax   = yAxis.getMax();
        Number yRange = subtract(ymax, ymin);

        Transform2D transformer = getTransformer(0);

        double[] start = transformer.transform(xrange[0], yrange[0]);
        double[] end   = transformer.transform(xrange[1], yrange[1]);

        zoom[2] = divide(subtract(start[1], ymin), yRange);
        zoom[3] = divide(subtract(end[1], ymin), yRange);
    }


    @Override
    public void onPan(PanEvent event) {
        if (chartInfo == null) {
            return;
        }

        int[] start = event.getStartPos();
        int[] end   = event.getEndPos();

        Transform2D t = getTransformer();

        double[] ts = t.transform(start[0], start[1]);
        double[] tt = t.transform(end[0], end[1]);

        double diffX = ts[0] - tt[0];
        double diffY = ts[1] - tt[1];

        Axis xAxis = chartInfo.getXAxis(0);
        Axis yAxis = chartInfo.getYAxis(0);

        Number[] x = panAxis(xAxis, diffX);
        Number[] y = panAxis(yAxis, diffY);

        // Set the zoom coordinates.
        zoom[0] = x[0];
        zoom[1] = x[1];
        zoom[2] = y[0];
        zoom[3] = y[1];

        updateChartInfo();
        updateChartPanel();
    }


    protected Number[] panAxis(Axis axis, double diff) {
        Number min = axis.getFrom();
        Number max = axis.getTo();

        min = add(min, diff);
        max = add(max, diff);

        return computeZoom(axis, min, max);
    }


    public void resetRanges() {
        zoomStack.push(new ZoomObj(zoom[0], zoom[1], zoom[2], zoom[3]));

        zoom[0] = 0d;
        zoom[1] = 1d;
        zoom[2] = 0d;
        zoom[3] = 1d;

        updateChartInfo();
        updateChartPanel();
    }


    /**
     * This method zooms the current chart out by a given <i>factor</i>.
     *
     * @param factor The factor should be between 0-100.
     */
    public void zoomOut(int factor) {
        if (factor < 0 || factor > 100 || chartInfo == null) {
            return;
        }

        zoomStack.push(new ZoomObj(zoom[0], zoom[1], zoom[2], zoom[3]));

        Axis xAxis = chartInfo.getXAxis(0);
        Axis yAxis = chartInfo.getYAxis(0);

        Number[] x = zoomAxis(xAxis, factor);
        Number[] y = zoomAxis(yAxis, factor);

        zoom[0] = x[0];
        zoom[1] = x[1];
        zoom[2] = y[0];
        zoom[3] = y[1];

        updateChartInfo();
        updateChartPanel();
    }


    /**
     * This method is used to zoom out. Zooming out is realized with a stacked
     * logic. Initially, you cannot zoom out. For each time you start a zoom-in
     * action, the extent of the chart is stored and pushed onto a stack. A
     * zoom-out will now lead you to the last extent that is popped from stack.
     */
    public void zoomOut() {
        if (!zoomStack.empty()) {
            zoom = zoomStack.pop().getZoom();

            updateChartInfo();
            updateChartPanel();
        }
    }


    public Number[] zoomAxis(Axis axis, int factor) {
        GWT.log("Prepare Axis for zooming (factor: " + factor + ")");

        Number min   = axis.getMin();
        Number max   = axis.getMax();
        Number range = isBigger(max, min)
            ? subtract(max, min)
            : subtract(min, max);

        Number curFrom = axis.getFrom();
        Number curTo   = axis.getTo();

        Number diff = isBigger(curTo, curFrom)
            ? subtract(curTo, curFrom)
            : subtract(curFrom, curTo);

        GWT.log("    max from    : " + min);
        GWT.log("    max to      : " + max);
        GWT.log("    max range   : " + range);
        GWT.log("    current from: " + curFrom);
        GWT.log("    current to  : " + curTo);
        GWT.log("    current diff: " + diff);

        Number newFrom = subtract(curFrom, divide(multi(diff, factor), 100));
        Number newTo   = add(curTo, divide(multi(diff, factor), 100));

        GWT.log("    new from: " + newFrom);
        GWT.log("    new to  : " + newTo);

        return new Number[] {
            divide(subtract(newFrom, min), range),
            divide(subtract(newTo, min), range)
        };
    }


    public static Number[] computeZoom(Axis axis, Number min, Number max) {
        Number[] hereZoom = new Number[2];

        Number absMin = axis.getMin();
        Number absMax = axis.getMax();
        Number diff   = isBigger(absMax, absMin)
            ? subtract(absMax, absMin)
            : subtract(absMin, absMax);

        hereZoom[0] = divide(subtract(min, absMin), diff);
        hereZoom[1] = divide(subtract(max, absMin), diff);

        return hereZoom;
    }


    /** Get Collection from ChartThemePanel. .*/
    public Collection getCtpCollection() {
        return this.ctp.getCollection();
    }


    /**
     * Updates the Transform2D object using the chart info service.
     */
    public void updateChartInfo() {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        info.getChartInfo(
            view.getCollection(),
            locale,
            mode.getName(),
            getChartAttributes(),
            new AsyncCallback<ChartInfo>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("ChartInfo ERROR: " + caught.getMessage());
                }

                @Override
                public void onSuccess(ChartInfo chartInfo) {
                    setChartInfo(chartInfo);
                }
            });
    }


    public void updateChartPanel() {
        int w = right.getWidth();
        int h = right.getHeight();

        chart.setSrc(getImgUrl(w, h));
    }


    /**
     * Returns the existing chart panel.
     *
     * @return the existing chart panel.
     */
    public Canvas getChartPanel() {
        return right;
    }


    /** Access the Canvas holding the rendered Chart. */
    public Canvas getChartImg() {
        return chart;
    }


    /** Get associated ChartInfo object. */
    public ChartInfo getChartInfo() {
        return chartInfo;
    }


    protected void setChartInfo(ChartInfo chartInfo) {
        this.chartInfo = chartInfo;
    }


    public Transform2D getTransformer() {
        return getTransformer(0);
    }


    /**
     * Returns the Transform2D object used to transform image coordinates into
     * chart (data) coordinates.
     *
     * @param pos The index of a specific transformer.
     *
     * @return the Transform2D object.
     */
    public Transform2D getTransformer(int pos) {
        if (chartInfo == null) {
            return null;
        }

        return chartInfo.getTransformer(pos);
    }


    /**
     * Returns the Transform2D count.
     *
     * @return the Transformer2D count
     */
    public int getTransformerCount() {
        if (chartInfo == null) {
            return 0;
        }

        return chartInfo.getTransformerCount();
    }


    /**
     * Creates a new chart panel with default size.
     *
     * @return the created chart panel.
     */
    protected Img createChartImg() {
        return createChartImg(DEFAULT_CHART_WIDTH, DEFAULT_CHART_HEIGHT);
    }


    /**
     * Creates a new chart panel with specified width and height.
     *
     * @param width The width for the chart panel.
     * @param height The height for the chart panel.
     *
     * @return the created chart panel.
     */
    protected Img createChartImg(int width, int height) {
        Img chart  = getChartImg(width, height);
        chart.setWidth100();
        chart.setHeight100();

        return chart;
    }


    /**
     * Builds the chart image and returns it.
     *
     * @param width The chart width.
     * @param height The chart height.
     *
     * @return the chart image.
     */
    protected Img getChartImg(int width, int height) {
        return new Img(getImgUrl(width, height));
    }


    /**
     * Builds the URL that points to the chart image.
     *
     * @param width The width of the requested chart.
     * @param height The height of the requested chart.
     * @param xr Optional x range (used for zooming).
     * @param yr Optional y range (used for zooming).
     *
     * @return the URL to the chart image.
     */
    protected String getImgUrl(int width, int height) {
        Config config = Config.getInstance();

        String imgUrl = GWT.getModuleBaseURL();
        imgUrl += "chart";
        imgUrl += "?uuid=" + collection.identifier();
        imgUrl += "&type=" + mode.getName();
        imgUrl += "&locale=" + config.getLocale();
        imgUrl += "&timestamp=" + new Date().getTime();
        imgUrl += "&width=" + Integer.toString(width);
        imgUrl += "&height=" + Integer.toString(height);

        Number[] zoom = getZoomValues();

        if (zoom != null) {
            if (zoom[0].intValue() != 0 || zoom[1].intValue() != 1) {
                // a zoom range of 0-1 means displaying the whole range. In such
                // case we don't need to zoom.
                imgUrl += "&minx=" + zoom[0];
                imgUrl += "&maxx=" + zoom[1];
            }

            if (zoom[2].intValue() != 0 || zoom[3].intValue() != 1) {
                // a zoom range of 0-1 means displaying the whole range. In such
                // case we don't need to zoom.
                imgUrl += "&miny=" + zoom[2];
                imgUrl += "&maxy=" + zoom[3];
            }
        }

        return imgUrl;
    }


    /** Get link to export image in given dimension, format and encoding. */
    public String getExportUrl(
        int width, int height, String format, String encoding) {
        String url = getImgUrl(width, height);
        url += "&format=" + format;
        url += "&export=true";
        url += "&encoding=" + encoding;

        return url;
    }


    /** Get link to export image in given dimension and format. */
    public String getExportUrl(int width, int height, String format) {
        String url = getImgUrl(width, height);
        url += "&format=" + format;
        url += "&export=true";

        return url;
    }


    public Map <String, String> getChartAttributes() {
        Map<String, String> attr = new HashMap<String, String>();

        Canvas chart = getChartPanel();
        attr.put("width",  chart.getWidth().toString());
        attr.put("height", chart.getHeight().toString());

        Number[] zoom = getZoomValues();

        if (zoom != null) {
            if (zoom[0].intValue() != 0 || zoom[1].intValue() != 1) {
                // a zoom range of 0-1 means displaying the whole range. In such
                // case we don't need to zoom.
                attr.put("minx", zoom[0].toString());
                attr.put("maxx", zoom[1].toString());
            }
            if (zoom[2].intValue() != 0 || zoom[3].intValue() != 1) {
                // a zoom range of 0-1 means displaying the whole range. In such
                // case we don't need to zoom.
                attr.put("miny", zoom[2].toString());
                attr.put("maxy", zoom[3].toString());
            }
        }

        return attr;
    }


    protected Number[] getZoomValues() {
        return zoom;
    }


    /** Return the 'parent' CollectionView. */
    public CollectionView getView() {
        return this.view;
    }


    public static Number subtract(Number left, Number right) {
        if (left instanceof Double) {
            return new Double(left.doubleValue() - right.doubleValue());
        }
        else if (left instanceof Long) {
            return new Long(left.longValue() - right.longValue());
        }
        else {
            return new Integer(left.intValue() - right.intValue());
        }
    }


    /** Add two numbers, casting to Type of param left. */
    public static Number add(Number left, Number right) {
        if (left instanceof Double) {
            return new Double(left.doubleValue() + right.doubleValue());
        }
        else if (left instanceof Long) {
            return new Long(left.longValue() + right.longValue());
        }
        else {
            return new Integer(left.intValue() + right.intValue());
        }
    }


    /** Divde left by right. Note that Long will be casted to double. */
    public static Number divide(Number left, Number right) {
        if (left instanceof Double) {
            return new Double(left.doubleValue() / right.doubleValue());
        }
        else if (left instanceof Long) {
            return new Double(left.doubleValue() / right.doubleValue());
        }
        else {
            return new Integer(left.intValue() / right.intValue());
        }
    }


    public static Number multi(Number left, Number right) {
        if (left instanceof Double) {
            return new Double(left.doubleValue() * right.doubleValue());
        }
        else if (left instanceof Long) {
            return new Long(left.longValue() * right.longValue());
        }
        else {
            return new Integer(left.intValue() * right.intValue());
        }
    }


    public static boolean isBigger(Number left, Number right) {
        if (left instanceof Double) {
            return left.doubleValue() > right.doubleValue();
        }
        else if (left instanceof Long) {
            return left.longValue() > right.longValue();
        }
        else {
            return left.intValue() > right.intValue();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
