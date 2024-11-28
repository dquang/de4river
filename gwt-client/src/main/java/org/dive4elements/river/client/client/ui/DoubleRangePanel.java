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

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FloatItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;

import org.dive4elements.river.client.client.FLYSConstants;

import java.util.Map;


/**
 * This class creates a DynamicForm with three input fields.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DoubleRangePanel
extends      DynamicForm
{
    /** The message class that provides i18n strings.*/
    protected FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);


    /** The constant name of the input field to enter the start of a distance.*/
    public static final String FIELD_FROM = "from";

    /** The constant name of the input field to enter the end of a distance.*/
    public static final String FIELD_TO = "to";

    /** The constant name of the input field to enter the step width of a
     * distance.*/
    public static final String FIELD_WIDTH = "step";

    /** The textboxes */
    protected FloatItem fromItem;
    protected FloatItem toItem;
    protected FloatItem stepItem;


    public DoubleRangePanel() {
    }

    public FloatItem getToItem() {
        return toItem;
    }


    /**
     * Creates a new form with a single input field that displays an array of
     * double values.
     *
     * @param name The name of the TextItem.
     * @param title The title of the TextItem.
     * @param values The double values that should be displayed initially.
     * @param handler The BlurHandler that is used to valide the input.
     */
    public DoubleRangePanel(
        String titleFrom, String titleTo, String titleStep,
        double from, double to, double step,
        int width,
        BlurHandler handler)
    {
        this(
            titleFrom, titleTo, titleStep,
            from, to, step,
            width,
            handler,
            "right");
    }


    public DoubleRangePanel(
        String titleFrom, String titleTo, String titleStep,
        double from, double to, double step,
        int width,
        BlurHandler handler,
        String labelOrientation)
    {
        fromItem = new FloatItem(FIELD_FROM);
        toItem   = new FloatItem(FIELD_TO);
        stepItem = new FloatItem(FIELD_WIDTH);

        fromItem.addBlurHandler(handler);
        toItem.addBlurHandler(handler);
        stepItem.addBlurHandler(handler);

        NumberFormat f = NumberFormat.getDecimalFormat();

        fromItem.setValue(f.format(from));
        toItem.setValue(f.format(to));
        stepItem.setValue(f.format(step));

        StaticTextItem fromText = new StaticTextItem("staticFrom");
        fromText.setValue(titleFrom);
        fromText.setShowTitle(false);
        fromItem.setShowTitle(false);

        StaticTextItem toText = new StaticTextItem("staticTo");
        toText.setValue(titleTo);
        toText.setShowTitle(false);
        toItem.setShowTitle(false);

        StaticTextItem stepText = new StaticTextItem("staticStep");
        stepText.setValue(titleStep);
        stepText.setShowTitle(false);
        stepItem.setShowTitle(false);

        int itemWidth = width / 6;
        fromItem.setWidth(itemWidth);
        fromText.setWidth(itemWidth);
        toItem.setWidth(itemWidth);
        toText.setWidth(itemWidth);
        stepItem.setWidth(itemWidth);
        stepText.setWidth(itemWidth);

        if (labelOrientation.equals("right")) {
            setFields(fromItem, fromText, toItem, toText, stepItem, stepText);
        }
        else {
            setFields(fromText, fromItem, toText, toItem, stepText, stepItem);
        }

        setFixedColWidths(false);
        setNumCols(6);
        setWidth(width);
        setAlign(Alignment.CENTER);
    }


    /**
     * This method takes distances values and sets them to the textboxes
     * visualizied by this widget.
     *
     * @param from  The from value.
     * @param to    The to value.
     * @param steps The max steps.
     */
    public void setValues(double from, double to, double steps) {
        NumberFormat f = NumberFormat.getDecimalFormat();

        fromItem.setValue(f.format(from));
        toItem.setValue(f.format(to));
        stepItem.setValue(f.format(steps));
    }

    public boolean validateForm() {
        try {
            return
                validateForm(fromItem) &&
                validateForm(toItem) &&
                validateForm(stepItem);
        }
        catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * This method validates the entered text in the input fields. If
     * there are values that doesn't represent a valid float, an error is
     * displayed.
     *
     * Also if negativeToAllowed is false, an error is registered if
     * the 'to' field contains a negative value.
     *
     * @param item The FormItem.
     */
    @SuppressWarnings("unchecked")
    protected boolean validateForm(FormItem item) {
        if (item instanceof StaticTextItem) {
            return true;
        }

        boolean valid = true;

        String v = (String) item.getValue();

        NumberFormat f = NumberFormat.getDecimalFormat();
        @SuppressWarnings("rawtypes")
        Map errors     = getErrors();

        try {
            if (v == null) {
                throw new NumberFormatException("empty");
            }

            f.parse(v);

            errors.remove(item.getFieldName());
        }
        catch (NumberFormatException nfe) {
            errors.put(item.getFieldName(), MESSAGES.wrongFormat());

            item.focusInItem();

            valid = false;
        }

        setErrors(errors, true);

        return valid;
    }


    /**
     * Returns the double value of <i>value</i>.
     *
     * @return the double value of <i>value</i>.
     */
    protected double getDouble(String value) {
        NumberFormat f = NumberFormat.getDecimalFormat();

        String[] splitted = value.split(" ");

        return f.parse(splitted[0]);
    }


    /**
     * Returns the start value.
     *
     * @return the start value.
     */
    public double getFrom() throws NullPointerException {
        String v = getValueAsString(FIELD_FROM);

        return getDouble(v);
    }


    /**
     * Returns the end value.
     *
     * @return the end value.
     */
    public double getTo() throws NullPointerException {
        String v = getValueAsString(FIELD_TO);

        return getDouble(v);
    }


    /**
     * Returns the step width.
     *
     * @return the step width.
     */
    public double getStep() throws NullPointerException {
        String v = getValueAsString(FIELD_WIDTH);

        return getDouble(v);
    }


    /**
     * Sets the value of the field with name <i>fieldname</i>.
     *
     * @param value The new value.
     * @param fieldname The name of the field.
     */
    public void setDoubleValue(double value, String fieldname) {
        NumberFormat f = NumberFormat.getDecimalFormat();
        setValue(fieldname, f.format(value));
    }


    /**
     * Sets a new start value.
     *
     * @param value The new start value.
     */
    public void setFrom(double value) {
        setDoubleValue(value, FIELD_FROM);
    }


    /**
     * Sets a new end value.
     *
     * @param value The new end value.
     */
    public void setTo(double value) {
        setDoubleValue(value, FIELD_TO);
    }


    /**
     * Sets a new step width.
     *
     * @param value The new step width.
     */
    public void setStep(double value) {
        setDoubleValue(value, FIELD_WIDTH);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
