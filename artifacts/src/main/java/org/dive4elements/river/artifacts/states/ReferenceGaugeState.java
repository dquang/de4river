/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.utils.RiverUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ReferenceGaugeState extends DefaultState {

    public static final String DATA_NAME = "reference_gauge";


    @Override
    protected Element[] createItems(
        XMLUtils.ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context)
    {
        River       river   = RiverUtils.getRiver((D4EArtifact) artifact);
        List<Gauge> gauges  = river.getGauges();
        Collections.sort(gauges);

        int num = gauges != null ? gauges.size() : 0;

        Element[] opts = new Element[num];

        for (int i = 0; i < num; i++ ) {
            Gauge g = gauges.get(i);

            String gaugeName      = g.getName();
            long   officialNumber = g.getOfficialNumber();

            opts[i] = createItem(
                cr, new String[] { gaugeName, String.valueOf(officialNumber) });
        }

        return opts;
    }


    @Override
    protected String getLabelFor(
        CallContext cc,
        String      name,
        String      value,
        String      type
    ) {
        if (name.equals(DATA_NAME)) {
            try {
                long  number = Long.valueOf(value);
                Gauge gauge  = Gauge.getGaugeByOfficialNumber(number);

                if (gauge != null) {
                    return gauge.getName();
                }
            }
            catch (NumberFormatException nfe) {
                // do nothing
            }
        }

        return super.getLabelFor(cc, name, value, type);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
