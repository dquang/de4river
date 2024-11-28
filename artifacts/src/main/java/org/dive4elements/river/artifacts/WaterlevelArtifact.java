/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import org.dive4elements.artifactdatabase.state.DefaultFacet;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.states.DefaultState;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;


/**
 * Clone of an WINFOArtifact to expose exactly one waterlevel only.
 * All Facets of the "longitudinal_section" output will be added to the
 * "w_differences" output and filterFacets adjusted accordingly.
 *
 * @TODO Straighten inheritance-line (waterlevel-WINFO or vice versa).
 */
public class WaterlevelArtifact extends WINFOArtifact {

    /** The log for this class. */
    private static Logger log = LogManager.getLogger(WaterlevelArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "waterlevel";

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance()
            .register(ARTIFACT_NAME, FacetActivity.INACTIVE);
    }

    /**
     * The default constructor.
     */
    public WaterlevelArtifact() {
    }


    /**
     * Setup and restate longitudinal_section filterfacets to apply to the
     * w_differences output, too. Also, for w_differences, add respective q-
     * filter facets.
     */
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
        // For w_differences, also allow q-facets.
        if(filterFacets != null) {
            List<Facet> list = new ArrayList<Facet>();
            List<Facet> wlist = filterFacets.get(ChartType.LS);
            if (wlist == null) {
                log.warn("No matching filterfacets found");
                dumpFilterFacets();
            } else {
                for (Facet f: wlist) {
                    if (!f.getName().equals(LONGITUDINAL_Q)) {
                        DefaultFacet df = new DefaultFacet(f.getIndex(),
                            "longitudinal_section.q", "");
                        list.add(df);
                    }
                }

                list.addAll(wlist);

                filterFacets.put("w_differences", list);
            }
        }
    }


    /**
     * Clone important stuff of an WINFOArtifact.
     * @param artifact the WINFOArtifact to clone stuff from.
     */
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta)
    {
        WINFOArtifact winfo = (WINFOArtifact) artifact;
        setData(winfo.cloneData());
        log.debug("Cloned data of winfo artifact.");
        // Statically add Facets.
        List<Facet> fs = new ArrayList<Facet>();
        DefaultState state = (DefaultState) getCurrentState(context);
        state.computeInit(this, hash(), context, meta, fs);
        if (!fs.isEmpty()) {
            log.debug("Facets to add in WaterlevelArtifact.initialize .");
            addFacets(getCurrentStateId(), fs);
        }
        else {
            log.debug("No facets to add in WaterlevelArtifact.initialize ("
                + state.getID() + ").");
        }
    }


    /**
     * Returns the name of the concrete artifact.
     *
     * @return the name of the concrete artifact.
     */
    public String getName() {
        return ARTIFACT_NAME;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
