/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.collections;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.dive4elements.artifacts.ArtifactDatabase;
import org.dive4elements.artifacts.ArtifactDatabaseException;
import org.dive4elements.artifacts.ArtifactNamespaceContext;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;


public class CollectionDescriptionHelper {

    private static final Logger log =
        LogManager.getLogger(CollectionDescriptionHelper.class);


    public static final String XPATH_ARTIFACT_STATE_DATA =
        "/art:result/art:ui/art:static/art:state/art:data";

    /** Constant XPath that points to the outputmodes of an artifact. */
    public static final String XPATH_ARTIFACT_OUTPUTMODES =
        "/art:result/art:outputmodes";


    protected ElementCreator ec;

    protected CallContext      context;
    protected ArtifactDatabase database;

    protected String name;
    protected String uuid;
    protected Date   creation;
    protected long   ttl;

    protected List<String>        artifacts;
    protected CollectionAttribute attribute;


    /**
     * @param name The name of the collection.
     * @param uuid The uuid of the collection.
     * @param creation The creation time of the collection.
     * @param ttl The time to live of the collection.
     */
    public CollectionDescriptionHelper(
        String      name,
        String      uuid,
        Date        creation,
        long        ttl,
        CallContext callContext
    ) {
        this.name      = name;
        this.uuid      = uuid;
        this.creation  = creation;
        this.ttl       = ttl;
        this.context   = callContext;
        this.database  = callContext.getDatabase();
        this.artifacts = new ArrayList<String>();
    }


    public void addArtifact(String uuid) {
        if (uuid != null && uuid.length() > 0) {
            artifacts.add(uuid);
        }
    }


    public void setAttribute(CollectionAttribute attribute) {
        if (attribute != null) {
            this.attribute = attribute;
        }
    }


    public Document toXML() {
        Document doc = XMLUtils.newDocument();

        ec = new ElementCreator(
            doc,
            ArtifactNamespaceContext.NAMESPACE_URI,
            ArtifactNamespaceContext.NAMESPACE_PREFIX);

        Element root = ec.create("artifact-collection");
        doc.appendChild(root);

        String creationTime = creation != null
            ? Long.toString(creation.getTime())
            : "";

        ec.addAttr(root, "name", name, true);
        ec.addAttr(root, "uuid", uuid, true);
        ec.addAttr(root, "creation", creationTime, true);
        ec.addAttr(root, "ttl", String.valueOf(ttl), true);

        appendArtifacts(root);
        appendAttribute(root);

        return doc;
    }


    /**
     * Appends parts of the DESCRIBE document of each Artifact to <i>root</i>.
     *
     * @param root The root node.
     */
    protected void appendArtifacts(Element root) {
        Element artifactsEl = ec.create("artifacts");

        for (String uuid: artifacts) {
            try {
                Element e = buildArtifactNode(uuid);

                if (e != null) {
                    artifactsEl.appendChild(e);
                }
            }
            catch (ArtifactDatabaseException dbe) {
                log.warn(dbe, dbe);
            }
        }

        root.appendChild(artifactsEl);
    }


    /**
     * Create the Artifacts Node that contains outputmode and statedata.
     *
     * @param uuid uuid of the artifact.
     */
    protected Element buildArtifactNode(String uuid)
    throws    ArtifactDatabaseException
    {
        log.debug("Append artifact '" + uuid + "' to collection description");

        // TODO
        String hash = "MYHASH";

        Element ci = ec.create("artifact");
        ec.addAttr(ci, "uuid", uuid, true);
        ec.addAttr(ci, "hash", hash, true);

        // XXX I am not sure if it works well every time with an empty document
        // in the describe operation of an artifact.
        Document description = database.describe(uuid, null, context.getMeta());

        // Add outputmode element(s).
        Node outputModes = (Node) XMLUtils.xpath(
            description,
            XPATH_ARTIFACT_OUTPUTMODES,
            XPathConstants.NODE,
            ArtifactNamespaceContext.INSTANCE);

        if (outputModes != null) {
            Document doc = ci.getOwnerDocument();
            ci.appendChild(doc.importNode(outputModes, true));
        }

        // Add state-data element(s).
        Node dataNode = ci.appendChild(
            ci.getOwnerDocument().createElement("art:data-items"));

        NodeList dataNodes = (NodeList) XMLUtils.xpath(
            description,
            XPATH_ARTIFACT_STATE_DATA,
            XPathConstants.NODESET,
            ArtifactNamespaceContext.INSTANCE);

        if (dataNodes != null) {
            Document doc = ci.getOwnerDocument();
            for (int i = 0, D = dataNodes.getLength(); i < D; i++) {
                dataNode.appendChild(doc.importNode(dataNodes.item(i), true));
            }
        }

        return ci;
    }


    protected void appendAttribute(Element root) {
        if (attribute != null) {
            Document owner = root.getOwnerDocument();
            Document attr  = attribute.toXML();

            root.appendChild(owner.importNode(attr.getFirstChild(), true));
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
