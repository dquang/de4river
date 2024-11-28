/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Positioning;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.MouseDownEvent;
import com.smartgwt.client.widgets.events.MouseDownHandler;
import com.smartgwt.client.widgets.events.MouseMoveEvent;
import com.smartgwt.client.widgets.events.MouseMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseUpEvent;
import com.smartgwt.client.widgets.events.MouseUpHandler;

import org.dive4elements.river.client.client.event.HasZoomHandlers;
import org.dive4elements.river.client.client.event.ZoomEvent;
import org.dive4elements.river.client.client.event.ZoomHandler;


/**
 * This control observes that panel retrieved by ChartOutputTab.getChartPanel().
 * If activated, a zoombox is drawn. One of the two edges is the position of the
 * mouse down event on the observed panel. The other edge is specified by the
 * current mouse position. If the mouse up event occurs, start and end point
 * relative to the left and upper border of the observed panel is determined and
 * a ZoomEvent is fired.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ZoomboxControl
extends      ImgButton
implements   MouseDownHandler, MouseUpHandler, MouseMoveHandler,
             HasZoomHandlers, MouseOutHandler
{
    protected List<ZoomHandler> handlers;

    protected ChartOutputTab chartTab;

    protected Canvas zoombox;

    protected int[] start;
    protected int[] end;


    public ZoomboxControl(ChartOutputTab chartTab, String imageUrl) {
        super();

        this.handlers = new ArrayList<ZoomHandler>();
        this.chartTab = chartTab;
        this.start    = new int[] { -1, -1 };
        this.end      = new int[2];
        this.zoombox  = new Canvas();

        initZoombox();

        String baseUrl = GWT.getHostPageBaseURL();
        setSrc(baseUrl + imageUrl);
        setActionType(SelectionType.CHECKBOX);
        setSize(20);
        setShowRollOver(false);
        setSelected(false);

        Canvas chart = chartTab.getChartPanel();
        chart.addMouseDownHandler(this);
        chart.addMouseOutHandler(this);
        chart.addMouseMoveHandler(this);
        chart.addMouseUpHandler(this);
    }


    /**
     * Initializes the zoombox that is displayed over the observed area. The
     * zoombox has an opaque background. Its height/width and x/y values are
     * determined by the start point (mouse down) and the current mouse
     * position.
     */
    protected void initZoombox() {
        Canvas chart = chartTab.getChartPanel();
        chart.addChild(zoombox);

        zoombox.setPosition(Positioning.ABSOLUTE);
        zoombox.setBorder("2px solid black");
        zoombox.setOpacity(50);
        zoombox.setWidth(1);
        zoombox.setHeight(1);
        zoombox.setLeft(-10000);
        zoombox.setTop(-10000);
    }


    /**
     * Registers a new ZoomHandler that wants to listen to ZoomEvents.
     *
     * @param handler A new ZoomHandler.
     */
    public void addZoomHandler(ZoomHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    /**
     * A mouse down event on the specified area will set the start point for the
     * zoombox.
     *
     * @param event The mouse down event which contains the xy coordinates of
     * the observed area.
     */
    public void onMouseDown(MouseDownEvent event) {
        if (!isSelected()) {
            return;
        }

        start[0] = getRelativeX(event.getX()) - 1;
        start[1] = getRelativeY(event.getY()) + 1;

        end[0] = start[0];
        end[1] = start[1];
    }


    /**
     * A mouse move event on the specified area will set the end point for the
     * zoombox. If the end point differs from the start point, an opaque box is
     * displayed.
     *
     * @param event The mouse move event which contains the xy coordinates of
     * the observed area.
     */
    public void onMouseMove(MouseMoveEvent event) {
        if (!isSelected() || !isZooming()) {
            return;
        }

        int x = getRelativeX(event.getX());
        int y = getRelativeY(event.getY());

        end[0] = x > start[0] ? x-1 : x+1;
        end[1] = y > start[1] ? y-1 : y+1;

        positionZoombox();
    }


    /**
     * The mouse up event finalizes the zoom operation. It sets the end point
     * for this operation, clears the zoombox and fires a ZoomEvent.
     *
     * @param event The mouse up event which contains the xy coordinates of the
     * observed area.
     */
    public void onMouseUp(MouseUpEvent event) {
        if (!isSelected()) {
            return;
        }

        end[0] = getRelativeX(event.getX());
        end[1] = getRelativeY(event.getY());

        fireZoomEvent();

        reset();
    }


    /**
     * The mouse out event is used to cancel an active zoom operation.
     *
     * @param event The mouse out event.
     */
    public void onMouseOut(MouseOutEvent event) {
        if (!isSelected() || !isMouseOut(event.getX(), event.getY())) {
            return;
        }

        reset();
    }


    /**
     * Returns the chart panel.
     *
     * @return the chart panel.
     */
    protected Canvas getChartPanel() {
        return chartTab.getChartPanel();
    }


    /**
     * This method is required to check manually if the mouse pointer really
     * moves out the chart area. The MouseOutEvent is also fired if the mouse
     * goes down which doesn't seem to be correct. So, we gonna check this
     * manually.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     *
     * @return true, if the mouse is really out of the chart area, otherwise
     * false.
     */
    protected boolean isMouseOut(int x, int y) {
        Canvas chart = getChartPanel();

        int left   = chart.getPageLeft();
        int right  = chart.getPageRight();
        int top    = chart.getPageTop();
        int bottom = chart.getPageBottom();

        if (x <= left || x >= right || y <= top || y >= bottom) {
            return true;
        }

        return false;
    }


    /**
     * Returns true, if a zoom action is in process.
     *
     * @return true, if a zoom action is in process.
     */
    public boolean isZooming() {
        return start[0] > 0 && start[1] > 0;
    }


    /**
     * Returns the X coordinate relative to the left border.
     *
     * @param x The X coordinate relative to the window.
     *
     * @return the X coordinate relative to the left border.
     */
    protected int getRelativeX(int x) {
        return x - chartTab.getChartPanel().getPageLeft();
    }


    /**
     * Returns the Y coordinate relative to the top border.
     *
     * @param y The Y coordinate relative to the window.
     *
     * @return the Y coordinate relative to the top border.
     */
    protected int getRelativeY(int y) {
        return y - chartTab.getChartPanel().getPageTop();
    }


    /**
     * Returns min and max x/y values based on the stored values in <i>start</i>
     * and <i>end</i>.
     *
     * @return an int[] as follows: [xmin, ymin, xmax, ymax].
     */
    protected int[] orderPositions() {
        int xmin = start[0] < end[0] ? start[0] : end[0];
        int ymin = start[1] < end[1] ? start[1] : end[1];

        int xmax = start[0] >= end[0] ? start[0] : end[0];
        int ymax = start[1] >= end[1] ? start[1] : end[1];

        return new int[] { xmin, ymin, xmax, ymax };
    }


    /**
     * Sets the width, height, x and y values of the zoombox.
     */
    protected void positionZoombox() {
        int[] values = orderPositions();

        zoombox.setLeft(values[0]);
        zoombox.setTop(values[1]);
        zoombox.setWidth(values[2] - values[0]);
        zoombox.setHeight(values[3] - values[1]);
    }


    /**
     * Clears the zoombox (set position and size to null).
     */
    protected void clearZoombox() {
        zoombox.setLeft(-10000);
        zoombox.setTop(-10000);
        zoombox.setWidth(1);
        zoombox.setHeight(1);
    }


    /**
     * Resets the zoom control (start point and zoombox).
     */
    protected void reset() {
        start[0] = -1;
        start[1] = -1;

        clearZoombox();
    }


    /**
     * Fires a ZoomEvent to all registered listeners.
     */
    protected void fireZoomEvent() {
        int[] pos = orderPositions();

        ZoomEvent event = new ZoomEvent(pos[0], pos[1], pos[2], pos[3]);

        for (ZoomHandler handler: handlers) {
            handler.onZoom(event);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
