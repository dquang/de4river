/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.importer.ImportAnnotationType;

public class AnnotationClassifier
{
    private static Logger log = LogManager.getLogger(AnnotationClassifier.class);

    public static final String TYPES_XPATH =
        "/annotation/types/type";

    public static final String FILE_PATTERNS_XPATH =
        "/annotation/patterns/file";

    public static final String DESCRIPTION_PATTERNS_XPATH =
        "/annotation/patterns/line";


    public static class Pair {

        protected Pattern              pattern;
        protected ImportAnnotationType annType;

        public Pair(Pattern pattern, ImportAnnotationType annType) {
            this.pattern  = pattern;
            this.annType = annType;
        }

        public ImportAnnotationType match(String s) {
            Matcher m = pattern.matcher(s);
            return m.matches() ? annType : null;
        }
    } // class Pair


    protected Map<String, ImportAnnotationType> types;
    protected List<Pair>                        filePatterns;
    protected List<Pair>                        descPatterns;

    protected ImportAnnotationType defaultType;

    public AnnotationClassifier() {
    }

    public AnnotationClassifier(Document rules) {
        types        = new HashMap<String, ImportAnnotationType>();
        filePatterns = new ArrayList<Pair>();
        descPatterns = new ArrayList<Pair>();

        buildRules(rules);
    }

    protected void buildRules(Document rules) {
        buildTypes(rules);
        buildFilePatterns(rules);
        buildDescriptionPatterns(rules);
    }

    protected void buildTypes(Document rules) {

        NodeList typeList = (NodeList)XMLUtils.xpath(
            rules,
            TYPES_XPATH,
            XPathConstants.NODESET,
            null);

        if (typeList == null) {
            log.info("no rules found.");
            return;
        }

        for (int i = 0, N = typeList.getLength(); i < N; ++i) {
            Element typeElement = (Element)typeList.item(i);
            String name = typeElement.getAttribute("name");
            if (name.length() == 0) {
                log.warn("ANNCLASS: rule has no name");
                continue;
            }

            ImportAnnotationType aic = new ImportAnnotationType(name);

            types.put(name, aic);

            if (typeElement.getAttribute("default").equals("true")) {
                defaultType = aic;
            }
        }
    }

    protected void buildFilePatterns(Document rules) {

        NodeList patternList = (NodeList)XMLUtils.xpath(
            rules,
            FILE_PATTERNS_XPATH,
            XPathConstants.NODESET,
            null);

        if (patternList == null) {
            log.info("no file patterns found.");
            return;
        }

        for (int i = 0, N = patternList.getLength(); i < N; ++i) {
            Element element = (Element)patternList.item(i);
            Pair pair = buildPair(element);
            if (pair != null) {
                filePatterns.add(pair);
            }
        }
    }

    protected void buildDescriptionPatterns(Document rules) {

        NodeList patternList = (NodeList)XMLUtils.xpath(
            rules,
            DESCRIPTION_PATTERNS_XPATH,
            XPathConstants.NODESET,
            null);

        if (patternList == null) {
            log.info("no line patterns found.");
            return;
        }

        for (int i = 0, N = patternList.getLength(); i < N; ++i) {
            Element element = (Element)patternList.item(i);
            Pair pair = buildPair(element);
            if (pair != null) {
                descPatterns.add(pair);
            }
        }
    }

    protected Pair buildPair(Element element) {
        String pattern = element.getAttribute("pattern");
        String type    = element.getAttribute("type");

        if (pattern.length() == 0) {
            log.warn("ANNCLASS: pattern has no 'pattern' attribute.");
            return null;
        }

        if (type.length() == 0) {
            log.warn("ANNCLASS: pattern has no 'type' attribute.");
            return null;
        }

        ImportAnnotationType annType = types.get(type);

        if (annType == null) {
            log.warn("ANNCLASS: pattern has unknown type '" + type + "'");
            return null;
        }

        Pattern p;

        try {
            p = Pattern.compile(pattern,
                    Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
        }
        catch (IllegalArgumentException iae) {
            log.warn("ANNCLASS: pattern '" + pattern + "' is invalid.", iae);
            return null;
        }

        return new Pair(p, annType);
    }

    public ImportAnnotationType getDefaultType() {
        return defaultType;
    }

    public ImportAnnotationType classifyFile(String filename) {
        return classifyFile(filename, null);
    }

    public ImportAnnotationType classifyFile(
        String                filename,
        ImportAnnotationType def
    ) {
        if (filename.toLowerCase().endsWith(".km")) {
            filename = filename.substring(0, filename.length()-3);
        }

        for (Pair pair: filePatterns) {
            ImportAnnotationType annType = pair.match(filename);
            if (annType != null) {
                return annType;
            }
        }

        return def;
    }

    public ImportAnnotationType classifyDescription(String description) {
        return classifyDescription(description, null);
    }

    public ImportAnnotationType classifyDescription(
        String                description,
        ImportAnnotationType def
    ) {
        for (Pair pair: descPatterns) {
            ImportAnnotationType annType = pair.match(description);
            if (annType != null) {
                return annType;
            }
        }

        return def;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
