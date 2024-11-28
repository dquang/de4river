/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.DateRange;


/** Access data of artifact used in BedQuality calculations. */
public class BedQualityAccess
extends      RangeAccess {

    private static final Logger log = LogManager
        .getLogger(BedQualityAccess.class);

    private List<String> bedDiameter;
    private List<String> bedloadDiameter;
    private List<DateRange> ranges;


    public BedQualityAccess(D4EArtifact artifact, CallContext context) {
        super(artifact);
    }

    public List<DateRange> getDateRanges() {
        if (ranges == null) {
            ranges = extractRanges(getString("periods"));
        }
        return ranges;
    }

    public List<String> getBedDiameter() {
        String value = getString("bed_diameter");
        if (bedDiameter == null && value != null) {
            bedDiameter = extractDiameter(value);
        }
        if (bedDiameter == null) {
            return new ArrayList<String>();
        }
        return bedDiameter;
    }

    public List<String> getBedloadDiameter() {
        String value = getString("load_diameter");
        if (bedloadDiameter == null && value != null) {
            bedloadDiameter = extractDiameter(value);
        }
        if (bedloadDiameter == null) {
            return new ArrayList<String>();
        }
        return bedloadDiameter;
    }

    private List<DateRange> extractRanges(String dateString) {
        List<DateRange> list = new LinkedList<DateRange>();
        if (dateString == null) {
            return list;
        }
        String[] dates = dateString.split(";");
        for (String s : dates) {
            String[] pair = s.split(",");
            try {
                long l1      = Long.parseLong(pair[0]);
                long l2      = Long.parseLong(pair[1]);
                Date first   = new Date(l1);
                Date second  = new Date(l2);
                DateRange dr = new DateRange(first, second);
                list.add(dr);
            }
            catch (NumberFormatException nfe) {
                continue;
            }
        }
        return list;
    }

    private List<String> extractDiameter(String value) {
        List<String> result = new LinkedList<String>();
        String[] diameter = value.split(";");
        for (String v : diameter) {
            log.debug("diameter: " + v);
            String[] parts = v.split("\\.");
            result.add(parts[parts.length - 1]);
            log.debug(parts[parts.length-1]);
        }
        return result;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
