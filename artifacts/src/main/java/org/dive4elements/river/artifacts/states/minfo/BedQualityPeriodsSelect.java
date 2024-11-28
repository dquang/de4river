/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import java.util.List;
import java.util.Date;
import java.util.TreeSet;
import java.text.DateFormat;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.river.artifacts.states.DefaultState;

import org.w3c.dom.Element;

import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.access.BedQualityAccess;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.minfo.BedOverview;
import org.dive4elements.river.artifacts.model.minfo.BedOverviewFactory;
import org.dive4elements.river.artifacts.model.minfo.BedloadOverview;
import org.dive4elements.river.artifacts.model.minfo.BedloadOverviewFactory;
import org.dive4elements.river.artifacts.model.DateRange;
import org.dive4elements.river.artifacts.resources.Resources;

import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.KMIndex;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;


public class BedQualityPeriodsSelect extends DefaultState {

    /** The log used in this class. */
    private static Logger log = LogManager.getLogger(
        BedQualityPeriodsSelect.class);

    private static final String I18N_NO_DATA =
        "state.minfo.bed.error.no_data";

    private static final String I18N_NO_DATA_FOR_PERIOD =
        "state.minfo.bed.error.no_data_for_period";

    /**
     * The default constructor that initializes an empty State object.
     */
    public BedQualityPeriodsSelect() {
    }

    /** Get the start and end date of the data at the current position. */
    protected Long[] getDataMinMaxDate(Artifact artifact) {
        D4EArtifact arti = (D4EArtifact) artifact;
        RangeAccess access = new RangeAccess(arti);
        double a = access.getFrom(true);
        double b = access.getTo(true);

        BedOverview overview = BedOverviewFactory.getOverview(
            access.getRiverName());
        BedloadOverview overview2 = BedloadOverviewFactory.getOverview(
            access.getRiverName());

        /* Filter is not implemented and only checks if a complete
         * KMIndex list is acceptable or not. So KMFiltering wont work */
        KMIndex<List<Date>> entries = overview.filter(BedOverview.ACCEPT);
        KMIndex<List<Date>> loads = overview2.filter(BedloadOverview.ACCEPT);
        TreeSet<Date> allDates = new TreeSet<Date>();

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getKm() >= a && entries.get(i).getKm() <= b) {
                allDates.addAll(entries.get(i).getValue());
            }
        }
        for (int i = 0; i < loads.size(); i++) {
            if (loads.get(i).getKm() >= a && loads.get(i).getKm() <= b) {
                allDates.addAll(loads.get(i).getValue());
            }
        }
        if (allDates.size() < 2) {
            return null;
        }

        return new Long[] {allDates.first().getTime(),
                           allDates.last().getTime()};
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        if (!name.equals("periods")) {
            return null;
        }
        Long[] values = getDataMinMaxDate(artifact);
        if (values == null) {
            return null;
        }
        Element def = createItem(
            cr,
            new String[] {"default",
                          values[0].toString() + "," + values[1].toString()}
        );

        return new Element[] { def };
    }

    @Override
    public void validate(Artifact artifact, CallContext context)
    throws IllegalArgumentException {
        D4EArtifact arti = (D4EArtifact) artifact;
        BedQualityAccess access = new BedQualityAccess(arti, context);

        Long[] minMax = getDataMinMaxDate(artifact);
        if (minMax == null) {
            throw new IllegalArgumentException(Resources.getMsg(
                    context.getMeta(),
                    I18N_NO_DATA,
                    I18N_NO_DATA));
        }

        long min = minMax[0];
        long max = minMax[1];
        for (DateRange range: access.getDateRanges()) {
            long a = range.getFrom().getTime();
            long b = range.getTo().getTime();
            log.debug("min max a b " + min + " " + max + " " + a + " " + b);
            if (b < min || a > max || a > b) {
                DateFormat df = Formatter.getDateFormatter(
                    context.getMeta(), "dd.MM.yyyy");
                throw new IllegalArgumentException(Resources.getMsg(
                        context.getMeta(),
                        I18N_NO_DATA_FOR_PERIOD,
                        I18N_NO_DATA_FOR_PERIOD,
                        new Object[] {df.format(range.getFrom()),
                                      df.format(range.getTo())}));
            }
        }
    }

    @Override
    protected String getUIProvider() {
        return "bedquality_periods_select";
    }

}
