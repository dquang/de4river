/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.chart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.DateItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.CollectionAttributeService;
import org.dive4elements.river.client.client.services.CollectionAttributeServiceAsync;
import org.dive4elements.river.client.client.utils.DoubleValidator;
import org.dive4elements.river.client.client.utils.IntegerValidator;
import org.dive4elements.river.client.shared.model.BooleanProperty;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.DoubleProperty;
import org.dive4elements.river.client.shared.model.IntegerProperty;
import org.dive4elements.river.client.shared.model.OutputSettings;
import org.dive4elements.river.client.shared.model.Property;
import org.dive4elements.river.client.shared.model.PropertyGroup;
import org.dive4elements.river.client.shared.model.PropertySetting;
import org.dive4elements.river.client.shared.model.Settings;
import org.dive4elements.river.client.shared.model.StringProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Dialog for the Chart-Properties, constructed from respective xml document.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class ChartPropertiesEditor
extends      Window
implements   ClickHandler
{
    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** CollectionAttribute Update Service. */
    protected CollectionAttributeServiceAsync updater =
        GWT.create(CollectionAttributeService.class);

    /** The tab called the editor window. */
    protected ChartOutputTab tab;

    /** The tabset for chart properties. */
    protected TabSet tabs;

    /** The collection. */
    protected Collection collection;

    /** The cloned output settings. */
    protected OutputSettings settings;

    /** The original output settings. */
    protected OutputSettings origSettings;



    /**
     * Setup editor dialog.
     * @param callerTab The tab called the editor window.
     */
    public ChartPropertiesEditor(ChartOutputTab callerTab) {
        this.tab = callerTab;
        this.tabs = new TabSet();

        init();
    }


    /**
     * Initialize the editor window and its components.
     */
    protected void init() {
        setTitle(MSG.properties());
        setCanDragReposition(true);
        setCanDragResize(true);

        collection = tab.getCollectionView().getCollection();
        String outputName = tab.getOutputName();
        origSettings = (OutputSettings)collection.getSettings(outputName);

        settings = (OutputSettings)origSettings.clone();
        if (settings == null) {
            return;
        }
        List<String> list = settings.getCategories();

        for (int i = 0; i < list.size(); i++) {
            Tab t = new Tab(MSG.getString(list.get(i)));
            List<Property> props = settings.getSettings(list.get(i));
            List<Property> origProps = origSettings.getSettings(list.get(i));
            VLayout layout = new VLayout();
            for (int j = 0; j < props.size(); j++) {
                if (props.get(j) instanceof PropertyGroup) {
                    layout.addMember(generatePropertyGroup(props.get(j),
                                                           origProps.get(j)));
                }
                else if (props.get(j) instanceof PropertySetting) {
                    PropertySetting p = (PropertySetting)props.get(j);
                    if (p.getAttribute("display").equals("false")) {
                        continue;
                    }
                    layout.addMember(generatePropertySetting(props.get(j),
                                                             origProps.get(j)));
                }
            }
            t.setPane(layout);
            tabs.addTab(t);
        }

        Button accept = new Button(MSG.label_ok());
        Button cancel = new Button(MSG.label_cancel());
        cancel.addClickHandler(this);
        accept.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                if(isDialogValid()) {
                    updateCollection();
                }
                else {
                    GWT.log("Dialog not valid");
                    SC.warn(MSG.error_dialog_not_valid());
                }
            }
        });

        HLayout buttons = new HLayout();
        buttons.addMember(accept);
        buttons.addMember(cancel);
        buttons.setAlign(Alignment.CENTER);
        buttons.setHeight(30);

        addItem(tabs);
        addItem(buttons);
        setWidth(380);
        setHeight(470);
        centerInPage();
    }


    /**
     * This method is called when the user aborts theming.
     * @param event The event.
     */
    @Override
    public void onClick(ClickEvent event) {
        this.destroy();
    }


    /**
     * Create a section from group (usually axis properties).
     */
    protected Canvas generatePropertyGroup(Property group, Property orig) {
        PropertyGroup pg = (PropertyGroup)group;
        PropertyGroup origPg = (PropertyGroup)orig;

        if (pg.getName().equals("axis")) {
            // Certain axis shall be skipped (W/Q-Diagrams cm-axis especially).
            String outputName = tab.getOutputName();
            if (outputName.equals("fix_wq_curve")
                || outputName.equals("computed_discharge_curve")
                || outputName.equals("extreme_wq_curve")) {
                String labelString = ((StringProperty)origPg.getPropertyByName(
                        "label")).getValue();
                if(labelString.equals("W [cm]")) {
                    VLayout layout = new VLayout();
                    layout.setHeight(0);
                    return layout;
                }
            }
            Label scale = new Label(MSG.scale() + " :");
            scale.setHeight(25);
            scale.setMargin(2);

            DynamicForm form1 = new DynamicForm();

            StringProperty label =
                (StringProperty)pg.getPropertyByName("label");
            FormItem title = createStringProperty(label);
            title.setValue(
                ((StringProperty)origPg.getPropertyByName("label")).getValue());

            StringProperty suggestedLabel =
                (StringProperty)pg.getPropertyByName("suggested-label");
            FormItem sugLabel = null;

            if (suggestedLabel != null) {
                // X Axis does not have a suggested label
                // otherwise add an hidden property for suggestedLabel
                sugLabel = createStringProperty(suggestedLabel);
                sugLabel.setValue(
                    ((StringProperty)origPg.getPropertyByName(
                        "suggested-label")).getValue());
            }

            IntegerProperty fontsize =
                (IntegerProperty)pg.getPropertyByName("font-size");
            FormItem fs = createIntegerProperty(fontsize);
            fs.setValue(
                ((IntegerProperty)
                    origPg.getPropertyByName("font-size")).getValue());

            form1.setFields(title, fs);

            VLayout root = new VLayout();
            root.addMember(form1);
            root.setHeight(90);


            DoubleProperty upper =
                (DoubleProperty)pg.getPropertyByName("upper");
            DoubleProperty lower =
                (DoubleProperty)pg.getPropertyByName("lower");

            FormItem range1candidate = null;
            FormItem range2candidate = null;
            Layout scaleLayout;
            DynamicForm form2 = new DynamicForm();

            if (upper != null && lower != null) {
                // Normal axis with double values
                scaleLayout = new HLayout();
                form2.setNumCols(6);
                range1candidate = createDoubleProperty(upper);
                range1candidate.setName("rangeupper");
                range1candidate.setWidth(70);
                range1candidate.setValue(
                    ((DoubleProperty)
                        origPg.getPropertyByName("upper")).toUIString());

                range2candidate = createDoubleProperty(lower);
                range2candidate.setName("rangelower");
                range2candidate.setWidth(70);
                range2candidate.setValue(
                    ((DoubleProperty)
                        origPg.getPropertyByName("lower")).toUIString());
            } else {
                // Time range axis
                scaleLayout = new VLayout();
                StringProperty dateUpper =
                    (StringProperty)pg.getPropertyByName("upper-time");
                StringProperty dateLower =
                    (StringProperty)pg.getPropertyByName("lower-time");
                if (dateUpper != null && dateLower != null) {
                    DateItem lowerDI = createDateProperty(dateLower);
                    DateItem upperDI = createDateProperty(dateUpper);
                    StringProperty origUp =
                        (StringProperty)origPg.getPropertyByName("upper-time");
                    StringProperty origLow =
                        (StringProperty)origPg.getPropertyByName("lower-time");
                    try {
                        lowerDI.setValue(new Date(
                                Long.valueOf(origLow.getValue())));
                        upperDI.setValue(new Date(
                                Long.valueOf(origUp.getValue())));
                    } catch (NumberFormatException e) {
                        // Just leave it at default then.
                    }
                    range1candidate = upperDI;
                    range2candidate = lowerDI;
                }
            }

            if (range1candidate != null && range2candidate != null) {
                final FormItem range1 = range1candidate;
                final FormItem range2 = range2candidate;

                BooleanProperty fixation =
                    (BooleanProperty)pg.getPropertyByName("fixation");
                FormItem fix = createBooleanProperty(fixation);
                fix.setValue(((BooleanProperty)
                        origPg.getPropertyByName("fixation"))
                    .getValue().booleanValue());
                fix.setWidth(30);

                fix.addChangedHandler(new ChangedHandler() {
                    @Override
                    public void onChanged(ChangedEvent e) {
                        if ((Boolean)e.getValue()) {
                            range1.enable();
                            range2.enable();
                        }
                        else {
                            range1.disable();
                            range2.disable();
                        }
                    }
                });
                if (fix.getValue().toString().equals("true")) {
                    range1.enable();
                    range2.enable();
                }
                else {
                    range1.disable();
                    range2.disable();
                }
                form2.setFields(fix, range2, range1);
                scaleLayout.setHeight(30);
                scaleLayout.addMember(scale);
                scaleLayout.addMember(form2);
                scaleLayout.setStyleName("property-dialog-axis");
                root.addMember(scaleLayout);
            } else {
                GWT.log("Invalid settings document. Without upper/lower.");
            }

            return root;
        }
        return null;
    }


    /**
     * Generate a form with items for the properties/settings, preset with
     * values.
     */
    protected DynamicForm generatePropertySetting(
        Property setting,
        Property orig)
    {
        DynamicForm form = new DynamicForm();
        FormItem item = new FormItem();
        if (setting instanceof BooleanProperty) {
            item = createBooleanProperty((BooleanProperty)setting);
            item.setValue(((BooleanProperty)orig).getValue().booleanValue());
        }
        else if (setting instanceof DoubleProperty) {
            item = createDoubleProperty((DoubleProperty)setting);
            item.setValue(((DoubleProperty)orig).toUIString());
        }
        else if (setting instanceof IntegerProperty) {
            item = createIntegerProperty((IntegerProperty)setting);
            item.setValue(((IntegerProperty)orig).getValue());
        }
        else if (setting instanceof StringProperty) {
            StringProperty property = (StringProperty) setting;
            item = createStringProperty(property);
            item.setValue(((StringProperty)orig).getValue());
        }
        else {
            GWT.log("generatePropertySetting: unknown setting type.");
        }
        form.setFields(item);
        return form;
    }


    protected FormItem createStringProperty(final StringProperty sp) {
        String name = sp.getName();
        if (name.contains("-")) {
            name = name.replace("-", "_");
        }

        String choiceAttribute = sp.getAttribute("choice");

        if (choiceAttribute != null && choiceAttribute.equals("logo")) {
            SelectItem logoChooser = new SelectItem();
            logoChooser.setImageURLPrefix(GWT.getHostPageBaseURL());
            logoChooser.setValueIconHeight(50);
            logoChooser.setValueIconWidth(100);

            LinkedHashMap valueMap = new LinkedHashMap<String, String>();
            LinkedHashMap<String, String> valueIcons =
                new LinkedHashMap<String, String>();
            valueMap.put("none", MSG.getString("none"));
            /*
             If you want to add images, remember to change code in these places:
             flys-artifacts:
             XYChartGenerator.java
             Timeseries*Generator.java and
             in the flys-client projects Chart*Propert*Editor.java.
             Also, these images have to be put in
             flys-artifacts/src/main/resources/images/
             flys-client/src/main/webapp/images/
             */
            valueMap.put("BfG", "");
            valueIcons.put("BfG", MSG.bfgLogo());
            logoChooser.setValueIcons(valueIcons);
            logoChooser.setValueMap(valueMap);
            logoChooser.setTitleStyle("color:#000;");
            logoChooser.setTitleAlign(Alignment.LEFT);
            logoChooser.setTitle(MSG.getString(name));
            logoChooser.setTitleAlign(Alignment.LEFT);
            logoChooser.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent e) {
                    String val;
                    if (e.getItem().getValue() == null) {
                        val = "";
                    }
                    else {
                        val = e.getItem().getValue().toString();
                    }
                    sp.setValue(val);
                }
            });
            return logoChooser;
        }
        else if (choiceAttribute != null && choiceAttribute.equals("placeh")) {
            SelectItem placeChooser = new SelectItem();
            LinkedHashMap valueMap = new LinkedHashMap<String, String>();
            valueMap.put("right", MSG.getString("right"));
            valueMap.put("left", MSG.getString("left"));
            valueMap.put("center", MSG.getString("center"));
            placeChooser.setValueMap(valueMap);
            placeChooser.setTitleStyle("color:#000;");
            placeChooser.setTitleAlign(Alignment.LEFT);
            placeChooser.setTitle(MSG.getString(name));
            placeChooser.setTitleAlign(Alignment.LEFT);
            placeChooser.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent e) {
                    String val;
                    if (e.getItem().getValue() == null) {
                        val = "";
                    }
                    else {
                        val = e.getItem().getValue().toString();
                    }
                    sp.setValue(val);
                }
            });
            return placeChooser;
        }
        else if (choiceAttribute != null && choiceAttribute.equals("placev")) {
            SelectItem placeChooser = new SelectItem();
            LinkedHashMap valueMap = new LinkedHashMap<String, String>();
            valueMap.put("top", MSG.getString("top"));
            valueMap.put("bottom", MSG.getString("bottom"));
            valueMap.put("center", MSG.getString("center"));
            placeChooser.setValueMap(valueMap);
            placeChooser.setTitleStyle("color:#000;");
            placeChooser.setTitleAlign(Alignment.LEFT);
            placeChooser.setTitle(MSG.getString(name));
            placeChooser.setTitleAlign(Alignment.LEFT);
            placeChooser.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent e) {
                    String val;
                    if (e.getItem().getValue() == null) {
                        val = "";
                    }
                    else {
                        val = e.getItem().getValue().toString();
                    }
                    sp.setValue(val);
                }
            });
            return placeChooser;
        }

        TextItem item = new TextItem();
        item.setTitle(MSG.getString(name));
        item.setTitleAlign(Alignment.LEFT);
        item.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent e) {
                String val;
                if (e.getItem().getValue() == null) {
                    val = "";
                }
                else {
                    val = e.getItem().getValue().toString();
                }
                sp.setValue(val);
            }
        });
        return item;
    }

    protected DateItem createDateProperty(final StringProperty sp) {
        String name = sp.getName();
        if (name.contains("-")) {
            name = name.replace("-", "_");
        }

        DateItem item = new DateItem(name, MSG.getString(name));
        item.setTitleAlign(Alignment.LEFT);
        item.setTitleStyle("color:#000;");
        item.setUseTextField(true);

        item.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent e) {
                DateItem di = (DateItem)e.getItem();
                sp.setValue(Long.toString(di.getValueAsDate().getTime()));
            }
        });
        return item;
    }

    /**
     *
     */
    protected FormItem createBooleanProperty(final BooleanProperty bp) {
        String name = bp.getName();
        if (name.contains("-")) {
            name = name.replace("-", "_");
        }

        CheckboxItem item = new CheckboxItem("item", MSG.getString(name));
        item.setLabelAsTitle(true);
        item.setTitleStyle("color:#000;");
        item.setTitleAlign(Alignment.LEFT);
        item.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent e) {
                String val;
                if (e.getItem().getValue() == null) {
                    val = "";
                }
                else {
                    val = e.getItem().getValue().toString();
                }
                bp.setValue(val);
            }
        });
        return item;
    }


    /**
     *
     */
    protected FormItem createDoubleProperty(final DoubleProperty dp) {
        String name = dp.getName();
        if (name.contains("-")) {
            name = name.replace("-", "_");
        }

        TextItem item = new TextItem();
        item.setTitle(MSG.getString(name));
        item.setTitleAlign(Alignment.LEFT);
        item.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent e) {
                 DoubleValidator validator = new DoubleValidator();
                 Map errors = e.getForm().getErrors();
                 if(validator.validate(e.getItem(), errors)) {
                     dp.setValueFromUI(e.getItem().getValue().toString());
                 }
                 e.getForm().setErrors(errors, true);
            }
        });
        return item;
    }


    /**
     *
     */
    protected FormItem createIntegerProperty(final IntegerProperty ip) {
        String name = ip.getName();
        if (name.contains("-")) {
            name = name.replace("-", "_");
        }

        TextItem item = new TextItem();
        item.setTitle(MSG.getString(name));
        item.setTitleAlign(Alignment.LEFT);
        item.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent e) {
                IntegerValidator validator = new IntegerValidator();
                Map errors = e.getForm().getErrors();
                if(validator.validate(e.getItem(), errors)) {
                    ip.setValue(e.getItem().getValue().toString());
                }
                e.getForm().setErrors(errors, true);
            }
        });
        return item;
    }


    protected void updateCollection() {
        final Config config = Config.getInstance();
        final String loc    = config.getLocale();

        GWT.log("PropertiesEditor.updateCollection via RPC now");

        Settings s = settings;
        collection.addSettings(this.tab.getOutputName(), s);
        updater.update(collection, loc, new AsyncCallback<Collection>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not update collection attributes.");
                SC.warn(MSG.getString(caught.getMessage()));
            }
            @Override
            public void onSuccess(Collection collection) {
                updateChartTab();
            }
        });
    }

    protected void updateChartTab() {
        this.tab.updateChartInfo();
        this.tab.updateChartPanel();
        this.destroy();
    }


    protected boolean isDialogValid() {
        boolean valid = true;
        for (int i = 0; i < tabs.getNumTabs(); i++) {
            Tab t = tabs.getTab(i);
            Canvas container = t.getPane();
            Canvas[] children = container.getChildren();
            for (Canvas c: children) {
                valid = validateCanvas(c);
                if(!valid) {
                    return valid;
                }
            }
        }
        return valid;
    }


    protected boolean validateCanvas(Canvas c) {
        boolean valid = true;
        if(c instanceof DynamicForm) {
            DynamicForm f = (DynamicForm) c;
            FormItem up = f.getItem("rangeupper");
            FormItem lo = f.getItem("rangelower");

            if(up != null && lo != null &&
               !up.isDisabled() && !lo.isDisabled())
            {
                validateRange(f);
            }
            return !f.hasErrors();
        }
        else if(c.getChildren().length > 0) {
            for (Canvas child: c.getChildren()) {
                valid = validateCanvas(child);
                if(!valid) {
                    return valid;
                }
            }
        }
        return valid;
    }

    protected boolean validateRange(DynamicForm form) {
        Map errors = form.getErrors();
        FormItem up = form.getItem("rangeupper");
        FormItem lo = form.getItem("rangelower");

        String v1 = up.getValue().toString();
        String v2 = lo.getValue().toString();

        if(v1.equals(v2)) {
            errors.put(up.getFieldName(), MSG.wrongFormat());
            errors.put(lo.getFieldName(), MSG.wrongFormat());
            form.setErrors(errors, true);
            return false;
        }
        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
