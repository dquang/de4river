/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.datacage.templating;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.utils.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;


/** Handles and evaluate meta-data template against dbs. */
public class Builder
{
    private static Logger log = LogManager.getLogger(Builder.class);

    public static final Pattern MAGIC_EXPR_SEPARATOR =
        Pattern.compile("#!#");

    public static final Pattern STRIP_LINE_INDENT =
        Pattern.compile("\\s*\\r?\\n\\s*");

    public static final Pattern BRACKET_XPATH =
        Pattern.compile("\\{([^}]+)\\}");

    public static final String DC_NAMESPACE_URI =
        "http://www.intevation.org/2011/Datacage";

    private static final Document EVAL_DOCUMENT =
        XMLUtils.newDocument();

    private static final XPathFactory XPATH_FACTORY =
        XPathFactory.newInstance();

    protected Document template;

    protected Map<String, CompiledStatement> compiledStatements;

    protected Map<String, Element> macros;

    private static final class KV implements Comparable<KV> {

        private Comparable<Object> key;
        private Object [] data;

        public KV(Comparable<Object> key, Object [] data) {
            this.key = key;
            this.data = data;
        }

        @Override
        public int compareTo(KV other) {
            return key.compareTo(other.key);
        }

        public Object [] getData() {
            return data;
        }
    }

    /** Connection to either of the databases. */
    public static class NamedConnection {

        protected String     name;
        protected Connection connection;
        protected boolean    cached;

        public NamedConnection() {
        }

        public NamedConnection(
            String     name,
            Connection connection
        ) {
            this(name, connection, true);
        }

        public NamedConnection(
            String     name,
            Connection connection,
            boolean    cached
        ) {
            this.name       = name;
            this.connection = connection;
            this.cached     = cached;
        }
    } // class NamedConnection

    public class BuildHelper
    {
        protected Node                                     output;
        protected Document                                 owner;
        protected StackFrames                              frames;
        protected List<NamedConnection>                    connections;
        protected Map<String, CompiledStatement.Instance>  statements;
        protected Deque<Pair<NamedConnection, ResultData>> connectionsStack;
        protected Deque<NodeList>                          macroBodies;
        protected Deque<Object>                            groupExprStack;
        protected FunctionResolver                         functionResolver;
        protected Map<String, XPathExpression>             expressions;


        public BuildHelper(
            Node                  output,
            List<NamedConnection> connections,
            Map<String, Object>   parameters
        ) {
            if (connections.isEmpty()) {
                throw new IllegalArgumentException("no connections given.");
            }

            this.connections = connections;
            connectionsStack =
                new ArrayDeque<Pair<NamedConnection, ResultData>>();
            this.output      = output;
            frames           = new StackFrames(parameters);
            owner            = getOwnerDocument(output);
            macroBodies      = new ArrayDeque<NodeList>();
            groupExprStack   = new ArrayDeque<Object>();
            functionResolver = new FunctionResolver(this);
            expressions      = new HashMap<String, XPathExpression>();
            statements       =
                new HashMap<String, CompiledStatement.Instance>();
        }

        public void build() throws SQLException {
            try {
                // XXX: Thread safety is now established by the builder pool.
                //synchronized (template) {
                    for (Node current: rootsToList()) {
                        build(output, current);
                    }
                //}
            }
            finally {
                closeStatements();
            }
        }

        protected void closeStatements() {
            for (CompiledStatement.Instance csi: statements.values()) {
                csi.close();
            }
            statements.clear();
        }

        /**
         * Return first statement node in NodeList, respecting
         * macros but not doing evaluation (e.g. of <dc:if>s).
         */
        private Node findStatementNode(NodeList nodes) {
            return findSelectNode(nodes, "statement");
        }

        private Node findPropertiesNode(NodeList nodes) {
            return findSelectNode(nodes, "properties");
        }

        private Node findSelectNode(NodeList nodes, String selectName) {
            int S = nodes.getLength();

            // Check direct children and take special care of macros.
            for (int i = 0; i < S; ++i) {
                Node node = nodes.item(i);
                String ns;
                // Regular statement node.
                if (node.getNodeType() == Node.ELEMENT_NODE
                && node.getLocalName().equals(selectName)
                && (ns = node.getNamespaceURI()) != null
                && ns.equals(DC_NAMESPACE_URI)) {
                    return node;
                }
                // Macro node. Descend.
                else if (node.getNodeType() == Node.ELEMENT_NODE
                    && node.getLocalName().equals("call-macro")
                    && (ns = node.getNamespaceURI()) != null
                    && ns.equals(DC_NAMESPACE_URI)) {

                    String macroName = ((Element)node).getAttribute("name");
                    Node inMacroNode =
                        findSelectNode(
                            getMacroChildren(macroName), selectName);
                    if (inMacroNode != null) {
                        return inMacroNode;
                    }
                }

            }

            return null;
        }

