/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.StepForwardEvent;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.client.services.RemoveArtifactServiceAsync;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Recommendation.Facet;
import org.dive4elements.river.client.shared.model.Recommendation.Filter;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO Probably better to branch off AbstractUIProvider.
// TODO Merge with other datacage-widget impls.
/**
 * Panel containing a Grid and a "next" button. The Grid is fed by a
 * DatacagePairWidget which is put in the input-helper area.
 */
public class DatacageTwinPanel
extends      TextProvider {

    private static final long serialVersionUID = 8906629596491827857L;

    protected static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected String dataName;

    protected User user;

    /** ListGrid that displays user-selected pairs to build differences with. */
    protected ListGrid differencesList;

    /**
     * List to track previously selected but now removed pairs. (Needed to
     * be able to identify artifacts that can be removed from the collection.
     */
    protected List<RecommendationPairRecord> removedPairs =
        new ArrayList<RecommendationPairRecord>();

    /** Service handle to clone and add artifacts to collection. */
    LoadArtifactServiceAsync loadArtifactService = GWT.create(
        org.dive4elements.river.client.client.services
        .LoadArtifactService.class);

    /** Service to remove artifacts from collection. */
    RemoveArtifactServiceAsync removeArtifactService = GWT.create(
        org.dive4elements.river.client.client.services
        .RemoveArtifactService.class);


    public DatacageTwinPanel(User user) {
        super();
        this.user = user;
    }


    /**
     * Remove first occurrence of "[" and "]" (if both do occur).
     * @param value String to be stripped of [] (might be null).
     * @return input string but with [ and ] removed, or input string if no
     *         brackets were found.
     * @see StringUtil.unbracket
     */
    public static final String unbracket(String value) {
        // null- guard.
        if (value == null) return value;

        int start = value.indexOf("[");
        int end   = value.indexOf("]");

        if (start < 0 || end < 0) {
            return value;
        }

        value = value.substring(start + 1, end);

        return value;
    }


    /**
     * Create a recommendation from a string representation of it.
     * @param from string in format as shown above.
     * @return recommendation from input string.
     */
    public Recommendation createRecommendationFromString(
        String from,
        String factory
    ) {
        // TODO Construct "real" filter.
        String[] parts = unbracket(from).split(";");
        Recommendation.Filter filter = new Recommendation.Filter();
        Recommendation.Facet  facet  = new Recommendation.Facet(
                parts[1],
                parts[2]);

        List<Recommendation.Facet> facets = new ArrayList<Recommendation.Facet>
            ();
        facets.add(facet);
        filter.add("longitudinal_section", facets);
        Recommendation r = new Recommendation(factory, parts[0],
            this.artifact.getUuid(), filter);
        r.setDisplayName(parts[3]);
        return r;
    }


    /**
     * Add RecomendationPairRecords from input String to the ListGrid.
     */
    public void populateGridFromString(String from, String factory){
        // Split this string.
        // Create according recommendations and display strings.
        String[] recs = from.split("#");
        if (recs.length % 2 != 0) return;
        for (int i = 0; i < recs.length; i+=2) {
            Recommendation minuend =
                createRecommendationFromString(recs[i+0], factory);
            Recommendation subtrahend =
                createRecommendationFromString(recs[i+1], factory);

            RecommendationPairRecord pr = new RecommendationPairRecord(
                minuend, subtrahend);
            // This Recommendation Pair comes from the data string and was thus
            // already cloned.
            pr.setIsAlreadyLoaded(true);
            this.differencesList.addData(pr);
        }
    }


    /**
     * Creates the graphical representation and interaction widgets
     * for the data.
     * @param dataList the data.
     * @return graphical representation and interaction widgets for data.
     */
    @Override
    public Canvas create(DataList dataList) {
        GWT.log("createData()");

        Canvas widget = createWidget();
        Canvas submit = getNextButton();

        VLayout layout       = new VLayout();
        HLayout helperLayout = new HLayout();
        helperLayout.addMember(new DatacagePairWidget(this.artifact,
            user, "winfo_diff_twin_panel", differencesList));

        layout.addMember(widget);
        layout.addMember(submit);
        layout.setMembersMargin(10);
        this.helperContainer.addMember(helperLayout);

        populateGrid(dataList, "waterlevel");
        return layout;
    }

    protected void populateGrid(DataList dataList, String factory) {
        Data data     = dataList.get(0);
        this.dataName = data.getLabel();
        for (int i = 0; i < dataList.size(); i++) {
            if (dataList.get(i) != null
                && dataList.get(i).getItems() != null
            ) {
                if (dataList.get(i).getItems() != null) {
                    populateGridFromString(
                        dataList.get(i).getItems()[0].getStringValue(),
                        factory);
                }
            }
        }
    }


    /**
     * Validates the selection.
     * @return List of internationalized errror messages (if any).
     */
    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();
        if (differencesList.getRecords().length == 0) {
            errors.add(MSG.error_no_waterlevel_pair_selected());
        }
        // Check whether minuend and subtrahend are equal.
        for (ListGridRecord record: differencesList.getRecords()) {
            RecommendationPairRecord r = (RecommendationPairRecord) record;
            if (r.getFirst().equals(r.getSecond())) {
                errors.add(MSG.error_same_waterlevels_in_pair());
            }
        }

        return errors;
    }


    /**
     * Creates layout with grid that displays selection inside.
     */
    public Canvas createWidget() {
        VLayout layout  = new VLayout();
        differencesList = new ListGrid();

        differencesList.setCanEdit(false);
        differencesList.setCanSort(false);
        differencesList.setShowHeaderContextMenu(false);
        differencesList.setHeight(150);
        differencesList.setShowAllRecords(true);

        ListGridField nameField    = new ListGridField("first",  "Minuend");
        ListGridField capitalField = new ListGridField("second", "Subtrahend");
        // Track removed rows, therefore more or less reimplement
        // setCanRecomeRecords.
        final ListGridField removeField  =
            new ListGridField("_removeRecord", "Remove Record"){{
                setType(ListGridFieldType.ICON);
                setIcon(GWT.getHostPageBaseURL() + MSG.removeFeature());
                setCanEdit(false);
                setCanFilter(false);
                setCanSort(false);
                setCanGroupBy(false);
                setCanFreeze(false);
                setWidth(25);
        }};

        differencesList.setFields(new ListGridField[] {nameField,
           capitalField, removeField});

        differencesList.addRecordClickHandler(new RecordClickHandler() {
                @Override
                public void onRecordClick(final RecordClickEvent event) {
                    // Just handle remove-clicks
                    if(!event.getField().getName()
                        .equals(removeField.getName())
                    ) {
                        return;
                    }
                    trackRemoved(event.getRecord());
                    event.getViewer().removeData(event.getRecord());
                }
            });
        layout.addMember(differencesList);

        return layout;
    }


    /**
     * Add record to list of removed records.
     */
    public void trackRemoved(Record r) {
        RecommendationPairRecord pr = (RecommendationPairRecord) r;
        this.removedPairs.add(pr);
    }

    /**
     * Set factory of recommendation such that the correct artifacts will
     * be cloned for difference calculations.
     */
    public void adjustRecommendation(Recommendation recommendation) {
        // XXX: THIS IS AN EVIL HACK TO MAKE W-DIFFERENCES WORK AGAIN!
        // TODO: Throw all this code away and do it with server side
        // recommendations!
        recommendation.setTargetOut("w_differences");

        if (recommendation.getIDs() != null) {
            GWT.log("Setting staticwkms factory for rec with ID "
                + recommendation.getIDs());
            recommendation.setFactory("staticwkms");
        }
        /*
        // So far, we do not need to rewrite the factory anymore,
        // except for staticwkms; probably other cases will pop up later.
        else if (recommendation.getFactory().equals("winfo")) {
            GWT.log("Setting waterlevel factory for a winfo rec.");
            recommendation.setFactory("waterlevel");
        }
        */
        else {
           GWT.log("Leave rec. id " + recommendation.getIDs() + ", factory "
               + recommendation.getFactory() + " untouched.");
        }
    }

    /**
     * Validates data, does nothing if invalid, otherwise clones new selected
     * waterlevels and add them to collection, forward the artifact.
     */
    @Override
    public void onClick(ClickEvent e) {
        GWT.log("DatacageTwinPanel.onClick");

        List<String> errors = validate();
        if (errors != null && !errors.isEmpty()) {
            showErrors(errors);
            return;
        }

        Config config = Config.getInstance();
        String locale = config.getLocale();

        ListGridRecord[] records = differencesList.getRecords();

        List<Recommendation> ar  = new ArrayList<Recommendation>();
        List<Recommendation> all = new ArrayList<Recommendation>();

        for (ListGridRecord record : records) {
            RecommendationPairRecord r =
                (RecommendationPairRecord) record;
            // Do not add "old" recommendations.
            if (!r.isAlreadyLoaded()) {
                // Check whether one of those is a dike or similar.
                // TODO differentiate and merge: new clones, new, old.
                Recommendation firstR = r.getFirst();
                adjustRecommendation(firstR);

                Recommendation secondR = r.getSecond();
                adjustRecommendation(secondR);
                ar.add(firstR);
                ar.add(secondR);
            }
            else {
                all.add(r.getFirst());
                all.add(r.getSecond());
            }
        }

        final Recommendation[] toClone = ar.toArray(
            new Recommendation[ar.size()]);
        final Recommendation[] toUse   = all.toArray(
            new Recommendation[all.size()]);

        // Find out whether "old" artifacts have to be removed.
        List<String> artifactIdsToRemove = new ArrayList<String>();
        for (RecommendationPairRecord rp: this.removedPairs) {
            Recommendation first  = rp.getFirst();
            Recommendation second = rp.getSecond();

            for (Recommendation recommendation: toUse) {
                if (first != null
                    && first.getIDs().equals(recommendation.getIDs())
                ) {
                    first = null;
                }
                if (second != null
                    && second.getIDs().equals(recommendation.getIDs())
                ) {
                    second = null;
                }

                if (first == null && second == null) {
                    break;
                }
            }
            if (first != null) {
                artifactIdsToRemove.add(first.getIDs());
            }
            if (second != null) {
                artifactIdsToRemove.add(second.getIDs());
            }
        }

        // Remove old artifacts, if any. Do this asychronously without much
        // feedback.
        for(final String uuid: artifactIdsToRemove) {
            removeArtifactService.remove(this.collection,
                uuid,
                locale,
                new AsyncCallback<Collection>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("RemoveArtifact (" + uuid + ") failed.");
                    }
                    @Override
                    public void onSuccess(Collection collection) {
                        GWT.log("RemoveArtifact succeeded");
                    }
                });
        }

        // Clone new ones (and spawn statics), go forward.
        parameterList.lockUI();
        loadArtifactService.loadMany(
            this.collection,
            toClone,
            //"staticwkms" and "waterlevel"
            null,
            locale,
            new AsyncCallback<Artifact[]>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Failure of cloning with factories!");
                    parameterList.unlockUI();
                }
                @Override
                public void onSuccess(Artifact[] artifacts) {
                    GWT.log("Successfully cloned " + toClone.length +
                        " with factories.");

                    fireStepForwardEvent(new StepForwardEvent(
                        getData(toClone, artifacts, toUse)));
                    parameterList.unlockUI();
                }
            });
    }


    /**
     * Create Data and DataItem from selection (a long string with identifiers
     * to construct diff-pairs).
     *
     * @param newRecommendations "new" recommendations (did not survive a
     *        backjump).
     * @param newArtifacts artifacts cloned from newRecommendations.
     * @param oldRecommendations old recommendations that survived a backjump.
     *
     * @return dataitem with a long string with identifiers to construct
     *         diff-pairs.
     */
    protected Data[] getData(
            Recommendation[] newRecommendations,
            Artifact[] newArtifacts,
            Recommendation[] oldRecommendations)
    {
        // Construct string with info about selections.
        String dataItemString = "";
        for (int i = 0; i < newRecommendations.length; i++) {
            Recommendation r = newRecommendations[i];
            Artifact newArtifact = newArtifacts[i];
            String uuid = newArtifact.getUuid();
            r.setMasterArtifact(uuid);
            if (i>0) dataItemString += "#";

            dataItemString += createDataString(uuid, r);
        }

        for (int i = 0; i < oldRecommendations.length; i++) {
            Recommendation r = oldRecommendations[i];
            String uuid = r.getIDs();
            if (dataItemString.length() > 0) dataItemString += "#";

            dataItemString += createDataString(uuid, r);
        }

        // TODO some hassle could be resolved by using multiple DataItems
        // (e.g. one per pair).
        DataItem item = new DefaultDataItem(dataName, dataName, dataItemString);
        return new Data[] { new DefaultData(
            dataName, null, null, new DataItem[] {item}) };
    }


    protected String createDataString(
        String artifact,
        Recommendation recommendation
    ) {
        return createDataString(artifact, recommendation, "staticwkms");
    }

    /**
     * Creates part of the String that encodes minuend or subtrahend.
     * @param artifact Artifacts UUID.
     * @param recommendation Recommendation to wrap in string.
     * @param factory The factory to encode.
     */
    protected String createDataString(
        String artifact,
        Recommendation recommendation,
        String factory)
    {
        Filter filter = recommendation.getFilter();
        Facet  f      = null;

        if(filter != null) {
            Map<String, List<Facet>>               outs = filter.getOuts();
            Set<Map.Entry<String, List<Facet>>> entries = outs.entrySet();

            for (Map.Entry<String, List<Facet>> entry: entries) {
                List<Facet> fs = entry.getValue();

                f = fs.get(0);
                if (f != null) {
                    break;
                }
            }

            return "[" + artifact + ";"
                + f.getName()
                + ";"
                + f.getIndex()
                + ";"
                + recommendation.getDisplayName() + "]";
        }
        else {
            return "["
                + artifact
                + ";" + factory + ";0;"
                + recommendation.getDisplayName() + "]";
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
