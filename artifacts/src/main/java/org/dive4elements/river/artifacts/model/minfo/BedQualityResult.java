/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.dive4elements.river.artifacts.model.DateRange;

public class BedQualityResult
implements Serializable
{

    protected LinkedList<BedQualityResultValue> values;
    protected DateRange dateRange;

    public BedQualityResult () {
        values = new LinkedList<BedQualityResultValue>();
    };

    public BedQualityResult (
        LinkedList<BedQualityResultValue> values,
        DateRange range
    ) {
        this.dateRange = range;
        this.values = values;
    }

    public void add(BedQualityResultValue[] values) {
        for (BedQualityResultValue value: values) {
            add(value);
        }
    }

    public void add(BedQualityResultValue value) {
        if (value.isEmpty()) {
            return;
        }
        /* Add first is here to mimic the result sorting before
         * a refactorization.*/
        values.addFirst(value);
    }

    public List<BedQualityResultValue> getValues() {
        return values;
    }

    public BedQualityResultValue getValue(String name, String type) {
        for (BedQualityResultValue value: values) {
            if (name.equals(value.getName()) && type.equals(value.getType())) {
                return value;
            }
        }
        return null;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange range) {
        this.dateRange = range;
    }
}
