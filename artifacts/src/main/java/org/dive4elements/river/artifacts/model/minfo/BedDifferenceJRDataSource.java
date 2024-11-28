/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.minfo;

import java.util.ArrayList;
import java.util.HashMap;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;


/**
 * @author <a href="mailto:aheinecke@intevation.de">Andre Heinecke</a>
 */
public class BedDifferenceJRDataSource implements JRDataSource
{
    private ArrayList<String[]> data;
    private HashMap<String, String> metaData;

    private int index = -1;

    public BedDifferenceJRDataSource() {
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
        else if ("calculation".equals(fieldName)) {
            value = metaData.get("calculation");
        }
        else if ("range".equals(fieldName)) {
            value = metaData.get("range");
        }
        else if ("date".equals(fieldName)) {
            value = metaData.get("date");
        }
        else if ("differences".equals(fieldName)) {
            value = metaData.get("differences");
        }
        else if ("kmheader".equals(fieldName)) {
            value = metaData.get("kmheader");
        }
        else if ("diffpairheader".equals(fieldName)) {
            value = metaData.get("diffpairheader");
        }
        else if ("diffheader".equals(fieldName)) {
            value = metaData.get("diffheader");
        }
        else if ("sounding1header".equals(fieldName)) {
            value = metaData.get("sounding1header");
        }
        else if ("sounding2header".equals(fieldName)) {
            value = metaData.get("sounding2header");
        }
        else if ("gap1header".equals(fieldName)) {
            value = metaData.get("gap1header");
        }
        else if ("gap2header".equals(fieldName)) {
            value = metaData.get("gap2header");
        }
        else if ("km".equals(fieldName)) {
            value = data.get(index)[0];
        }
        else if ("diffpair".equals(fieldName)) {
            value = data.get(index)[1];
        }
        else if ("diff".equals(fieldName)) {
            value = data.get(index)[2];
        }
        else if ("sounding1".equals(fieldName)) {
            value = data.get(index)[3];
        }
        else if ("sounding2".equals(fieldName)) {
            value = data.get(index)[4];
        }
        else if ("gap1".equals(fieldName)) {
            value = data.get(index)[5];
        }
        else if ("gap2".equals(fieldName)) {
            value = data.get(index)[6];
        }
        return value;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
