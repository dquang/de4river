/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;


/**
 * @author <a href="mailto:aheinecke@intevation.de">Andre Heinecke</a>
 */
public class SQRelationJRDataSource implements JRDataSource
{
    /** The log used in this exporter.*/
    private static Logger log = LogManager.getLogger(SQRelationJRDataSource.class);

    private ArrayList<String[]> data;
    private HashMap<String, String> metaData;

    private int index = -1;

    public SQRelationJRDataSource() {
        data = new ArrayList<String[]>();
        metaData = new HashMap<String, String>();
    }

    public void addData(String[] val) {
        data.add(val);
    }

    public void addMetaData(String key, String value) {
        metaData.put(key, value);
    }

    public boolean next() throws JRException {
        index++;
        return (index < data.size());
    }

    public Object getFieldValue(JRField field) throws JRException {
        Object value = "";
        String fieldName = field.getName();
        if ("river".equals(fieldName)) {
            value = metaData.get("river");
        }
        else if ("date".equals(fieldName)) {
            value = metaData.get("date");
        }
        else if ("calculation".equals(fieldName)) {
            value = metaData.get("calculation");
        }
        else if ("location".equals(fieldName)) {
            value = metaData.get("location");
        }
        else if ("outliers".equals(fieldName)) {
            value = metaData.get("outliers");
        }
        else if ("outliertest".equals(fieldName)) {
            value = metaData.get("outliertest");
        }
        else if ("periods".equals(fieldName)) {
            value = metaData.get("periods");
        }
        else if ("msName".equals(fieldName)) {
            value = metaData.get("msName");
        }
        else if ("msGauge".equals(fieldName)) {
            value = metaData.get("msGauge");
        }
        else if ("km".equals(fieldName)) {
            value = data.get(index)[0];
        }
        else if ("param".equals(fieldName)) {
            value = data.get(index)[1];
        }
        else if ("a".equals(fieldName)) {
            value = data.get(index)[2];
        }
        else if ("b".equals(fieldName)) {
            value = data.get(index)[3];
        }
        else if ("total".equals(fieldName)) {
            value = data.get(index)[7];
        }
        else if ("out".equals(fieldName)) {
            value = data.get(index)[8];
        }
        else if ("sd".equals(fieldName)) {
            value = data.get(index)[4];
        }
        else if ("qmax".equals(fieldName)) {
            value = data.get(index)[5];
        }
        else if ("cferg".equals(fieldName)) {
            value = data.get(index)[10];
        }
        else if ("cduan".equals(fieldName)) {
            value = data.get(index)[9];
        }
        else if ("r2".equals(fieldName)) {
            value = data.get(index)[6];
        }
        return value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
