/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.importer.parsers;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.LineNumberReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import java.math.BigDecimal;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.HashMap;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportMainValueType;
import org.dive4elements.river.importer.ImportMainValue;
import org.dive4elements.river.importer.ImportNamedMainValue;
import org.dive4elements.river.importer.ImportGauge;

public class StaFileParser
{
    private static Logger log = LogManager.getLogger(StaFileParser.class);

    public static final String ENCODING = "ISO-8859-1";

    public static final String TYPES =
        System.getProperty("flys.backend.main.value.types", "QWTD");

    public static final Pattern QWTD_ =
        Pattern.compile("\\s*([^\\s]+)\\s+([^\\s]+)\\s+([" +
            Pattern.quote(TYPES) + "]).*");


    public StaFileParser() {
    }

    public boolean parse(ImportGauge gauge) throws IOException {

        File file = gauge.getStaFile();

        log.info("parsing STA file: " + file);

        LineNumberReader in = null;
        try {
            in = new LineNumberReader(
                new InputStreamReader(
                new FileInputStream(file), ENCODING));

            String line = in.readLine();

            if (line == null) {
                log.warn("STA file is empty.");
                return false;
            }

            if (line.length() < 37) {
                log.warn("First line in STA file is too short.");
                return false;
            }

            String gaugeName = line.substring(16, 35).trim();

            Long gaugeNumber = null;

            String gaugeNumberString = line.substring(8, 16).trim();

            try {
                gaugeNumber = Long.parseLong(gaugeNumberString);
            }
            catch (NumberFormatException nfe) {
                log.warn("STA: gauge number '" + gaugeNumberString +
                         "' is not a valid long number.");
            }

            gauge.setName(gaugeName);
            gauge.setOfficialNumber(gaugeNumber);

            if (log.isDebugEnabled()) {
                log.debug(
                    "name/number: '" + gaugeName + "' '" + gaugeNumber + "'");
            }

            String [] values = line.substring(38).trim().split("\\s+", 2);

            if (values.length < 2) {
                log.warn("STA: Not enough columns for aeo and datum.");
            }
            try {
                gauge.setAeo(new BigDecimal(values[0].replace(",", ".")));
                gauge.setDatum(new BigDecimal(values[1].replace(",", ".")));
            }
            catch (NumberFormatException nfe) {
                log.warn("STA: cannot parse aeo or datum.");
                return false;
            }

            line = in.readLine();

            if (line == null) {
                log.warn("STA file has not enough lines");
                return false;
            }

            if (line.length() < 36) {
                log.warn("STA: second line is too short");
                return false;
            }

            try {
                gauge.setStation(
                    new BigDecimal(line.substring(29, 36).trim()));
            }
            catch (NumberFormatException nfe) {
                log.warn("STA: parsing of the datum of the gauge failed");
                return false;
            }

            // overread the next six lines
            for (int i = 0; i < 6; ++i) {
                if ((line = in.readLine()) == null) {
                    log.warn("STA file is too short");
                    return false;
                }
            }

            HashMap<String, ImportMainValueType> types =
                new HashMap<String, ImportMainValueType>();

            ArrayList<ImportNamedMainValue> namedMainValues =
                new ArrayList<ImportNamedMainValue>();

            ArrayList<ImportMainValue> mainValues =
                new ArrayList<ImportMainValue>();

            while ((line = in.readLine()) != null) {
                Matcher m = QWTD_.matcher(line);
                if (m.matches()) {
                    BigDecimal value;
                    try {
                        value = new BigDecimal(m.group(2).replace(",", "."));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn("STA: value not parseable in line "
                            + in.getLineNumber());
                        continue;
                    }
                    String typeString = m.group(3);
                    log.debug("\t type: " + typeString);
                    ImportMainValueType type = types.get(typeString);
                    if (type == null) {
                        type = new ImportMainValueType(typeString);
                        types.put(typeString, type);
                    }
                    String name = m.group(1);
                    NameAndTimeInterval nat =
                        NameAndTimeInterval.parseName(name);
                    ImportNamedMainValue namedMainValue =
                        new ImportNamedMainValue(type, nat.getName());
                    namedMainValues.add(namedMainValue);

                    ImportMainValue mainValue = new ImportMainValue(
                        gauge,
                        namedMainValue,
                        value,
                        nat.getTimeInterval());

                    mainValues.add(mainValue);
                }
                else {
                    // TODO: treat as a comment
                }
            }
            gauge.setMainValueTypes(
                new ArrayList<ImportMainValueType>(types.values()));
            gauge.setNamedMainValues(namedMainValues);
            gauge.setMainValues(mainValues);
        }
        catch (FileNotFoundException fnfe) {
            log.error(fnfe.getMessage());
            return false;
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
        log.info("finished parsing STA file: " + file);
        return true;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
