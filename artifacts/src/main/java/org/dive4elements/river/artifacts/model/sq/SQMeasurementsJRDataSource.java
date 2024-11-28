/* Copyright (C) 2011, 2012, 2013, 2015 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;


/**
 * @author <a href="mailto:aheinecke@intevation.de">Andre Heinecke</a>
 */
public class SQMeasurementsJRDataSource implements JRDataSource
{
    /** The log used in this exporter.*/
    private static Logger log = LogManager.getLogger(
        SQMeasurementsJRDataSource.class);

    private ArrayList<String[]> data;

    private int index = -1;

    public SQMeasurementsJRDataSource() {
        data = new ArrayList<String[]>();
    }

    public void addData(String[] val) {
        data.add(val);
    }

    public boolean next() throws JRException {
        index++;
        return (index < data.size());
    }

    public Object getFieldValue(JRField field) throws JRException {
        Object value = "";
        String fieldName = field.getName();
        if ("param".equals(fieldName)) {
            value = data.get(index)[0];
        }
        else if ("transport".equals(fieldName)) {
            value = data.get(index)[1];
        }
        else if ("discharge".equals(fieldName)) {
            value = data.get(index)[2];
        }
        else if ("date".equals(fieldName)) {
            value = data.get(index)[3];
        }
        else if ("outlier".equals(fieldName)) {
            value = data.get(index)[4];
        }
        return value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
