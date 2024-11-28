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

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.ItemChangedEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedHandler;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.ColorPickerItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.validator.IsFloatValidator;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.CollectionItemAttributeService;
import org.dive4elements.river.client.client.services.CollectionItemAttributeServiceAsync;
import org.dive4elements.river.client.client.services.ThemeListingService;
import org.dive4elements.river.client.client.services.ThemeListingServiceAsync;
import org.dive4elements.river.client.client.utils.DoubleValidator;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.CollectionItemAttribute;
import org.dive4elements.river.client.shared.model.FacetRecord;
import org.dive4elements.river.client.shared.model.Style;
import org.dive4elements.river.client.shared.model.StyleSetting;
import org.dive4elements.river.client.shared.model.Theme;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Editor window for styles.
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class StyleEditorWindow
extends Window
implements ClickHandler
{
    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /** The collection. */
    protected Collection collection;

    /** The parent ThemePanel. */
    protected ThemePanel panel;

    /** The attributes. */
    protected CollectionItemAttribute attributes;

    /** The selected facet. */
    protected FacetRecord facet;

    /** Main layout. */
    protected VLayout layout;

    /** The form that contains all the input widgets. */
    protected DynamicForm df;

    protected VLayout properties;

    protected Canvas container;

    protected Map<String, Style> styleGroups;

    protected Style current;

    protected SelectItem styleChooser;

    /** The service used to set collection item attributes. */
    protected CollectionItemAttributeServiceAsync itemAttributeService =
        GWT.create(CollectionItemAttributeService.class);

    /** The service used to request a list of themes. */
    protected ThemeListingServiceAsync themeListingService =
        GWT.create(ThemeListingService.class);


    /**
     * Setup editor dialog.
     * @param collection The collection the current theme belongs to.
     * @param attributes The collection attributes.
     * @param facet      The selected facet.
     */
    public StyleEditorWindow (
        Collection collection,
        CollectionItemAttribute attributes,
        FacetRecord facet)
    {
        this.collection = collection;
        this.attributes = attributes;
        this.facet = facet;
        this.layout = new VLayout();
        this.properties = new VLayout();
        this.container = new Canvas();
        this.styleChooser = new SelectItem("style", "Style");

        styleChooser.setTitleStyle("color:#000;");
        styleChooser.setTitleAlign(Alignment.LEFT);
        styleChooser.setValue("aktuell");
        styleChooser.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent ce) {
                String value = ce.getValue().toString();
                Style s = null;
                if (value.equals("aktuell")) {
                    s = current;
                }
                else if (styleGroups.containsKey(value)) {
                    s = styleGroups.get(value);
                }

                if (s != null) {
                    setNewStyle(s);
                    properties.removeMember(container);
                    container = createPropertyGrid(s);
                    properties.addMember(container);
                }
            }
        });

        DynamicForm f = new DynamicForm();
        f.setFields(styleChooser);
        f.setColWidths("40%", "60%");

        layout.addMember(f);
        init();
        initPanels();
    }


    /**
     * Initialize the window and set the layout.
     */
    protected void init() {
        setTitle(MSG.properties());
        setCanDragReposition(true);
        setCanDragResize(true);
        layout.setMargin(10);

        layout.setWidth100();
        layout.setHeight100();

        Config config = Config.getInstance();
        String locale = config.getLocale();

        Theme theme = facet.getTheme();
        Style style = attributes.getStyle(theme.getFacet(), theme.getIndex());
        if(style == null) {
            GWT.log("StyleEditorWindow.init(): style == null");
            return;
        }
        String name = style.getName();
        this.current = style;

        themeListingService.list(
            locale,
            name,
            new AsyncCallback<Map<String, Style> >() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("No listloaded.");
                }
                @Override
                public void onSuccess(Map<String, Style> list) {
                    GWT.log("Successfully loaded list.");

                    styleGroups = list;
                    Set<String> keys = list.keySet();
                    LinkedHashMap<String, String> valueMap =
                        new LinkedHashMap<String, String>();
                    valueMap.put("aktuell", "Aktuell");
                    for (String s: keys) {
                        Style tmp = styleGroups.get(s);
                        tmp.setFacet(current.getFacet());
                        tmp.setIndex(current.getIndex());
                        valueMap.put(s, s);
                    }
                    styleChooser.setValueMap(valueMap);
                }
            });
    }


    /**
     * Initialize the static window content like buttons and main layout.
     */
    protected void initPanels() {
        HLayout buttons = new HLayout();
        Button accept = new Button(MSG.label_ok());
        Button cancel = new Button(MSG.label_cancel());
        cancel.addClickHandler(this);
        accept.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                // TODO Fix this, for whatever reason it doesnt work
                // (always valid).
                if (df == null) {
                    return;
                }
                if (!df.hasErrors() && df.validate()) {
                    saveStyle();
                }
            }
        });

        buttons.addMember(accept);
        buttons.addMember(cancel);
        buttons.setAlign(Alignment.CENTER);
        buttons.setHeight(30);

        Theme theme = facet.getTheme();
        Style style = attributes.getStyle(theme.getFacet(), theme.getIndex());

        container = createPropertyGrid(style);
        properties.addMember(container);
        layout.addMember(properties);
        layout.addMember(buttons);
        addItem(layout);
        setWidth(400);
        setHeight(410);
    }


    /**
     * Setter for the parent panel.
     * @param panel The panel.
     */
    public void setThemePanel (ThemePanel panel) {
        this.panel = panel;
    }


    /**
     * this method is called when the user aborts theming.
     * @param event The event.
     */
    @Override
    public void onClick(ClickEvent event) {
        this.hide();
    }


    /**
     * This method creates the property grid for available styling attributes.
     * @return The layout containing the UI elements.
     */
    protected VLayout createPropertyGrid(Style style) {
        VLayout vl = new VLayout();

        StaticTextItem name = new StaticTextItem("name", "Name");
        name.setValue(facet.getName());
        name.setTitleStyle("color:#000;");
        name.setTitleAlign(Alignment.LEFT);
        name.setDisabled(true);
        name.setShowDisabled(false);

        DynamicForm form = new DynamicForm();
        form.setFields(name);
        form.setColWidths("40%", "60%");


        vl.addMember(form);

        if (style == null) {
            SC.warn("No style found.");
            return vl;
        }

        // Done via array to keep the order.
        String[] sets = {"showlines",
                         "showpoints",
                         "linetype",
                         "linesize",
                         "linecolor",
                         "font",
                         "textstyle",
                         "textsize",
                         "pointcolor",
                         "pointsize",
                         "showpointlabel",
                         "textcolor",
                         "backgroundcolor",
                         "showbackground",
                         "showlinelabel",
                         "labelfontface",
                         "labelfontcolor",
                         "labelfontsize",
                         "labelfontstyle",
                         "textorientation",
                         "labelshowbg",
                         "labelbgcolor",
                         "bandwidth",
                         "bandwidthcolor",
                         "transparency",
                         "showminimum",
                         "showmaximum"};

        for (String settingName: sets) {
            StyleSetting set = style.getSetting(settingName);

            if (set == null || set.isHidden()) {
                continue;
            }

            DynamicForm property = createPropertyUI(
                set.getDisplayName(),
                set.getName().toLowerCase(),
                set.getType().toLowerCase(),
                set.getDefaultValue());
            if (property != null) {
                vl.addMember(property);
            }
        }

        // Add settings not in whitelist above.
        for (StyleSetting set: style.getSettings()) {

            if (Arrays.asList(sets).contains(set.getName()) ||
                set == null ||
                set.isHidden()
            ) {
                continue;
            }

            DynamicForm property = createPropertyUI(
                set.getDisplayName(),
                set.getName().toLowerCase(),
                set.getType().toLowerCase(),
                set.getDefaultValue());
            if (property != null) {
                vl.addMember(property);
            }
        }

        return vl;
    }


    /**
     * Create a property form.
     * @param dname The display name.
     * @param name The property name.
     * @param type The property type.
     * @param value The current value.
     *
     * @return The dynamic form for the attribute property.
     */
    protected DynamicForm createPropertyUI(
        String dname,
        String name,
        String type,
        String value)
    {
        df = new DynamicForm();
        df.setColWidths("40%", "60%");

        FormItem f;
        if(type.equals("int")) {
            f = new SelectItem(name, MSG.getString(name));
            if (name.equals("linesize")) {
                f = createLineSizeUI(f);
                f.setValue(value);
            }
            else if (name.equals("labelfontsize") || name.equals("textsize")) {
                LinkedHashMap<String, String> valueMap =
                    new LinkedHashMap<String, String>();
                valueMap.put("3", "3");
                valueMap.put("5", "5");
                valueMap.put("8", "8");
                valueMap.put("10", "10");
                valueMap.put("12", "12");
                valueMap.put("14", "14");
                valueMap.put("18", "18");
                valueMap.put("24", "24");
                f.setValueMap(valueMap);
                f.setValue(value);
            }
            else if (name.equals("bandwidth")) {
                LinkedHashMap<String, String> valueMap =
                    new LinkedHashMap<String, String>();
                valueMap.put("0", "0");
                valueMap.put("1", "1");
                valueMap.put("2", "2");
                valueMap.put("3", "3");
                valueMap.put("4", "4");
                valueMap.put("5", "5");
                valueMap.put("6", "6");
                valueMap.put("7", "7");
                valueMap.put("8", "8");
                valueMap.put("9", "9");
                valueMap.put("10", "10");
                valueMap.put("11", "11");
                f.setValueMap(valueMap);
                f.setValue(value);
            }
            else if (name.equals("pointsize")) {
                LinkedHashMap<String, String> valueMap =
                    new LinkedHashMap<String, String>();
                valueMap.put("1", "1");
                valueMap.put("2", "2");
                valueMap.put("3", "3");
                valueMap.put("4", "4");
                valueMap.put("5", "5");
                valueMap.put("6", "6");
                valueMap.put("7", "7");
                f.setValueMap(valueMap);
                f.setValue(value);
            }
            else if (name.equals("numclasses")) {
                LinkedHashMap<String, String> valueMap =
                    new LinkedHashMap<String, String>();
                valueMap.put("5", "5");
                valueMap.put("6", "6");
                valueMap.put("7", "7");
                valueMap.put("8", "8");
                valueMap.put("9", "9");
                valueMap.put("10", "10");
                valueMap.put("12", "12");
                valueMap.put("14", "14");
                valueMap.put("16", "16");
                valueMap.put("18", "18");
                valueMap.put("20", "20");
                f.setValueMap(valueMap);
                f.setValue(value);
                // FIXME: Make that work again
                return null;
            }
            else if (name.contains("transparency")) {
                LinkedHashMap<String, String> valueMap =
                    new LinkedHashMap<String, String>();
                for (int n = 10; n < 100; n += 10) {
                    valueMap.put(Integer.toString(n), n + "%");
                }
                f.setValueMap(valueMap);
                f.setValue(value);
            }
        }
        else if (type.equals("boolean")) {
            if(name.equals("textorientation")) {
                f = new SelectItem(name, MSG.getString(name));
                LinkedHashMap<String, String> valueMap =
                    new LinkedHashMap<String, String>();
                valueMap.put("true", MSG.getString("horizontal"));
                valueMap.put("false", MSG.getString("vertical"));
                f.setValueMap(valueMap);
                f.setValue(value);
            }
            else {
                CheckboxItem c = new CheckboxItem(name, MSG.getString(name));
                if(value.equals("true")) {
                    c.setValue(true);
                }
                else {
                    c.setValue(false);
                }
                c.setLabelAsTitle(true);
                f = c;
            }
        }
        else if (type.equals("color")) {
            ColorPickerItem c = new ColorPickerItem(name, MSG.getString(name));
            c.setValue(rgbToHtml(value));
            f = c;
        }
        else if (type.equals("double")) {
            f = new FormItem(name);
            IsFloatValidator fpv = new IsFloatValidator();

            f.setValidators(fpv);
            f.setValidateOnChange(true);
            f.setTitle(MSG.getString(name));
            f.addBlurHandler(new BlurHandler() {
                @Override
                public void onBlur(BlurEvent e) {
                     DoubleValidator validator = new DoubleValidator();
                     Map<?, ?> errors = e.getForm().getErrors();
                     if(validator.validate(e.getItem(), errors)) {
                         e.getForm().setErrors(errors, true);
                     }
                     else {
                         e.getForm().setErrors(errors, true);
                     }
                }
            });
            f.setValue(value);
        }
        else if (type.equals("dash")) {
            f = new SelectItem(name, MSG.getString(name));
            LinkedHashMap<String, String> valueIcons =
                new LinkedHashMap<String, String>();
            f.setImageURLPrefix(GWT.getHostPageBaseURL()
                + "images/linestyle-dash-");
            f.setImageURLSuffix(".png");
            f.setValueIconHeight(20);
            f.setValueIconWidth(80);
            LinkedHashMap<String, String> valueMap =
                new LinkedHashMap<String, String>();
            valueMap.put("10", "");
            valueMap.put("10,5", "");
            valueMap.put("20,10", "");
            valueMap.put("30,10", "");
            valueMap.put("20,5,15,5", "");
            valueIcons.put("10", "10");
            valueIcons.put("10,5", "10-5");
            valueIcons.put("20,10", "20-10");
            valueIcons.put("30,10", "30-10");
            valueIcons.put("20,5,15,5", "20-5-15-5");
            f.setValueIcons(valueIcons);
            f.setValueMap(valueMap);
            f.setValue(value);
        }
        else if (type.equals("font")) {
            f = new SelectItem(name, MSG.getString(name));
            LinkedHashMap<String, String> valueMap =
                new LinkedHashMap<String, String>();
            valueMap.put(
                "arial", "<span style='font-family:arial'>Arial</span>");
            valueMap.put(
                "courier", "<span style='font-family:courier'>Courier</span>");
            valueMap.put(
                "verdana", "<span style='font-family:verdana'>Verdana</span>");
            valueMap.put(
                "times", "<span style='font-family:times'>Times</span>");
            f.setValueMap(valueMap);
            f.setValue(value);
        }
        else if (type.equals("style")) {
            f = new SelectItem(name, MSG.getString(name));
            LinkedHashMap<String, String> valueMap =
                new LinkedHashMap<String, String>();
            valueMap.put(
                "standard", "<span style='font-style:normal'>Normal</span>");
            valueMap.put(
                "bold", "<span style='font-weight:bold'>Bold</span>");
            valueMap.put(
                "italic", "<span style='font-style:italic'>Italic</span>");
            f.setValueMap(valueMap);
            f.setValue(value);
        }
        else if (type.equals("symbol")) {
            //create an empty element as long as this property can not be
            //changed.
            f = new StaticTextItem("");
        }
        else {
            f = new FormItem();
        }
        f.setTitleStyle("color:#000;");
        f.setTitleAlign(Alignment.LEFT);
        df.setFields(f);
        df.addItemChangedHandler(new ItemChangedHandler() {
            @Override
            public void onItemChanged(ItemChangedEvent e) {
                String name = e.getItem().getName();
                String newValue = e.getNewValue().toString();
                setNewValue(name, newValue);
            }
        });

        return df;
    }


    protected FormItem createLineSizeUI(FormItem f) {
        LinkedHashMap<String, String> valueIcons =
            new LinkedHashMap<String, String>();
        f.setImageURLPrefix(GWT.getHostPageBaseURL() + "images/linestyle-");
        f.setImageURLSuffix("px.png");
        f.setValueIconHeight(20);
        f.setValueIconWidth(80);
        LinkedHashMap<String, String> valueMap =
            new LinkedHashMap<String, String>();
        valueMap.put("1", "");
        valueMap.put("2", "");
        valueMap.put("3", "");
        valueMap.put("4", "");
        valueMap.put("5", "");
        valueMap.put("6", "");
        valueMap.put("7", "");
        valueMap.put("8", "");
        valueIcons.put("1", "1");
        valueIcons.put("2", "2");
        valueIcons.put("3", "3");
        valueIcons.put("4", "4");
        valueIcons.put("5", "5");
        valueIcons.put("6", "6");
        valueIcons.put("7", "7");
        valueIcons.put("8", "8");
        f.setValueIcons(valueIcons);
        f.setValueMap(valueMap);
        return f;
    }


    /**
     * Static method to convert a color from RGB to HTML notation.
     * @param rgb String in RGB notation.
     *
     * @return String in HTML notation.
     */
    protected static String rgbToHtml(String rgb) {
        String[] parts = rgb.split(",");
        int values[] = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
            try {
                values[i] = Integer.parseInt(parts[i]);
            }
            catch(NumberFormatException nfe) {
                return "#000000";
            }
        }
        String hex = "#";
        for (int i = 0; i < values.length; i++) {
           if (values[i] < 16) {
                hex += "0";
           }
           hex += Integer.toHexString(values[i]);
        }
        return hex;
    }


    /**
     * Static method to convert a color from HTML to RGB notation.
     * @param html String in HTML notation.
     *
     * @return String in RGB notation.
     */
    protected static String htmlToRgb(String html) {
        if (!html.startsWith("#")) {
            return "0, 0, 0";
        }

        int r = Integer.valueOf(html.substring(1, 3), 16);
        int g = Integer.valueOf(html.substring(3, 5), 16);
        int b = Integer.valueOf(html.substring(5, 7), 16);

        return r + ", " + g + ", " + b;
    }


    /**
     * Saves the current style attributes and requests a redraw.
     */
    protected void saveStyle () {
        GWT.log("StyleEditorWindow.saveStyle()");
        Config config = Config.getInstance();
        String locale = config.getLocale();

        itemAttributeService.setCollectionItemAttribute(
            this.collection,
            attributes.getArtifact(),
            locale,
            attributes,
            new AsyncCallback<Void>() {
                @Override
                public void onFailure (Throwable caught) {
                    GWT.log("Could not set Collection item attributes.");
                }
                @Override
                public void onSuccess(Void v) {
                    GWT.log("Successfully saved collection item attributes.");
                    panel.requestRedraw();
                }
            });


        this.hide();
    }


    /**
     * Sets a new value for an attribute.
     * @param name Attribute name.
     * @param value The new value.
     */
    protected final void setNewValue(String name, String value) {
        Theme t = facet.getTheme();
        Style s = attributes.getStyle(t.getFacet(), t.getIndex());
        StyleSetting set = s.getSetting(name);
        String type = set.getType();

        if(name.indexOf("color") != -1
           || (type != null && type.toLowerCase().indexOf("color") > -1)) {
            value = htmlToRgb(value);
        }
        set.setDefaultValue(value);
    }


    protected final void setNewStyle(Style style) {
        Theme t = facet.getTheme();
        Style s = attributes.getStyle(t.getFacet(), t.getIndex());
        attributes.removeStyle(s.getName());
        attributes.appendStyle(style);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