        private String[][] extractProperties(Element propertiesNode) {
            ArrayList<String[]> props = new ArrayList<String[]>();
            NodeList list = propertiesNode.getElementsByTagNameNS(
                DC_NAMESPACE_URI, "property");
            for (int i = 0, L = list.getLength(); i < L; ++i) {
                Element property = (Element)list.item(i);
                String name = property.getAttribute("name");
                if (name.isEmpty()) {
                    log.warn("dc:property without name");
                    continue;
                }
                String alias = property.getAttribute("alias");
                if (alias.isEmpty()) {
                    alias = name;
                }
                props.add(new String [] { name, alias });
            }
            return props.toArray(new String[props.size()][]);
        }

        /**
         * Handle a dc:context node.
         */
        protected void containerContext(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:container-context");

            String container = expand(current.getAttribute("container"));

            if (container.isEmpty()) {
                log.warn(
                    "dc:container-context: no 'container' attribute found");
                return;
            }

            NodeList subs = current.getChildNodes();
            Node propertiesNode = findPropertiesNode(subs);

            if (propertiesNode == null) {
                log.warn("dc:container-context: cannot find properties.");
                return;
            }

            String [][] properties = extractProperties(
                (Element)propertiesNode);

            if (properties.length == 0) {
                log.warn("dc:properties: No properties defined.");
            }

            Object [] result = new Object[1];
            if (!frames.getStore(container, result)) {
                log.warn("dc:container-context: cannot find container.");
                return;
            }
            Object c = result[0];
            if (c instanceof Object []) {
                c = Arrays.asList((Object [])c);
            }
            if (!(c instanceof Collection)) {
                log.warn(
                    "dc:container-context: container is not a collection.");
                return;
            }

            Collection<?> collection = (Collection<?>)c;

            // only descent if there are results
            if (collection.isEmpty()) {
                return;
            }

            String [] columnNames = new String[properties.length];
            for (int i = 0; i < columnNames.length; ++i) {
                columnNames[i] = properties[i][1].toUpperCase();
            }

            ResultData rd = new ResultData(columnNames);

            for (Object obj: collection) {
                Object [] row = new Object[properties.length];
                for (int i = 0; i < properties.length; ++i) {
                    row[i] = getProperty(obj, properties[i][0]);
                }
                rd.add(row);
            }

            // A bit of a fake because the data is not from a
            // real connection.
            NamedConnection connection = connectionsStack.isEmpty()
                ? connections.get(0)
                : connectionsStack.peek().getA();

            connectionsStack.push(
                new Pair<NamedConnection, ResultData>(connection, rd));
            try {
                for (int i = 0, S = subs.getLength(); i < S; ++i) {
                    build(parent, subs.item(i));
                }
            }
            finally {
                connectionsStack.pop();
            }
        }

        /** Poor man's bean access. */
        private Object getProperty(Object obj, String name) {
            String mname =
                "get" + Character.toUpperCase(name.charAt(0))
                + name.substring(1);

            try {
                Method meth = obj.getClass().getMethod(mname);
                return meth.invoke(obj);
            }
            catch (InvocationTargetException ite) {
                log.warn(ite);
            }
            catch (IllegalAccessException iae) {
                log.warn(iae);
            }
            catch (NoSuchMethodException nsme) {
                log.warn(nsme);
            }
            return null;
        }

