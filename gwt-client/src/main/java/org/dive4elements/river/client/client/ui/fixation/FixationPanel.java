/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.fixation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.FixingsOverviewService;
import org.dive4elements.river.client.client.services.FixingsOverviewServiceAsync;
import org.dive4elements.river.client.client.ui.AbstractUIProvider;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.FixAnalysisArtifact;
import org.dive4elements.river.client.shared.model.FixFilter;
import org.dive4elements.river.client.shared.model.FixingsOverviewInfo;

import java.util.Date;
import java.util.HashMap;


/**
 * This UIProvider creates helper panel for fixation analysis without input
 * elements.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public abstract class FixationPanel
extends               AbstractUIProvider
implements            ResizedHandler
{
    private static final long serialVersionUID = -3667553404493415619L;

    protected static HashMap<String, FixationPanel> instances =
        new HashMap<String, FixationPanel>();

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected FixingsOverviewServiceAsync overviewService =
        GWT.create(FixingsOverviewService.class);

    protected String htmlOverview;
    protected FixingsOverviewInfo fixInfo;
    protected TabSet tabs;
    protected Tab events;
    protected Tab chart;
    protected VLayout chartContainer;
    protected Img chartImg;
    protected TextItem kmText;

    public static final DateTimeFormat DTF = DateTimeFormat.getFormat(
        "dd.MM.yyyy");


    public FixationPanel() {
        chartImg = new Img();
        htmlOverview = "";
    }


    /** Get the (master) artifact UUID. */
    protected String getArtifactUuid() {
        return this.artifact.getUuid();
    }

    protected void init() {
    }

    @Override
    public Data[] getData() {
        return null;
    }

    @Override
    public Canvas create(DataList list) {
        VLayout layout = new VLayout();

        Canvas helper = createHelper();
        this.helperContainer.addMember(helper);

        Canvas submit = getNextButton();
        Canvas widget = createWidget(list);

        layout.addMember(widget);
        layout.addMember(submit);
        return layout;
    }

    @Override
    public Canvas createOld(DataList list) {
        return new DynamicForm();
    }

    protected Canvas createHelper() {
        Config config    = Config.getInstance();
        String locale    = config.getLocale ();

        tabs = new TabSet();
        events = new Tab(MESSAGES.events());
        chart = new Tab(MESSAGES.kmchart());

        chartContainer = new VLayout();
        Canvas scroll = createChartHelper();

        VLayout layout = new VLayout();
        layout.addResizedHandler(this);
        layout.addMember(chartContainer);
        layout.addMember(scroll);
        layout.setAlign(Alignment.CENTER);
        chart.setPane(layout);

        final HTMLPane eventPane = new HTMLPane();

        String river = artifact.getArtifactDescription().getRiver();
        createCallback();

        String callBack = "fixationCallback(this.checked, this.name)";

        if (this.artifact instanceof FixAnalysisArtifact == false)
            return chartContainer;

        FixAnalysisArtifact art = (FixAnalysisArtifact) this.artifact;

        overviewService.generateOverview(
            locale,
            artifact.getUuid(),
            getOverviewFilter(art.getFilter()),
            renderCheckboxes(),
            callBack,
            new AsyncCallback<FixingsOverviewInfo>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not receive overview.");
                    SC.warn(caught.getMessage());
                }
                @Override
                public void onSuccess(FixingsOverviewInfo info) {
                    GWT.log("Successfully loaded overview.");
                    fixInfo = info;
                    htmlOverview = info.getHTML();
                    FixAnalysisArtifact art = (FixAnalysisArtifact)artifact;
                    FixFilter filter = art.getFilter();
                    filter.setRiver(info.getRiver());
                    if (filter.getCurrentKm() == -Double.MAX_VALUE ||
                        filter.getCurrentKm() == -1d) {
                        filter.setCurrentKm(info.getFrom());
                        filter.setToKm(info.getTo());
                    }
                    if (kmText != null) {
                        NumberFormat nf = NumberFormat.getDecimalFormat();
                        try {
                            double d = Double.valueOf(filter.getCurrentKm());
                            kmText.setValue(nf.format(d));
                        } catch (NumberFormatException e) {
                            kmText.setValue(filter.getCurrentKm());
                        }
                    }
                    eventPane.setContents(htmlOverview);
                    updateChartTab(fixInfo.getFrom());
                    events.setPane(eventPane);
                    success();
                }
            });

        tabs.addTab(events);
        tabs.addTab(chart);

        return tabs;
    }


    protected Canvas createChartHelper() {
        DynamicForm form = new DynamicForm();
        Button lower = new Button("<<");
        lower.setWidth(30);
        Button upper = new Button(">>");
        upper.setWidth(30);
        kmText = new TextItem();
        kmText.setWidth(60);
        kmText.setShowTitle(false);


        form.setFields(kmText);
        form.setWidth(60);
        lower.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                FixFilter filter = updateChartTabLow();
                NumberFormat nf = NumberFormat.getDecimalFormat();
                try {
                    double d = Double.valueOf(filter.getCurrentKm());
                    kmText.setValue(nf.format(d));
                } catch (NumberFormatException e) {
                    kmText.setValue(filter.getCurrentKm());
                }
            }
        });

        upper.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                FixFilter filter = updateChartTabUp();
                NumberFormat nf = NumberFormat.getDecimalFormat();
                try {
                    double d = Double.valueOf(filter.getCurrentKm());
                    kmText.setValue(nf.format(d));
                } catch (NumberFormatException e) {
                    kmText.setValue(filter.getCurrentKm());
                }
            }
        });

        kmText.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent ce) {
                //TODO: get current value.
                if(ce.getItem().getValue() != null) {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    try {
                        double d = nf.parse(ce.getItem().getValue().toString());
                        updateChartTab(d);
                    }
                    catch(NumberFormatException nfe) {
                        // Do nothing.
                    }
                }
            }
        });

        HLayout layout = new HLayout();
        layout.setAlign(Alignment.CENTER);

        layout.addMember(lower);
        layout.addMember(form);
        layout.addMember(upper);
        return layout;
    }

    protected void updateChartTab(double km) {
        Config config    = Config.getInstance();
        String locale    = config.getLocale ();

        FixAnalysisArtifact art = (FixAnalysisArtifact) this.artifact;

        if (fixInfo != null) {
            if (km < fixInfo.getFrom()) km = fixInfo.getFrom();
            if (km > fixInfo.getTo())   km = fixInfo.getTo();
        }

        FixFilter filter = art.getFilter();

        if (km < filter.getFromKm()) km = filter.getFromKm();
        if (km > filter.getToKm())   km = filter.getToKm();

        filter.setCurrentKm(km);

        int hWidth = helperContainer.getWidth() - 12;
        int hHeight = helperContainer.getHeight() - 62;

        if ((int)(hHeight *4f/3) < hWidth) {
            hWidth = (int)(hHeight * 4f/3);
        }
        else {
            hHeight = (int)(hWidth *3f/4);
        }

        String imgUrl = URL.encode(GWT.getModuleBaseURL()
            + "fixings-km-chart"
            + "?locale=" + locale
            + "&filter=" + getChartFilter(filter, hWidth, hHeight));

        if (chartContainer.hasMember(chartImg)) {
            chartImg.setWidth(hWidth);
            chartImg.setHeight(hHeight);
            chartImg.setSrc(imgUrl);
        }
        else {
            chartImg = new Img(imgUrl, hWidth, hHeight);
            chartContainer.addMember(chartImg);
        }
    }


    protected FixFilter updateChartTabLow() {
        FixAnalysisArtifact art = (FixAnalysisArtifact) this.artifact;

        FixFilter filter = art.getFilter();

        double curr = filter.getCurrentKm();
        if (curr > filter.getFromKm()) {
            double newVal = (curr - 0.1) * 10;
            long round = Math.round(newVal);
            updateChartTab(((double)round) / 10);
        }
        return filter;
    }


    protected FixFilter updateChartTabUp() {
        FixAnalysisArtifact art = (FixAnalysisArtifact) this.artifact;

        FixFilter filter = art.getFilter();

        double curr = filter.getCurrentKm();
        if (curr < filter.getToKm()) {
            double newVal = (curr + 0.1) * 10;
            long round = Math.round(newVal);
            updateChartTab(((double)round) / 10);
        }
        return filter;
    }


    @Override
    public void onResized(ResizedEvent re) {
        FixAnalysisArtifact art = (FixAnalysisArtifact) this.artifact;

        updateChartTab(art.getFilter().getCurrentKm());
    }


    private native void createCallback() /*-{
        $wnd.fixationCallback = @org.dive4elements.river.client.client.ui.fixation.FixationPanel::helperCallback(ZLjava/lang/String;);
    }-*/;

    private static void helperCallback(boolean checked, String name) {
        String[] parts = name.split(":");
        String uuid = parts[0];
        String cid = parts[1];

        FixationPanel p = FixationPanel.getInstance(uuid);
        if (p != null) {
            p.setValues(cid, checked);
        }
    }

    private static FixationPanel getInstance(String uuid) {
        return instances.get(uuid);
    }

    public abstract Canvas createWidget(DataList data);
    public abstract void setValues(String cid, boolean checked);
    public abstract boolean renderCheckboxes();
    public abstract void success();


    /** Creates JSON string from filter. */
    public static String getOverviewFilter(FixFilter filter) {
        String river = filter.getRiver();

        if (river != null && river.length() > 0) {
            JSONObject jfix = new JSONObject();
            JSONObject jfilter = new JSONObject();
            JSONObject jrName = new JSONObject();
            JSONString jrValue = new JSONString(river);
            jrName.put("name", jrValue);
            jfilter.put("river", jrName);
            jfix.put("fixings", createFilter(filter, jfilter));
            return jfix.toString();
        }
        return "";
    }

    public String getChartFilter(FixFilter filter, int width, int height) {
        String river     = filter.getRiver();
        double currentKm = filter.getCurrentKm();
        double fromKm    = filter.getFromKm();
        double toKm      = filter.getToKm();

        if (river != null && river.length() > 0 &&
            currentKm >= fromKm && currentKm <= toKm)
        {
            JSONObject jfix = new JSONObject();
            JSONObject jfilter = new JSONObject();
            JSONObject jrName = new JSONObject();
            JSONString jrValue = new JSONString(river);
            JSONObject jkm = new JSONObject();
            JSONNumber jkmValue = new JSONNumber(currentKm);
            JSONObject jextent = new JSONObject();
            JSONNumber jwidth = new JSONNumber(width);
            JSONNumber jheight = new JSONNumber(height);

            jkm.put("value", jkmValue);
            jrName.put("name", jrValue);
            jfilter.put("river", jrName);
            jfilter.put("km", jkm);
            jextent.put("width", jwidth);
            jextent.put("height", jheight);
            jfilter.put("extent", jextent);
            jfix.put("fixings", createFilter(filter, jfilter));
            return jfix.toString();
        }
        return "";
    }

    protected static JSONObject createFilter(
        FixFilter filter,
        JSONObject root
    ) {
        double fromKm = filter.getFromKm();
        double toKm   = filter.getToKm();
        boolean hasDate = filter.hasDate();

        if (fromKm >= 0 && toKm >= 0 && fromKm <= toKm) {
            JSONObject range = new JSONObject();
            JSONObject fromtokm = new JSONObject();
            JSONNumber f = new JSONNumber(fromKm);
            JSONNumber t = new JSONNumber(toKm);
            fromtokm.put("from", f);
            fromtokm.put("to", t);
            root.put("range", fromtokm);
        }

        JSONObject and = new JSONObject();
        if (hasDate) {
            long fromDate = filter.getFromDate();
            long toDate   = filter.getToDate();

            Date df = new Date(fromDate);
            Date dt = new Date(toDate);

            JSONObject daterange = new JSONObject();
            JSONString f = new JSONString(DTF.format(df));
            JSONString t = new JSONString(DTF.format(dt));

            daterange.put("from", f);
            daterange.put("to", t);
            and.put("date-range", daterange);
        }

        int fromClass = filter.getFromClass();
        int toClass   = filter.getToClass();

        if (fromClass >= 0 && toClass >= 0 && fromClass <= toClass) {
            JSONObject classrange = new JSONObject();
            JSONNumber f = new JSONNumber(fromClass);
            JSONNumber t = new JSONNumber(toClass);

            classrange.put("from", f);
            classrange.put("to", t);
            and.put("sector-range", classrange);
        }

        int[] events = filter.getEvents();

        if (events.length > 0) {
            StringBuilder cids = new StringBuilder();

            for (int i = 0; i < events.length; i++) {
                if (i > 0) cids.append(' ');
                cids.append(events[i]);
            }
            JSONObject columns = new JSONObject();
            columns.put("cids", new JSONString(cids.toString()));
            and.put("columns", columns);
        }
        if (and.size() > 0) {
            JSONObject jFilter = new JSONObject();
            jFilter.put("and", and);
            root.put("filter", jFilter);
        }
        return root;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
