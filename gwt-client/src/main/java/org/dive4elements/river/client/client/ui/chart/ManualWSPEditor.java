/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.RedrawRequestEvent;
import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.client.services.FeedServiceAsync;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.client.utils.DoubleValidator;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItem;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DefaultArtifact;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.Property;
import org.dive4elements.river.client.shared.model.PropertyGroup;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Settings;
import org.dive4elements.river.client.shared.model.StringProperty;

import java.util.List;
import java.util.Map;

/**
 * UI to enter point data and save it to an PointArtifact.
 */
public class ManualWSPEditor
extends      Window
implements   ClickHandler
{
    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** Name of the main data item to be fed. */
    public static final String LINE_DATA = "manualpoints.lines";

    /** When we change something, we need a RedrawRequest(Handler). */
    protected RedrawRequestHandler redrawRequestHandler;

    /** The collection */
    protected Collection collection;

    /** Service handle to clone and add artifacts to collection. */
    LoadArtifactServiceAsync loadArtifactService = GWT.create(
        org.dive4elements.river.client.client.services
        .LoadArtifactService.class);

    /** Service to feed the artifact with new point-data. */
    FeedServiceAsync feedService = GWT.create(
        org.dive4elements.river.client.client.services.FeedService.class);

    /** UUID of artifact to feed. */
    protected String uuid;

    /** Name of the outputmode, important when feeding data. */
    protected String outputModeName;

    /** Name of the data item for lines in this context. */
    protected String dataItemName;

    /** Input Field for y-coor of line. */
    protected TextItem valueInputPanel;

    /** Input Field for name of line. */
    protected TextItem nameInputPanel;

    /** Line data that is not added in this session. */
    protected JSONArray oldLines = null;


    /**
     * Setup editor dialog.
     * @param collection The collection to use.
     */
    public ManualWSPEditor(Collection collection,
        RedrawRequestHandler handler, String outputModeName
    ) {
        this.collection = collection;
        this.redrawRequestHandler = handler;
        this.outputModeName = outputModeName;
        this.dataItemName = outputModeName + "." + LINE_DATA;
        init();
    }


    /** Searches collection for first artifact to serve (manual) line data. */
    public String findManualPointsUUID() {
        int size = collection.getItemLength();

        for (int i = 0; i < size; i++) {
            CollectionItem item = collection.getItem(i);
            String dataValue = item.getData().get(dataItemName);
            if (dataValue != null) {
                // Found it.
                uuid = item.identifier();
                return uuid;
            }
        }

        return null;
    }


    /**
     * Initialize the editor window and its components.
     */
    protected void init() {
        setTitle(MSG.addWSP());
        setCanDragReposition(true);
        setCanDragResize(true);

        if(findManualPointsUUID() == null) {
            addArtifactCreateUI();
        }
        else {
            createUI();
        }
    }


    /** Create and setup/add the ui. */
    public void createUI() {
        Button accept = new Button(MSG.label_ok());
        Button cancel = new Button(MSG.label_cancel());
        cancel.addClickHandler(this);

        accept.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                okClicked();
            }
        });

        HLayout buttons = new HLayout();
        buttons.addMember(accept);
        buttons.addMember(cancel);
        buttons.setAlign(Alignment.CENTER);
        buttons.setHeight(30);

        // Use X and Y as default fallback.
        String yAxis = "Y";

        // Get header text from collection settings.
        Settings settings = this.collection.getSettings(outputModeName);
        List<Property> axes = settings.getSettings("axes");
        if(axes != null) {
            for (Property p: axes) {
                PropertyGroup pg = (PropertyGroup)p;
                StringProperty id =
                    (StringProperty)pg.getPropertyByName("id");
                if (id.getValue().equals("W")) {
                    StringProperty name =
                        (StringProperty)pg.getPropertyByName("label");
                    yAxis = name.getValue();
                }
            }
        }

        DynamicForm form = new DynamicForm();
        valueInputPanel = new TextItem();
        valueInputPanel.setTitle(yAxis);
        valueInputPanel.setShowTitle(true);
        valueInputPanel.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent e) {
                 DoubleValidator validator = new DoubleValidator();
                 Map errors = e.getForm().getErrors();
                 validator.validate(e.getItem(), errors);
                 e.getForm().setErrors(errors, true);
            }
        });
        nameInputPanel = new TextItem();
        nameInputPanel.setTitle(MSG.pointname());
        nameInputPanel.setShowTitle(true);
        form.setFields(valueInputPanel, nameInputPanel);

        VLayout layout = new VLayout();
        layout.addMember(form);

        // Find the artifacts uuid.
        // TODO this has been called already, why call it again?
        findManualPointsUUID();
        CollectionItem item = collection.getItem(uuid);

        // Store the old line data.
        if (item != null) {
            String jsonData = item.getData().get(dataItemName);
            oldLines = (JSONArray) JSONParser.parse(jsonData);
        }
        else {
            GWT.log("No old lines found for " + uuid);
        }

        addItem(layout);

        addItem(buttons);
        setWidth(360);
        setHeight(120);
        centerInPage();
    }


    /**
     * Create JSON representation of the points present in the form.
     * Add old data, too.
     * @return a jsonarray with the old and the new lines.
     */
    protected JSONArray jsonArrayFromForm() {
        if (oldLines == null) {
            oldLines = new JSONArray();
        }

        double val;
        if (valueInputPanel.getValue() == null)
            return oldLines;
        try {
            NumberFormat nf = NumberFormat.getDecimalFormat();
            double d = nf.parse(valueInputPanel.getValue().toString());
            val = d;
        }
        catch(NumberFormatException nfe) {
            GWT.log("fehler... nfe... TODO");
            return oldLines;
        }

        JSONArray data = new JSONArray();
        data.set(0, new JSONNumber(val));
        if (nameInputPanel.getValue() == null) {
            data.set(1, new JSONString(valueInputPanel.getValue().toString()));
        }
        else {
            data.set(1, new JSONString(nameInputPanel.getValue().toString()));
        }
        oldLines.set(oldLines.size(), data);

        return oldLines;
    }


    /**
     * Called when OK Button was clicked. Then, if entered values are valid,
     * fire a RedrawRequest and destroy.
     */
    protected void okClicked() {
        if (valueInputPanel.getValue() == null) {
            return;
        }
        GWT.log(valueInputPanel.getValue().toString());
        if(isDialogValid()) {
            // Feed JSON-encoded content of form.
            JSONArray list = jsonArrayFromForm();

            Data[] feedData = new Data[] {
                DefaultData.createSimpleStringData(dataItemName,
                    list.toString())
            };

            feedService.feed(
                Config.getInstance().getLocale(),
                new DefaultArtifact(uuid, "TODO:hash"),
                feedData,
                new AsyncCallback<Artifact>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("Could not feed artifact with lines.");
                        SC.warn(MSG.getString(caught.getMessage()));
                        enable();
                    }
                    @Override
                    public void onSuccess(Artifact fartifact) {
                        GWT.log("Successfully set lines ");
                        redrawRequestHandler.onRedrawRequest(
                            new RedrawRequestEvent());
                        destroy();
                    }
                });
        }
        else {
            GWT.log("Dialog not valid");
            SC.warn(MSG.error_dialog_not_valid());
        }
    }


    /** Add a ManualPointArtifact to Collection. */
    public void addArtifactCreateUI() {
        final Label standByLabel = new Label(MSG.standby());
        addItem(standByLabel);

        setWidth(360);
        setHeight(120);
        centerInPage();

        Config config = Config.getInstance();
        String locale = config.getLocale();

        loadArtifactService.load(
            this.collection,
            new Recommendation("manualpoints", ""),
            "manualpoints",
            locale,
            new AsyncCallback<Artifact>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("Creating manualpoint artifact failed!");
                }
                @Override
                public void onSuccess(Artifact artifact) {
                    GWT.log("Successfully created artifact.");
                    removeItem(standByLabel);
                    uuid = artifact.getUuid();
                    createUI();
                }
            });
    }


    /**
     * This method is called when the user aborts point editing.
     * @param event The event.
     */
    @Override
    public void onClick(ClickEvent event) {
        this.destroy();
    }


    /** Return false if x or y attribute is missing. */
    protected boolean isDialogValid() {
        return (DoubleValidator.isDouble(valueInputPanel.getValue()));
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
