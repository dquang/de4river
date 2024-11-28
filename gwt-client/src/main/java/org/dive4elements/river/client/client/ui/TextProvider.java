/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.events.ItemChangedEvent;
import com.smartgwt.client.widgets.form.events.ItemChangedHandler;
import com.smartgwt.client.widgets.form.validator.Validator;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;

import java.util.ArrayList;
import java.util.List;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class TextProvider
extends      AbstractUIProvider
implements   ItemChangedHandler
{
    private static final long serialVersionUID = -6868303464989138497L;

    public static final String FIELD_NAME  = "textprovider_inputfield";
    public static final int    FORM_WIDTH  = 400;
    public static final int    TITLE_WIDTH = 75;


    protected static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected DynamicForm form;


    @Override
    public Canvas create(DataList dataList) {
        Canvas form = createForm();

        initDefaultValues(dataList);

        return form;
    }


    protected Canvas createForm() {
        return createForm(null);
    }


    protected Canvas createForm(String title) {
        form = new DynamicForm();
        form.addItemChangedHandler(this);
        form.setTitlePrefix("");
        form.setTitleSuffix(": ");
        form.setTitleAlign(Alignment.LEFT);
        form.setTitleOrientation(TitleOrientation.LEFT);
        form.setTitleWidth(getTitleWidth());
        form.setWidth(getFormWidth());

        DataSourceField item = createField();
        item.setTitle(title);

        Validator validator = getValidator();
        if (validator != null) {
            item.setValidators(validator);
        }

        DataSource source = new DataSource();
        source.setFields(item);

        form.setDataSource(source);

        return form;
    }


    protected void initDefaultValues(DataList dataList) {
        Data     data = dataList.get(0);
        DataItem item = data.getDefault();

        String value = item.getStringValue();

        form.setValue(getFieldName(), value);
    }


    protected DataSourceField createField() {
        return new DataSourceField(getFieldName(), getFieldType());
    }


    /**
     * Get field name.
     * @return fields name (developer-centric).
     */
    protected String getFieldName() {
        return FIELD_NAME;
    }


    /**
     * Get field type.
     * @return fields type.
     */
    protected FieldType getFieldType() {
        return FieldType.TEXT;
    }


    protected Validator getValidator() {
        return null;
    }


    protected int getFormWidth() {
        return FORM_WIDTH;
    }


    protected int getTitleWidth() {
        return TITLE_WIDTH;
    }


    /** @return null. */
    protected String getDataName() {
        return null;
    }


    protected String getValueAsString() {
        return (String) form.getValue(getFieldName());
    }


    @Override
    public Canvas createOld(DataList dataList) {
        Data       data  = dataList.get(0);
        DataItem[] items = data.getItems();

        HLayout layout = new HLayout();
        Label   label  = new Label(dataList.getLabel());
        Label   value  = new Label(items[0].getLabel());

        layout.setHeight(35);
        layout.setWidth(400);
        label.setWidth(200);

        layout.addMember(label);
        layout.addMember(value);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        if (!form.validate()) {
            errors.add(MSG.wrongFormat());
        }

        return errors;
    }


    @Override
    protected Data[] getData() {
        String value = getValueAsString();
        String name  = getDataName();

        DataItem item = new DefaultDataItem(name, name, value);
        return new Data[] { new DefaultData(
            name, null, null, new DataItem[] { item }) };
    }


    @Override
    public void onItemChanged(ItemChangedEvent event) {
        form.validate();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
