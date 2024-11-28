/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.dive4elements.river.exports.process.Processor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.D4EArtifact;

import org.dive4elements.artifacts.common.utils.ElementConverter;

public class DiagramAttributes
implements   ElementConverter, D4EArtifact.FacetFilter
{
    private static Logger log = LogManager.getLogger(DiagramAttributes.class);

    public interface Evaluator {
        Object evaluate(D4EArtifact artifact, CallContext context);
    } // interface Evaluator

    public static final Evaluator TRUE = new Evaluator() {
        @Override
        public Object evaluate(D4EArtifact artifact, CallContext context) {
            return Boolean.TRUE;
        }
    };

    public static final Evaluator FALSE = new Evaluator() {
        @Override
        public Object evaluate(D4EArtifact artifact, CallContext context) {
            return Boolean.FALSE;
        }
    };

    public class Instance {

        private List<Processor> processors;

        public Instance() {
            processors = createProcessors();
        }

        private List<Processor> createProcessors() {
            List<Processor> processors =
                new ArrayList<Processor>(axesProcessors.size());
            for (AxisProcessor ap: axesProcessors) {
                Processor pr = ap.createProcessor();
                if (pr != null) {
                    processors.add(pr);
                }
            }
            return processors;
        }

        public List<Processor> getProcessorsForAxisName(String axisName) {
            List<Processor> retval = new ArrayList<Processor>(5);
            for (Processor pr: processors) {
                String aName = pr.getAxisName();
                if (aName != null && axisName.equals(aName)) {
                    retval.add(pr);
                }
            }
            return retval;
        }

        public List<Processor> getProcessors() {
            return processors;
        }

        public Title getTitle() {
            return DiagramAttributes.this.getTitle();
        }

        public Title getSubtitle() {
            return DiagramAttributes.this.getSubtitle();
        }

        public DomainAxisAttributes getDomainAxis() {
            return DiagramAttributes.this.getDomainAxis();
        }

        public int getAxisIndex(String axisName) {
            return DiagramAttributes.this.getAxisIndex(axisName);
        }

        public String getAxisName(int index) {
            return DiagramAttributes.this.getAxisName(index);
        }

        public List<AxisAttributes> getAxesAttributes() {
            return DiagramAttributes.this.getAxesAttributes();
        }
    } // class Instance

    public static class AxisAttributes {
        private String  name;
        private boolean isLeftAlign; // TODO: Remove!
        private boolean forceAlign;  // TODO: Remove!
        private boolean includeZero; // TODO: Use Evaluator

        private Evaluator isInverted;
        private Evaluator isLog;

        public AxisAttributes() {
        }

        public AxisAttributes(
            String    name,
            boolean   isLeftAlign,
            boolean   forceAlign,
            boolean   includeZero,
            Evaluator isInverted,
            Evaluator isLog
        ) {
            this.name        = name;
            this.isLeftAlign = isLeftAlign;
            this.forceAlign  = forceAlign;
            this.includeZero = includeZero;
            this.isInverted  = isInverted;
            this.isLog       = isLog;
        }

        public String getName() {
            return name;
        }

        public boolean isLeftAlign() {
            return isLeftAlign;
        }

        public boolean forceAlign() {
            return forceAlign;
        }

        public boolean includeZero() {
            return includeZero;
        }

        public Evaluator isInverted() {
            return isInverted;
        }

        public Evaluator isLog() {
            return isLog;
        }
    } // class AxisAttributes

    public class DomainAxisAttributes extends AxisAttributes {

        private Title title;

        public DomainAxisAttributes() {
        }

        public DomainAxisAttributes(
            String    name,
            boolean   isLeftAlign,
            boolean   forceAlign,
            boolean   includeZero,
            Evaluator isInverted,
            Evaluator isLog,
            Title     title
        ) {
            super(name, isLeftAlign, forceAlign, includeZero, isInverted,
                    isLog);
            this.title = title;
        }

        public Title getTitle() {
            return title;
        }
    } // class DomainAxisAttributes

    public static class AxisProcessor {

        private Class<Processor> processorClass;
        private String axisName;

        public AxisProcessor(Class<Processor> processorClass, String axisName) {
            this.processorClass = processorClass;
            this.axisName = axisName;
        }

        public Processor createProcessor() {
            try {
                Processor pr = processorClass.newInstance();
                pr.setAxisName(axisName);
                return pr;
            }
            catch (InstantiationException ie) {
                log.error(ie, ie);
            }
            catch (IllegalAccessException iae) {
                log.error(iae, iae);
            }
            return null;
        }

    } // class AxisProcessor

    public abstract static class ConvertEvaluator
    implements Evaluator
    {
        protected String key;
        protected String type;

        public ConvertEvaluator() {
        }

        public ConvertEvaluator(String key, String type) {
            this.key  = key;
            this.type = type;
        }

        protected Object convert(Object value) {
            if (value == null || type == null || type.isEmpty()) {
                return value;
            }
            if (value instanceof String) {
                String v = (String)value;
                if ("double".equals(type)) {
                    return Double.valueOf(v);
                }
                if ("int".equals(type)) {
                    return Integer.valueOf(v);
                }
                if ("string".equals(type)) {
                    return v;
                }
            }
            // TODO: Support more types
            return value;
        }
    } // class ConvertEvaluator

    public static class ContextEvaluator extends ConvertEvaluator {

        public ContextEvaluator() {
        }

        public ContextEvaluator(String key, String type) {
            super(key, type);
        }

        @Override
        public Object evaluate(D4EArtifact artifact, CallContext context) {
            return convert(context.getContextValue(key));
        }
    } // class ContextEvaluator

    public static class ArtifactEvaluator extends ConvertEvaluator {

        public ArtifactEvaluator() {
        }

        public ArtifactEvaluator(String key, String type) {
            super(key, type);
        }

        @Override
        public Object evaluate(D4EArtifact artifact, CallContext context) {
            return convert(artifact.getDataAsString(key));
        }
    } // class ContextEvaluator

    public static class StringEvaluator implements Evaluator {

        private String value;

        public StringEvaluator() {
        }
        public StringEvaluator(String value) {
            this.value = value;
        }

        @Override
        public Object evaluate(D4EArtifact artifact, CallContext context) {
            return value;
        }
    } // class StringEvaluator

    public static class Title {

        private String key;
        private String def;
        private List<Evaluator> arguments;

        public Title() {
            arguments = new ArrayList<Evaluator>(5);
        }

        public Title(String key) {
            this(key, key);
        }

        public Title(String key, String def) {
            this();
            this.key = key;
            this.def = def;
        }

        public String getKey() {
            return key;
        }

        public void addArgument(Evaluator argument) {
            arguments.add(argument);
        }

        public String evaluate(D4EArtifact artifact, CallContext context) {
            if (key == null || key.isEmpty()) {
                return def;
            }
            Object [] args = new Object[arguments.size()];
            for (int i = 0; i < args.length; ++i) {
                args[i] = arguments.get(i).evaluate(artifact, context);
            }
            return Resources.getMsg(context.getMeta(), key, def, args);
        }
    } // class Title

    private List<AxisAttributes> axesAttrs;
    private List<AxisProcessor>  axesProcessors;

    private Title title;
    private Title subtitle;

    private DomainAxisAttributes domainAxis;

    public DiagramAttributes() {
        axesAttrs      = new ArrayList<AxisAttributes>(5);
        axesProcessors = new ArrayList<AxisProcessor>(5);
    }

    @Override
    public Object convert(Element config) {
        parseAxis(config);
        parseProcessors(config);
        parseTitle(config);
        parseSubtitle(config);
        parseDomainAxis(config);
        return this;
    }

    public List<AxisAttributes> getAxesAttributes() {
        return axesAttrs;
    }

    private void parseAxis(Element config) {
        NodeList axisNodes = config.getElementsByTagName("axis");

        for (int i = 0, N = axisNodes.getLength(); i < N; ++i) {
            Element axisElement = (Element)axisNodes.item(i);
            String name = axisElement.getAttribute("name").trim();
            String align = axisElement.getAttribute("align").trim();
            String includeZero =
                axisElement.getAttribute("include-zero").trim();

            String isInverted = axisElement.getAttribute("inverted");
            String isLog = axisElement.getAttribute("logarithmic");

            if (name.isEmpty()) {
                continue;
            }
            boolean isleftAlign = false;
            boolean forceAlign = false;
            for (String part: align.split("[\\s,]")) {
                part = part.trim();
                     if ("left" .equals(part)) isleftAlign = true;
                else if ("right".equals(part)) isleftAlign = false;
                else if ("force".equals(part)) forceAlign  = true;
            }

            Evaluator isInvertedE = parseEvaluator(isInverted, FALSE);

            Evaluator isLogE = parseEvaluator(isLog, FALSE);

            axesAttrs.add(new AxisAttributes(
                name, isleftAlign, forceAlign,
                includeZero.equals("true"),
                isInvertedE, isLogE));
        }
    }

    private static Evaluator createDynamicEvaluator(
        String    className,
        Evaluator def
    ) {
        try {
            Class<Evaluator> clazz = (Class<Evaluator>)Class.forName(className);
            return clazz.newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            log.error(cnfe, cnfe);
        }
        catch (InstantiationException ie) {
            log.error(ie, ie);
        }
        catch (IllegalAccessException iae) {
            log.error(iae, iae);
        }
        return def;
    }

    private static Evaluator parseEvaluator(String s, Evaluator def) {
        if ((s = s.trim()).isEmpty()) return def;
        if ("true".equals(s)) return TRUE;
        if ("false".equals(s)) return FALSE;
        if (s.endsWith("()")) {
            return createDynamicEvaluator(
                s.substring(0, s.length()-2).trim(),
                def);
        }
        return def;
    }

    public List<AxisProcessor> getAxesProcessors() {
        return axesProcessors;
    }

    public Title getTitle() {
        return title;
    }

    public Title getSubtitle() {
        return subtitle;
    }

    public DomainAxisAttributes getDomainAxis() {
        return domainAxis;
    }

    private void parseProcessors(Element config) {
        NodeList processorNodes = config.getElementsByTagName("processor");

        for (int i = 0, N = processorNodes.getLength(); i < N; ++i) {
            Element processorElement = (Element)processorNodes.item(i);
            String className = processorElement.getAttribute("class").trim();
            String axisName = processorElement.getAttribute("axis").trim();
            if (className.isEmpty() || axisName.isEmpty()) {
                continue;
            }

            try {
                Class<Processor> processorClass =
                    (Class<Processor>)Class.forName(className);
                axesProcessors.add(new AxisProcessor(processorClass, axisName));
            }
            catch (ClassNotFoundException cnfe) {
                log.error(cnfe, cnfe);
            }
        }
    }

    private void parseTitle(Element config) {
        title = extractTitle(config, "title");
    }

    private void parseSubtitle(Element config) {
        subtitle = extractTitle(config, "subtitle");
    }

    private void parseDomainAxis(Element config) {
        Title title = extractTitle(config, "domain-axis");
        String includeZero = "";
        String isInverted = "";
        String isLog = "";

        NodeList dAlist = config.getElementsByTagName("domain-axis");
        if (dAlist.getLength() > 0) {
            Element dAelement = (Element)dAlist.item(0);

            includeZero = dAelement.getAttribute("include-zero");
            isInverted = dAelement.getAttribute("inverted");
            isLog = dAelement.getAttribute("logarithmic");
        }

        domainAxis = new DomainAxisAttributes(
            "X",
            false,
            false,
            includeZero.equals("true"),
            parseEvaluator(isInverted, FALSE),
            parseEvaluator(isLog, FALSE),
            title);
    }

    private static Title extractTitle(Element config, String tagName) {
        NodeList titleNodes = config.getElementsByTagName(tagName);
        if (titleNodes.getLength() < 1) {
            return null;
        }
        Element titleElement = (Element)titleNodes.item(0);
        String key = titleElement.getAttribute("key");
        String def = titleElement.getAttribute("default");
        Title title = new Title(key, def);
        NodeList argumentNodes = titleElement.getElementsByTagName("arg");
        for (int i = 0, N = argumentNodes.getLength(); i < N; ++i) {
            Element argumentElement = (Element)argumentNodes.item(i);
            String expression = argumentElement.getAttribute("expr");
            String type = argumentElement.getAttribute("type");

            Evaluator ev = new StringEvaluator(expression);

            if (expression.endsWith("()")) {
                ev = createDynamicEvaluator(
                    expression.substring(0, expression.length()-2).trim(),
                    ev);
            }
            else if (expression.startsWith("artifact.")) {
                ev = new ArtifactEvaluator(
                    expression.substring("artifact.".length()), type);
            }
            else if (expression.startsWith("context.")) {
                ev = new ContextEvaluator(
                    expression.substring("context.".length()), type);
            }

            title.addArgument(ev);
        }
        return title;
    }

    public int getAxisIndex(String axisName) {
        for (int i = axesAttrs.size()-1; i >= 0; --i) {
            if (axesAttrs.get(i).getName().equals(axisName)) {
                return i;
            }
        }
        return -1;
    }

    public String getAxisName(int index) {
        return index < 0 || index >= axesAttrs.size()
            ? "" // null?
            : axesAttrs.get(index).getName();
    }

    @Override
    public boolean accept(String outName, String facetName) {
        Instance instance = new Instance();
        for (Processor pr: instance.getProcessors()) {
            if (pr.canHandle(facetName)) {
                return true;
            }
        }
        return false;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
