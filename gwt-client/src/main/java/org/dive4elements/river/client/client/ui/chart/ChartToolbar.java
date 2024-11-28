/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.ZoomEvent;
import org.dive4elements.river.client.client.event.ZoomHandler;
import org.dive4elements.river.client.client.ui.ImgLink;
import org.dive4elements.river.client.client.ui.Toolbar;


/**
 * Toolbar with buttons/icons to open datacage, switch to zoom mode, zoom out
 * etc.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartToolbar extends Toolbar implements ZoomHandler {

    protected static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    public static final int PANEL_HEIGHT = 35;

    protected Button manageThemes;
    protected Button datacage;
    protected ImgLink downloadPNG;
    protected ImgLink downloadPDF;
    protected ImgLink downloadSVG;
    protected ImgLink downloadCSV;
    protected MousePositionPanel position;
    protected ZoomboxControl zoombox;
    protected ImgButton zoomToMaxExtent;
    protected ImgButton historyBack;
    protected ImgButton zoomOut;
    protected ImgButton chartProperties;
    protected Button addPoints;
    protected Button addWSP;
    protected ImgLink exportAT;
    protected PanControl panControl;


    /** @param chartTab Output-Tab on which this toolbar is located. */
    public ChartToolbar(ChartOutputTab chartTab) {
        super(chartTab);
        initTools();
    }


    protected void initTools() {
        ChartOutputTab chartTab = getChartOutputTab();

        manageThemes    = new Button(MSG.manageThemes());
        datacage        = new Button(MSG.databasket());
        position        = new MousePositionPanel(chartTab);
        zoombox         = new ZoomboxControl(chartTab, MSG.zoom_in());
        zoomToMaxExtent = new ImgButton();
        zoomOut         = new ImgButton();
        historyBack     = new ImgButton();
        panControl      = new PanControl(chartTab, MSG.pan());
        chartProperties = new ImgButton();
        addPoints       = new Button(MSG.points());

        if (chartTab.getMode().getName().equals("cross_section")) {
            addWSP = new Button(MSG.addWSPButton());
            addWSP.setTooltip(MSG.addWSPTooltip());
            final ChartOutputTab finalChartTab = chartTab;
            addWSP.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent ce) {
                    new ManualWSPEditor(
                        finalChartTab.getView().getCollection(),
                        finalChartTab,
                        finalChartTab.getMode().getName()).show();
                    }});
        }

        addPoints.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPointWindow();
            }
        });
        addPoints.setTooltip(MSG.addPointsTooltip());

        manageThemes.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                getChartOutputTab().toggleThemePanel();
            }
        });

        datacage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GWT.log("Clicked 'datacage' button.");
                openDatacageWindow((ChartOutputTab) getOutputTab());
            }
        });

        String baseUrl = GWT.getHostPageBaseURL();
        String moduleUrl = GWT.getModuleBaseURL();
        Config config = Config.getInstance();

        if (chartTab.getMode().getName().equals("fix_wq_curve")) {
            exportAT = new ImgLink(
                baseUrl + MSG.downloadAT(),
                moduleUrl + "export" +
                   "?uuid=" + chartTab.getCollection().identifier() +
                   "&name=" + chartTab.getMode().getName() +
                   "&mode=" + chartTab.getMode().getName() + "_at_export" +
                   "&type=at" +
                   "&server=" + config.getServerUrl() +
                   "&locale=" + config.getLocale() +
                   "&km=" + chartTab.getCollectionView().getCurrentKm(),
                20,
                20
            );
            exportAT.setTooltip(MSG.exportATTooltip());
        }

        downloadPNG = new ImgLink(
            baseUrl + MSG.downloadPNG(),
            chartTab.getExportUrl(-1, -1, "png"),
            20,
            20);
        downloadPNG.setTooltip(MSG.downloadPNGTooltip());

        downloadPDF = new ImgLink(
            baseUrl + MSG.downloadPDF(),
            chartTab.getExportUrl(1280, 1024, "pdf"),
            20,
            20);
        downloadPDF.setTooltip(MSG.downloadPDFTooltip());

        downloadSVG = new ImgLink(
            baseUrl + MSG.downloadSVG(),
            chartTab.getExportUrl(1280, 1024, "svg"),
            20,
            20);
        downloadSVG.setTooltip(MSG.downloadSVGTooltip());

        downloadCSV = new ImgLink(
            baseUrl + MSG.downloadCSV(),
            chartTab.getExportUrl(-1, -1, "csv", "windows-1252"),
            20,
            20);
        downloadCSV.setTooltip(MSG.downloadCSVTooltip());

        zoomToMaxExtent.setSrc(baseUrl + MSG.zoom_all());
        adjustImageButton(zoomToMaxExtent);
        zoomToMaxExtent.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getChartOutputTab().resetRanges();
                // Relink the export buttons.
                onZoom(null);
            }
        });
        zoomToMaxExtent.setTooltip(MSG.zoomToMaxExtentTooltip());

        zoomOut.setSrc(baseUrl + MSG.zoom_out());
        adjustImageButton(zoomOut);
        zoomOut.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getChartOutputTab().zoomOut(10);
                // Relink the export buttons.
                onZoom(null);
            }
        });
        zoomOut.setTooltip(MSG.zoomOutTooltip());

        historyBack.setSrc(baseUrl + MSG.zoom_back());
        adjustImageButton(historyBack);
        historyBack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getChartOutputTab().zoomOut();
                // Relink the export buttons.
                onZoom(null);
            }
        });
        historyBack.setTooltip(MSG.historyBackTooltip());

        zoombox.addZoomHandler(chartTab);
        zoombox.addZoomHandler(this);
        zoombox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panControl.deselect();
            }
        });
        zoombox.setTooltip(MSG.zoomboxTooltip());

        panControl.addPanHandler(chartTab);
        panControl.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                zoombox.deselect();
            }
        });
        panControl.setTooltip(MSG.panControlTooltip());

        chartProperties.setSrc(baseUrl + MSG.properties_ico());
        adjustImageButton(chartProperties);
        chartProperties.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openPropertiesEditor();
            }
        });
        chartProperties.setTooltip(MSG.chartPropertiesTooltip());

        initLayout();
    }

    /** Set width, height and other properties of an imagebutton. */
    public void adjustImageButton(ImgButton imgButton) {
        imgButton.setWidth(20);
        imgButton.setHeight(20);
        imgButton.setShowDown(false);
        imgButton.setShowRollOver(false);
        imgButton.setShowRollOverIcon(false);
        imgButton.setShowDisabled(false);
        imgButton.setShowDisabledIcon(true);
        imgButton.setShowDownIcon(false);
        imgButton.setShowFocusedIcon(false);
    }


    protected ChartOutputTab getChartOutputTab() {
        return (ChartOutputTab)getOutputTab();
    }


    protected void initLayout() {
        setWidth100();
        setHeight(PANEL_HEIGHT);
        setMembersMargin(10);
        setPadding(5);
        setBorder("1px solid black");

        Label spacer = new Label();
        spacer.setWidth("*");
        datacage.setWidth("95px");
        position.setWidth("200px");

        addMember(manageThemes);
        addMember(datacage);
        addMember(downloadPNG);
        addMember(downloadPDF);
        addMember(downloadSVG);
        addMember(downloadCSV);
        if (getChartOutputTab().getMode().getName().equals("fix_wq_curve")) {
            addMember(exportAT);
        }
        addMember(zoomToMaxExtent);
        addMember(historyBack);
        addMember(zoomOut);
        addMember(zoombox);
        addMember(panControl);
        addMember(chartProperties);
        addMember(addPoints);

        if (getChartOutputTab().getMode().getName().equals("cross_section")) {
            addMember(addWSP);
        }

        addMember(spacer);
        addMember(position);
    }

    /**
     * Open the chart property editor dialog.
     */
    protected void openPropertiesEditor() {
        ChartPropertiesEditor editor =
            new ChartPropertiesEditor(getChartOutputTab());
        editor.show();
    }


    /** Open editor for custom points. */
    protected void openPointWindow() {
        ChartOutputTab chartTab = getChartOutputTab();
        if (chartTab.getMode().getName().equals("historical_discharge")) {
            new ManualDatePointsEditor(chartTab.getView().getCollection(),
                chartTab, chartTab.getMode().getName()).show();
        }
        else {
            new ManualPointsEditor(chartTab.getView().getCollection(),
                chartTab, chartTab.getMode().getName()).show();
        }
    }


    /**
     * Sets new sources to the export button/images, such that the
     * correct zoom values are included in the request when clicked.
     * @param evt ignored.
     */
    @Override
    public void onZoom(ZoomEvent evt) {
        ChartOutputTab chartTab = getChartOutputTab();
        downloadPNG.setSource(chartTab.getExportUrl(-1, -1, "png"));
        downloadPDF.setSource(chartTab.getExportUrl(-1, -1, "pdf"));
        downloadSVG.setSource(chartTab.getExportUrl(-1, -1, "svg"));
    }

    public void deselectControls() {
        zoombox.deselect();
    }

    public void updateLinks() {
        ChartOutputTab chartTab = getChartOutputTab();
        String moduleUrl = GWT.getModuleBaseURL();
        Config config = Config.getInstance();

        // make sure that the current km is set correctly
        // for the other buttons this is handled by onZoom
        downloadCSV.setSource(
                chartTab.getExportUrl(-1, -1, "csv", "windows-1252"));

        if (chartTab.getMode().getName().equals("fix_wq_curve")) {
            exportAT.setSource(
                   moduleUrl + "export" +
                   "?uuid=" + chartTab.getCollection().identifier() +
                   "&name=" + chartTab.getMode().getName() +
                   "&mode=" + chartTab.getMode().getName() + "_at_export" +
                   "&type=at" +
                   "&server=" + config.getServerUrl() +
                   "&locale=" + config.getLocale() +
                   "&km=" + chartTab.getCollectionView().getCurrentKm());
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
