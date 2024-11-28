/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.DateRange;

import org.dive4elements.river.utils.RiverUtils;

import gnu.trove.TDoubleArrayList;
import gnu.trove.TLongArrayList;

import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Access
{
    private static Logger log = LogManager.getLogger(Access.class);

    protected D4EArtifact artifact;

    public Access() {
    }

    public Access(D4EArtifact artifact) {
        this.artifact = artifact;
    }

    public D4EArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(D4EArtifact artifact) {
        this.artifact = artifact;
    }


    /** Get a data entry as string. */
    protected String getString(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "' value");
            return null;
        }
        return (String)sd.getValue();
    }

    /** Get a data entry as double, or null */
    protected Double getDouble(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "' value");
            return null;
        }
        try {
            return Double.valueOf((String)sd.getValue());
        }
        catch (NumberFormatException nfe) {
            log.warn(key + " '" + sd.getValue() + "' is not numeric.");
        }
        return null;
    }

    protected Long getLong(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "' value");
            return null;
        }
        try {
            return Long.valueOf((String)sd.getValue());
        }
        catch (NumberFormatException nfe) {
            log.warn(key + " '" + sd.getValue() + "' is not a long integer.");
        }
        return null;
    }

    protected Integer getInteger(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "' value");
            return null;
        }
        try {
            return Integer.valueOf((String)sd.getValue());
        }
        catch (NumberFormatException nfe) {
            log.warn(key + " '" + sd.getValue() + "' is not a integer.");
        }
        return null;
    }

    protected int [] getIntArray(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key +"' value");
            return null;
        }
        return RiverUtils.intArrayFromString((String)sd.getValue());
    }

    protected DateRange [] getDateRange(String key) {

        StateData sd = artifact.getData(key);

        if (sd == null) {
            log.warn("missing '" + key + "'");
            return null;
        }

        String data = (String)sd.getValue();
        String[] pairs = data.split("\\s*;\\s*");

        ArrayList<DateRange> aPs = new ArrayList<DateRange>(pairs.length);

        for (int i = 0; i < pairs.length; i++) {
            String[] fromTo = pairs[i].split("\\s*,\\s*");
            if (fromTo.length >= 2) {
                try {
                    Date from = new Date(Long.parseLong(fromTo[0]));
                    Date to   = new Date(Long.parseLong(fromTo[1]));
                    DateRange aP = new DateRange(from, to);
                    if (!aPs.contains(aP)) {
                        aPs.add(aP);
                    }
                }
                catch (NumberFormatException nfe) {
                    log.warn(key + " contains no long values.", nfe);
                }
            }
        }

        DateRange [] result = aPs.toArray(new DateRange[aPs.size()]);

        if (log.isDebugEnabled()) {
            for (int i = 0; i < result.length; ++i) {
                DateRange ap = result[i];
                log.debug("period " +
                    ap.getFrom() + " - " + ap.getTo());
            }
        }

        return result;
    }

    protected Boolean getBoolean(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "' value");
            return null;
        }
        return Boolean.valueOf((String)sd.getValue());
    }

    protected double [] getDoubleArray(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "'");
            return null;
        }
        String [] parts = ((String)sd.getValue()).split("[\\s;]+");
        TDoubleArrayList list = new TDoubleArrayList(parts.length);
        for (String part: parts) {
            try {
                list.add(Double.parseDouble(part));
            }
            catch (NumberFormatException nfe) {
                log.warn("'" + part + "' is not numeric.");
            }
        }
        return list.toNativeArray();
    }

    protected long [] getLongArray(String key) {
        StateData sd = artifact.getData(key);
        if (sd == null) {
            log.warn("missing '" + key + "'");
            return null;
        }
        String [] parts = ((String)sd.getValue()).split("[\\s;]+");
        TLongArrayList list = new TLongArrayList(parts.length);
        for (String part: parts) {
            try {
                list.add(Long.parseLong(part));
            }
            catch (NumberFormatException nfe) {
                log.warn("'" + part + "' is not numeric.");
            }
        }
        return list.toNativeArray();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
