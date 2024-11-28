/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.context;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.DefaultArtifactContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.river.exports.GeneratorLookup;
import org.dive4elements.river.exports.OutGenerator;


/**
 * This class is used to store application wide information in a global context.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class RiverContext extends DefaultArtifactContext {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(RiverContext.class);

    /** The key that is used to store the StateEngine in the context. */
    public static final String ARTIFACT_KEY =
        "artifact";

    /** The key that is used to store the TransitionEngine in the context. */
    public static final String TRANSITION_ENGINE_KEY =
        "artifact.transition.engine";

    /** The key that is used to store the StateEngine in the context. */
    public static final String STATE_ENGINE_KEY =
        "artifact.state.engine";

    /** The key that is used to store the Map of OutGenerator classes in the
     * context. */
    public static final String OUTGENERATORS_KEY =
        "flys.export.outgenerators";

    public static final String FACETFILTER_KEY =
        "flys.export.facetfilter";

    /** The key that is used to store the map of themes in the context. */
    public static final String THEMES =
        "flys.themes.map";

    /** The key that is used to store a map of theme mappings in the context. */
    public static final String THEME_MAPPING =
        "flys.themes.mapping.map";

    /** The key that is used to store a map of WMS urls for each river. */
    public static final String RIVER_WMS =
        "flys.floodmap.river.wms";

    /** The key that is used to store an instance of Scheduler in the context.*/
    public static final String SCHEDULER =
        "flys.wsplgen.scheduler";

    /** Key to store the configured modules in the context. */
    public static final String MODULES = "flys.modules";


    /**
     * The default constructor.
     */
    public RiverContext() {
        super();
    }


    /**
     * A constructor with a config document.
     */
    public RiverContext(Document config) {
        super(config);
    }

    /**
     * Returns the OutGenerator for a specified <i>type</i>.
     *
     * @param name The name of the output type.
     * @param type Defines the type of the desired OutGenerator.
     *
     * @return Instance of an OutGenerator for specified <i>type</i>.
     */
    public static OutGenerator getOutGenerator(
        CallContext context,
        String      name
    ) {
        RiverContext flysContext = context instanceof RiverContext
            ? (RiverContext) context
            : (RiverContext) context.globalContext();

        GeneratorLookup generators =
            (GeneratorLookup)flysContext.get(RiverContext.OUTGENERATORS_KEY);

        if (generators == null) {
            return null;
        }

        GeneratorLookup.Item item = generators.getGenerator(name);

        if (item == null) {
            log.error("No generator class found for " + name);
            return null;
        }

        try {
            Class<OutGenerator> clazz = item.getGenerator();
            OutGenerator generator = clazz.newInstance();
            generator.setup(item.getContext());
            return generator;
        }
        catch (InstantiationException ie) {
            log.error(ie, ie);
        }
        catch (IllegalAccessException iae) {
            log.error(iae, iae);
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
