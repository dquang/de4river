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

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.ModuleService;
import org.dive4elements.river.client.client.services.ModuleServiceAsync;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.Module;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The ModuleSelection combines the river selection and the module selection in
 * one widget. It will display a vertical splitted widget - the upper part will
 * render checkboxes for each module, the lower one will display a combobox at
 * the left and a map panel on the right to choose the river.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ModuleSelection extends MapSelection {

    private static final long serialVersionUID = -5634831815175543328L;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    /** The module checkboxes.*/
    protected static RadioGroupItem radio;

    /** */
    protected Module[] modules;

    /** The ModuleService used to retrieve the available modules of a user.*/
    protected ModuleServiceAsync moduleService = GWT.create(
        ModuleService.class);

    private Map<String, List<String> > modulesRiverMap;
    protected Map<String, HLayout> rivers;

    /**
     * The default constructor.
     */
    public ModuleSelection() {
        rivers = null;
        modulesRiverMap = new LinkedHashMap<String, List<String> >();

        readModules();
    }


    /**
     * This method returns a widget that renders the checkboxes for each module
     * and the MapSelection that lets the user choose the river.
     *
     * @param data The provided rivers.
     *
     * @return the module selection combined with the river selection.
     */
    @Override
    public Canvas create(DataList data) {
        GWT.log("ModuleSelection - create()");
        createCallback();
        VLayout newLayout = new VLayout();
        newLayout.setMembersMargin(10);
        newLayout.setAlign(VerticalAlignment.TOP);
        Canvas moduleSelection = createWidget();

        moduleSelection.setHeight(100);
        newLayout.setHeight(70);
        newLayout.addMember(moduleSelection);

        return newLayout;
    }

    private void readModules() {
        Config config = Config.getInstance();
        String locale = config.getLocale();

        moduleService.list(locale, new AsyncCallback<Module[]>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("Could not recieve a list of modules.");
                SC.warn(MSG.getString(caught.getMessage()));
            }

            @Override
            public void onSuccess(Module[] newmodules) {
                GWT.log("Retrieved " + newmodules.length + " modules.");
                modules = newmodules;
                setModules();
            }
        });
    }

    private void checkRivers(String selected) {
        if (selected == null) {
            selected = getSelectedModule();
        }
        if (rivers != null
            && !rivers.isEmpty()
            && modules != null
            && selected != null
        ) {
            List<String> allowedRivers = modulesRiverMap.get(selected);
            if ( allowedRivers == null ) {
                GWT.log("No configured rivers for module: " + selected);
            }
            for (Map.Entry<String, HLayout> s: rivers.entrySet()) {
                if ( allowedRivers == null ) {
                    s.getValue().hide();
                    continue;
                }
                if (!allowedRivers.contains(s.getKey())) {
                    s.getValue().hide();
                } else {
                    s.getValue().show();
                }
            }
        }
    }

    private void setModules() {
        LinkedHashMap<String, String> values =
            new LinkedHashMap<String, String>();

        if (this.modules!= null) {
            for(Module module : this.modules) {
                values.put(module.getName(), module.getLocalizedName());
                if (module.isSelected()) {
                    GWT.log("Module " + module.getName() + " is selected.");
                    if (radio != null) {
                        radio.setDefaultValue(module.getName());
                    }
                }
                modulesRiverMap.put(module.getName(), module.getRivers());
            }
        }
        if (radio != null) {
            radio.setValueMap(values);
        }
        checkRivers(null);
    }

    /**
     * Creates a widget that displays a checkbox for each module.
     *
     * @return a widget with checkboxes.
     */
    protected Canvas createWidget() {
        HLayout layout = new HLayout();

        Label      label = new Label(MESSAGES.module_selection());
        DynamicForm form = new DynamicForm();

        radio = new RadioGroupItem("plugin");
        radio.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                checkRivers((String)event.getValue());
            }
        });

        label.setWidth(50);
        label.setHeight(25);


        radio.setShowTitle(false);
        radio.setVertical(true);

        setModules();

        form.setFields(radio);

        layout.addMember(label);
        layout.addMember(form);

        return layout;
    }


    /**
     * This method prepares the data of two widgets - the module selection and
     * the river selection. The returning field will contain the Data that
     * represents the module selection at first position, the second position
     * stores the Data object that represents the river selection.
     *
     * @return the Data that was chosen in this widget.
     */
    @Override
    protected Data[] getData() {

        String module = radio.getValueAsString();

        DataItem[] items = new DefaultDataItem[1];
        items[0]         = new DefaultDataItem(module, module, module);

        Data       data  = new DefaultData("module", null, null, items);

        return new Data[] {data};
    }

    public void setRivers(Map<String, HLayout> rivers) {
        this.rivers = rivers;
    }

    private native void createCallback() /*-{
        $wnd.getModule = @org.dive4elements.river.client.client.ui.ModuleSelection::getSelectedModule();
    }-*/;

    private static String getSelectedModule() {
        if (radio == null) {
            return null;
        }
        return radio.getValueAsString();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
