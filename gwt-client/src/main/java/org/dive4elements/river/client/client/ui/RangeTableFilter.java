/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.FilterHandler;
import org.dive4elements.river.client.client.event.RangeFilterEvent;
import org.dive4elements.river.client.client.utils.DoubleValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class RangeTableFilter
extends      HLayout
implements   ChangedHandler, KeyUpHandler
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected List<FilterHandler> handlers;

    protected TextItem fromField;
    protected TextItem toField;
    protected DynamicForm filterForm;

    public RangeTableFilter() {
        super();
        fromField = new TextItem();
        fromField.setTitle(MESSAGES.from());
        fromField.setWidth(60);
        toField = new TextItem();
        toField.setTitle(MESSAGES.to());
        toField.setWidth(60);


        handlers    = new ArrayList<FilterHandler>();

        fromField.addChangedHandler(this);
        fromField.addKeyUpHandler(this);
        toField.addChangedHandler(this);
        toField.addKeyUpHandler(this);

        filterForm = new DynamicForm();
        filterForm.setNumCols(4);
        filterForm.setFields(fromField, toField);

        addMember(filterForm);
    }


    @Override
    public void onChanged(ChangedEvent event) {
        // This event handler is to slow...
//        fireFilterCriteriaChanged(getSearchString());
    }


    @Override
    public void onKeyUp(KeyUpEvent event) {
        DoubleValidator validator = new DoubleValidator();
        Map<?,?> errors = filterForm.getErrors();
        if(event.getItem().getValue() != null &&
           !validator.validate(event.getItem(), errors)) {
            filterForm.setErrors(errors, true);
            GWT.log("no valid input!");
            return;
        }
        else {
            errors.clear();
            filterForm.setErrors(errors, true);
        }
        //To deactivate "As you type" filter add
        // ' && event.getKeyName().equals("Enter")'
        // to the if-clause.
        if (event != null) {
            fireFilterCriteriaChanged(getFrom(), getTo());
        }
    }


    public String getFrom() {
        if (fromField.getValueAsString() == null) {
            return "";
        }
        else {
            return fromField.getValueAsString();
        }
    }


    public String getTo() {
        if (toField.getValueAsString() == null) {
            return "";
        }
        else {
            return toField.getValueAsString();
        }
    }


    public void addFilterHandler(FilterHandler handler) {
        if (handler != null) {
            handlers.add(handler);
        }
    }


    protected void fireFilterCriteriaChanged(String from, String to) {
        RangeFilterEvent filter = new RangeFilterEvent(from, to);

        for (FilterHandler handler: handlers) {
            handler.onFilterCriteriaChanged(filter);
        }
    }


    @Override
    public void clear() {
        fromField.clearValue();
        toField.clearValue();
        fireFilterCriteriaChanged("", "");
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
