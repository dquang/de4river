/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.access;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.Segment;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.utils.DoubleUtil;

public class Calculation4Access
extends      RangeAccess
{
    private static Logger log = LogManager.getLogger(Calculation4Access.class);

    protected List<Segment> segments;

    protected double [] fromToStep;

    protected Boolean isQ;

    protected Boolean isRange;


    public Calculation4Access(D4EArtifact artifact) {
        super(artifact);
    }

    public List<Segment> getSegments() {
        if (segments == null) {
            String input = getString("wq_values");
            if (input == null || (input = input.trim()).length() == 0) {
                log.warn("no wq_values given");
                segments = Collections.<Segment>emptyList();
            }
            else {
                segments = Segment.parseSegments(input);
            }
        }
        return segments;
    }

    public boolean isQ() {
        if (isQ == null) {
            Boolean value = getBoolean("wq_isq");
            isQ = value != null && value;
        }
        return isQ;
    }

    public boolean isRange() {
        if (isRange == null) {
            String mode = getString("ld_mode");
            isRange = mode == null || mode.equals("distance");
        }
        return isRange;
    }

    public double [] getFromToStep() {
        if (fromToStep == null) {
            // XXX: Is this really needed in this calculation?
            if (!isRange()) {
                return null;
            }

            // XXX: D4EArtifact sucks!
            // TODO further use RangeAccess functionality.
            double [] fromTo = getKmRange();

            if (fromTo == null) {
                return null;
            }

            Double dStep = getDouble("ld_step");
            if (dStep == null) {
                return null;
            }

            fromToStep =  new double [] {
                fromTo[0],
                fromTo[1],
                DoubleUtil.round(dStep / 1000d)
            };
        }
        return fromToStep;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
