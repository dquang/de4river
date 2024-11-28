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

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Settings;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.collections.D4EArtifactCollection;
import org.dive4elements.river.themes.ThemeDocument;


/**
 * An OutGenerator is used to create a collected outputs of a list of Artifacts.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface OutGenerator {

    /**
     * Pre-initialize generator from configuration.
     */
    void setup(Object config);

    /**
     * Initializes the OutGenerator with meta information which are necessary
     * for the output generation.
     *
     * @param outName The name of the out to serve.
     * @param request The incomding request document.
     * @param out     The output stream.
     * @param context The CallContext that provides further information and
     * objects used for the output generation.
     */
    void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    );

    /**
     * This method is used to tell the OutGenerator which artifact is the master
     * artifact which is used for special operations.
     *
     * @param master The master artifact.
     */
    void setMasterArtifact(Artifact master);

    /**
     * This method is used to set the Collection of the OutGenerator.
     *
     * @param collection A reference to the collection.
     */
    void setCollection(D4EArtifactCollection collection);

    /**
     * Creates the output of an Artifact and appends that single output to the
     * total output.
     *
     * @param bundle The Facet and artifact that provides information and
     * data for the single output.
     * @param attr A document that might contain some attributes used while
     * producing the output.
     * @param visible Specifies, if this output should be visible or not.
     */
    void doOut(ArtifactAndFacet bundle, ThemeDocument attr, boolean visible);

    /**
     * Writes the collected output of all artifacts specified in the
     * <i>request</i> (see init()) document to the OutputStream <i>out</i> (see
     * init()).
     */
    void generate() throws IOException;

    /**
     * Used to set a <i>Settings</i> object for the <i>Output</i>
     * that is produced by this <i>OutGenerator</i>.
     *
     * @param settings The <i>Settings</i> that might be used while
     * <i>Output</i> creation.
     */
    void setSettings(Settings settings);

    /**
     * Returns the Settings for the Output produced by this OutGenerator.
     *
     * @return the Settings for the Output produced by this OutGenerator.
     */
    Settings getSettings();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
