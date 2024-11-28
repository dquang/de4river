/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.FormItemValueFormatter;
import com.smartgwt.client.widgets.form.FormItemValueParser;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurEvent;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.shared.model.FacetRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * SpinnerItem-like element with text label and up/down buttons.
 */
public class KMSpinner extends HLayout {
    protected List<KMSpinnerChangeListener> listeners =
        new ArrayList<KMSpinnerChangeListener>();

    protected Label label;
    protected FacetRecord facetRecord;
    protected double value;

    public KMSpinner(double initialValue, FacetRecord facetRecord) {
        super(2);
        this.facetRecord = facetRecord;
        this.value = initialValue;

        setWidth("99%");
        setHeight(24);

        // minusButton shall ask service for previous available cs.
        Button minusButton = new Button("-");
        minusButton.setWidth(18);
        minusButton.setHeight(22);
        minusButton.setValign(VerticalAlignment.CENTER);
        minusButton.addClickHandler(
            new com.smartgwt.client.widgets.events.ClickHandler() {
            public void onClick(ClickEvent evt) {
                fireChangedEvent(value - 0.1d, false);
            }
        });

        DynamicForm form = new DynamicForm();
        final TextItem kmField = new TextItem();
        kmField.setValue(initialValue);
        kmField.setWidth("*");
        kmField.setTitle("");
        kmField.setHeight(20);

        FormItemValueFormatter doubleFormat = new FormItemValueFormatter() {
            public String formatValue(
                Object value, Record record, DynamicForm form, FormItem item) {
                if (value != null) {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    try {
                        double d = Double.valueOf(
                            value.toString()).doubleValue();
                        return nf.format(d);
                    }
                    catch (Exception e) {
                        GWT.log("EditorValueFormatter exception: "
                            + e.toString());

                        // Remove junk chars from input string
                        return doublefyString(value.toString());
                    }
                }
                else {
                    return null;
                }
            }
        };
        kmField.setEditorValueFormatter(doubleFormat);

        FormItemValueParser doubleParser = new FormItemValueParser() {
            public Object parseValue(
                String value, DynamicForm form, FormItem item) {
                if (value == null)
                    return null;
                try {
                    NumberFormat nf = NumberFormat.getDecimalFormat();
                    double d = nf.parse(value.toString());
                    return Double.toString(d);
                }
                catch(NumberFormatException nfe) {
                    return value;
                }
            }
        };
        kmField.setEditorValueParser(doubleParser);

        // Update on focus lost and enter-pressed.
        kmField.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent be) {
                if (kmField.getValue() != null) {
                    try {
                        fireChangedEvent(Double.parseDouble(
                                kmField.getValue().toString()), true);
                    }
                    catch(NumberFormatException nfe) {
                        GWT.log("entered string cannot be parsed to double.");
                    }
                }
            }
        });
        kmField.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent kpe) {
                if (kpe.getKeyName().equals("Enter")) {
                    kmField.blurItem();
                }
            }
        });

        // TODO: i18n Now add all the validators, formatters,
        // editors/parsers  etc.
        form.setFields(kmField);
        form.setTitle("");
        form.setTitlePrefix("");
        form.setTitleSuffix("");
        form.setTitleWidth(0);
        form.setWidth(50);

        // PlusButton shall ask service for next available cs.
        Button plusButton = new Button("+");
        plusButton.setWidth(18);
        plusButton.setHeight(22);
        plusButton.setValign(VerticalAlignment.CENTER);
        plusButton.addClickHandler(
            new com.smartgwt.client.widgets.events.ClickHandler() {
            public void onClick(ClickEvent evt) {
                fireChangedEvent(value + 0.1d, true);
            }
        });

        this.setMembersMargin(5);
        this.addMember(minusButton);
        this.addMember(form);
        this.addMember(plusButton);
    }

    public void addChangeListener(KMSpinnerChangeListener listener) {
        this.listeners.add(listener);
    }

    protected void fireChangedEvent(double val, boolean up) {
        for(KMSpinnerChangeListener listener : listeners) {
            listener.spinnerValueEntered(this, val, facetRecord, up);
        }
    }

    /**
     * Remove junk chars from double string.
     * This method should work for most locales, but not for
     * exotic ones that do not use "." or "," as decimal
     * separator.
     * @return
     */
    protected String doublefyString(String str) {
        StringBuilder buf = new StringBuilder(str.length());

        for (int n = 0; n < str.length(); n++) {
            char c = str.charAt(n);
            if ((c >= '0' && c <= '9') || c == '.' || c == ',') {
                buf.append(c);
            }
        }

        return buf.toString();
    }
}

