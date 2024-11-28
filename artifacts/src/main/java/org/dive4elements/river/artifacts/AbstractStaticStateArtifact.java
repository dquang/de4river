/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.ArtifactNamespaceContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.river.artifacts.states.StaticState;

/**
 * A abstract baseclass for Artifacts which are using only one static state.
 *
 * This class is intended to be used without the config/stateengine to generate
 * the static state.
 *
 * @author <a href="mailto:bjoern.ricks@intevation.de">Björn Ricks</a>
 */
public abstract class AbstractStaticStateArtifact extends StaticD4EArtifact {

    private transient StaticState staticstate;

    private static final Logger log =
        LogManager.getLogger(AbstractStaticStateArtifact.class);

    /**
     * Get a list containing the one and only State.
     * @param  context ignored.
     * @return list with one and only state.
     */
    @Override
    protected List<State> getStates(Object context) {
        ArrayList<State> states = new ArrayList<State>();
        states.add(getStaticState());
        return states;
    }


    /**
     * Get the "current" state.
     * @param cc ignored.
     * @return always the set static state.
     */
    @Override
    public State getCurrentState(Object cc) {
        return getStaticState();
    }

    /**
     * A child class must override this method to set its static state
     */
    protected abstract void initStaticState();

    protected void setStaticState(StaticState state) {
        this.staticstate = state;
    }

    protected StaticState getStaticState() {
        if (staticstate == null) {
            initStaticState();
        }
        return staticstate;
    }

    /**
     * Get the state.
     * @param context ignored.
     * @param stateID ignored.
     * @return the state.
     */
    @Override
    protected State getState(Object context, String stateID) {
        return getStaticState();
    }

    @Override
    public Document describe(Document data, CallContext cc) {
        log.debug("Describe artifact: " + identifier());

        Document desc = XMLUtils.newDocument();

        ElementCreator creator = new ElementCreator(
            desc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ProtocolUtils.createRootNode(creator);
        desc.appendChild(root);

        Element name = ProtocolUtils.createArtNode(
            creator, "name",
            new String[] { "value" },
            new String[] { getName() });

        root.appendChild(name);
        root.appendChild(createOutputModes(cc, desc, creator));

        ProtocolUtils.appendDescribeHeader(
            creator, root, identifier(), hash());

        // Add the data to an anonymous state.
        Collection<StateData> datas = getAllData();
        if (datas.size() > 0) {
            Element ui = creator.create("ui");
            Element staticE = creator.create("static");

            StaticState current = getStaticState();
            Element state = current.describeStatic(this, desc, root, cc, null);
            staticE.appendChild(state);

            for (StateData dataItem : datas) {
                Element itemelent = creator.create("data");
                creator.addAttr(itemelent, "name", dataItem.getName(), true);
                creator.addAttr(itemelent, "type", dataItem.getType(), true);
                state.appendChild(itemelent);
                Element valuement = creator.create("item");
                creator.addAttr(
                    valuement, "label", dataItem.getDescription(), true);
                creator.addAttr(
                    valuement, "value", dataItem.getValue().toString(), true);
                itemelent.appendChild(valuement);
            }

            ui.appendChild(staticE);
            root.appendChild(ui);
        }

        return desc;
    }
}
