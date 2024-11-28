/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;

import org.dive4elements.river.client.client.event.OutputParameterChangeHandler;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.client.ui.ImgLink;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.OutputMode;
import org.dive4elements.river.client.shared.model.Theme;


public class OverviewOutputTab extends ChartOutputTab  {

    private class NoChartThemePanel extends ChartThemePanel {

        public NoChartThemePanel(OutputMode mode, CollectionView view) {
            super(mode, view);
        }

        @Override
        public void activateTheme(Theme theme, boolean active) { }

        @Override
        public void feedTellArea(
            final String artifact,
            Theme under,
            Theme over,
            boolean between
        ) { }

        @Override
        public void createAreaArtifact(
            final Theme   over,
            final Theme   under,
            final boolean between
        ) { }

        @Override
        public void addOutputParameterChangeHandler(
            OutputParameterChangeHandler h) { }

        @Override
        public void addRedrawRequestHandler(RedrawRequestHandler h){ }
    }



    private class MinimumChartToolbar extends ChartToolbar {

        public MinimumChartToolbar(ChartOutputTab tab) {
            super(tab);
        }

        @Override
        protected void initTools() {
            GWT.log("CREATE NEW MINIMALISTIC CHART TOOLBAR");
            ChartOutputTab chartTab = getChartOutputTab();

            String baseUrl = GWT.getHostPageBaseURL();

            downloadPNG = new ImgLink(
                baseUrl + MSG.downloadPNG(),
                chartTab.getExportUrl(-1, -1, "png"),
                20,
                20);
            downloadPNG.setTooltip(MSG.downloadPNGTooltip());

            initLayout();
        }


        @Override
        protected void initLayout() {
            setWidth100();
            setHeight(PANEL_HEIGHT);
            setMembersMargin(10);
            setPadding(5);
            setBorder("1px solid black");

            addMember(downloadPNG);
        }
    }



    public OverviewOutputTab(
        String         title,
        Collection     collection,
        OutputMode     mode,
        CollectionView collectionView
        ){
        super(title, collection, mode, collectionView);
        left.setVisible(false);
    }


    @Override
    public ChartThemePanel createThemePanel(
        OutputMode mode, CollectionView view
        ) {
        return new NoChartThemePanel(mode, view);
    }

    @Override
    public ChartToolbar createChartToolbar(ChartOutputTab tab) {
        return new MinimumChartToolbar(tab);
    }
}
