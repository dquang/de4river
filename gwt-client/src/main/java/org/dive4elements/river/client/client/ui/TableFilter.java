/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.event.FilterHandler;
import org.dive4elements.river.client.client.event.StringFilterEvent;
import org.dive4elements.river.client.client.FLYSConstants;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class TableFilter
extends      HLayout
implements   ChangedHandler, KeyUpHandler
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected List<FilterHandler> handlers;

    protected TextItem searchfield;


    public TableFilter() {
        super();
        searchfield = new TextItem(MESSAGES.search());
        handlers    = new ArrayList<FilterHandler>();

        searchfield.addChangedHandler(this);
        searchfield.addKeyUpHandler(this);
        searchfield.setWidth(120);

        DynamicForm form = new DynamicForm();
        form.setFields(searchfield);

        addMember(form);
    }


    public void onChanged(ChangedEvent event) {
        // This event handler is to slow...
//        fireFilterCriteriaChanged(getSearchString());
    }


    public void onKeyUp(KeyUpEvent event) {
        //To deactivate "As you type" filter add
        // ' && event.getKeyName().equals("Enter")'
        // to the if-clause.
        if (event != null) {
            fireFilterCriteriaChanged(getSearchString());
        }
    }


    public String getSearchString() {
        if (searchfield.getValueAsString() == null) {
            return "";
        }
        else {
            return searchfield.getValueAsString();
        }
    }


    public void addFilterHandler(FilterHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    protected void fireFilterCriteriaChanged(String searchstring) {
        StringFilterEvent filter = new StringFilterEvent(searchstring);

        for (FilterHandler handler: handlers) {
            handler.onFilterCriteriaChanged(filter);
        }
    }


    public void clear() {
        searchfield.clearValue();
        fireFilterCriteriaChanged("");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
