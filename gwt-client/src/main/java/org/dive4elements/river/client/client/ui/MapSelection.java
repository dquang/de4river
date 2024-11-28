/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;


/**
 * This UIProvider displays the DataItems contained in the Data object in a
 * combo box as SelectProvider does. Furthermore, there is a map displayed that
 * lets the user choose a river by selecting it on the map.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class MapSelection extends SelectProvider {

    private static final long serialVersionUID = 1261822454641198692L;

    protected ModuleSelection moduleSelection;

    public MapSelection() {
    }


    /**
     * This method currently returns a
     * {@link com.smartgwt.client.widgets.form.DynamicForm} that contains all
     * data items in a combobox stored in <i>data</i>.<br>
     *
     * <b>TODO: The map panel for the river selection needs to be
     * implemented!</b>
     *
     * @param data The {@link DataList} object.
     *
     * @return a combobox.
     */
    @Override
    protected Canvas createWidget(DataList data) {
        GWT.log("MapSelection - create()");

        HLayout h = new HLayout();
        h.setAlign(VerticalAlignment.TOP);
        h.setHeight(100);
        moduleSelection = new ModuleSelection();

        Canvas form  = moduleSelection.create(data);
        form.setWidth(400);
        form.setLayoutAlign(VerticalAlignment.TOP);

        // TODO implement event handling in the river map
        // (here? or in LinkSelection)

        h.addMember(form);

        return h;
    }

    @Override
    protected Data[] getData() {
        if (moduleSelection != null) {
            return moduleSelection.getData();
        }
        else {
            return null;
        }
    }

    public ModuleSelection getModuleSelection() {
        return this.moduleSelection;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
