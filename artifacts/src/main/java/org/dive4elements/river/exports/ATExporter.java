/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import org.w3c.dom.Document;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Settings;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.model.WQ;
import org.dive4elements.river.collections.D4EArtifactCollection;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;
import org.dive4elements.river.model.TimeInterval;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;

public class ATExporter
implements   OutGenerator
{
    private static Logger log = LogManager.getLogger(ATExporter.class);

    public static final String DEFAULT_ENCODING = "UTF-8";

    protected WQ           data;
    protected CallContext  context;
    protected OutputStream out;
    protected D4EArtifact  master;
    protected String       outName;

    protected D4EArtifactCollection collection;


    public ATExporter() {
    }

    @Override
    public void setup(Object config) {
        log.debug("ATExporter.setup");
    }

    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        this.outName = outName;
        this.context = context;
        this.out     = out;
    }


    @Override
    public void setMasterArtifact(Artifact master) {
        this.master = (D4EArtifact) master;
    }

    @Override
    public void setCollection(D4EArtifactCollection collection) {
        this.collection = collection;
    }

    @Override
    public void doOut(
        ArtifactAndFacet artifactf,
        ThemeDocument    attr,
        boolean          visible
    ) {
        data = (WQ)artifactf.getData(context);
    }

    @Override
    public void generate() throws IOException {

        if (data == null) {
            log.debug("no W/Q data");
            return;
        }

        River river = new RiverAccess(master).getRiver();
        double[] kms = new RangeAccess(master).getLocations();

        Gauge gauge = river.determineGaugeAtStation(kms[0]);
        if (gauge != null) {
            // at gauge.
            TimeInterval interval =
                gauge.fetchMasterDischargeTable().getTimeInterval();
            ATWriter.write(
                data,
                new OutputStreamWriter(out, DEFAULT_ENCODING),
                context.getMeta(),
                river.getName(),
                kms[0],
                gauge.getName(),
                gauge.getDatum(),
                interval.getStartTime(),
                river.getWstUnit().getName());
        }
        else {
            // at km
            ATWriter.write(
                data,
                new OutputStreamWriter(out),
                context.getMeta(),
                river.getName(),
                kms[0],
                null,
                null,
                null,
                river.getWstUnit().getName());
        }

    }


    /**
     * Returns an instance of <i>EmptySettings</i> currently!
     *
     * @return an instance of <i>EmptySettings</i>.
     */
    @Override
    public Settings getSettings() {
        return new EmptySettings();
    }


    /**
     * This method is not implemented!
     *
     * @param settings A settings object.
     */
    @Override
    public void setSettings(Settings settings) {
        // do nothing here
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
