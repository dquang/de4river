/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.services.AdvanceService;
import org.dive4elements.river.client.client.services.AdvanceServiceAsync;
import org.dive4elements.river.client.client.services.ArtifactService;
import org.dive4elements.river.client.client.services.ArtifactServiceAsync;
import org.dive4elements.river.client.client.services.CreateCollectionService;
import org.dive4elements.river.client.client.services.CreateCollectionServiceAsync;
import org.dive4elements.river.client.client.services.StepForwardService;
import org.dive4elements.river.client.client.services.StepForwardServiceAsync;
import org.dive4elements.river.client.client.ui.CollectionView;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.GaugeInfo;
import org.dive4elements.river.client.shared.model.RiverInfo;


/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class GaugeListGrid extends InfoListGrid implements RecordClickHandler {

    private static final int ABFLUSSTAFEL_COLUMN = 6;

    /** The ArtifactService used to communicate with the Artifact server. */
    protected ArtifactServiceAsync artifactService =
        GWT.create(ArtifactService.class);

    /** The StepForwardService used to put data into an existing artifact. */
    protected StepForwardServiceAsync forwardService =
        GWT.create(StepForwardService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected CreateCollectionServiceAsync createCollectionService =
        GWT.create(CreateCollectionService.class);

    /** The StepForwardService used to put data into an existing artifact. */
    protected AdvanceServiceAsync advanceService =
        GWT.create(AdvanceService.class);

    public GaugeListGrid(FLYS flys) {
        super(flys);
        //TODO i18n!!!
        ListGridField nfield = new ListGridField("name", "Pegel");
        ListGridField sfield = new ListGridField("kmstart", "Start [km]", 60);
        ListGridField efield = new ListGridField("kmend", "Ende [km]", 60);
        ListGridField stfield = new ListGridField("station", "Station [km]");
        ListGridField lfield = new ListGridField("infolink", "Info");
        ListGridField cfield = new ListGridField(
            "curvelink", MSG.gauge_curve_link());
        cfield.addRecordClickHandler(this);

        this.setShowRecordComponents(true);
        this.setShowRecordComponentsByCell(true);
        this.setFields(nfield, sfield, efield, stfield, lfield, cfield);
    }

    public void setRiverInfo(RiverInfo riverinfo) {
        List<GaugeInfo> gauges = riverinfo.getGauges();

        if (gauges != null && !gauges.isEmpty()) {

            ArrayList<GaugeInfo> emptygauges = new ArrayList<GaugeInfo>();

            if (!riverinfo.isKmUp()) {
                for (GaugeInfo gauge : gauges) {
                    addGauge(gauge, emptygauges);
                }
            }
            else {
                for (int i = gauges.size()-1; i >= 0; i--) {
                    GaugeInfo gauge = gauges.get(i);
                    addGauge(gauge, emptygauges);
                }
            }

            // put empty gauges to the end
            for (GaugeInfo gauge : emptygauges) {
                addGauge(gauge);
            }
        }
    }

    private void addGauge(GaugeInfo gauge, List<GaugeInfo> empty) {
        if (gauge.getKmStart() != null && gauge.getKmEnd() != null) {
            addGauge(gauge);
        }
        else {
            empty.add(gauge);
        }
    }

    private void addGauge(GaugeInfo gauge) {
        this.addData(new GaugeRecord(gauge));
    }

    public void open() {
        ArrayList<Double> locations = new ArrayList<Double>();

        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {
                DataList dl = data[i];
                String state = dl.getState();
                GWT.log("GaugeListGrid - open " + state);
                if (state.equals("state.winfo.location_distance")) {
                    Double ldfrom = null;
                    Double ldto = null;

                    for (int j = dl.size()-1; j >= 0; --j) {
                        Data d = dl.get(j);
                        String label = d.getLabel();
                        GWT.log("GaugeListGrid - setData - label "
                            + label + " " + d.getStringValue());
                        if (label.equals("ld_from")) {
                            ldfrom = getDoubleValue(d);
                        }
                        else if (label.equals("ld_to")) {
                            ldto = getDoubleValue(d);
                        }
                        else if (label.equals("ld_locations")) {
                            getLocationsFromData(locations, d);
                            openOnLocations(locations);
                            return;
                        }
                    }
                    if (ldfrom != null) {
                        openOnDistance(ldfrom, ldto);
                        return;
                    }
                }
                else if(state.equals("state.winfo.distance_only") ||
                        state.equals("state.winfo.distance")) {
                    Double ldfrom = null;
                    Double ldto = null;

                    for (int j = dl.size()-1; j >= 0; --j) {
                        Data d = dl.get(j);
                        String label = d.getLabel();
                        GWT.log("GaugeListGrid - setData - label "
                            + label + " " + d.getStringValue());
                        if (label.equals("ld_from")) {
                            ldfrom = getDoubleValue(d);
                        }
                        else if (label.equals("ld_to")) {
                            ldto = getDoubleValue(d);
                        }
                    }

                    if (ldfrom != null) {
                        openOnDistance(ldfrom, ldto);
                        return;
                    }
                }
                else if (state.equals("state.winfo.location")) {
                    getLocations("ld_locations", locations, dl);
                    openOnLocations(locations);
                    return;
                }
                else if (state
                    .equals("state.winfo.reference.curve.input.start")
                ) {
                    getLocations("reference_startpoint", locations, dl);
                }
                else if (state
                    .equals("state.winfo.reference.curve.input.end")
                ) {
                    getLocations("reference_endpoint", locations, dl);
                }
                else if (state
                    .equals("state.winfo.historicalq.reference_gauge")
                ) {
                    for (int j = dl.size()-1; j >= 0; --j) {
                        Data d = dl.get(j);
                        String label = d.getLabel();
                        if (label.equals("reference_gauge")) {
                            String tmp = d.getStringValue();
                            if (tmp != null) {
                                Long gaugereference = Long.valueOf(tmp);
                                if (gaugereference != null) {
                                    openOnReference(gaugereference);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!locations.isEmpty()) {
            openOnLocations(locations);
        }
        else {
            openAll();
        }
    }

    void getLocations(String labelname, List<Double> locations, DataList dl) {
        for (int j = dl.size()-1; j >= 0; --j) {
            Data d = dl.get(j);
            String label = d.getLabel();
            if (label.equals(labelname)) {
                getLocationsFromData(locations, d);
            }
        }
    }

    void getLocationsFromData(List<Double> locations, Data data) {
        DataItem[] items = data.getItems();
        for (int k = 0; k < items.length; k++) {
            String tmp = items[k].getStringValue();
            GWT.log("GaugeListGrid - getLocationsFromData " + tmp);
            if (tmp != null) {
                if (tmp.contains(" ")) {
                    // string contains several values ...
                    String[] values = tmp.split(" ");
                    for(int i=0; i < values.length; i++) {
                        Double value = Double.valueOf(values[i]);
                        if (value != null) {
                            locations.add(value);
                        }
                    }
                }
                else {
                    Double value = Double.valueOf(tmp);
                    if (value != null) {
                        locations.add(value);
                    }
                }
            }
        }
    }

    public void openOnReference(Long number) {
        GWT.log("GaugeListGrid - openOnReference " + number);
        for (ListGridRecord record: this.getRecords()) {
            GaugeRecord item = (GaugeRecord)record;
            if (item.getOfficialNumber().equals(number)) {
                expandRecord(item);
            }
            else {
                collapseRecord(item);
            }
        }
    }

    public void openOnDistance(Double start, Double end) {
        GWT.log("GaugeListGrid - openOnDistance " + start + " " + end);

        for (ListGridRecord record: this.getRecords()) {
            GaugeRecord item = (GaugeRecord)record;
            if (end == null && item.getKmStart() != null) {
                if (item.getKmStart() >= start) {
                    expandRecord(item);
                }
                else {
                    collapseRecord(item);
                }
            }
            else if (item.getKmStart() != null && item.getKmEnd() != null) {
                // as getStart()/getEnd() return Double objects,
                // they can be null and
                // can cause NPEs when comparing with double... strange...
                GWT.log("GaugeListGrid - openOnDistance item "
                    + item.getKmStart() + " " + item.getKmEnd());
                if ((start >= item.getKmStart() && start <= item.getKmEnd()) ||
                      (end >= item.getKmStart() &&   end <= item.getKmEnd()) ||
                    (start <= item.getKmStart() &&   end >= item.getKmEnd())) {
                    expandRecord(item);
                }
                else {
                    collapseRecord(item);
                }
            }
            else {
                collapseRecord(item);
            }
        }
    }

    /**
     * Open Gauge entry if a location fits to the gauge.
     */
    public void openOnLocations(List<Double> locations) {
        GWT.log("GaugeListGrid - openOnLocations " + locations);

        if (locations == null || locations.isEmpty()) {
            return;
        }

        for (ListGridRecord record: this.getRecords()) {
            GaugeRecord item = (GaugeRecord)record;
            boolean isset = false;
            for (Double location: locations) {
                if (locations == null) {
                    continue;
                }

                Double start = item.getKmStart();
                Double end   = item.getKmEnd();
                if (start == null || end == null) {
                    // should not occur but avoid NullPointerException
                    continue;
                }

                if (location >= start && location <= end) {
                    isset = true;
                    break;
                }
            }
            if (isset) {
                expandRecord(item);
            }
            else {
                collapseRecord(item);
            }
        }
    }

    @Override
    protected Canvas getExpandPanel(ListGridRecord record) {
        GaugeRecord item = (GaugeRecord)record;
        return new GaugeInfoPanel(item, flys);
    }

    /**
     * When clicked on the gauge discharge link, open new Gauge Discharge
     * Curve view.
     */
    @Override
    public void onRecordClick(RecordClickEvent event) {
        final GaugeRecord gauge = (GaugeRecord)event.getRecord();
        Config config       = Config.getInstance();
        final String locale = config.getLocale();
        createCollectionService.create(
                locale,
                flys.getCurrentUser().identifier(),
                new AsyncCallback<Collection>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not create the new collection.");
                        SC.warn(FLYS.getExceptionString(MSG, caught));
                    }

                    @Override
                    public void onSuccess(Collection collection) {
                        GWT.log("Successfully created a new collection.");
                        createArtifact(collection, locale, gauge);
                    }
                }
            );
    }

    private void createArtifact(
        final Collection collection,
        final String locale,
        final GaugeRecord gauge
    ) {
        artifactService.create(
           locale, "gaugedischargecurve", null,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Could not create the new artifact.");
                    SC.warn(FLYS.getExceptionString(MSG, caught));
                }

                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully created a new artifact.");

                    DataItem riverItem = new DefaultDataItem(
                        "river",
                        "river",
                        gauge.getRiverName());
                    Data river = new DefaultData(
                        "river",
                        null,
                        null,
                        new DataItem[]{riverItem});

                    DataItem refItem = new DefaultDataItem(
                        "reference_gauge",
                        "reference_gauge",
                        gauge.getOfficialNumber().toString());
                    Data ref = new DefaultData(
                        "reference_gauge",
                        null,
                        null,
                        new DataItem[]{refItem});

                    DataItem locItem = new DefaultDataItem(
                        "ld_locations",
                        "ld_locations",
                        gauge.getStation().toString());
                    Data loc = new DefaultData(
                        "ld_locations",
                        null,
                        null,
                        new DataItem[]{locItem});

                    DataItem nameItem = new DefaultDataItem(
                        "gauge_name",
                        "gauge_name",
                        gauge.getName());
                    Data name = new DefaultData(
                        "gauge_name",
                        null,
                        null,
                        new DataItem[]{nameItem});

                    Data[] data = new Data[]{river, ref, loc, name};
                    forwardService.go(locale, artifact, data,
                        new AsyncCallback<Artifact>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            GWT.log("Could not feed the artifact.");
                            SC.warn(caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Artifact artifact) {
                            GWT.log("Successfully feed the artifact.");
                            CollectionView view = new CollectionView(
                                flys,
                                collection,
                                artifact);
                            flys.getWorkspace().addView(
                                collection.identifier(),
                                view);
                            view.addArtifactToCollection(artifact);
                        }
                    });
                }
            });
    }

    @Override
    public String getCellCSSText(ListGridRecord record, int rowNum,
            int colNum) {
        if (colNum == ABFLUSSTAFEL_COLUMN) {
            // display the ablfusstafel cell like a link
            return "text-decoration: underline; color: #0000EE; "
                + "cursor: pointer;";
        }
        else {
            return super.getCellCSSText(record, rowNum, colNum);
        }
    }
}
