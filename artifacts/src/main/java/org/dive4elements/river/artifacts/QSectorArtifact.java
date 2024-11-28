/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.model.GaugeFinder;
import org.dive4elements.river.artifacts.model.GaugeFinderFactory;
import org.dive4elements.river.artifacts.model.GaugeRange;
import org.dive4elements.river.artifacts.model.NamedDouble;

import org.dive4elements.river.artifacts.services.FixingsKMChartService;

import org.dive4elements.river.artifacts.states.DefaultState;

import org.dive4elements.river.artifacts.resources.Resources;


/**
 * Artifact to produce sector markers.
 */
public class QSectorArtifact
extends      StaticD4EArtifact
{
    /** The log for this class. */
    private static Logger log = LogManager.getLogger(QSectorArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "qsector";


    /**
     * Trivial Constructor.
     */
    public QSectorArtifact() {
        log.debug("QSectorArtifact.QSectorArtifact()");
    }


    /**
     * Gets called from factory, to set things up.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("QSectorArtifact.setup");
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
        initialize(null, context, callMeta);
    }


    /** Return the name of this artifact. */
    public String getName() {
        return ARTIFACT_NAME;
    }


    /** Get list of NamedDouble s (QSectors). */
    public Object getQSectors(double km, CallContext context) {

        String river = getDataAsString("river");
        List<NamedDouble> qsectors = new ArrayList<NamedDouble>();

        GaugeFinderFactory ggf = GaugeFinderFactory.getInstance();
        GaugeFinder        gf  = ggf.getGaugeFinder(river);

        if (gf == null) {
            log.warn("No gauge finder found for river '" + river + "'");
            return null;
        }

        GaugeRange gr = gf.find(km);
        if (gr == null) {
            log.debug("No gauge range found for km "
                + km + " on river " + river + ".");
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug(gr);
        }

        for (int i = 0;
             i < FixingsKMChartService.I18N_Q_SECTOR_BOARDERS.length;
             ++i
        ) {
            String key   = FixingsKMChartService.I18N_Q_SECTOR_BOARDERS[i];
            String def   = FixingsKMChartService.DEFAULT_Q_SECTOR_BORDERS[i];
            String label = Resources.getMsg(context.getMeta(), key, def);

            qsectors.add(new NamedDouble(label, gr.getSectorBorder(i)));
        }

        return qsectors;
    }


    /** Setup state and facet. */
    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta
    ) {
        log.debug("QSectorArtifact.initialize");
        List<Facet> fs = new ArrayList<Facet>();

        D4EArtifact flys = (D4EArtifact) artifact;
        importData(flys, "river");

        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);
        if (!fs.isEmpty()) {
            log.debug("Facets to add in QSectorArtifact.initialize .");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in QSectorArtifact.initialize ("
                + state.getID() + ").");
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