        /**
         * Handle a dc:context node.
         */
        protected void context(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:context");

            NodeList subs = current.getChildNodes();
            Node stmntNode = findStatementNode(subs);

            if (stmntNode == null) {
                log.warn("dc:context: cannot find statement");
                return;
            }

            String stmntText = stmntNode.getTextContent();

            String con = current.getAttribute("connection");

            String key = con + "-" + stmntText;

            CompiledStatement.Instance csi = statements.get(key);

            if (csi == null) {
                CompiledStatement cs = compiledStatements.get(stmntText);
                csi = cs.new Instance();
                statements.put(key, csi);
            }

            NamedConnection connection = connectionsStack.isEmpty()
                ? connections.get(0)
                : connectionsStack.peek().getA();

            if (con.length() > 0) {
                for (NamedConnection nc: connections) {
                    if (con.equals(nc.name)) {
                        connection = nc;
                        break;
                    }
                }
            }

            ResultData rd = csi.execute(
                connection.connection,
                frames,
                connection.cached);

            // only descent if there are results
            if (!rd.isEmpty()) {
                connectionsStack.push(
                    new Pair<NamedConnection, ResultData>(connection, rd));
                try {
                    for (int i = 0, S = subs.getLength(); i < S; ++i) {
                        build(parent, subs.item(i));
                    }
                }
                finally {
                    connectionsStack.pop();
                }
            }
        }

        public boolean hasResult() {
            return !connectionsStack.isEmpty()
                && !connectionsStack.peek().getB().isEmpty();
        }

        protected ResultData createFilteredResultData(
            ResultData rd,
            String     filter
        ) {
            if (filter == null) return rd;

            XPathExpression x;
            try {
                x = getXPathExpression(filter);
            }
            catch (XPathExpressionException xee) {
                log.warn("Invalid filter expression '" + filter + "'.");
                return rd;
            }

            List<Object []> rows = rd.getRows();
            String [] columns = rd.getColumnLabels();

            List<Object []> filtered = new ArrayList<Object[]>(rows.size());

            for (Object [] row: rows) {
                frames.enter();
                try {
                    frames.put(columns, row);
                    Object result = x.evaluate(
                        EVAL_DOCUMENT, XPathConstants.BOOLEAN);

                    if (result instanceof Boolean && (Boolean)result) {
                        filtered.add(row);
                    }
                }
                catch (XPathExpressionException xee) {
                    log.warn("unable to apply filter expression '" +
                        filter + "' to dataset. " + xee.getMessage(), xee);
                }
                finally {
                    frames.leave();
                }
            }
            return new ResultData(columns, filtered);
        }

        protected void filter(Node parent, Element current)
        throws SQLException
        {
            String expr = current.getAttribute("expr");

            if ((expr = expr.trim()).length() == 0) {
                expr = null;
            }

            NodeList subs = current.getChildNodes();
            int S = subs.getLength();
            if (S == 0) {
                log.debug("dc:filter has no children");
                return;
            }

            ResultData orig = null;
            Pair<Builder.NamedConnection, ResultData> pair = null;

            if (expr != null && !connectionsStack.isEmpty()) {
                pair = connectionsStack.peek();
                orig = pair.getB();
                pair.setB(createFilteredResultData(orig, expr));
            }

            try {
                for (int i = 0; i < S; ++i) {
                    build(parent, subs.item(i));
                }
            }
            finally {
                if (orig != null) {
                    pair.setB(orig);
                }
            }
        }

        protected Map<Object, ResultData> createGroupedResultData(
            ResultData rd,
            String     expr,
            String     type
        ) {
            List<Object []> rows = rd.getRows();
            String [] columns = rd.getColumnLabels();

            String [] exprs = MAGIC_EXPR_SEPARATOR.split(expr);

            XPathExpression [] xs = new XPathExpression[exprs.length];

            try {
                for (int i = 0; i < exprs.length; ++i) {
                    xs[i] = getXPathExpression(exprs[i]);
                }
            }
            catch (XPathExpressionException xee) {
                log.warn("Invalid expression '" + expr + "'.");
                return Collections.<Object, ResultData>emptyMap();
            }

            QName returnType = typeToQName(type);

            Map<Object, ResultData> groups = new TreeMap<Object, ResultData>();

            for (Object [] row: rows) {
                frames.enter();
                try {
                    frames.put(columns, row);

                    for (XPathExpression x: xs) {
                        Object key = x.evaluate(EVAL_DOCUMENT, returnType);
                        ResultData group = groups.get(key);

                        if (group == null) {
                            group = new ResultData(rd.getColumnLabels());
                            groups.put(key, group);
                        }
                        group.add(row);
                    }
                }
                catch (XPathExpressionException xxe) {
                    log.warn("unable to apply expression '" +
                        expr + "' to dataset.");
                }
                finally {
                    frames.leave();
                }
            }
            return groups;
        }

