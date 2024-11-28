/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import java.util.Map;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.Alignment;

import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;

import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;

import com.smartgwt.client.widgets.form.DynamicForm;

import com.smartgwt.client.widgets.form.fields.TextItem;

import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import org.dive4elements.river.client.client.Config;

import org.dive4elements.river.client.client.ui.CollectionView;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.FixAnalysisArtifact;
import org.dive4elements.river.client.shared.model.FixFilter;
import org.dive4elements.river.client.shared.model.OutputMode;

import java.util.Date;


/**
 * Tab representing and showing one Chart-output with a "navi" thing.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class NaviChartOutputTab
extends      ChartOutputTab
implements   TabSelectedHandler
{
    protected TextItem currentkm;

    public NaviChartOutputTab(
        String         title,
        Collection     collection,
        OutputMode     mode,
        CollectionView collectionView
    ){
        super(title, collection, mode, collectionView);
        right.removeChild(chart);
        right.addChild(createNaviChart());
        collectionView.registerTabHandler(this);
    }


    protected Canvas createNaviChart() {
        final Artifact art = collectionView.getArtifact();
        VLayout root = new VLayout();
        root.setWidth100();
        root.setHeight100();

        HLayout layout = new HLayout();
        layout.setAlign(Alignment.CENTER);

        DynamicForm form = new DynamicForm();
        Button lower = new Button("<<");
        lower.setWidth(30);
        Button upper = new Button(">>");
        upper.setWidth(30);
        currentkm = new TextItem();
        currentkm.setWidth(60);
        currentkm.setShowTitle(false);

        form.setFields(currentkm);
        form.setWidth(60);

        double fromKm;
        double toKm;

        if (art instanceof FixAnalysisArtifact) {
            FixAnalysisArtifact fix = (FixAnalysisArtifact) art;
            FixFilter fixFilter = fix.getFilter();
            String s = fix.getArtifactDescription().getDataValueAsString(
                "ld_step");
            try {
                double ds = Double.parseDouble(s);
                collectionView.setSteps(ds);
            }
            catch(NumberFormatException nfe) {
                collectionView.setSteps(100d);
            }
            fromKm = fixFilter.getFromKm();
            toKm   = fixFilter.getToKm();
        }
        else {
            // Probably WINFOArtifact kind of artifact.
            String ld_step =
                    art.getArtifactDescription().getDataValueAsString(
                        "ld_step");
            try {
                collectionView.setSteps(Double.valueOf(ld_step));
            }
            catch (Exception e) {
                GWT.log("No ld_steps data or not parsable.");
                return root;
            }

            double[] kmRange = art.getArtifactDescription().getKMRange();
            if (kmRange == null || kmRange.length == 2) {
                fromKm = kmRange[0];
                toKm   = kmRange[1];
            }
            else {
                GWT.log("No KM range in description found.");
                return root;
            }
        }

        collectionView.setMinKm(fromKm);
        collectionView.setMaxKm(toKm);

        final NumberFormat nf = NumberFormat.getDecimalFormat();

        // Always jump to the from km when initialized.
        try {
            double d = Double.valueOf(fromKm);
            currentkm.setValue(nf.format(d));
        } catch (NumberFormatException e) {
            currentkm.setValue(fromKm);
        }
        collectionView.setCurrentKm(fromKm);

        lower.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent ce) {
                tbarPanel.deselectControls();
                updateChartDown();
                try {
                    double d = Double.valueOf(collectionView.getCurrentKm());
                    currentkm.setValue(nf.format(d));
                    tbarPanel.onZoom(null);
                } catch (NumberFormatException e) {
                    currentkm.setValue(collectionView.getCurrentKm());
                }
            }
        });

        upper.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent ce) {
                tbarPanel.deselectControls();
                updateChartUp();
                try {
                    double d = Double.valueOf(collectionView.getCurrentKm());
                    currentkm.setValue(nf.format(d));
                    tbarPanel.onZoom(null);
                } catch (NumberFormatException e) {
                    currentkm.setValue(collectionView.getCurrentKm());
                }
            }
        });

        currentkm.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent kpe) {
                if (!kpe.getKeyName().equals("Enter")) {
                    return;
                }
                if(kpe.getItem().getValue() != null) {
                    tbarPanel.deselectControls();
                    try {
                        String s = kpe.getItem().getValue().toString();
                        double d;
                        try {
                            d = nf.parse(s);
                            currentkm.setValue(nf.format(d));
                        } catch (NumberFormatException e) {
                            d = -1d;
                        }
                        if (d <= collectionView.getMaxKm() &&
                            d >= collectionView.getMinKm()) {
                            collectionView.setCurrentKm(d);
                            tbarPanel.updateLinks();
                            tbarPanel.onZoom(null);
                            if (right != null) {
                                updateChartPanel();
                                updateChartInfo();
                            }
                        }
                    }
                    catch(NumberFormatException nfe) {
                        // Do nothing.
                    }
                }
            }
        });
        layout.addMember(lower);
        layout.addMember(form);
        layout.addMember(upper);

        root.addMember(chart);
        root.addMember(layout);
        return root;
    }


    /**
     * Callback when km-up-button is clicked.
     * Increases collectionViews KM and refreshes view.
     */
    protected void updateChartUp() {
        double currentKm = collectionView.getCurrentKm();
        if (currentKm < collectionView.getMaxKm()) {
            // Why this math?
            double newVal = currentKm * 100;
            newVal += (collectionView.getSteps() / 10);
            collectionView.setCurrentKm((double)Math.round(newVal) / 100);
            tbarPanel.updateLinks();
            updateChartPanel();
            updateChartInfo();
        }
    }

    /**
     * Callback when km-down-button is clicked.
     * Decreases collectionViews KM and refreshes view.
     */
    protected void updateChartDown() {
        double currentKm = collectionView.getCurrentKm();
        if (currentKm > collectionView.getMinKm()) {
            // Why this math?
            double newVal = currentKm * 100;
            newVal -= (collectionView.getSteps() / 10);
            collectionView.setCurrentKm((double)Math.round(newVal) / 100);
            tbarPanel.updateLinks();
            updateChartPanel();
            updateChartInfo();
        }

    }

   /**
     * Returns the existing chart panel.
     *
     * @return the existing chart panel.
     */
    @Override
    public Canvas getChartPanel() {
        return chart;
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
    @Override
    protected String getImgUrl(int width, int height) {
        Config config = Config.getInstance();

        String imgUrl = GWT.getModuleBaseURL();
        imgUrl += "chart";
        imgUrl += "?uuid=" + collection.identifier();
        imgUrl += "&type=" + mode.getName();
        imgUrl += "&locale=" + config.getLocale();
        imgUrl += "&timestamp=" + new Date().getTime();
        imgUrl += "&width=" + Integer.toString(width);
        imgUrl += "&height=" + Integer.toString(height - 40);

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

        if (collectionView.getArtifact() instanceof FixAnalysisArtifact) {
            if (collectionView.getCurrentKm() == -1) {
                FixAnalysisArtifact fix =
                    (FixAnalysisArtifact) collectionView.getArtifact();
                collectionView.setCurrentKm(fix.getFilter().getFromKm());
            }
        }
        else if (collectionView.getCurrentKm() == -1) {
            collectionView.setCurrentKm(
                collectionView.getArtifact().getArtifactDescription()
                .getKMRange()[0]);
        }
        if (collectionView.getCurrentKm() != -1) {
            imgUrl += "&currentKm=" + collectionView.getCurrentKm();
        }

        return imgUrl;
    }

    public void onTabSelected(TabSelectedEvent tse) {
        if (this.equals(tse.getTab())) {
            updateChartPanel();
            updateChartInfo();
            currentkm.setValue(collectionView.getCurrentKm());
        }
    }

    @Override
    public Map<String, String> getChartAttributes() {
        Map<String, String> attr = new HashMap<String, String>();

        attr = super.getChartAttributes();
        attr.put("km", String.valueOf(collectionView.getCurrentKm()));

        return attr;
    }

    /** In contrast to supers implementation, include the currently selected
     * km in the url. */
    @Override
    public String getExportUrl(int width, int height, String format) {
        String url = super.getExportUrl(width, height, format);
        if (collectionView.getCurrentKm() != -1) {
            url += "&currentKm=" + collectionView.getCurrentKm();
        }
        return url;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
