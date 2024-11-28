/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.i18n.client.NumberFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This UIProvider creates a widget to enter W or Q data for
 * Fixation analysis
 *
 * @author <a href="mailto:aheinecke@intevation.de">Andre Heinecke</a>
 */
public class WQAdaptedFixingInputPanel
extends      WQAdaptedInputPanel
{
    private static final long serialVersionUID = -3218827566805476423L;

    @Override
    protected List<String> validateRange(Map<String, double[]> ranges) {
        List<String> errors = new ArrayList<String>();
        NumberFormat nf     = NumberFormat.getDecimalFormat();

        for (DoubleArrayPanel dap: wqranges.values()) {

            if (!dap.validateForm()) {
                errors.add(MSG.error_invalid_double_value());
                return errors;
            }

            int idx = 0;

            double[] values = dap.getInputValues();

            double[] good   = new double[values.length];

            for (double value: values) {
                if (value <= 0) {
                    String tmp = MSG.error_validate_positive();
                    tmp = tmp.replace("$1", nf.format(value));
                    errors.add(tmp);
                } else {
                    good[idx++] = value;
                }
            }

            double[] justGood = new double[idx];
            for (int i = 0; i < justGood.length; i++) {
                justGood[i] = good[i];
            }

            if (!errors.isEmpty()) {
                dap.setValues(justGood);
            }
        }
        return errors;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
