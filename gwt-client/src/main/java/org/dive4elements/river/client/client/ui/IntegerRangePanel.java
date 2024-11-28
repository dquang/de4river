/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.IntegerItem;
import com.smartgwt.client.widgets.form.validator.IntegerRangeValidator;
import com.smartgwt.client.widgets.form.validator.Validator;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.IntegerRangeData;


public class IntegerRangePanel extends RangePanel {

    private static final long serialVersionUID = -7471199535764887461L;

    protected Integer maxLower;
    protected Integer maxUpper;


    @Override
    protected Data[] getData() {
        Integer lo = getLowerAsInt();
        Integer up = getUpperAsInt();

        return new Data[] { new IntegerRangeData(getDataName(), null, lo, up) };
    }


    @Override
    protected FormItem newFormItem(String name) {
        IntegerItem item = new IntegerItem(name, "");
        item.setShowTitle(false);

        return item;
    }


    @Override
    protected Validator[] createLowerValidators(DataList dataList) {
        setMaxLower(dataList);
        setMaxUpper(dataList);

        Validator validator = newRangeValidator();

        if (validator != null) {
            return new Validator[] { validator };
        }

        return null;
    }


    @Override
    protected Validator[] createUpperValidators(DataList dataList) {
        setMaxLower(dataList);
        setMaxUpper(dataList);

        Validator validator = newRangeValidator();

        if (validator != null) {
            return new Validator[] { validator };
        }

        return null;
    }


    @Override
    public Object getMaxLower() {
        return maxLower;
    }


    @Override
    public Object getMaxUpper() {
        return maxUpper;
    }


    public Integer getLowerAsInt() {
        String raw = getLower();

        if (raw != null && raw.length() > 0) {
            try {
                return Integer.valueOf(raw);
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        return null;
    }


    public Integer getUpperAsInt() {
        String raw = getUpper();

        if (raw != null && raw.length() > 0) {
            try {
                return Integer.valueOf(raw);
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        return null;
    }


    protected Validator newRangeValidator() {
        Integer maxLower = getMaxLowerAsInt();
        Integer maxUpper = getMaxUpperAsInt();

        if (maxLower != null && maxUpper != null) {
            IntegerRangeValidator validator = new IntegerRangeValidator();
            validator.setMax(maxUpper);
            validator.setMin(maxLower);

            return validator;
        }

        return null;
    }


    public Integer getMaxLowerAsInt() {
        return maxLower;
    }


    protected void setMaxLower(DataList dataList) {
        IntegerRangeData range = (IntegerRangeData) dataList.get(0);
        setMaxLower((Integer) range.getLower());
    }


    public void setMaxLower(Integer maxLower) {
        this.maxLower = maxLower;
    }


    public Integer getMaxUpperAsInt() {
        return maxUpper;
    }


    protected void setMaxUpper(DataList dataList) {
        IntegerRangeData range = (IntegerRangeData) dataList.get(0);
        setMaxUpper((Integer) range.getUpper());
    }


    public void setMaxUpper(Integer maxUpper) {
        this.maxUpper = maxUpper;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
