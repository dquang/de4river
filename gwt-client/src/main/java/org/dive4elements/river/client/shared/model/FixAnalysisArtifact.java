/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

import com.google.gwt.core.client.GWT;


/**
 * The Fixanalysis implementation of an Artifact (client side).
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class FixAnalysisArtifact extends DefaultArtifact {

    /** The name of this artifact: 'fixanalysis'.*/
    public static final String NAME = "fixanalysis";

    protected FixFilter filter;


    public FixAnalysisArtifact() {
        this.filter = null;
    }


    public  FixAnalysisArtifact(String uuid, String hash) {
        super(uuid, hash);
        this.filter = null;
    }


    public FixAnalysisArtifact(
        String                   uuid,
        String                   hash,
        boolean                  inBackground,
        List<CalculationMessage> messages
    ) {
        super(uuid, hash, inBackground, messages);
    }


    public String getName() {
        return NAME;
    }


    public FixFilter getFilter () {
        return createFilter();
    }


    protected FixFilter createFilter() {
        if (this.filter == null) {
            this.filter = new FixFilter();
        }
        DataList[] old = artifactDescription.getOldData();

        String river = artifactDescription.getDataValueAsString("river");
        if (river != null) {
            this.filter.setRiver(river);
        }

        String from = artifactDescription.getDataValueAsString("ld_from");
        if (from != null) {
            try {
                double fkm = Double.parseDouble(from);
                this.filter.setFromKm(fkm);
            }
            catch(NumberFormatException nfe) {
                GWT.log("Could not parse from km.");
            }
        }

        String to = artifactDescription.getDataValueAsString("ld_to");
        if (to != null) {
            try {
                double tkm = Double.parseDouble(to);
                this.filter.setToKm(tkm);
            }
            catch(NumberFormatException nfe) {
                GWT.log("Could not parse to km");
            }
        }

        String start = artifactDescription.getDataValueAsString("start");
        if (start != null) {
            try {
                long s = Long.parseLong(start);
                this.filter.setFromDate(s);
            }
            catch(NumberFormatException nfe) {
                GWT.log("Could not parse start date");
            }
        }

        String end = artifactDescription.getDataValueAsString("end");
        if (end != null) {
            try {
                long e = Long.parseLong(end);
                this.filter.setToDate(e);
            }
            catch(NumberFormatException nfe) {
                GWT.log("Could not parse end date");
            }
        }

        String q1 = artifactDescription.getDataValueAsString("q1");
        if (q1 != null) {
            try {
                int q1i = Integer.parseInt(q1);
                this.filter.setFromClass(q1i);
            }
            catch(NumberFormatException nfe) {
                GWT.log("Could not parse start class");
            }
        }

        String q2 = artifactDescription.getDataValueAsString("q2");
        if (q2 != null) {
            try {
                int q2i = Integer.parseInt(q2);
                this.filter.setToClass(q2i);
            }
            catch(NumberFormatException nfe) {
                GWT.log("could not parse end class");
            }
        }

        for (DataList list: old) {
            List<Data> items = list.getAll();
            String state = list.getState();
            if(state.equals("state.fix.eventselect")) {
                Data de = getData(items, "events");
                IntegerArrayData iad = (IntegerArrayData) de;
                this.filter.setEvents(iad.getValues());
            }
        }

        return this.filter;
    }

    protected Data getData(List<Data> data, String name) {
        for (Data d: data) {
            if (name.equals(d.getLabel())) {
                return d;
            }
        }
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
