/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import java.util.ArrayList;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.events.MouseMoveEvent;
import com.smartgwt.client.widgets.events.MouseMoveHandler;

import org.dive4elements.river.client.shared.Transform2D;


/**
 * Panel showing the mouse position in data space.
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MousePositionPanel extends HLayout implements MouseMoveHandler {

    /** Is associated to a ChartTab. */
    protected ChartOutputTab chartTab;

    protected HLayout xLayout;
    protected ArrayList<HLayout> yLayouts;


    public MousePositionPanel(ChartOutputTab chartTab) {
        super();

        this.chartTab = chartTab;

        chartTab.getChartPanel().addMouseMoveHandler(this);

        initLayout();
    }


    /**
     * Initializes the layout of this component. <b>Note:</b> This layout has a
     * fixed width of 195px plus a margin of 5px.
     */
    protected void initLayout() {
        setMembersMargin(5);

        xLayout = null;
        yLayouts = new ArrayList<HLayout>();
    }


    /**
     * Listens to mouse move events to refresh the xy position.
     *
     * @param event The move event.
     */
    public void onMouseMove(MouseMoveEvent event) {
        updateMousePosition(event.getX(), event.getY());
    }


    /**
     * This method takes pixel coordinates, transforms those values into chart
     * coordinates using the Transform2D class and updates the mouse position.
     *
     * @param x The x part of the pixel.
     * @param y The y part of the pixel.
     */
    public void updateMousePosition(double x, double y) {
        int transformerCount = chartTab.getTransformerCount();

        Canvas chart = chartTab.getChartPanel();
        int xOffset = chart.getPageLeft();
        int yOffset = chart.getPageTop();

        x = x - xOffset;
        y = y - yOffset;

        // Create Layout for x coordinates.
        if (xLayout == null){
            Label xDesc  = new Label("Position: X = ");
            Label xLabel = new Label();
            xLayout      = new HLayout();
            xLayout.setWidth(125);
            xLayout.addMember(xDesc);
            xLayout.addMember(xLabel);
            xDesc.setWidth(70);
            xLabel.setWidth(55);
            addMember(xLayout);
        }

        for (int i = 0; i < transformerCount; i++) {
            HLayout yLayout = null;
            // If no layout exists for this y axis, create one.
            // else use the existing one.
            if (yLayouts.size() <= i) {
                Label yDesc     = new Label("Y" + (i+1) + " = ");
                Label yLabel    = new Label();
                yLayout = new HLayout();
                yLayout.setWidth(80);
                yLayout.addMember(yDesc);
                yLayout.addMember(yLabel);
                yDesc.setWidth(30);
                yLabel.setWidth(50);
                addMember(yLayout);
                yLayouts.add(i, yLayout);
            }
            else {
                yLayout = yLayouts.get(i);
            }

            Transform2D transformer = chartTab.getTransformer(i);

            if (transformer == null) {
                return;
            }

            // Get the label for the coordinates.
            Canvas xLabel = xLayout.getMember(1);
            Canvas yLabel = yLayout.getMember(1);

            double[] xy    = transformer.transform(x, y);
            String[] xyStr = transformer.format(new Number[] {
                new Double(xy[0]), new Double(xy[1]) });
            // Set the coordinates.
            xLabel.setContents(xyStr[0]);
            yLabel.setContents(xyStr[1]);
        }

        // Remove y coordinates.
        if (yLayouts.size() > transformerCount) {
            for (int i = yLayouts.size() - 1; i >= transformerCount; i--) {
                removeMember(yLayouts.get(i));
                yLayouts.remove(i);
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
