/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.io.File;

import org.w3c.dom.Element;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.model.DGM;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.utils.RiverUtils;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DGMSelect extends DefaultState {

    private static final Logger log = LogManager.getLogger(DGMSelect.class);

    public static final String ERR_EMPTY         = "error_no_dgm_selected";
    public static final String ERR_INVALID_DGM   = "error_invalid_dgm_selected";
    public static final String ERR_BAD_DGM_RANGE = "error_bad_dgm_range";
    public static final String ERR_BAD_DGM_RIVER = "error_bad_dgm_river";


    @Override
    protected String getUIProvider() {
        return "dgm_datacage_panel";
    }


    @Override
    protected Element createStaticData(
        D4EArtifact   flys,
        ElementCreator creator,
        CallContext    cc,
        String         name,
        String         value,
        String         type
    ) {
        Element dataElement = creator.create("data");
        creator.addAttr(dataElement, "name", name, true);
        creator.addAttr(dataElement, "type", type, true);

        Element itemElement = creator.create("item");
        creator.addAttr(itemElement, "value", value, true);

        creator.addAttr(itemElement, "label", getLabel(cc, value), true);
        dataElement.appendChild(itemElement);

        return dataElement;
    }


    public static String getLabel(CallContext cc, String value) {
        log.debug("Create label for value: " + value);

        try {
            DGM dgm = DGM.getDGM(Integer.parseInt(value));

            File file = new File(dgm.getPath());
            return file.getName();
        }
        catch (NumberFormatException nfe) {
            log.warn("Cannot parse int value: '" + value + "'");
        }

        return "";
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        D4EArtifact flys = (D4EArtifact) artifact;

        DGM dgm = getDGM(flys);

        if (dgm == null) {
            throw new IllegalArgumentException(ERR_INVALID_DGM);
        }

        double l = dgm.getRange().getA().doubleValue();
        double u = dgm.getRange().getB().doubleValue();

        double[] range = RiverUtils.getKmFromTo(flys);

        if (range[0] < l || range[0] > u || range[1] < l || range[1] > u) {
            throw new IllegalArgumentException(ERR_BAD_DGM_RANGE);
        }

        River selectedRiver = RiverUtils.getRiver(flys);
        River dgmRiver      = dgm.getRiver();

        if (selectedRiver != dgmRiver) {
            throw new IllegalArgumentException(ERR_BAD_DGM_RIVER);
        }

        return true;
    }


    /**
     * Returns the DGM specified in the parameters of <i>flys</i>.
     *
     * @param flys The D4EArtifact that knows the ID of a DGM.
     *
     * @throws IllegalArgumentException If the D4EArtifact doesn't know the ID
     * of a DGM.
     *
     * @return the DGM specified by D4EArtifact's parameters.
     */
    public static DGM getDGM(D4EArtifact flys)
    throws IllegalArgumentException
    {
        try {
            Integer dgmId = flys.getDataAsInteger("dgm");
            if (dgmId == null) {
                throw new IllegalArgumentException(ERR_EMPTY);
            }

            log.debug("Found selected dgm: '" + dgmId + "'");

            return DGM.getDGM(dgmId);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(ERR_INVALID_DGM);
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
