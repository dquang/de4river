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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.importer.ImportDischargeZone;
import org.dive4elements.river.importer.ImportFlowVelocityModel;
import org.dive4elements.river.importer.ImportFlowVelocityModelValue;
import org.dive4elements.river.backend.utils.EpsilonComparator;


public class FlowVelocityModelParser extends LineParser {

    private static final Logger log =
        LogManager.getLogger(FlowVelocityModelParser.class);

    private static final Pattern META_REGEX =
        Pattern.compile(".*Rechnung [unter ]*(.*) \\(Pegel (.*)\\).*");

    private static final Pattern META_GAUGE =
        Pattern.compile("(.*) Q=(\\w*)m3/s");

    private static final Pattern META_MAINVALUE_A =
        Pattern.compile("([a-zA-Z]+)+(\\d+)*[\\w()]*");

    private static final Pattern META_MAINVALUE_B =
        Pattern.compile(
            "(([a-zA-Z]+)+(\\d+)*)\\s*-\\s*(([a-zA-Z]+)+(\\d+)*\\S*)");

    private static final Pattern META_MAINVALUE_C =
        Pattern.compile("([0-9]++)\\s?(\\S*)|([0-9]++,[0-9]++)\\s?(\\S*)");

    private static final Pattern META_MAINVALUE_D =
        Pattern.compile(
            "(([0-9]*)\\s?(\\w*)|([0-9]++,[0-9]++)\\s?(\\w*))\\s*"
            + "bis (([0-9]*)\\s?(\\S*)|([0-9]++,[0-9]++)\\s?(\\S*))");

    private static final Pattern META_MAINVALUE_E =
        Pattern.compile(
            "(([a-zA-Z]+)+(\\d+)*)\\s*bis (([a-zA-Z]+)+(\\d+)*\\S*)");

    private static final NumberFormat nf =
        NumberFormat.getInstance(DEFAULT_LOCALE);


    private List<ImportFlowVelocityModel> models;

    private ImportFlowVelocityModel current;

    protected String description;

    protected TreeSet<Double> kmExists;


    public FlowVelocityModelParser() {
        models = new ArrayList<ImportFlowVelocityModel>();
        kmExists = new TreeSet<Double>(EpsilonComparator.CMP);
    }


    public List<ImportFlowVelocityModel> getModels() {
        return models;
    }

    @Override
    public void parse(File file) throws IOException {
        description = file.getName();

        super.parse(file);
    }

    @Override
    protected void reset() {
        current = new ImportFlowVelocityModel(description);
        kmExists.clear();
    }


    @Override
    protected void finish() {
        models.add(current);
        // description = null;
    }


    @Override
    protected void handleLine(int lineNum, String line) {
        if (line.startsWith(START_META_CHAR)) {
            handleMetaLine(stripMetaLine(line));
        }
        else {
            handleDataLine(line);
        }
    }


    protected void handleMetaLine(String line) {
        Matcher m = META_REGEX.matcher(line);

        if (m.matches()) {
            String mainValueStr = m.group(1);
            log.debug("mainValueStr = '" + mainValueStr + "'");
            String gaugeStr     = m.group(2);

            Object[] valueData = handleMainValueString(mainValueStr);
            Object[] gaugeData = handleGaugeString(gaugeStr);

            if (valueData == null || valueData.length < 2) {
                log.warn("skip invalid MainValue part in '" + line + "'");
                return;
            }

            if (gaugeData == null || gaugeData.length < 2) {
                log.warn("skip invalid gauge part in '" + line + "'");
                return;
            }

            if (log.isDebugEnabled()) {
                log.debug("Found meta information:");
                log.debug("   Gauge: " + gaugeData[0]);
                log.debug("   Value: " + gaugeData[1]);
                log.debug("   Lower: " + valueData[0]);
                log.debug("   upper: " + valueData[1]);
            }

            current.setDischargeZone(new ImportDischargeZone(
                (String) gaugeData[0],
                (BigDecimal) gaugeData[1],
                (String) valueData[0],
                (String) valueData[1]
            ));
        }
    }