        protected void group(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:group");

            if (connectionsStack.isEmpty()) {
                log.debug("dc:group without having results");
                return;
            }

            NodeList subs = current.getChildNodes();
            int S = subs.getLength();

            if (S == 0) {
                log.debug("dc:group has no children");
                return;
            }

            String expr = current.getAttribute("expr").trim();
            String type = current.getAttribute("type").trim();

            Pair<Builder.NamedConnection, ResultData> pair =
                connectionsStack.peek();

            ResultData orig = connectionsStack.peek().getB();

            Map<Object, ResultData> groups =
                createGroupedResultData(orig, expr, type);

            boolean debug = log.isDebugEnabled();

            try {
                for (Map.Entry<Object, ResultData> entry: groups.entrySet()) {
                    Object     key = entry.getKey();
                    ResultData rd  = entry.getValue();
                    pair.setB(rd);
                    groupExprStack.push(key);
                    if (debug) {
                        log.debug("group key: " + key);
                    }
                    try {
                        for (int i = 0; i < S; ++i) {
                            build(parent, subs.item(i));
                        }
                    }
                    finally {
                        groupExprStack.pop();
                    }
                }
            }
            finally {
                pair.setB(orig);
            }
        }


        protected ResultData createSortedResultData(
            ResultData orig,
            String expr,
            String type
        ) {
            XPathExpression x;
            try {
                x = getXPathExpression(expr);
            }
            catch (XPathExpressionException xee) {
                log.warn("Invalid sort expression '" + expr + "'.");
                return orig;
            }

            QName returnType = typeToQName(type);

            List<Object []> rows = orig.getRows();
            String [] columns = orig.getColumnLabels();

            List<KV> sorted = new ArrayList<KV>(rows.size());

            for (Object [] row: rows) {
                frames.enter();
                try {
                    frames.put(columns, row);
                    Object key = x.evaluate(EVAL_DOCUMENT, returnType);

                    if (key instanceof Comparable) {
                        sorted.add(new KV((Comparable<Object>)key, row));
                    }
                }
                catch (XPathExpressionException xee) {
                    log.warn("unable to apply expression '" +
                        expr + "' to dataset. " + xee.getMessage(), xee);
                }
                finally {
                    frames.leave();
                }
            }
            Collections.sort(sorted);
            List<Object []> result = new ArrayList<Object []>(sorted.size());
            for (KV kv: sorted) {
                result.add(kv.getData());
            }
            return new ResultData(columns, result);
        }

        protected void sort(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:sort");

            if (connectionsStack.isEmpty()) {
                log.debug("dc:sort without having results");
                return;
            }

            String expr = current.getAttribute("expr").trim();
            String type = current.getAttribute("type").trim();

            if (expr.isEmpty()) {
                log.warn("missing 'expr' in dc:sort");
                return;
            }

            Pair<Builder.NamedConnection, ResultData> pair =
                connectionsStack.peek();

            ResultData orig = connectionsStack.peek().getB();

            ResultData sorted = createSortedResultData(orig, expr, type);

            NodeList subs = current.getChildNodes();

            pair.setB(sorted);
            try {
                for (int i = 0, S = subs.getLength(); i < S; ++i) {
                    build(parent, subs.item(i));
                }
            }
            finally {
                pair.setB(orig);
            }
        }

        public Object getGroupKey() {
            return groupExprStack.isEmpty()
                ? null
                : groupExprStack.peek();
        }

        protected void virtualColumn(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:virtual-column");

            if (connectionsStack.isEmpty()) {
                log.debug("dc:virtual-column without having results");
                return;
            }

            NodeList subs = current.getChildNodes();
            int S = subs.getLength();

            if (S == 0) {
                log.debug("dc:virtual-column has no children");
                return;
            }

            String name = expand(current.getAttribute("name"));
            String expr = current.getAttribute("expr").trim();
            String type = current.getAttribute("type").trim();

            QName returnType = typeToQName(type);

            XPathExpression x;
            try {
                x = getXPathExpression(expr);
            }
            catch (XPathExpressionException xee) {
                log.warn("Invalid expression '" + expr + "'.");
                return;
            }

            Pair<Builder.NamedConnection, ResultData> pair =
                connectionsStack.peek();

            ResultData orig = connectionsStack.peek().getB();

            int index = orig.indexOfColumn(name);

            ResultData rd = index >= 0 // Already has column with this name?
                ? replaceColumn(orig, index, x, returnType)
                : addColumn(orig, name, x, returnType);

            pair.setB(rd);
            try {
                for (int i = 0; i < S; ++i) {
                    build(parent, subs.item(i));
                }
            }
            finally {
                pair.setB(orig);
            }
        }

