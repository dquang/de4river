/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Element;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.utils.StringUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.artifacts.WINFOArtifact;


/**
 * Get me doubles (km).
 */
public class EnterMultipleLocationsState extends EnterLocationState {
    /** The log for this class. */
    private static Logger log = LogManager.getLogger(
        EnterMultipleLocationsState.class);

    @Override
    protected String getUIProvider() {
        log.debug("multi location panel");
        return "multi_location_panel";
    }


    /** Deal with multiple double values. */
    @Override
    protected String getLabelFor(
        CallContext cc,
        String      name,
        String      value,
        String      type
    ) {
        String[] vals = value.split(" ");
        for (int i = 0; i < vals.length; i++) {
            vals[i] = super.getLabelFor(cc, name, vals[i], type);
        }

        return StringUtils.join(" ", vals);
    }

    /**
     * This method creates a list of items. These items represent the amount of
     * input data that is possible for this state.
     *
     * @param cr The ElementCreator.
     * @param name The name of the amount of data.
     *
     * @return a list of items.
     */
    @Override
    protected Element[] createItems(
        ElementCreator cr,
        Artifact    artifact,
        String      name,
        CallContext context
    ) {
        if (name.equals("reference_endpoint")) {
            Element[] elements = new Element[1];
            WINFOArtifact winfo = (WINFOArtifact) artifact;
            Double km = winfo.getReferenceStartKm();
            elements[0] = createItem(
                cr,
                new String[] {"start_km", km.toString()});
            return elements;
        }
        return null;
    }

}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
