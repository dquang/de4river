/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.util.HashMap;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;
import java.io.File;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.ImportAnnotation;
import org.dive4elements.river.importer.ImportRange;
import org.dive4elements.river.importer.ImportEdge;
import org.dive4elements.river.importer.ImportAnnotationType;
import org.dive4elements.river.importer.ImportAttribute;
import org.dive4elements.river.importer.ImportPosition;

public class AnnotationsParser
{
    private static Logger log = LogManager.getLogger(AnnotationsParser.class);

    public static final String ENCODING = "ISO-8859-1";

    public static final String [] TO_SCAN = {
        "Basisdaten",
        "Streckendaten",
        ".." + File.separator +
            "Morphologie" + File.separator + "Streckendaten"
    };

    protected HashMap<String, ImportAttribute> attributes;
    protected HashMap<String, ImportPosition>  positions;
    protected TreeSet<ImportAnnotation>        annotations;
    protected AnnotationClassifier             classifier;

    public AnnotationsParser() {
        this(null);
    }

    public AnnotationsParser(AnnotationClassifier classifier) {
        attributes  = new HashMap<String, ImportAttribute>();
        positions   = new HashMap<String, ImportPosition>();
        annotations = new TreeSet<ImportAnnotation>();
        this.classifier = classifier;
    }

    public void parseFile(File file) throws IOException {
        log.info("parsing km file: '" + file + "'");

        ImportAnnotationType defaultIAT = null;

        if (classifier != null) {
            defaultIAT = classifier.classifyFile(
                file.getName(),
                classifier.getDefaultType());
        }

        LineNumberReader in = null;
        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line = null;
            while ((line = in.readLine()) != null) {
                if ((line = line.trim()).length() == 0
                || line.startsWith("*")) {
                    continue;
                }

                String [] parts = line.split("\\s*;\\s*");

                if (parts.length < 3) {
                    log.warn("ANN: not enough columns in line "
                        + in.getLineNumber());
                    continue;
                }

                ImportPosition position = positions.get(parts[0]);
                if (position == null) {
                    position = new ImportPosition(parts[0]);
                    positions.put(parts[0], position);
                }

                ImportAttribute attribute = attributes.get(parts[1]);
                if (attribute == null) {
                    attribute = new ImportAttribute(parts[1]);
                    attributes.put(parts[1], attribute);
                }

                String [] r = parts[2].replace(",", ".").split("\\s*#\\s*");

                BigDecimal from, to;

                try {
                    from = new BigDecimal(r[0]);
                    to   = r.length < 2 ? null : new BigDecimal(r[1]);
                    if (to != null && from.compareTo(to) > 0) {
                        BigDecimal t = from; from = to; to = t;
                    }
                }
                catch (NumberFormatException nfe) {
                    log.warn("ANN: invalid number in line "
                        + in.getLineNumber());
                    continue;
                }

                ImportEdge edge = null;

                if (parts.length == 4) { // Only 'Unterkante'
                    try {
                        edge = new ImportEdge(
                            null,
                            new BigDecimal(parts[3].trim().replace(',', '.')));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn("ANN: cannot parse 'Unterkante' in line " +
                            in.getLineNumber());
                    }
                }
                else if (parts.length > 4) { // 'Unterkante' and 'Oberkante'
                    String bottom = parts[3].trim().replace(',', '.');
                    String top    = parts[4].trim().replace(',', '.');
                    try {
                        BigDecimal b = bottom.length() == 0
                            ? null
                            : new BigDecimal(bottom);
                        BigDecimal t = top.length() == 0
                            ? null
                            : new BigDecimal(top);
                        edge = new ImportEdge(t, b);
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(
                            "ANN: cannot parse 'Unterkante' or 'Oberkante' "
                            + "in line "
                            + in.getLineNumber());
                    }
                }

                ImportRange range = new ImportRange(from, to);

                ImportAnnotationType type = classifier != null
                    ? classifier.classifyDescription(line, defaultIAT)
                    : null;

                ImportAnnotation annotation = new ImportAnnotation(
                    attribute, position, range, edge, type);

                if (!annotations.add(annotation)) {
                    log.info("ANN: duplicated annotation '" + parts[0] +
                        "' in line " + in.getLineNumber());
                }
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void parse(File root) throws IOException {

        for (String toScan: TO_SCAN) {
            File directory = FileTools.repair(new File(root, toScan));
            if (!directory.isDirectory()) {
                log.warn("ANN: '" + directory + "' is not a directory.");
                continue;
            }
            File [] files = directory.listFiles();
            if (files == null) {
                log.warn("ANN: cannot list directory '" + directory + "'");
                continue;
            }

            for (File file: files) {
                if (file.isFile() && file.canRead()
                && file.getName().toLowerCase().endsWith(".km")) {
                    parseFile(file);
                }
            }
        } // for all directories to scan
    }

    public List<ImportAnnotation> getAnnotations() {
        return new ArrayList<ImportAnnotation>(annotations);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
