/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifactdatabase.state.ArtifactAndFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Settings;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.map.WMSDBLayerFacet;
import org.dive4elements.river.artifacts.model.map.WMSLayerFacet;
import org.dive4elements.river.artifacts.model.map.WSPLGENLayerFacet;
import org.dive4elements.river.artifacts.states.WaterlevelGroundDifferences;
import org.dive4elements.river.collections.D4EArtifactCollection;
import org.dive4elements.river.themes.ThemeDocument;
import org.dive4elements.river.utils.ArtifactMapfileGenerator;
import org.dive4elements.river.utils.GeometryUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Envelope;


public class MapGenerator implements OutGenerator, FacetTypes {

    private static Logger log = LogManager.getLogger(MapGenerator.class);

    protected D4EArtifactCollection collection;

    protected Artifact master;

    protected Settings settings;

    protected Document request;

    protected OutputStream out;

    protected CallContext context;

    protected List<WMSLayerFacet> layers;

    protected Envelope maxExtent;
    protected Envelope initialExtent;

    protected String srid;

    protected String outName;

    @Override
    public void setup(Object config) {
        log.debug("MapGenerator.setup");
    }

    @Override
    public void init(
        String outName,
        Document request,
        OutputStream out,
        CallContext context
    ) {
        log.debug("MapGenerator.init");

        this.outName  = outName;
        this.request  = request;
        this.out      = out;
        this.context  = context;

        this.layers = new ArrayList<WMSLayerFacet>();

        this.maxExtent = null;
        this.initialExtent = null;
    }


    @Override
    public void setMasterArtifact(Artifact master) {
        log.debug("MapGenerator.setMasterArtifact");
        this.master = master;
    }

    @Override
    public void setCollection(D4EArtifactCollection collection) {
        this.collection = collection;
    }

    @Override
    public void doOut(
        ArtifactAndFacet artifactFacet,
        ThemeDocument    attr,
        boolean          visible)
    {
        String name = artifactFacet.getFacetName();

        log.debug("MapGenerator.doOut: " +
                artifactFacet.getArtifact().identifier() + " | " + name);
        D4EArtifact flys = (D4EArtifact) artifactFacet.getArtifact();

        Facet nativeFacet = artifactFacet.getFacet();

        if (nativeFacet instanceof WMSLayerFacet) {
            WMSLayerFacet wms = (WMSLayerFacet) nativeFacet;
            Envelope   extent = wms.getOriginalExtent();

            layers.add(wms);

            setMaxExtent(extent);
            setSrid(wms.getSrid());

            if (FLOODMAP_WSPLGEN.equals(name)) {
                setInitialExtent(extent);
                createWSPLGENLayer(flys, wms, attr);
            }
            // FIXME: Already generated by HWSBarrierState
            // wms has a wrong SRID which would break that layer
            //else if (FLOODMAP_USERSHAPE.equals(name)) {
            //    createUserShapeLayer(flys, wms);
            //}
            else {
                log.debug("doOut: createDatabaseLayer for facet name: " + name);
                createDatabaseLayer(flys, wms, attr);
            }
        }
        else {
            log.warn("Facet not supported: " + nativeFacet.getClass());
        }
    }


