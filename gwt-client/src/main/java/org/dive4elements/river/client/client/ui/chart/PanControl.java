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

import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.MouseDownEvent;
import com.smartgwt.client.widgets.events.MouseDownHandler;
import com.smartgwt.client.widgets.events.MouseMoveEvent;
import com.smartgwt.client.widgets.events.MouseMoveHandler;
import com.smartgwt.client.widgets.events.MouseOutEvent;
import com.smartgwt.client.widgets.events.MouseOutHandler;
import com.smartgwt.client.widgets.events.MouseUpEvent;
import com.smartgwt.client.widgets.events.MouseUpHandler;

import org.dive4elements.river.client.client.event.HasPanHandlers;
import org.dive4elements.river.client.client.event.PanEvent;
import org.dive4elements.river.client.client.event.PanHandler;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class PanControl
extends      ImgButton
implements   MouseDownHandler, MouseMoveHandler, MouseUpHandler,
             MouseOutHandler, HasPanHandlers
{
    protected ChartOutputTab chartTab;

    protected List<PanHandler> handlers;

    protected int[] start;
    protected int[] end;


    public PanControl(ChartOutputTab chartTab, String imageUrl) {
        super();

        this.chartTab = chartTab;
        this.handlers = new ArrayList<PanHandler>();
        this.start    = new int[] { -1, -1 };
        this.end      = new int[] { -1, -1 };

        String baseUrl = GWT.getHostPageBaseURL();
        setSrc(baseUrl + imageUrl);
        setActionType(SelectionType.CHECKBOX);
        setSize(20);
        setShowRollOver(false);
        setSelected(false);

        chartTab.getChartPanel().addMouseDownHandler(this);
        chartTab.getChartPanel().addMouseMoveHandler(this);
        chartTab.getChartPanel().addMouseUpHandler(this);
        chartTab.getChartPanel().addMouseOutHandler(this);
    }


    /**
     * Method used to register a new PanHandler.
     *
     * @param handler A new PanHandler.
     */
    public void addPanHandler(PanHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    /**
     * This event starts the dragging operation if the control is activated.
     *
     * @param event The mouse down event which contains the start coordinates.
     */
    public void onMouseDown(MouseDownEvent event) {
        if (!isSelected()) {
            return;
        }

        start[0] = event.getX();
        start[1] = event.getY();

        end[0] = start[0];
        end[1] = start[1];
    }


    /**
     * This event is used to reposition the chart image based on the current
     * drag operation.
     *
     * @param event The move event which contains the new coordinates to update
     * the chart image position.
     */
    public void onMouseMove(MouseMoveEvent event) {
        if (!isSelected() || start[0] == -1 || start[1] == -1) {
            return;
        }

        int x = event.getX() - end[0];
        int y = event.getY() - end[1];

        end[0] = end[0] + x;
        end[1] = end[1] + y;

        Canvas c = chartTab.getChartImg();
        c.moveBy(x, y);
    }


    /**
     * This event stops the dragging operation and fires a DragEnd event to the
     * registered listeners.
     *
     * @param event The mouse up event which contains the end coordinates.
     */
    public void onMouseUp(MouseUpEvent event) {
        if (!isSelected()) {
            return;
        }

        end[0] = event.getX();
        end[1] = event.getY();

        Canvas c = chartTab.getChartImg();
        c.setLeft(0);
        c.setTop(0);

        fireOnPan();

        start[0] = -1;
        start[1] = -1;
    }


    /**
     * This event is used to cancel the current dragging operation.
     *
     * @param event The mouse out event.
     */
    public void onMouseOut(MouseOutEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (!isSelected() || !isMouseOut(x, y) || start[0] == -1) {
            return;
        }

        Canvas c = chartTab.getChartImg();
        c.setLeft(0);
        c.setTop(0);

        fireOnPan();

        start[0] = -1;
        start[1] = -1;
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
        Canvas chart = chartTab.getChartImg();

        if (chart instanceof Img) {
            chart = chart.getParentElement();
        }

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
     * A pan event is fired to inform the registered listeners about a pan
     * operation has finished.
     */
    protected void fireOnPan() {
        PanEvent event = new PanEvent(start, end);

        for (PanHandler handler: handlers) {
            handler.onPan(event);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
