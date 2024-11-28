/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifactdatabase.state.StateEngine;
import org.dive4elements.artifactdatabase.state.Output;

import org.dive4elements.river.utils.RiverUtils;

import org.dive4elements.river.artifacts.states.DefaultState;
import org.dive4elements.river.artifacts.context.RiverContext;
import org.dive4elements.river.artifacts.resources.Resources;


/** Artifact, open to generate any (?) out. */
public class ChartArtifact extends D4EArtifact {

    private static final Logger log =
        LogManager.getLogger(ChartArtifact.class);

    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callmeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("ChartArtifact.setup");
        this.identifier = identifier;
        name = "new_chart";

        List<State> states = getStates(context);

        setCurrentState(states.get(0));
    }

    @Override
    protected void appendBackgroundActivity(
        ElementCreator cr,
        Element        root,
        CallContext    context
    ) {
        Element inBackground = cr.create("background-processing");
        root.appendChild(inBackground);

        cr.addAttr(
            inBackground,
            "value",
            String.valueOf(context.isInBackground()),
            true);
    }


    /**
     * Append output mode nodes to a document.
     */
    @Override
    protected void appendOutputModes(
        Document    doc,
        Element     outs,
        CallContext context,
        String      uuid)
    {
        List<String> stateIds = getPreviousStateIds();

        RiverContext flysContext = RiverUtils.getFlysContext(context);
        StateEngine engine      = (StateEngine) flysContext.get(
            RiverContext.STATE_ENGINE_KEY);

        for (String stateId: stateIds) {
            log.debug("Append output modes for state: " + stateId);
            DefaultState state = (DefaultState) engine.getState(stateId);

            List<Output> list = state.getOutputs();
            if (list == null || list.isEmpty()) {
                log.debug("-> No output modes for this state.");
                continue;
            }

            List<Facet> fs = getFacets(stateId);

            if (fs == null || fs.isEmpty()) {
                log.debug("No facets for previous state found.");
                continue;
            }

            log.debug("Found " + fs.size() + " facets in previous states.");

            List<Output> generated = generateOutputs(list, fs);

            ProtocolUtils.appendOutputModes(doc, outs, generated);
        }

        try {
            DefaultState cur = (DefaultState) getCurrentState(context);
            List<Output> list = cur.getOutputs();
            if (list != null && list.size() > 0) {
                log.debug(
                    "Append output modes for current state: " + cur.getID());

                List<Facet> fs = getFacets(cur.getID());

                if (fs != null && fs.size() > 0) {
                    List<Output> generated = generateOutputs(list, fs);

                    log.debug("Found " + fs.size() + " current facets.");
                    if (!generated.isEmpty()) {
                        ProtocolUtils.appendOutputModes(
                            doc, outs, generated);
                    }
                }
                else {
                    log.debug("No facets found for the current state.");
                }
            }
        }
        catch (IllegalArgumentException iae) {
            // state is not valid, so we do not append its outputs.
        }
    }

    public static class ChartState extends DefaultState {

        public static final String FIELD_MODE = "chart_type";

        public static final String DURATION_CURVE =
            "chart.new.durationcurve";

        public static final String COMPUTED_DISCHARGE_CURVE =
            "chart.new.computeddischargecurve";

        public static final String DISCHARGE_LONGITUDINAL_CURVE =
            "chart.new.longitudinal_section";

        public static final String W_DIFFERENCES =
            "chart.new.w_differences";

        public static final String WATERLEVEL =
            "chart.new.crosssection";

        public static final String[] CHARTS = {
            COMPUTED_DISCHARGE_CURVE,
            DURATION_CURVE,
            DISCHARGE_LONGITUDINAL_CURVE,
            W_DIFFERENCES,
            WATERLEVEL };



        @Override
        public Object computeAdvance(
            D4EArtifact artifact,
            String       hash,
            CallContext  context,
            List<Facet>  facets,
            Object       old)
        {
            log.debug("ChartState.computeAdvance");


            return null;
        }


        @Override
        protected Element[] createItems(
            XMLUtils.ElementCreator cr,
            Artifact    artifact,
            String      name,
            CallContext context)
        {
            CallMeta meta   = context.getMeta();
            Element[] charts = new Element[CHARTS.length];

            int i = 0;

            for (String chart: CHARTS) {
                charts[i++] = createItem(
                    cr, new String[] {
                        Resources.getMsg(meta, chart, chart),
                        chart
                    });
            }

            return charts;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
