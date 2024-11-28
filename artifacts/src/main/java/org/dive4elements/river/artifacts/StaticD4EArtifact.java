/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifactdatabase.data.StateData;
import org.dive4elements.artifactdatabase.ProtocolUtils;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.Output;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

/**
 * A basic D4EArtifact.
 */
public abstract class StaticD4EArtifact extends D4EArtifact {

    /** Private log. */
    private static final Logger log =
        LogManager.getLogger(StaticD4EArtifact.class);

    /**
     * Create description document which includes outputmodes.
     * @param data ignored.
     */
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

        ProtocolUtils.appendDescribeHeader(
            creator, root, identifier(), hash());
        root.appendChild(createOutputModes(cc, desc, creator));

        // Add the data to an anonymous state.
        Collection<StateData> datas = getAllData();
        if (datas.size() > 0) {
            Element ui = creator.create("ui");
            Element staticE = creator.create("static");
            Element state = creator.create("state");

            for (StateData dataItem : datas) {
                Element itemelent = creator.create("data");
                creator.addAttr(itemelent, "name", dataItem.getName(), true);
                creator.addAttr(itemelent, "type", dataItem.getType(), true);
                Element valuement = creator.create("item");
                creator.addAttr(
                    valuement, "label", dataItem.getDescription(), true);
                creator.addAttr(
                    valuement, "value", dataItem.getValue().toString(), true);
                itemelent.appendChild(valuement);
                state.appendChild(itemelent);
            }

            ui.appendChild(staticE);
            staticE.appendChild(state);
            root.appendChild(ui);
        }

        return desc;
    }


    protected Element createOutputModes(
        CallContext    cc,
        Document       doc,
        ElementCreator creator)
    {
        log.debug("createOutputModes");

        Element outs = ProtocolUtils.createArtNode(
            creator, "outputmodes", null, null);

        State state       = getCurrentState(cc);

        log.debug("Current state is " + state.getID());

        List<Output> list = state.getOutputs();

        if (list != null && list.size() > 0) {
            List<Facet> fs = getFacets(state.getID());
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

        return outs;
    }

    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("StaticD4EArtifact.setup");
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
