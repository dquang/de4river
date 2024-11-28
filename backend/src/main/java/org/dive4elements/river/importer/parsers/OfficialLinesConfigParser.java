/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class OfficialLinesConfigParser {

    private static Logger log = LogManager.getLogger(
        OfficialLinesConfigParser.class);

    public static final String ENCODING = "ISO-8859-1";

    private List<String> mainValueNames;

    public OfficialLinesConfigParser() {
        mainValueNames = new ArrayList<String>();
    }

    public void reset() {
        mainValueNames.clear();
    }

    public void parse(File file) throws IOException {

        log.info("Parsing offical lines config file: " + file);

        LineNumberReader reader =
            new LineNumberReader(
            new InputStreamReader(
            new FileInputStream(file), ENCODING));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).length() == 0
                    || line.charAt(0) == '*') {
                    continue;
                }
                NameAndTimeInterval nat = NameAndTimeInterval.parseName(line);
                mainValueNames.add(nat.getName());
            }
        }
        finally {
            reader.close();
        }
    }

    public List<String> getMainValueNames() {
        return mainValueNames;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