    protected void createWSPLGENLayer(
        D4EArtifact   flys,
        WMSLayerFacet wms,
        ThemeDocument attr
    ) {
        try {
            if(wms instanceof WSPLGENLayerFacet) {
                // Retrieve waterlevel ground differences from artifact
                StateData dFrom =
                    flys.getData(WaterlevelGroundDifferences.LOWER_FIELD);
                StateData dTo =
                    flys.getData(WaterlevelGroundDifferences.UPPER_FIELD);
                StateData dStep =
                    flys.getData(WaterlevelGroundDifferences.DIFF_FIELD);

                String fromStr = dFrom != null
                    ? (String) dFrom.getValue()
                    : null;
                String toStr   = dTo   != null
                    ? (String) dTo.getValue()
                    : null;
                String stepStr = dStep != null
                    ? (String) dStep.getValue()
                    : null;

                float from = Float.parseFloat(fromStr);
                float to   = Float.parseFloat(toStr);
                float step = Float.parseFloat(stepStr);

                ArtifactMapfileGenerator mfg = new ArtifactMapfileGenerator();
                mfg.createUeskLayer(
                    flys,
                    (WSPLGENLayerFacet) wms,
                    attr.createDynamicMapserverStyle(
                        from, to, step, context.getMeta()),
                    context);
            }
            else {
                log.warn("Cannot create WSPLGEN layer from: " +
                        wms.getClass());
            }
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
    }


    protected void createUserShapeLayer(D4EArtifact flys, WMSLayerFacet wms) {
        ArtifactMapfileGenerator mfg = new ArtifactMapfileGenerator();

        try {
            mfg.createUserShapeLayer(flys, wms);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
    }


    protected void createDatabaseLayer(
        D4EArtifact   flys,
        WMSLayerFacet wms,
        ThemeDocument attr
    ) {
        log.debug("createDatabaseLayer for facet: " + wms.getName());

        ArtifactMapfileGenerator mfg = new ArtifactMapfileGenerator();

        try {
            File baseDir = mfg.getShapefileBaseDir();
            File artDir  = new File(baseDir, flys.identifier());

            if (artDir != null && !artDir.exists()) {
                log.debug("Create new directory: " + artDir.getPath());
                artDir.mkdir();
            }

            if (wms instanceof WMSDBLayerFacet) {
                mfg.createDatabaseLayer(
                        flys,
                        (WMSDBLayerFacet) wms,
                        attr.createMapserverStyle());
            }
            else {
                log.warn("Cannot create DB layer from: " + wms.getClass());
            }
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe, fnfe);
        }
        catch (IOException ioe) {
            log.error(ioe, ioe);
        }
    }


    @Override
    public void generate()
            throws IOException
    {
        log.debug("MapGenerator.generate");

        ArtifactMapfileGenerator mfg = new ArtifactMapfileGenerator();
        mfg.generate();

        Document response = XMLUtils.newDocument();
        ElementCreator c  = new ElementCreator(
                response,
                ArtifactNamespaceContext.NAMESPACE_URI,
                ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root   = c.create("floodmap");
        Element layers = c.create("layers");

        response.appendChild(root);
        root.appendChild(layers);

        appendLayers(layers);
        appendMapInformation(root, c);

        XMLUtils.toStream(response, out);
    }


    protected void appendLayers(Element parent) {
        for (WMSLayerFacet facet: layers) {
            parent.appendChild(facet.toXML(parent.getOwnerDocument()));
        }
    }


    protected void setMaxExtent(Envelope maxExtent) {
        if (maxExtent == null) {
            return;
        }

        if (this.maxExtent == null) {
            log.debug("Set max extent to: " + maxExtent);
            this.maxExtent = new Envelope(maxExtent);
            return;
        }

        this.maxExtent.expandToInclude(maxExtent);
    }


    protected void setInitialExtent(Envelope initialExtent) {
        if (this.initialExtent == null && initialExtent != null) {
            log.debug("Set initial extent to: " + initialExtent);
            this.initialExtent = new Envelope(initialExtent);
        }
    }


    protected void setSrid(String srid) {
        if (srid == null || srid.length() == 0) {
            return;
        }

        this.srid = srid;
    }


    protected void appendMapInformation(Element parent, ElementCreator c) {
        String mE;
        if (this.maxExtent != null) {
            mE = GeometryUtils.jtsBoundsToOLBounds(this.maxExtent);
        } else {
            log.error("Layer without extent. Probably no geometry at all.");
            mE = "0 0 1 1";
        }

        Element maxExtent = c.create("maxExtent");
        maxExtent.setTextContent(mE);

        if(this.initialExtent != null) {
            String iE = GeometryUtils.jtsBoundsToOLBounds(this.initialExtent);
            Element initExtent = c.create("initialExtent");
            initExtent.setTextContent(iE);
            parent.appendChild(initExtent);
        }

        Element srid = c.create("srid");
        srid.setTextContent(this.srid);

        // TODO zoom levels
        // TODO resolutation

        parent.appendChild(maxExtent);
        parent.appendChild(srid);
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


    @Override
    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
