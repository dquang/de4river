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
import com.smartgwt.client.widgets.form.validator.CustomValidator;
import com.smartgwt.client.widgets.form.validator.Validator;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class AutoIntegerPanel extends TextProvider {

    private static final long serialVersionUID = -6525461829035465820L;

    public static final String FIELD_NAME = "integer_field";

    public static final String FIELD_DEFAULT_VALUE = "auto";


    protected static FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected String dataName;


    @Override
    public Canvas create(DataList dataList) {
        Data   data   = dataList.get(0);
        this.dataName = data.getLabel();

        Canvas label  = new Label(data.getDescription());
        Canvas form   = createForm(getTitle());
        Canvas submit = getNextButton();

        VLayout layout = new VLayout();
        layout.setHeight(35);
        label.setHeight(35);

        layout.addMember(label);
        layout.addMember(form);
        layout.addMember(submit);
        layout.setMembersMargin(10);

        initDefaultValues(dataList);

        return layout;
    }


    @Override
    protected void initDefaultValues(DataList dataList) {
        super.initDefaultValues(dataList);

        String def = getValueAsString();

        if (def == null || def.length() == 0) {
            form.setValue(getFieldName(), FIELD_DEFAULT_VALUE);
        }
    }


    protected String getTitle() {
        return MSG.uesk_profile_distance();
    }


    @Override
    protected String getDataName() {
        return dataName;
    }


    @Override
    protected String getValueAsString() {
        String v = (String) form.getValue(getFieldName());
        return v.toLowerCase();
    }


    @Override
    protected Validator getValidator() {
        Validator v = new AutoIntegerValidator();
        v.setValidateOnChange(false);

        return v;
    }


    public class AutoIntegerValidator extends CustomValidator {
        @Override
        protected boolean condition(Object value) {
            String v = (String) value;

            if (v == null || v.length() == 0) {
                return false;
            }

            if (v.trim().equalsIgnoreCase("auto")) {
                return true;
            }

            try {
                Integer.parseInt(v);
                return true;
            }
            catch (NumberFormatException nfe) {
                return false;
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
