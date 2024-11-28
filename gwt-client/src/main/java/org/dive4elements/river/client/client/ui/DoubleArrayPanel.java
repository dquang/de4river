/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.TitleOrientation;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.FocusHandler;

import org.dive4elements.river.client.client.FLYSConstants;

import java.util.Map;

public class DoubleArrayPanel
extends      DynamicForm
{
    /** The message class that provides i18n strings. */
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    protected TextItem ti;

    private String title;

    /** The constant input field name. */
    public static final String FIELD_NAME = "doublearray";


    public DoubleArrayPanel(
        String title,
        double[] values,
        BlurHandler handler)
    {
        this(title, values, handler, null, TitleOrientation.RIGHT);
    }


    /**
     * Creates a new form with a single input field that displays an array of
     * double values.
     *
     * @param name The name of the TextItem.
     * @param title The title of the TextItem.
     * @param values The double values that should be displayed initially.
     * @param blurHandler The BlurHandler that is used to valide the input.
     * @param focusHandler The FocueHandler that is used to valide the input.
     */
    public DoubleArrayPanel(
        String title,
        double[] values,
        BlurHandler blurHandler,
        FocusHandler focusHandler,
        TitleOrientation titleOrientation)
    {
        this.title = title;
        ti                 = new TextItem(FIELD_NAME);
        StaticTextItem sti = new StaticTextItem("staticarray");

        ti.setShowTitle(false);
        sti.setShowTitle(false);
        sti.setValue(title);

        ti.addBlurHandler(blurHandler);
        if (focusHandler != null) {
            ti.addFocusHandler(focusHandler);
        }

        if (titleOrientation == TitleOrientation.RIGHT) {
            setFields(ti, sti);
        }
        else {
            setFields(sti, ti);
        }

        setTitleOrientation(titleOrientation);
        setNumCols(2);

        if (values == null) {
            return;
        }

        NumberFormat f = NumberFormat.getDecimalFormat();

        StringBuilder text = new StringBuilder();
        boolean firstItem  = true;

        for (double val: values) {
            if (!firstItem) {
                text.append(" ");
            }

            text.append(f.format(val));

            firstItem = false;
        }

        ti.setValue(text.toString());
    }


    /**
     * This method takes the double array to set the values to the textbox.
     *
     * @param values The double values.
     */
    public void setValues(double[] values) {
        NumberFormat f = NumberFormat.getDecimalFormat();

        if(values == null || values.length == 0) {
            ti.clearValue();
            return;
        }
        StringBuilder text = new StringBuilder();
        boolean firstItem  = true;
        if (values != null) {
            for (double val: values) {
                if (!firstItem) {
                    text.append(" ");
                }

                text.append(f.format(val));

                firstItem = false;
            }
        }

        ti.clearValue();
        ti.setValue(text.toString());
    }


    /**
     * This method appends a double value to the current list of values.
     *
     * @param value A new value.
     */
    public void addValue(double value) {
        NumberFormat f = NumberFormat.getDecimalFormat();

        String current = ti.getValueAsString();

        if (current == null || current.length() == 0) {
            current = f.format(value);
        }
        else {
            current += " " + f.format(value);
        }

        ti.setValue(current);
    }


    protected boolean validateForm() {
        return validateForm(ti);
    }


    /**
     * This method validates the entered text in the location input field. If
     * there are values that doesn't represent a valid location, an error is
     * displayed.
     *
     * @param item The FormItem.
     */
    @SuppressWarnings("unchecked")
    protected boolean validateForm(FormItem item) {
        if (item instanceof StaticTextItem) {
            return true;
        }

        boolean  valid = true;
        String   value = (String) item.getValue();

        if (value == null) {
            return valid;
        }

        String[] parts = value.split("\\s+");

        if (parts == null) {
            return valid;
        }

        NumberFormat nf = NumberFormat.getDecimalFormat();
        @SuppressWarnings("rawtypes")
        Map errors = getErrors();

        try {
            for (String part: parts) {

                if (part.length() == 0) {
                    continue;
                }

                nf.parse(part);
            }

            errors.remove(item.getFieldName());
        }
        catch (NumberFormatException nfe) {
            errors.put(item.getFieldName(), MESSAGES.wrongFormat());

            valid = false;
        }

        setErrors(errors, true);

        return valid;
    }


    /**
     * This method returns the double array that has been entered in
     * <i>item</i>.
     *
     * @param item The item that contains the desired values.
     *
     * @return the values as double array.
     */
    public double[] getInputValues(FormItem item) {
        String value = (String) item.getValue();

        if (value == null) {
            return null;
        }

        String[] parts  = value.split("\\s+");

        if (parts == null) {
            return null;
        }

        NumberFormat f = NumberFormat.getDecimalFormat();

        double[] values = new double[parts.length];

        int i = 0;
        OUTER: for (String part: parts) {
            if (part.length() == 0) {
                continue;
            }

            try {
                double x = f.parse(part);
                for (int j = 0; j < i; ++j) {
                    if (values[j] == x) {
                        continue OUTER;
                    }
                }
                values[i++] = x;
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        double [] out = new double[i];
        System.arraycopy(values, 0, out, 0, i);

        return out;
    }


    /**
     * Returns the double values of this panel.
     *
     * @return the double values of this panel.
     */
    public double[] getInputValues() {
        return getInputValues(ti);
    }

    public String getItemTitle() {
        return this.title;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