    protected Object[] handleMainValueString(String mainValueStr) {
        Matcher mA = META_MAINVALUE_A.matcher(mainValueStr.trim());
        if (mA.matches()) {
            log.debug("mainValueStr matches META_MAINVALUE_A");
            String name = mA.group(0);

            return new Object[] { name, name };
        }

        Matcher mB = META_MAINVALUE_B.matcher(mainValueStr.trim());
        if (mB.matches()) {
            log.debug("mainValueStr matches META_MAINVALUE_B");
            String lower = mB.group(1);
            String upper = mB.group(4);

            return new Object[] { lower, upper };
        }

        Matcher mC = META_MAINVALUE_C.matcher(mainValueStr.trim());
        if (mC.matches()) {
            log.debug("mainValueStr matches META_MAINVALUE_C");
            String facA  = mC.group(1);
            String nameA = mC.group(2);
            String facB  = mC.group(3);
            String nameB = mC.group(4);

            String fac  = facA  != null ? facA  : facB;
            String name = nameA != null ? nameA : nameB;

            String mainValue = fac + " " + name;

            return new Object[] { mainValue, mainValue };
        }

        Matcher mD = META_MAINVALUE_D.matcher(mainValueStr.trim());
        if (mD.matches()) {
            log.debug("mainValueStr matches META_MAINVALUE_D");
            String loFacA  = mD.group(2);
            String loNameA = mD.group(3);
            String loFacB  = mD.group(4);
            String loNameB = mD.group(5);

            String upFacA  = mD.group(7);
            String upNameA = mD.group(8);
            String upFacB  = mD.group(9);
            String upNameB = mD.group(10);

            String loFac  = loFacA  != null ? loFacA  : loFacB;
            String loName = loNameA != null ? loNameA : loNameB;

            String upFac  = upFacA  != null ? upFacA  : upFacB;
            String upName = upNameA != null ? upNameA : upNameB;

            String loMainValue = loFac + " " + loName;
            String upMainValue = upFac + " " + upName;

            return new Object[] { loMainValue, upMainValue };
        }

        Matcher mE = META_MAINVALUE_E.matcher(mainValueStr.trim());
        if (mE.matches()) {
            log.debug("mainValueStr matches META_MAINVALUE_E");
            String lower = mE.group(1);
            String upper = mE.group(4);

            return new Object[] { lower, upper };
        }

    log.debug("mainValueStr not matched");
        return null;
    }


    protected Object[] handleGaugeString(String gaugeStr) {
        Matcher m = META_GAUGE.matcher(gaugeStr);

        if (m.matches()) {
            String name = m.group(1);
            String qStr = m.group(2);

            try {
                return new Object[] {
                    name,
                    new BigDecimal(nf.parse(qStr).doubleValue()) };
            }
            catch (ParseException pe) {
                log.warn("Could not parse Q value: '" + qStr + "'");
            }
        }

        return null;
    }


    protected void handleDataLine(String line) {
        String[] cols = line.split(SEPERATOR_CHAR);

        if (cols.length < 5) {
            log.warn("skip invalid data line: '" + line + "'");
            return;
        }

        try {
            double km = nf.parse(cols[0]).doubleValue();

            Double key = Double.valueOf(km);

            if (kmExists.contains(key)) {
                log.warn("duplicate station '" + km + "': -> ignored");
                return;
            }

            double q      = nf.parse(cols[1]).doubleValue();
            double total  = nf.parse(cols[2]).doubleValue();
            double main   = nf.parse(cols[3]).doubleValue();
            double stress = nf.parse(cols[4]).doubleValue();

            current.addValue(new ImportFlowVelocityModelValue(
                new BigDecimal(km),
                new BigDecimal(q),
                new BigDecimal(total),
                new BigDecimal(main),
                new BigDecimal(stress)
            ));

            kmExists.add(key);
        }
        catch (ParseException pe) {
            log.warn("Unparseable flow velocity values:", pe);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
