/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Settings;

import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.collections.D4EArtifactCollection;
import org.dive4elements.river.themes.ThemeDocument;

import org.w3c.dom.Document;

public class ReportGenerator
implements   OutGenerator
{
    private static Logger log = LogManager.getLogger(ReportGenerator.class);

    protected Document     result;
    protected OutputStream out;
    protected CallContext  context;
    protected String       outName;

    public ReportGenerator() {
    }

    @Override
    public void setup(Object config) {
        log.debug("ReportGenerator.setup");
    }

    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        log.debug("init");
        this.outName = outName;
        this.out     = out;
        this.context = context;
        result       = null;
    }

    @Override
    public void setMasterArtifact(Artifact master) {
        // not needed
    }

    @Override
    public void setCollection(D4EArtifactCollection collection) {
        // not needed
    }

    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    attr,
        boolean          visible
    ) {
        log.debug("doOut");
        Facet facet = artifactFacet.getFacet();
        if (facet != null) {
            Calculation report = (Calculation) artifactFacet.getData(context);
            if (result == null) {
                result = XMLUtils.newDocument();
                report.toXML(result, context.getMeta());
            }
        }
    }

    @Override
    public void generate() throws IOException {
        log.debug("generate");
        XMLUtils.toStream(result != null
            ? result
            : XMLUtils.newDocument(), out);
    }


    /**
     * Returns an instance of <i>EmptySettings</i> currently!
     *
     * @return an instance of <i>EmptySettings</i>.
     */
    public Settings getSettings() {
        return new EmptySettings();
    }


    /**
     * Not implemented. Override it in subclasses if those need a
     * <i>Settings</i> object.
     */
    public void setSettings(Settings settings) {
        // do nothing
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
