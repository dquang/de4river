/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.validator.Validator;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.RangeData;

import java.util.ArrayList;
import java.util.List;


/**
 * An UIProvider for inserting ranges.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class RangePanel extends AbstractUIProvider {

    private static final long serialVersionUID = -9213089589150335651L;

    public static final String FIELD_LOWER = "field_lower";
    public static final String FIELD_UPPER = "field_upper";


    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected DynamicForm lowerForm;
    protected DynamicForm upperForm;

    protected String dataName;


    public abstract Object getMaxLower();

    public abstract Object getMaxUpper();



    @Override
    public Canvas create(DataList data) {
        setDataName(data);

        VLayout root = new VLayout();

        root.addMember(createLabel(data));
        root.addMember(createForm(data));
        root.addMember(getNextButton());

        initDefaults(data);

        return root;
    }


    @Override
    public Canvas createOld(DataList dataList) {
        Data       data  = dataList.get(0);
        DataItem[] items = data.getItems();

        HLayout layout = new HLayout();

        Label label = new Label(dataList.getLabel());
        label.setWidth(200);
        label.setHeight(20);

        Label value = new Label(items[0].getLabel());
        value.setHeight(20);

        layout.addMember(label);
        layout.addMember(value);
        layout.addMember(getBackButton(dataList.getState()));

        return layout;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        if (!lowerForm.validate()) {
            String msg = MSG.error_validate_range();
            msg = msg.replace("$1", getLower());
            msg = msg.replace("$2", String.valueOf(getMaxLower()));
            msg = msg.replace("$3", String.valueOf(getMaxLower()));
            errors.add(msg);
        }

        if (!upperForm.validate()) {
            String msg = MSG.error_validate_range();
            msg = msg.replace("$1", getUpper());
            msg = msg.replace("$2", String.valueOf(getMaxLower()));
            msg = msg.replace("$3", String.valueOf(getMaxUpper()));
            errors.add(msg);
        }

        return errors;
    }


    @Override
    protected Data[] getData() {
        return new Data[0];
    }


    protected void initDefaults(DataList dataList) {
        RangeData data = findRangeData(dataList);

        if (data != null) {
            setLower(String.valueOf(data.getDefaultLower()));
            setUpper(String.valueOf(data.getDefaultUpper()));
        }
    }


    protected RangeData findRangeData(DataList dataList) {
        for (int i = 0, n = dataList.size(); i < n; i++) {
            Data tmp = dataList.get(i);

            if (tmp instanceof RangeData) {
                return (RangeData) tmp;
            }
        }
        return null;
    }


    protected void setDataName(DataList dataList) {
        Data data = dataList.get(0);

        this.dataName = data.getLabel();
    }


    public String getDataName() {
        return dataName;
    }


    public String getLower() {
        return lowerForm.getValueAsString(FIELD_LOWER);
    }


    public void setLower(String lower) {
        lowerForm.setValue(FIELD_LOWER, lower);
    }


    public String getUpper() {
        return upperForm.getValueAsString(FIELD_UPPER);
    }


    public void setUpper(String upper) {
        upperForm.setValue(FIELD_UPPER, upper);
    }


    protected Canvas createLabel(DataList dataList) {
        RangeData rangeData = findRangeData(dataList);

        if (rangeData == null) {
            return new Canvas();
        }

        Label label = new Label(rangeData.getDescription());

        label.setWidth100();
        label.setHeight(25);

        return label;
    }


    protected Canvas createForm(DataList dataList) {
        lowerForm = createLowerForm(dataList);
        upperForm = createUpperForm(dataList);

        HLayout formLayout = new HLayout();
        formLayout.addMember(lowerForm);
        formLayout.addMember(createSpacer());
        formLayout.addMember(upperForm);

        return formLayout;
    }


    protected DynamicForm newForm() {
        DynamicForm form = new DynamicForm();
        form.setTitlePrefix("");
        form.setTitleSuffix("");
        form.setTitle("");
        form.setTitleField("");

        return form;
    }


    protected FormItem newFormItem(String name) {
        TextItem item = new TextItem(name, "");
        item.setShowTitle(false);

        return item;
    }


    protected DynamicForm createLowerForm(DataList dataList) {
        DynamicForm lowerForm = newForm();
        FormItem    lower     = createLowerField(dataList);

        lowerForm.setFields(lower);

        return lowerForm;
    }


    protected DynamicForm createUpperForm(DataList dataList) {
        DynamicForm upperForm = newForm();
        FormItem    upper     = createUpperField(dataList);

        upperForm.setFields(upper);

        return upperForm;
    }


    protected Canvas createSpacer() {
        Label spacer = new Label("-");
        spacer.setWidth(25);
        spacer.setHeight(25);
        spacer.setAlign(Alignment.CENTER);

        return spacer;
    }


    protected FormItem createLowerField(DataList dataList) {
        return createField(FIELD_LOWER, createLowerValidators(dataList));
    }


    protected FormItem createUpperField(DataList dataList) {
        return createField(FIELD_UPPER, createUpperValidators(dataList));
    }


    protected FormItem createField(String name, Validator[] validators) {
        FormItem field = newFormItem(name);

        if (validators != null && validators.length > 0) {
            field.setValidators(validators);
        }

        return field;
    }


    protected Validator[] createLowerValidators(DataList dataList) {
        return null;
    }


    protected Validator[] createUpperValidators(DataList dataList) {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