        protected ResultData addColumn(
            ResultData      rd,
            String          name,
            XPathExpression expr,
            QName           returnType
        ) {
            String [] origColumns = rd.getColumnLabels();
            int index = origColumns.length;
            String [] newColumns = Arrays.copyOf(origColumns, index+1);
            newColumns[index] = name.toUpperCase();
            ResultData result = new ResultData(newColumns);
            fillResult(result, rd, index, index+1, expr, returnType);
            return result;
        }

        protected ResultData replaceColumn(
            ResultData      rd,
            int             index,
            XPathExpression expr,
            QName           returnType
        ) {
            String [] columns = rd.getColumnLabels();
            ResultData result = new ResultData(columns);
            fillResult(result, rd, index, columns.length, expr, returnType);
            return result;
        }

        protected void fillResult(
            ResultData      result,
            ResultData      rd,
            int             index,
            int             size,
            XPathExpression expr,
            QName           returnType
        ) {
            String [] origColumns = rd.getColumnLabels();
            for (Object [] row: rd.getRows()) {
                frames.enter();
                try {
                    frames.put(origColumns, row);
                    Object value = expr.evaluate(EVAL_DOCUMENT, returnType);
                    Object [] copy = Arrays.copyOf(row, size);
                    copy[index] = value;
                    result.add(copy);
                }
                catch (XPathExpressionException xxe) {
                    log.warn("unable to apply expression '" +
                        expr + "' to dataset.", xxe);
                }
                finally {
                    frames.leave();
                }
            }
        }

        protected void iterate(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:iterate");
            String container = expand(current.getAttribute("container"));
            String var = expand(current.getAttribute("var")).toUpperCase();

            if (container.isEmpty()) {
                log.warn("'container' not set.");
                return;
            }

            if (var.isEmpty()) {
                log.warn("'var' not set.");
                return;
            }

            Object [] result = new Object[1];

            if (frames.getStore(container, result)) {
                Object c = result[0];
                if (c instanceof Object []) {
                    c = Arrays.asList((Object [])c);
                }
                if (c instanceof Collection) {
                    frames.enter();
                    try {
                        Collection<?> col = (Collection<?>)c;
                        for (Object o: col) {
                            if (o instanceof String) {
                                o = ((String)o).toLowerCase();
                            }
                            frames.put(var, o);
                            NodeList subs = current.getChildNodes();
                            for (int i = 0, N = subs.getLength(); i < N; ++i) {
                                build(parent, subs.item(i));
                            }
                        }
                    }
                    finally {
                        frames.leave();
                    }
                }
            }
        }


        /**
         * Kind of foreach over results of a statement within a context.
         */
        protected void foreach(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:for-each");

            if (connectionsStack.isEmpty()) {
                log.debug("dc:for-each without having results");
                return;
            }

            NodeList subs = current.getChildNodes();
            int S = subs.getLength();

            if (S == 0) {
                log.debug("dc:for-each has no children");
                return;
            }

            Pair<Builder.NamedConnection, ResultData> pair =
                connectionsStack.peek();

            ResultData rd = pair.getB();

            String [] columns = rd.getColumnLabels();

            for (Object [] row: rd.getRows()) {
                frames.enter();
                try {
                    frames.put(columns, row);
                    for (int i = 0; i < S; ++i) {
                        build(parent, subs.item(i));
                    }
                }
                finally {
                    frames.leave();
                }
            }
        }

        /**
         * Create element.
         */
        protected void element(Node parent, Element current)
        throws SQLException
        {
            String attr = expand(current.getAttribute("name"));

            if (log.isDebugEnabled()) {
                log.debug("dc:element -> '" + attr + "'");
            }

            if (attr.length() == 0) {
                log.warn("no name attribute found");
                return;
            }

            Element element = owner.createElement(attr);

            NodeList children = current.getChildNodes();
            for (int i = 0, N = children.getLength(); i < N; ++i) {
                build(element, children.item(i));
            }

            parent.appendChild(element);
        }

        protected void text(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:text");
            String value = expand(current.getTextContent());
            parent.appendChild(owner.createTextNode(value));
        }

