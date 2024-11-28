/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.i18n.client.NumberFormat;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.form.fields.FloatItem;
import com.smartgwt.client.widgets.form.fields.StaticTextItem;
import com.smartgwt.client.widgets.form.fields.events.BlurHandler;


public class DoubleRangeOnlyPanel extends DoubleRangePanel {

    public DoubleRangeOnlyPanel(
        String      titleFrom,
        String      titleTo,
        double      from,
        double      to,
        int         width,
        BlurHandler handler
    ) {
        this(titleFrom, titleTo, from, to, width, handler, "right");
    }


    public DoubleRangeOnlyPanel(
        String      titleFrom,
        String      titleTo,
        double      from,
        double      to,
        int         width,
        BlurHandler handler,
        String      labelOrientation
    ) {
        super();

        fromItem = new FloatItem(FIELD_FROM);
        toItem   = new FloatItem(FIELD_TO);
        stepItem = new FloatItem(FIELD_WIDTH);

        fromItem.addBlurHandler(handler);
        toItem.addBlurHandler(handler);

        NumberFormat nf = NumberFormat.getDecimalFormat();

        fromItem.setValue(nf.format(from));
        toItem.setValue(nf.format(to));

        StaticTextItem fromText = new StaticTextItem("staticFrom");
        fromText.setValue(titleFrom);
        fromText.setShowTitle(false);
        fromItem.setShowTitle(false);

        StaticTextItem toText = new StaticTextItem("staticTo");
        toText.setValue(titleTo);
        toText.setShowTitle(false);
        toItem.setShowTitle(false);

        int itemWidth = width / 4;
        fromItem.setWidth(itemWidth);
        fromText.setWidth(itemWidth);
        toItem.setWidth(itemWidth);
        toText.setWidth(itemWidth);

        if (labelOrientation.equals("right")) {
            setFields(fromItem, fromText, toItem, toText);
        }
        else {
            setFields(fromText, fromItem, toText, toItem);
        }

        setFixedColWidths(false);
        setNumCols(4);
        setWidth(width);
        setAlign(Alignment.CENTER);
    }


    @Override
    public boolean validateForm() {
        return validateForm(fromItem) && validateForm(toItem);
    }


    @Override
    public double getStep() {
        return -1;
    }
}
