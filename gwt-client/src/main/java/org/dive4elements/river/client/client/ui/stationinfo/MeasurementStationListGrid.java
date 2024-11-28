/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.stationinfo;

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
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.MeasurementStation;
import org.dive4elements.river.client.shared.model.RiverInfo;

import java.util.List;

/**
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class MeasurementStationListGrid
extends InfoListGrid
implements RecordClickHandler {

    /** The ArtifactService used to communicate with the Artifact server. */
    protected ArtifactServiceAsync artifactService =
        GWT.create(ArtifactService.class);

    /** The StepForwardService used to put data into an existing artifact. */
    protected StepForwardServiceAsync forwardService =
        GWT.create(StepForwardService.class);

    /** The ArtifactService used to communicate with the Artifact server. */
    protected CreateCollectionServiceAsync createCollectionService =
        GWT.create(CreateCollectionService.class);

    public MeasurementStationListGrid(FLYS flys) {
        super(flys);
        ListGridField nfield = new ListGridField("name", "Messtelle");
        ListGridField mfield = new ListGridField(
            "measurementtype", "Messstellenart");
        ListGridField sfield = new ListGridField("kmstart", "Start [km]", 60);
        ListGridField efield = new ListGridField("kmend", "Ende [km]", 60);
        ListGridField lfield = new ListGridField("infolink", "Info");
        ListGridField cfield = new ListGridField(
            "curvelink", "Feststofftransport-Abfluss-Beziehung");
        cfield.addRecordClickHandler(this);

        this.setShowRecordComponents(true);
        this.setShowRecordComponentsByCell(true);
        this.setFields(nfield, mfield, sfield, efield, lfield, cfield);
    }

    /**
     * Resets the items of the tree.
     * If the list of gauges is empty or null the tree will be empty.
     */
    @Override
    public void setRiverInfo(RiverInfo riverinfo) {
        List<MeasurementStation> stations = riverinfo.getMeasurementStations();
        GWT.log("MeasurmentStationListGrid - setRiverInfo " + stations);

        if (stations != null && !stations.isEmpty()) {

            if (!riverinfo.isKmUp()) {
                for (MeasurementStation station : stations) {
                    addStation(station);
                }
            }
            else {
                for (int i = stations.size()-1; i >= 0; i--) {
                    MeasurementStation station = stations.get(i);
                    addStation(station);
                }
            }
        }
    }

    private void addStation(MeasurementStation station) {
        ListGridRecord record = new MeasurementStationRecord(station);
        this.addData(record);
    }

    @Override
    public void open() {
    }

    @Override
    protected Canvas getExpandPanel(ListGridRecord record) {
        MeasurementStationRecord station = (MeasurementStationRecord)record;
        return new MeasurementStationInfoPanel(station);
    }

    @Override
    public void onRecordClick(RecordClickEvent event) {
        final MeasurementStationRecord station =
            (MeasurementStationRecord)event.getRecord();
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
                        createArtifact(collection, locale, station);
                    }
                }
            );
    }

    private void createArtifact(
        final Collection collection,
        final String locale,
        final MeasurementStationRecord station
    ) {
        artifactService.create(
           locale, "staticsqrelation", null,
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
                        station.getRiverName());
                    Data river = new DefaultData(
                        "river",
                        null,
                        null,
                        new DataItem[]{riverItem});

                    DataItem refItem = new DefaultDataItem(
                        "station",
                        "station",
                        station.getID().toString());
                    Data ref = new DefaultData(
                        "station",
                        null,
                        null,
                        new DataItem[]{refItem});

                    DataItem nameItem = new DefaultDataItem(
                        "station_name",
                        "station_name",
                        station.getName().toString());
                    Data name = new DefaultData(
                        "station_name",
                        null,
                        null,
                        new DataItem[]{nameItem});

                    DataItem locationItem = new DefaultDataItem(
                        "ld_locations",
                        "ld_locations",
                        station.getKmStart().toString());
                    Data location = new DefaultData(
                        "ld_locations",
                        null,
                        null,
                        new DataItem[]{locationItem});

                    Data[] data = new Data[]{river, ref, name, location};
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
        if (colNum == 6) {
            return "text-decoration: underline; color: #0000EE; "
                + "cursor: pointer;";
        }
        else {
            return super.getCellCSSText(record, rowNum, colNum);
        }
    }

}