        protected void message(Node parent, Element current)
        throws SQLException
        {
            log.debug("dc:message");
            if (log.isDebugEnabled()) {
                String value = current.getTextContent();
                if (value.indexOf('{') >= 0) { // Performance tweak
                    value = expandXPathValue(value);
                }
                log.debug("MESSAGE: " + value);
            }
        }

        /**
         * Add attribute to an element
         * @see Element
         */
        protected void attribute(Node parent, Element current) {

            if (parent.getNodeType() != Node.ELEMENT_NODE) {
                log.warn("need element here");
                return;
            }

            String name  = expand(current.getAttribute("name"));
            String value = expand(current.getAttribute("value"));

            Element element = (Element)parent;

            element.setAttribute(name, value);
        }

        /**
         * Call-Macro node.
         * Evaluate child-nodes of the given macro element (not its text).
         */
        protected void callMacro(Node parent, Element current)
        throws SQLException
        {
            String name = current.getAttribute("name");

            if (name.length() == 0) {
                log.warn("missing 'name' attribute in 'call-macro'");
                return;
            }

            Element macro = macros.get(name);

            if (macro != null) {
                macroBodies.push(current.getChildNodes());
                try {
                    NodeList subs = macro.getChildNodes();
                    for (int j = 0, M = subs.getLength(); j < M; ++j) {
                        build(parent, subs.item(j));
                    }
                }
                finally {
                    macroBodies.pop();
                }
            }
            else {
                log.warn("no macro '" + name + "' found.");
            }
        }

        protected void macroBody(Node parent, Element current)
        throws SQLException
        {
            if (!macroBodies.isEmpty()) {
                NodeList children = macroBodies.peek();
                for (int i = 0, N = children.getLength(); i < N; ++i) {
                    build(parent, children.item(i));
                }
            }
            else {
                log.warn("no current macro");
            }
        }

        /** Get macro node children, not resolving bodies. */
        protected NodeList getMacroChildren(String name) {

            Element macro = macros.get(name);
            return macro != null
                ? macro.getChildNodes()
                : null;
        }

        protected void ifClause(Node parent, Element current)
        throws SQLException
        {
            String test = current.getAttribute("test");

            if (test.length() == 0) {
                log.warn("missing 'test' attribute in 'if'");
                return;
            }

            Boolean result = evaluateXPathToBoolean(test);

            if (result != null && result.booleanValue()) {
                NodeList subs = current.getChildNodes();
                for (int i = 0, N = subs.getLength(); i < N; ++i) {
                    build(parent, subs.item(i));
                }
            }
        }

        protected void choose(Node parent, Element current)
        throws SQLException
        {
            Node branch = null;

            NodeList children = current.getChildNodes();
            for (int i = 0, N = children.getLength(); i < N; ++i) {
                Node child = children.item(i);
                String ns = child.getNamespaceURI();
                if (ns == null
                || !ns.equals(DC_NAMESPACE_URI)
                || child.getNodeType() != Node.ELEMENT_NODE
                ) {
                    continue;
                }
                String name = child.getLocalName();
                if ("when".equals(name)) {
                    Element when = (Element)child;
                    String test = when.getAttribute("test");
                    if (test.length() == 0) {
                        log.warn("no 'test' attribute found for when");
                        continue;
                    }

                    Boolean result = evaluateXPathToBoolean(test);
                    if (result != null && result.booleanValue()) {
                        branch = child;
                        break;
                    }

                    continue;
                }
                else if ("otherwise".equals(name)) {
                    branch = child;
                    // No break here.
                }
            }

            if (branch != null) {
                NodeList subs = branch.getChildNodes();
                for (int i = 0, N = subs.getLength(); i < N; ++i) {
                    build(parent, subs.item(i));
                }
            }
        }

        protected XPathExpression getXPathExpression(String expr)
        throws XPathExpressionException
        {
            XPathExpression x = expressions.get(expr);
            if (x == null) {
                XPath xpath = XPATH_FACTORY.newXPath();
                xpath.setXPathVariableResolver(frames);
                xpath.setXPathFunctionResolver(functionResolver);
                x = xpath.compile(expr);
                expressions.put(expr, x);
            }
            return x;
        }

        protected Object evaluateXPath(String expr, QName returnType) {

            /*
            if (log.isDebugEnabled()) {
                log.debug("evaluate: '" + expr + "'");
            }
            */

            try {
                XPathExpression x = getXPathExpression(expr);
                return x.evaluate(EVAL_DOCUMENT, returnType);
            }
            catch (XPathExpressionException xpee) {
                log.error("expression: " + expr, xpee);
            }
            return null;
        }

