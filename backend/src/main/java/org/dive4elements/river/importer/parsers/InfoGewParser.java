/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;

import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.common.utils.FileTools;

import org.dive4elements.river.importer.ImportRiver;


/** Processes files mentioned in an info file for a river. */
public class InfoGewParser
{
    private static Logger log = LogManager.getLogger(InfoGewParser.class);

    public static final String ENCODING = "ISO-8859-1";

    public static final Pattern GEWAESSER =
        Pattern.compile("^\\s*Gew\u00e4sser\\s*:\\s*(.+)");

    public static final Pattern WST_DATEI =
        Pattern.compile("^\\s*WSTDatei\\s*:\\s*(.+)");

    public static final Pattern BB_INFO =
        Pattern.compile("^\\s*B\\+B-Info\\s*:\\s*(.+)");

    public static final Pattern GEW_UUID =
        Pattern.compile("^\\s*uuid\\s*:\\s*(.+)");

    protected ArrayList<ImportRiver> rivers;

    protected AnnotationClassifier annotationClassifier;

    public InfoGewParser() {
        this(null);
    }

    public InfoGewParser(AnnotationClassifier annotationClassifier) {
        rivers = new ArrayList<ImportRiver>();
        this.annotationClassifier = annotationClassifier;
    }

    public List<ImportRiver> getRivers() {
        return rivers;
    }

    public static final String normalize(String f) {
        return f.replace("\\", "/").replace("/", File.separator);
    }

    /** Handle a gew, wst, or bb_info file. */
    public void parse(File file) throws IOException {

        LineNumberReader in = null;

        File root = file.getParentFile();

        try {
            in =
                new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line = null;

            String riverName  = null;
            String modelUuid  = null;
            File   wstFile    = null;
            File   bbInfoFile = null;

            while ((line = in.readLine()) != null) {
                if ((line = line.trim()).length() == 0) {
                    continue;
                }
                Matcher m = GEWAESSER.matcher(line);

                if (m.matches()) {
                    String river = m.group(1);
                    log.info("Found river '" + river + "'");
                    if (riverName != null) {
                        rivers.add(new ImportRiver(
                            riverName,
                            modelUuid,
                            wstFile,
                            bbInfoFile,
                            annotationClassifier));
                    }
                    riverName  = river;
                    modelUuid  = null;
                    wstFile    = null;
                    bbInfoFile = null;
                }
                else if ((m = WST_DATEI.matcher(line)).matches()) {
                    String wstFilename = m.group(1);
                    File wst = new File(wstFilename = normalize(wstFilename));
                    if (!wst.isAbsolute()) {
                        wst = new File(root, wstFilename);
                    }
                    wst = FileTools.repair(wst);
                    if (!wst.isFile() || !wst.canRead()) {
                        log.error(
                            "cannot access WST file '" + wstFilename + "'");
                        continue;
                    }
                    log.info("Found wst file '" + wst + "'");
                    wstFile = wst;
                }
                else if ((m = GEW_UUID.matcher(line)).matches()) {
                    modelUuid = m.group(1);
                    log.debug("Found model uuid " + modelUuid +
                        " for river " + riverName);
                }
                else if ((m = BB_INFO.matcher(line)).matches()) {
                    //TODO: Make it relative to the wst file.
                    String bbInfo = m.group(1);
                    bbInfoFile = new File(normalize(bbInfo));
                }
            }
            if (riverName != null && wstFile != null) {
                rivers.add(new ImportRiver(
                    riverName,
                    modelUuid,
                    wstFile,
                    bbInfoFile,
                    annotationClassifier));
            }
        }
        finally {
            if (in != null) {
                in.close();
            }
        }

        for (ImportRiver river: rivers) {
            river.parseDependencies();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
