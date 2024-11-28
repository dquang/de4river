/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.artifactdatabase.ProtocolUtils;

import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifactdatabase.state.AbstractState;
import org.dive4elements.artifactdatabase.state.Facet;

import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.river.artifacts.resources.Resources;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultState extends AbstractState {

    /** The log that is used in this class. */
    private static Logger log = LogManager.getLogger(DefaultState.class);


    /** The three possible compute types. */
    public static enum ComputeType {
        FEED, ADVANCE, INIT
    }


    protected StateData getData(D4EArtifact artifact,  String name) {
        return artifact.getData(name);
    }


    /**
     * Append to a node and return xml description relevant for gui.
     */
    public Element describeStatic(
        Artifact    artifact,
        Document    document,
        Node        root,
        CallContext context,
        String      uuid)
    {
        ElementCreator creator = new ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        CallMeta meta = context.getMeta();

        String helpText = getHelpText();
        if (helpText != null) {
            helpText = replaceHelpUrl(
                Resources.getMsg(meta, helpText, helpText));
        }

        String label = Resources.getMsg(meta, getID(), getID());
        Element ui   = ProtocolUtils.createArtNode(
            creator, "state",
            new String[] { "name", "uiprovider", "label", "helpText"},
            new String[] { getID(), getUIProvider(), label, helpText });

        Map<String, StateData> theData = getData();
        if (theData == null) {
            return ui;
        }

        D4EArtifact flys = (D4EArtifact)artifact;

        for (String name: theData.keySet()) {
            appendStaticData(flys, context, creator, ui, name);
        }

        return ui;
    }


    protected void appendStaticData(
        D4EArtifact   flys,
        CallContext    context,
        ElementCreator cr,
        Element        ui,
        String         name
    ) {
        StateData data  = getData(flys, name);
        String    value = (data != null) ? (String) data.getValue() : null;

        if (value == null) {
            return;
        }

        String type = data.getType();

        if (log.isDebugEnabled()) {
            log.debug(
                "Append element " + type + "'" +
                name + "' (" + value + ")");
        }

        Element e = createStaticData(flys, cr, context, name, value, type);

        ui.appendChild(e);

    }


    /**
     * Creates a <i>data</i> element used in the static part of the DESCRIBE
     * document.
     *
     * @param creator The ElementCreator that is used to build new Elements.
     * @param cc The CallContext object used for nested i18n retrieval.
     * @param name The name of the data item.
     * @param value The value as string.
     *
     * @return an Element.
     */
    protected Element createStaticData(
        D4EArtifact   flys,
        ElementCreator creator,
        CallContext    cc,
        String         name,
        String         value,
        String         type
    ) {
        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        Element itemElement = creator.create("item");
        creator.addAttr(itemElement, "value", value, true);

        creator.addAttr(
            itemElement,
            "label",
            getLabelFor(cc, name, value, type),
            true);

        dataElement.appendChild(itemElement);

        return dataElement;
    }


    /**
     * @param cc
     * @param name
     * @param value
     * @param type
     *
     * @return
     */
    protected String getLabelFor(
        CallContext cc,
        String      name,
        String      value,
        String      type
    ) {
        CallMeta meta = cc.getMeta();

        try {
            // XXX A better way to format the output would be to use the
            // 'type' value of the data objects.
            double doubleVal = Double.parseDouble(value);
            Locale         l = Resources.getLocale(meta);
            NumberFormat  nf = NumberFormat.getInstance(l);

            return nf.format(doubleVal);
        }
        catch (NumberFormatException nfe) {
            return Resources.getMsg(meta, value, value);
        }
    }


    /**
     * This method returns the default value and label for <i>data</i>.
     *
     * Override this method in a subclass to set an appropiate default
     * value.
     * The default label can be ignored by the client (e.g. the gwt-client).
     * but shall not be null.
     *
     * Example implementation:
     *  if (data != null && data.getName().equals("the_answer")) {
     *       return new String[] {"42", "the_answer"};
     *   }
     *
     * @param context The CallContext used for i18n.
     * @param data The data objects that the defaults are for.
     * @return a String[] with [default value, default label].
     */
    protected String[] getDefaultsFor(CallContext context, StateData data) {
        return null;
    }


    public Element describe(
        Artifact    artifact,
        Document    document,
        Node        root,
        CallContext context,
        String      uuid)
    {
        ElementCreator creator = new ElementCreator(
            document,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        String helpText = getHelpText();
        if (helpText != null) {
            helpText = replaceHelpUrl(
                Resources.getMsg(context.getMeta(), helpText, helpText));
        }

        Element ui        = null;
        String uiprovider = getUIProvider();
        if (uiprovider != null) {
            ui = ProtocolUtils.createArtNode(
                creator, "dynamic",
                new String[] { "uiprovider", "helpText" },
                new String[] { uiprovider, helpText });
        }
        else {
            ui = ProtocolUtils.createArtNode(
                creator, "dynamic",
                new String[] { "helpText" },
                new String[] { helpText });
        }

        Map<String, StateData> theData = getData();
        if (theData == null) {
            return ui;
        }

        D4EArtifact flys = (D4EArtifact)artifact;

        for (String name: theData.keySet()) {
            StateData data = getData(flys, name);

            if (data == null) {
                data = getData(name);
            }

            Element select = createData(creator, artifact, data, context);

            String[] defaults = getDefaultsFor(context, data);
            if (defaults != null && defaults.length > 1) {
                creator.addAttr(select, "defaultValue", defaults[0], true);
                creator.addAttr(select, "defaultLabel", defaults[1], true);
            }

            appendItems(artifact, creator, name, context, select);
            ui.appendChild(select);
        }

        return ui;
    }


    /**
     * @param artifact
     * @param creator
     * @param name
     * @param context
     * @param select
     */
    protected void appendItems(
        Artifact       artifact,
        ElementCreator creator,
        String         name,
        CallContext    context,
        Element        select
    ) {
        Element choices = ProtocolUtils.createArtNode(
            creator, "choices", null, null);

        select.appendChild(choices);

        Element[] items = createItems(creator, artifact, name, context);
        if (items != null) {
            for (Element item: items) {
                choices.appendChild(item);
            }
        }
    }


    /**
     * This method creates the root node that contains the list of selectable
     * items.
     *
     * @param cr The ElementCreator.
     *
     * @return the root node of the item list.
     */
    protected Element createData(
        ElementCreator cr,
        Artifact    artifact,
        StateData   data,
        CallContext context)
    {
        Element select = ProtocolUtils.createArtNode(
            cr, "select", null, null);
        cr.addAttr(select, "name", data.getName(), true);

        Element label = ProtocolUtils.createArtNode(
            cr, "label", null, null);

        select.appendChild(label);

        label.setTextContent(Resources.getMsg(
            context.getMeta(),
            getID(),
            getID()));

        return select;
    }


    /**
     * This method creates a list of items. These items represent the amount of
     * input data that is possible for this state.
     *
     * @param cr The ElementCreator.
     * @param name The name of the amount of data.
     *
     * @return a list of items.
     */
    protected Element[] createItems(
        ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context
    ) {
        return null;
    }


    /**
     * This method is used to create an <i>item</i> Element that contains two
     * further elements <i>label</i> and <i>value</i>. The label and value
     * elements both have text nodes.
     *
     * @param cr The ElementCreator used to build new Elements.
     * @param obj This implementation awaits a String array with [0] = label and
     * [1] = value.
     *
     * @return an Element.
     */
    protected Element createItem(XMLUtils.ElementCreator cr, Object obj) {
        Element item  = ProtocolUtils.createArtNode(cr, "item", null, null);
        Element label = ProtocolUtils.createArtNode(cr, "label", null, null);
        Element value = ProtocolUtils.createArtNode(cr, "value", null, null);

        String[] arr = (String[]) obj;

        label.setTextContent(arr[0]);
        value.setTextContent(arr[1]);

        item.appendChild(label);
        item.appendChild(value);

        return item;
    }


    /**
     * This method transform a given value into a StateData object.
     *
     * @param flys The D4EArtifact.
     * @param name The name of the data object.
     * @param val The value of the data object.
     *
     * @return a StateData object with <i>name</i> and <i>val</i>ue.
     */
    public StateData transform(
        D4EArtifact flys,
        CallContext  cc,
        StateData    stateData,
        String       name,
        String       val
    ) {
        if (log.isDebugEnabled()) {
            log.debug("Transform data ('" + name + "','" + val + "')");
        }

        stateData.setValue(val);

        return stateData;
    }


    /** Override this to do validation.
     *
     * Throw an IllegalArgumentException with a localized
     * error message that should be presented to the user in case
     * the date provided is invalid. */
    public void validate(Artifact artifact, CallContext context)
    throws IllegalArgumentException {
        validate(artifact); /* For compatibility so that classes that
                               override this method still work. */
    }
    /**
     * This method is deprecated.
     * Override the function with the callcontext instead to do
     * localization of error.s
     */
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        return true;
    }


    /**
     * Returns which UIProvider shall be used to aid user input.
     */
    protected String getUIProvider() {
        return null;
    }


    public Object computeAdvance(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return null;
    }


    public Object computeFeed(
        D4EArtifact artifact,
        String       hash,
        CallContext  context,
        List<Facet>  facets,
        Object       old
    ) {
        return null;
    }


    public Object computeInit(
        D4EArtifact artifact,
        String       hash,
        Object       context,
        CallMeta     meta,
        List<Facet>  facets)
    {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
