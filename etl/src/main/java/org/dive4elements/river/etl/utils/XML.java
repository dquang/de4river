/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.etl.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;

public final class XML
{
    /** Logger for this class. */
    private static Logger log = LogManager.getLogger(XML.class);

    public static class MapXPathVariableResolver
    implements          XPathVariableResolver
    {
        protected Map<String, String> variables;


        public MapXPathVariableResolver() {
            this.variables = new HashMap<String, String>();
        }


        public MapXPathVariableResolver(Map<String, String> variables) {
            this.variables = variables;
        }


        public void addVariable(String name, String value) {
            variables.put(name, value);
        }


        @Override
        public Object resolveVariable(QName variableName) {
            String key = variableName.getLocalPart();
            return variables.get(key);
        }
    } // class MapXPathVariableResolver

    private XML() {
    }

        /**
     * Creates a new XML document
     * @return the new XML document ot null if something went wrong during
     * creation.
     */
    public static final Document newDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            return factory.newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException pce) {
            log.error(pce.getLocalizedMessage(), pce);
        }
        return null;
    }

    /**
     * Loads a XML document namespace aware from a file
     * @param file The file to load.
     * @return the XML document or null if something went wrong
     * during loading.
     */
    public static final Document parseDocument(File file) {
        return parseDocument(file, Boolean.TRUE);
    }

    public static final Document parseDocument(File file, Boolean namespaceAware) {
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            return parseDocument(inputStream, namespaceAware);
        }
        catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
        finally {
            if (inputStream != null) {
                try { inputStream.close(); }
                catch (IOException ioe) {}
            }
        }
        return null;
    }


    public static final Document parseDocument(InputStream inputStream) {
        return parseDocument(inputStream, Boolean.TRUE);
    }

    public static final Document parseDocument(
        InputStream inputStream,
        Boolean     namespaceAware
    ) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        if (namespaceAware != null) {
            factory.setNamespaceAware(namespaceAware.booleanValue());
        }

        try {
            return factory.newDocumentBuilder().parse(inputStream);
        }
        catch (ParserConfigurationException pce) {
            log.error(pce.getLocalizedMessage(), pce);
        }
        catch (SAXException se) {
            log.error(se.getLocalizedMessage(), se);
        }
        catch (IOException ioe) {
            log.error(ioe.getLocalizedMessage(), ioe);
        }
        return null;
    }


    /**
     * Creates a new XPath without a namespace context.
     * @return the new XPath.
     */
    public static final XPath newXPath() {
        return newXPath(null, null);
    }

    /**
     * Creates a new XPath with a given namespace context.
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The new XPath
     */
    public static final XPath newXPath(
        NamespaceContext      namespaceContext,
        XPathVariableResolver resolver)
    {
        XPathFactory factory = XPathFactory.newInstance();
        XPath        xpath   = factory.newXPath();
        if (namespaceContext != null) {
            xpath.setNamespaceContext(namespaceContext);
        }

        if (resolver != null) {
            xpath.setXPathVariableResolver(resolver);
        }
        return xpath;
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. No namespace context is used.
     * @param root  The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnTyp The type of the result.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
        Object root,
        String query,
        QName  returnTyp
    ) {
        return xpath(root, query, returnTyp, null);
    }

    /**
     * Evaluates an XPath query on a given object and returns the result
     * as a given type. Optionally a namespace context is used.
     * @param root The object which is used as the root of the tree to
     * be searched in.
     * @param query The XPath query
     * @param returnType The type of the result.
     * @param namespaceContext The namespace context to be used or null
     * if none should be used.
     * @return The result of type 'returnTyp' or null if something
     * went wrong during XPath evaluation.
     */
    public static final Object xpath(
        Object           root,
        String           query,
        QName            returnType,
        NamespaceContext namespaceContext
    ) {
        return xpath(root, query, returnType, namespaceContext, null);
    }

    public static final Object xpath(
        Object           root,
        String           query,
        QName            returnType,
        NamespaceContext namespaceContext,
        Map<String, String> variables)
    {
        if (root == null) {
            return null;
        }

        XPathVariableResolver resolver = variables != null
            ? new MapXPathVariableResolver(variables)
            : null;

        try {
            XPath xpath = newXPath(namespaceContext, resolver);
            if (xpath != null) {
                return xpath.evaluate(query, root, returnType);
            }
        }
        catch (XPathExpressionException xpee) {
            log.error(xpee.getLocalizedMessage(), xpee);
        }

        return null;
    }

    public static Document transform(
        Document           document,
        File               xformFile
    ) {
        try {
            Transformer transformer =
                TransformerFactory
                    .newInstance()
                    .newTransformer(
                        new StreamSource(xformFile));

            DOMResult result = new DOMResult();

            transformer.transform(new DOMSource(document), result);

            return (Document)result.getNode();
        }
        catch (TransformerConfigurationException tce) {
            log.error(tce, tce);
        }
        catch (TransformerException te) {
            log.error(te, te);
        }

        return null;
    }

   /**
     * Streams out an XML document to a given output stream.
     * @param document The document to be streamed out.
     * @param out      The output stream to be used.
     * @return true if operation succeeded else false.
     */
    public static boolean toStream(Document document, OutputStream out) {
        try {
            Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
            DOMSource    source = new DOMSource(document);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            return true;
        }
        catch (TransformerConfigurationException tce) {
            log.error(tce.getLocalizedMessage(), tce);
        }
        catch (TransformerFactoryConfigurationError tfce) {
            log.error(tfce.getLocalizedMessage(), tfce);
        }
        catch (TransformerException te) {
            log.error(te.getLocalizedMessage(), te);
        }

        return false;
    }

    public static String toString(Document document) {
        try {
            Transformer transformer =
                TransformerFactory.newInstance().newTransformer();
            DOMSource    source = new DOMSource(document);
            StringWriter out    = new StringWriter();
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
            out.flush();
            return out.toString();
        }
        catch (TransformerConfigurationException tce) {
            log.error(tce.getLocalizedMessage(), tce);
        }
        catch (TransformerFactoryConfigurationError tfce) {
            log.error(tfce.getLocalizedMessage(), tfce);
        }
        catch (TransformerException te) {
            log.error(te.getLocalizedMessage(), te);
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
