/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.utils;

import java.util.Map;

import com.google.gwt.core.client.GWT;

import com.smartgwt.client.widgets.form.fields.FormItem;

import org.dive4elements.river.client.client.FLYSConstants;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class IntegerValidator implements Validator {

    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    /**
     *
     */
    public boolean validate(FormItem item, Map errors) {
        boolean valid = true;

        String v = item.getValue().toString();

        try {
            if (v == null) {
                throw new NumberFormatException("empty");
            }

            int value = Integer.parseInt(v);

            errors.remove(item.getFieldName());
        }
        catch (NumberFormatException nfe) {
            errors.put(item.getFieldName(), MSG.wrongFormat());

            item.focusInItem();

            valid = false;
        }
        return valid;
    }
}
