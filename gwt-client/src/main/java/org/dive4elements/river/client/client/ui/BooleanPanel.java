/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class BooleanPanel extends TextProvider {

    private static final long serialVersionUID = -8448442865635399232L;

    public static final String FIELD_NAME  = "boolean_field";
    public static final int    TITLE_WIDTH = 0;

    protected String dataName;


    @Override
    public Canvas create(DataList dataList) {
        Data       data  = dataList.get(0);
        DataItem[] items = data.getItems();

        this.dataName = data.getLabel();

        VLayout layout = new VLayout();
        Label   label  = new Label(data.getDescription());
        Canvas  form   = createForm(getTitle(items[0]));

        layout.setMembersMargin(10);
        layout.setHeight(35);
        label.setHeight(35);

        layout.addMember(label);
        layout.addMember(form);
        layout.addMember(getNextButton());
        layout.setMembersMargin(10);

        initDefaultValues(dataList);

        return layout;
    }


    protected String getTitle(DataItem item) {
        return item.getLabel();
    }


    @Override
    protected void initDefaultValues(DataList dataList) {
        Data     data = dataList.get(0);
        DataItem item = data.getDefault();

        String value = item.getStringValue();
        Boolean bool = Boolean.valueOf(value);

        if (bool) {
            form.setValue(getFieldName(), bool);
        }
    }


    @Override
    protected FieldType getFieldType() {
        return FieldType.BOOLEAN;
    }


    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }


    @Override
    protected String getDataName() {
        return dataName;
    }


    @Override
    protected String getValueAsString() {
        Boolean aBool = (Boolean) form.getValue(getFieldName());

        return aBool != null ? aBool.toString() : "false";
    }


    @Override
    protected int getTitleWidth() {
        return TITLE_WIDTH;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
