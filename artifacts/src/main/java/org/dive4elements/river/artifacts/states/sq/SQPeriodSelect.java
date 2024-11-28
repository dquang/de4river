/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.sq;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.w3c.dom.Element;

import org.dive4elements.river.artifacts.states.PeriodsSelect;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.model.SQOverview;
import org.dive4elements.river.artifacts.model.SQOverviewFactory;

import org.dive4elements.river.utils.KMIndex;

import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.river.backend.utils.EpsilonComparator;

public class SQPeriodSelect extends PeriodsSelect {

    public static final String UI_PROVIDER = "sq.period.select";

    private static final long serialVersionUID = 1L;

    /** Get either the start date of the data or the end. */
    protected Long getDataMinMaxDate(Artifact artifact, boolean minDate) {
        D4EArtifact arti = (D4EArtifact) artifact;
        RangeAccess access = new RangeAccess(arti);
        double km = access.getLocations()[0];

        SQOverview overview = SQOverviewFactory.getOverview(
            access.getRiverName());

        /* Filter is not implemented and only checks if a complete
         * KMIndex list is acceptable or not. So KMFiltering wont work */
        KMIndex<List<Date>> entries = overview.filter(SQOverview.ACCEPT);
        TreeSet<Date> allDates = new TreeSet<Date>();

        for (int i = 0; i < entries.size(); i++) {
            if (EpsilonComparator.CMP.compare(entries.get(i).getKm(), km)
                == 0
            ) {
                allDates.addAll(entries.get(i).getValue());
            }
        }
        if (allDates.size() < 2) {
            return null;
        }

        return minDate
            ? allDates.first().getTime()
            : allDates.last().getTime();
    }

    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        if (!name.equals("start") && !name.equals("end")) {
            return null;
        }
        Long value = getDataMinMaxDate(artifact, name.equals("start"));
        if (value == null) {
            return null;
        }
        Element def = createItem(
            cr,
            new String[] {"default", value.toString()});

        return new Element[] { def };
    }

    @Override
    public String getUIProvider() {
        return UI_PROVIDER;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