        protected Boolean evaluateXPathToBoolean(String expr) {

            Object result = evaluateXPath(expr, XPathConstants.BOOLEAN);

            return result instanceof Boolean
                ? (Boolean)result
                : null;
        }

        protected void convert(Element current) {

            String variable = expand(current.getAttribute("var"));
            String type     = expand(current.getAttribute("type"));

            Object [] result = new Object[1];

            if (frames.getStore(variable, result)) {
                Object object = TypeConverter.convert(result[0], type);
                frames.put(variable.toUpperCase(), object);
            }
        }


        /** Put <dc:variable> content as variable on stackframes. */
        protected void variable(Element current) {

            String varName = expand(current.getAttribute("name"));
            String expr    = current.getAttribute("expr");
            String type    = current.getAttribute("type");

            if (varName.length() == 0 || expr.length() == 0) {
                log.error("dc:variable 'name' or 'expr' empty.");
            }
            else {
                frames.put(
                    varName.toUpperCase(),
                    evaluateXPath(expr, typeToQName(type)));
            }
        }

        protected String expand(String s) {
            Matcher m = CompiledStatement.VAR.matcher(s);

            Object [] result = new Object[1];

            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String key = m.group(1);
                result[0] = null;
                if (frames.getStore(key, result)) {
                    m.appendReplacement(
                        sb,
                        result[0] != null
                            ? Matcher.quoteReplacement(result[0].toString())
                            : "");
                }
                else {
                    m.appendReplacement(sb, "\\${" + key + "}");
                }
            }
            m.appendTail(sb);
            return sb.toString();
        }

        protected String expandXPathValue(String value) {
            StringBuffer sb = new StringBuffer();
            Matcher m = BRACKET_XPATH.matcher(value);
            while (m.find()) {
                String expr = m.group(1);
                Object result = evaluateXPath(expr, XPathConstants.STRING);
                if (result instanceof String) {
                    m.appendReplacement(
                        sb,
                        Matcher.quoteReplacement((String)result));
                }
                else {
                    m.appendReplacement(sb, "");
                }
            }
            m.appendTail(sb);
            return sb.toString();
        }

        protected void evaluateAttributeValue(Attr attr) {
            String value = attr.getValue();
            if (value.indexOf('{') >= 0) { // Performance tweak
                attr.setValue(expandXPathValue(value));
            }
        }

