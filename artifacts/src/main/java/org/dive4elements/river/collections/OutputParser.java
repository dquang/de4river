/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.ManagedFacetAdapter;


/**
 * The OutputParsers task is to pull Artifacts from database and put
 * its outputs and facets into some structures.
 */
public class OutputParser {

    /** Constant XPath that points to the outputmodes of an artifact. */
    public static final String XPATH_ARTIFACT_OUTPUTMODES =
        "/art:result/art:outputmodes/art:output";

    private static Logger log = LogManager.getLogger(OutputParser.class);

    protected ArtifactDatabase db;
    protected CallMeta         meta;
    protected CallContext      context;

    /** Map outputs name to Output. */
    protected Map<String, Output> outs;

    /** Map facets name to list of Facets. */
    protected List<Facet> facets;


    /**
     * @param db Database used to fetch artifacts, outputs and facets.
     */
    public OutputParser(ArtifactDatabase db, CallContext context) {
        this.db      = db;
        this.meta    = context.getMeta();
        this.context = context;
        this.outs    = new HashMap<String, Output>();
        this.facets  = new ArrayList<Facet>();
    }


    /**
     * Gets raw artifact with given id and sorts outputs in mapping.
     * Converts Facets to ManagedFacets on the way.
     * @param uuid uuid of artifact to load from database.
     */
    public void parse(String uuid)
    throws ArtifactDatabaseException
    {
        log.debug("OutputParser.parse: " + uuid);

        D4EArtifact flys = (D4EArtifact) db.getRawArtifact(uuid);

        List<Output> outList = flys.getOutputs(context);

        log.debug("   has " + outList.size() + " Outputs.");

        for (Output out: outList) {
            String name = out.getName();
            log.debug("Process Output '" + name + "'");

            Output o = outs.get(name);
            int  pos = 1;

            if (o == null) {
                o = new DefaultOutput(
                    out.getName(),
                    out.getDescription(),
                    out.getMimeType(),
                    new ArrayList<Facet>(),
                    out.getType());
                outs.put(name, o);
            }
            else {
                log.debug("OutputParser.parse: Use 'old' Output");
                pos = o.getFacets().size() + 1;
            }

            List<Facet> mfacets = facet2ManagedFacet(
                uuid, out.getFacets(), pos);
            o.addFacets(mfacets);
            this.facets.addAll(mfacets);
        }
    }


    /**
     * Access mapping of Outputname to Output.
     */
    public Map<String, Output> getOuts() {
        return outs;
    }


    /**
     * Access all facets.
     */
    public List<Facet> getFacets() {
        return this.facets;
    }


    /**
     * Creates a list of ManagedFacets from list of Facets.
     * @param pos Position of first facet (for each other the positions
     *            will be increased).
     */
    protected List<Facet> facet2ManagedFacet(
        String      uuid,
        List<Facet> old,
        int         pos)
    {
        List<Facet> newFacets = new ArrayList<Facet>(old.size());

        log.debug("There are " + old.size() + " Facets for this Output.");

        for (Facet f: old) {
            newFacets.add(new ManagedFacetAdapter(f, uuid, pos++, 1, 1));
        }

        return newFacets;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
