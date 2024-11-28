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
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.widgets.form.fields.FormItem;

import org.dive4elements.river.client.client.FLYSConstants;

/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class DoubleValidator implements Validator {

    /** The interface that provides i18n messages. */
    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);


    /** Statically determine doubility of String value. */
    public static boolean isDouble(Object obj) {
        if (obj == null) {
            return false;
        }

        boolean valid = true;
        String v = obj.toString();

        NumberFormat f = NumberFormat.getDecimalFormat();

        try {
            if (v == null) {
                throw new NumberFormatException("empty");
            }

            double value = f.parse(v);
        }
        catch (NumberFormatException nfe) {
            valid = false;
        }
        return valid;

    }


    /**
     * @return true if items value can be converted to double, if false,
     *         expect error message in \param errors map.
     */
    public boolean validate(FormItem item, Map errors) {
        boolean valid = true;

        if(item.getValue() == null) {
            return false;
        }
        String v = item.getValue().toString();

        NumberFormat f = NumberFormat.getDecimalFormat();

        try {
            if (v == null) {
                throw new NumberFormatException("empty");
            }

            double value = f.parse(v);

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
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

