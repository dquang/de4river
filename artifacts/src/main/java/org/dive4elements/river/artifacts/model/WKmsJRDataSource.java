/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;


/**
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class WKmsJRDataSource implements JRDataSource
{
    /** The log used in this exporter.*/
    private static Logger log = LogManager.getLogger(WKmsJRDataSource.class);

    /**
     *
     */
    private ArrayList<String[]> data;
    private HashMap<String, String> metaData;

    private int index = -1;

    /**
     *
     */
    public WKmsJRDataSource()
    {
        data = new ArrayList<String[]>();
        metaData = new HashMap<String, String>();
    }


    /**
     *
     */
    public void addData(String[] data) {
        this.data.add(data);
    }


    /**
     *
     */
    public void addMetaData(String key, String value) {
        this.metaData.put(key, value);
    }


    /**
     *
     */
    public boolean next() throws JRException
    {
        index++;

        return (index < data.size());
    }


    /**
     *
     */
    public Object getFieldValue(JRField field) throws JRException
    {
        Object value = "";
        String fieldName = field.getName();
        if ("river".equals(fieldName)) {
            value = metaData.get("river");
        }
        else if ("date".equals(fieldName)) {
            value = metaData.get("date");
        }
        else if ("range".equals(fieldName)) {
            value = metaData.get("range");
        }
        else if ("gauge".equals(fieldName)) {
            value = metaData.get("gauge");
        }
        else if ("datum".equals(fieldName)) {
            value = metaData.get("datum");
        }
        else if ("calculation".equals(fieldName)) {
            value = metaData.get("calculation");
        }
        else if ("differences".equals(fieldName)) {
            value = metaData.get("differences");
        }
        else if ("wUnit".equals(fieldName)) {
            value = metaData.get("wUnit");
        }
        else if ("valid_since".equals(fieldName)) {
            value = metaData.get("valid_since");
        }
        else if ("km".equals(fieldName)) {
            value = data.get(index)[0];
        }
        else if ("W".equals(fieldName)) {
            value = data.get(index)[1];
        }
        else if ("Q".equals(fieldName)) {
            value = data.get(index)[2];
        }
        else if ("description".equals(fieldName)) {
            value = data.get(index)[3];
        }
        else if ("location".equals(fieldName)) {
            value = data.get(index)[4];
        }
        else if ("gaugename".equals(fieldName)) {
            value = data.get(index)[5];
        }
        else if ("day".equals(fieldName)) {
            value = data.get(index)[6];
        }
        return value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
