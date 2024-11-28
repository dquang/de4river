/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.LayoutSpacer;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.IntegerOptionsData;
import org.dive4elements.river.client.shared.model.MultiAttributeData;
import org.dive4elements.river.client.shared.model.StringOptionsData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ParameterMatrixPanel extends AbstractUIProvider {

    private static final long serialVersionUID = -5827445025768340371L;

    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    private ParameterMatrix matrix;

    @Override
    protected Data[] getData() {
        Map<String, List<String>> selection = matrix.getSelection();
        Set<Map.Entry<String, List<String>>> entries = selection.entrySet();

        Data[] list = new Data[matrix.getColumnNames().size()];

        int i = 0;

        for (Map.Entry<String, List<String>> entry: entries) {
            String value = buildValueString(entry.getValue());

            DataItem item = new DefaultDataItem(
                entry.getKey(),
                null,
                value);

            list[i++] = new DefaultData(
                entry.getKey(), null, null, new DataItem[] { item });
        }

        // To delete old values already given, construct empty ones
        // for all not-specified options.
        for (String colName : matrix.getColumnNames()) {
            boolean found = false;
            for (Data data : list) {
                if (data != null && data.getLabel().equals(colName)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add an empty data for this.
                list[i++] = new DefaultData(
                    colName, null, null, new DataItem[] { });
            }
        }

        return list;
    }


    protected String buildValueString(List<String> values) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;

        for (String value: values) {
            if (!first) {
                sb.append(";");
            }

            sb.append(value);

            first = false;
        }

        return sb.toString();
    }


    /** Canvas to show in non-edit mode. */
    @Override
    public Canvas createOld(DataList dataList) {
        HLayout layout  = new HLayout();
        VLayout vLayout = new VLayout();

        layout.setWidth(300);
        vLayout.setWidth(280);

        for (int i = 0, n = dataList.size(); i < n; i++) {
            HLayout row  = new HLayout();
            VLayout cols = new VLayout();

            row.setWidth(300);
            cols.setWidth(100);

            Data       data  = dataList.get(i);
            DataItem[] items = data.getItems();

            Label parameter = new Label(data.getDescription());
            parameter.setWidth(200);

            for (int j = 0, m = items.length; j < m; j++) {
                DataItem item  = items[j];
                Label    value = new Label(item.getLabel());

                value.setValign(
                    com.smartgwt.client.types.VerticalAlignment.TOP);
                value.setWidth(130);
                value.setHeight(15);

                cols.addMember(value);
                LayoutSpacer spacer = new LayoutSpacer();
                spacer.setHeight(5);
                cols.addMember(spacer);
            }

            row.addMember(parameter);
            row.addMember(cols);

            vLayout.addMember(row);
        }

        Canvas back = getBackButton(dataList.getState());

        layout.addMember(vLayout);
        layout.addMember(back);

        return layout;
    }


    /** Create the main canvas in the "editing" mode. */
    @Override
    public Canvas create(DataList dataList) {
        VLayout v = new VLayout();
        v.addMember(createTitle(dataList));

        matrix = new ParameterMatrix();

        for (Data data: dataList.getAll()) {
            if (data instanceof IntegerOptionsData) {
                matrix.addColumn((IntegerOptionsData) data);
            }
            else if (data instanceof StringOptionsData) {
                matrix.addColumn((StringOptionsData) data);
            }
            else if (data instanceof MultiAttributeData) {
                matrix.addColumn((MultiAttributeData)data);
            }
        }

        // If too many items are shown, show it in the helper Panel.
        // TODO its not about the datalist, but about the "rows" in the data.
        if (dataList.getAll().size() > 5) {
            v.addMember(matrix.create(false));
        }
        else {
            helperContainer.addMember(matrix.create(true));
        }
        v.addMember(getNextButton());

        return v;
    }


    /** Reaturns a label with description of first Data. */
    protected Canvas createTitle(DataList dataList) {
        Data data = dataList.get(0);
        Label label = new Label(data.getDescription());
        label.setHeight(35);

        return label;
    }


    /** Selection shall not be empty. */
    @Override
    public List<String> validate() {
        GWT.log ("validation. validation. validation. ");
        List<String> errors = new ArrayList<String>();
        // Early stop on one (only) error.
        boolean ok = false;
        for (Map.Entry<String, List<String>> entry:
                 matrix.getSelection().entrySet()
        ) {
            /* single entries are allowed!!
                if (entry.getValue() == null || entry.getValue().size() == 0) {
                    errors.add(MESSAGES.error_values_needed());
                    return errors;
                }
                */
            if (entry.getValue() != null && entry.getValue().size() > 0) {
                ok = true;
            }
        }
        if (!ok) {
            errors.add(MESSAGES.error_values_needed());
        }
        return errors;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
