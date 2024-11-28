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

import org.dive4elements.artifactdatabase.data.DefaultStateData;
import org.dive4elements.artifactdatabase.data.StateData;

import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;

import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.StaticWKmsArtifact;
import org.dive4elements.river.artifacts.model.CalculationResult;
import org.dive4elements.river.artifacts.model.WKms;
import org.dive4elements.river.artifacts.model.extreme.ExtremeResult;
import org.dive4elements.river.artifacts.model.fixings.FixRealizingResult;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.utils.RiverUtils;
import org.dive4elements.river.backend.utils.StringUtil;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class WaterlevelSelectState extends DefaultState {

    private static final Logger log =
        LogManager.getLogger(WaterlevelSelectState.class);

    public static final String SPLIT_CHAR = ";";

    public static final String WINFO_WSP_STATE_ID = "state.winfo.waterlevel";

    public static final String I18N_STATIC_KEY = "wsp.selected.string";


    @Override
    protected String getUIProvider() {
        return "wsp_datacage_panel";
    }


    /**
     * @param flys ignored
     * @param cc   ignrored
     * @param stateData ignored
     */
    @Override
    public StateData transform(
        D4EArtifact flys,
        CallContext  cc,
        StateData    stateData,
        String       name,
        String       val
    ) {
        if (!isValueValid(val)) {
            log.error("The given input string is not valid: '" + val + "'");
            return null;
        }

        return new DefaultStateData(
            name, null, null, StringUtil.unbracket(val));
    }


    @Override
    public boolean validate(Artifact artifact)
    throws IllegalArgumentException
    {
        D4EArtifact flys = (D4EArtifact) artifact;

        StateData data = flys.getData("wsp");

        if (data == null) {
            throw new IllegalArgumentException("WSP is empty");
        }

        return true;
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

        String[] labels = getLabels(cc, value);
        Object[] obj    = new Object[] { labels[0] };

        String attrValue = Resources.getMsg(
            cc.getMeta(), I18N_STATIC_KEY, I18N_STATIC_KEY, obj);

        creator.addAttr(itemElement, "label", attrValue, true);
        dataElement.appendChild(itemElement);

        return dataElement;
    }


    /**
     * Get name to display for selected watelerlevel (for example "Q=123")
     * from the CalculationResult.
     */
    public static String[] getLabels(CallContext cc, String value) {
        String[] parts = value.split(SPLIT_CHAR);

        D4EArtifact artifact = RiverUtils.getArtifact(parts[0], cc);

        Object rawData = artifact.compute(
            cc,
            null,
            //WINFO_WSP_STATE_ID,
            artifact.getCurrentStateId(),
            ComputeType.ADVANCE,
            false);

        WKms[] wkms = null;

        // TODO issue1020: Fetch cases in which only WKms or
        // other weird stuff arrives.
        if (rawData instanceof CalculationResult) {
            CalculationResult calcResult = (CalculationResult) rawData;
            if (calcResult.getData() instanceof ExtremeResult) {
                wkms = ((ExtremeResult) calcResult.getData()).getWQKms();
            }
            else if (calcResult.getData() instanceof FixRealizingResult) {
                wkms = ((FixRealizingResult) calcResult.getData()).getWQKms();
            }
            else {
                wkms = (WKms[]) calcResult.getData();
            }
        }
        else if (rawData instanceof WKms) {
            wkms = new WKms[] {(WKms) rawData};
        }
        else if (rawData instanceof WKms[]) {
            wkms = (WKms[]) rawData;
        }
        else if (artifact instanceof StaticWKmsArtifact) {
            wkms = new WKms[] {((StaticWKmsArtifact) artifact).getWKms(0)};
        }
        else {
            if (rawData == null) {
                log.error("Do not know how to handle null data " +
                    "from artifact class " + artifact.getClass());
            }
            else {
                log.error("Do not know how to handle " + rawData.getClass());
            }

            wkms = null;
        }
        if (wkms == null || wkms.length == 0) {
            log.error("No data for label generation.");
            // This is critical, will fail without much grace later further down
            // the road.
            return new String[] {""};
        }

        int idx = -1;
        try {
            idx = Integer.parseInt(parts[2]);
            if (wkms[idx] == null) {
                log.error("null label for " + value + " (" + parts[2] + ")");
                return new String[] {""};
            }
            String name = wkms[idx].getName();

            return new String[] { StringUtil.wWrap(name) };
        }
        catch (NumberFormatException nfe) { /* do nothing */
            log.error("Cannot get label for " + value + " (" + parts[2] + ")");
            return new String[] {""};
        }
    }


    /**
     * Validates the given String. A valid string for this state requires the
     * format: "UUID;FACETNAME;FACETINDEX".
     *
     * @param value The string value requires validation.
     *
     * @return true, if the string applies the specified format, otherwise
     * false.
     */
    public static boolean isValueValid(String value) {
        log.debug("Validate string: '" + value + "'");

        value = StringUtil.unbracket(value);

        log.debug("Validate substring: '" + value + "'");

        if (value == null || value.length() == 0) {
            return false;
        }

        String[] parts = value.split(SPLIT_CHAR);

        if (parts == null || parts.length < 3) {
            return false;
        }

        if (parts[0] == null || parts[0].length() == 0) {
            return false;
        }

        if (parts[1] == null || parts[1].length() == 0) {
            return false;
        }

        try {
            Integer.parseInt(parts[2]);
        }
        catch (NumberFormatException nfe) {
            log.error("Index is not a valid integer!", nfe);
        }

        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf-8 :
