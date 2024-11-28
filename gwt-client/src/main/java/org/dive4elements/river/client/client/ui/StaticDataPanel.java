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
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;


/**
 * This UIProvider displays the old DataItems of GaugeDischargeCurveArtifact
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public class StaticDataPanel
extends      AbstractUIProvider
{
    private static final long serialVersionUID = 7411866539525588336L;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants messages = GWT.create(FLYSConstants.class);

    /** The combobox.*/
    protected DynamicForm form;

    @Override
    public Canvas create(DataList data) {
        VLayout layout   = new VLayout();
        return layout;
    }

    @Override
    public Canvas createOld(DataList dataList) {
        VLayout vLayout = new VLayout();
        vLayout.setWidth("400px");

        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            Data data        = dataList.get(i);
            DataItem[] items = data.getItems();

            for (DataItem item: items) {
                HLayout hLayout = new HLayout();

                hLayout.addMember(new Label(MSG.getString(data.getLabel())));
                hLayout.addMember(new Label(item.getStringValue()));

                vLayout.addMember(hLayout);
                vLayout.setWidth("130px");
            }
        }

        return vLayout;
    }

    @Override
    protected Data[] getData() {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
