/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Settings;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.FileTools;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.river.collections.D4EArtifactCollection;
import org.dive4elements.river.themes.ThemeDocument;
import org.w3c.dom.Document;


public class ShapeExporter implements OutGenerator
{
    private static final String XPATH_FACET = "/art:action/@art:type";
    private static Logger log = LogManager.getLogger(ShapeExporter.class);
    private Artifact master;
    private Document request;
    private OutputStream out;
    private CallContext context;
    private D4EArtifactCollection collection;
    private String facet;
    private File dir;
    private String outName;

    @Override
    public void setup(Object config) {
        log.debug("ShapeExporter.setup");
    }

    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        this.outName = outName;
        this.request = request;
        this.out = out;
        this.context = context;
    }

    @Override
    public void setMasterArtifact(Artifact master) {
        this.master = master;
    }

    @Override
    public void setCollection(D4EArtifactCollection collection) {
        this.collection = collection;
    }

    @Override
    public void doOut(
        ArtifactAndFacet bundle,
        ThemeDocument attr,
        boolean visible
    ) {
        String name = bundle.getFacetName();

        if (!isFacetValid(name)) {
            log.debug("Facet '" + name + "' is not valid for this exporter!");
            return;
        }

        addData(bundle.getData(context));
    }

    private void addData(Object data) {
        if (data instanceof File) {
            this.dir = (File)data;
        }
    }

    private boolean isFacetValid(String name) {
        String thisFacet = getFacet();
        if (thisFacet == null || thisFacet.length() == 0) {
            return false;
        }
        else if (facet == null || facet.length() == 0) {
            return false;
        }
        else {
            return thisFacet.equals(facet);
        }
    }


    /**
     * Returns the name of the desired facet.
     *
     * @return the name of the desired facet.
     */
    protected String getFacet() {
        if (facet == null) {
            facet = getFacetFromRequest();
        }

        return facet;
    }

    @Override
    public void generate() throws IOException {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith("wsplgen") &&
                    !pathname.getName().endsWith(".par")) {
                    return true;
                }
                else {
                    return false;
                }
            }
        };
        FileTools.createZipArchive(this.dir, out, filter);
        out.close();
    }

    @Override
    public void setSettings(Settings settings) {
        //Do nothing.
    }

    @Override
    public Settings getSettings() {
        // This exporter has no settings.
        return null;
    }

    /**
     * Extracts the name of the requested facet from request document.
     *
     * @return the name of the requested facet.
     */
    protected String getFacetFromRequest() {
        return XMLUtils.xpathString(
            request, XPATH_FACET, ArtifactNamespaceContext.INSTANCE);
    }
}