        protected void build(Node parent, Node current)
        throws SQLException
        {
            String ns = current.getNamespaceURI();
            if (ns != null && ns.equals(DC_NAMESPACE_URI)) {
                if (current.getNodeType() != Node.ELEMENT_NODE) {
                    log.warn("need elements here");
                }
                else {
                    String localName = current.getLocalName();
                    Element curr = (Element)current;
                    if ("attribute".equals(localName)) {
                        attribute(parent, curr);
                    }
                    else if ("context".equals(localName)) {
                        context(parent, curr);
                    }
                    else if ("container-context".equals(localName)) {
                        containerContext(parent, curr);
                    }
                    else if ("if".equals(localName)) {
                        ifClause(parent, curr);
                    }
                    else if ("choose".equals(localName)) {
                        choose(parent, curr);
                    }
                    else if ("call-macro".equals(localName)) {
                        callMacro(parent, curr);
                    }
                    else if ("macro-body".equals(localName)) {
                        macroBody(parent, curr);
                    }
                    else if ("macro".equals(localName)
                         ||  "comment".equals(localName)
                         ||  "statement".equals(localName)
                         ||  "properties".equals(localName)) {
                        // Simply ignore them.
                    }
                    else if ("element".equals(localName)) {
                        element(parent, curr);
                    }
                    else if ("for-each".equals(localName)) {
                        foreach(parent, curr);
                    }
                    else if ("iterate".equals(localName)) {
                        iterate(parent, curr);
                    }
                    else if ("filter".equals(localName)) {
                        filter(parent, curr);
                    }
                    else if ("group".equals(localName)) {
                        group(parent, curr);
                    }
                    else if ("sort".equals(localName)) {
                        sort(parent, curr);
                    }
                    else if ("virtual-column".equals(localName)) {
                        virtualColumn(parent, curr);
                    }
                    else if ("text".equals(localName)) {
                        text(parent, curr);
                    }
                    else if ("message".equals(localName)) {
                        message(parent, curr);
                    }
                    else if ("variable".equals(localName)) {
                        variable(curr);
                    }
                    else if ("convert".equals(localName)) {
                        convert(curr);
                    }
                    else {
                        log.warn("unknown '" + localName + "' -> ignore");
                    }
                }
                return;
            }

            if (current.getNodeType() == Node.TEXT_NODE) {
                String txt = current.getNodeValue();
                if (txt != null && txt.trim().length() == 0) {
                    return;
                }
            }

            if (current.getNodeType() == Node.COMMENT_NODE) {
                // Ignore XML comments
                return;
            }

            Node copy = owner.importNode(current, false);

            NodeList children = current.getChildNodes();
            for (int i = 0, N = children.getLength(); i < N; ++i) {
                build(copy, children.item(i));
            }
            if (copy.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap nnm = ((Element)copy).getAttributes();
                for (int i = 0, N = nnm.getLength(); i < N; ++i) {
                    Node n = nnm.item(i);
                    if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                        evaluateAttributeValue((Attr)n);
                    }
                }
            }
            parent.appendChild(copy);
        }
    } // class BuildHelper


    public Builder() {
        compiledStatements = new HashMap<String, CompiledStatement>();
        macros             = new HashMap<String, Element>();
    }

    public Builder(Document template) {
        this();
        this.template = template;
        extractMacros();
        compileStatements();
    }

    protected static QName typeToQName(String type) {
        if ("number" .equals(type)) return XPathConstants.NUMBER;
        if ("bool"   .equals(type)) return XPathConstants.BOOLEAN;
        if ("node"   .equals(type)) return XPathConstants.NODE;
        if ("nodeset".equals(type)) return XPathConstants.NODESET;
        return XPathConstants.STRING;
    }

    /** Handle <dc:statement> elements. */
    protected void compileStatements() {

        NodeList nodes = template.getElementsByTagNameNS(
            DC_NAMESPACE_URI, "statement");

        for (int i = 0, N = nodes.getLength(); i < N; ++i) {
            Element stmntElement = (Element)nodes.item(i);
            String stmnt = trimStatement(stmntElement.getTextContent());
            if (stmnt == null || stmnt.length() == 0) {
                throw new IllegalArgumentException("found empty statement");
            }
            CompiledStatement cs = new CompiledStatement(stmnt);
            // For faster lookup store a shortend string into the template.
            stmnt = "s" + i;
            stmntElement.setTextContent(stmnt);
            compiledStatements.put(stmnt, cs);
        }
    }

    protected void extractMacros() {
        NodeList ms = template.getElementsByTagNameNS(
            DC_NAMESPACE_URI, "macro");

        for (int i = 0, N = ms.getLength(); i < N; ++i) {
            Element m = (Element)ms.item(i);
            macros.put(m.getAttribute("name"), m);
        }
    }

    protected List<Node> rootsToList() {

        NodeList roots = template.getElementsByTagNameNS(
            DC_NAMESPACE_URI, "template");

        List<Node> elements = new ArrayList<Node>();

        for (int i = 0, N = roots.getLength(); i < N; ++i) {
            NodeList rootChildren = roots.item(i).getChildNodes();
            for (int j = 0, M = rootChildren.getLength(); j < M; ++j) {
                Node child = rootChildren.item(j);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    elements.add(child);
                }
            }
        }

        return elements;
    }

    protected static final String trimStatement(String stmnt) {
        if (stmnt == null) return null;
        //XXX: Maybe a bit to radical for multiline strings?
        return STRIP_LINE_INDENT.matcher(stmnt.trim()).replaceAll(" ");
    }

    protected static Document getOwnerDocument(Node node) {
        Document document = node.getOwnerDocument();
        return document != null ? document : (Document)node;
    }

    public void build(
        List<NamedConnection> connections,
        Node                  output,
        Map<String, Object>   parameters
    )
    throws SQLException
    {
        long startTime = System.currentTimeMillis();
        try {
            BuildHelper helper =
                new BuildHelper(output, connections, parameters);
            helper.build();
        }
        finally {
            if (log.isDebugEnabled()) {
                long stopTime = System.currentTimeMillis();
                log.debug("Building the datacage result took " +
                    (stopTime-startTime)/1000f + " secs.");
            }
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
