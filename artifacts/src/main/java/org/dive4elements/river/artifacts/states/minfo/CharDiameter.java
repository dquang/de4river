/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states.minfo;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.CallMeta;
import org.dive4elements.artifacts.common.model.KVP;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.artifacts.states.MultiStringArrayState;

public class CharDiameter extends MultiStringArrayState {

    private static final Logger log = LogManager.getLogger(CharDiameter.class);

    public static final String UI_PROVIDER = "parameter-matrix";

    private static final String CHAR_DIAMETER_MIN  = "calc.bed.dmin";
    private static final String CHAR_DIAMETER_MAX  = "calc.bed.dmax";
    private static final String CHAR_DIAMETER_90  = "calc.bed.d90";
    private static final String CHAR_DIAMETER_84  = "calc.bed.d84";
    private static final String CHAR_DIAMETER_80  = "calc.bed.d80";
    private static final String CHAR_DIAMETER_75  = "calc.bed.d75";
    private static final String CHAR_DIAMETER_70  = "calc.bed.d70";
    private static final String CHAR_DIAMETER_60  = "calc.bed.d60";
    private static final String CHAR_DIAMETER_50  = "calc.bed.d50";
    private static final String CHAR_DIAMETER_40  = "calc.bed.d40";
    private static final String CHAR_DIAMETER_30  = "calc.bed.d30";
    private static final String CHAR_DIAMETER_25  = "calc.bed.d25";
    private static final String CHAR_DIAMETER_20  = "calc.bed.d20";
    private static final String CHAR_DIAMETER_16  = "calc.bed.d16";
    private static final String CHAR_DIAMETER_10  = "calc.bed.d10";
    private static final String CHAR_DIAMETER_DM  = "calc.bed.dm";

    public static final String[] CHAR_DIAMETER = {
        CHAR_DIAMETER_DM,
        CHAR_DIAMETER_10,
        CHAR_DIAMETER_16,
        CHAR_DIAMETER_20,
        CHAR_DIAMETER_25,
        CHAR_DIAMETER_30,
        CHAR_DIAMETER_40,
        CHAR_DIAMETER_50,
        CHAR_DIAMETER_60,
        CHAR_DIAMETER_70,
        CHAR_DIAMETER_75,
        CHAR_DIAMETER_80,
        CHAR_DIAMETER_84,
        CHAR_DIAMETER_90,
        CHAR_DIAMETER_MAX,
        CHAR_DIAMETER_MIN
    };

    @Override
    public String getUIProvider() {
        return UI_PROVIDER;
    }

    @Override
    protected KVP<String, String>[] getOptions(
        Artifact artifact,
        String parameterName,
        CallContext context
    )
    throws IllegalArgumentException
    {
        CallMeta meta   = context.getMeta();

        List<KVP<String, String>> rows = new ArrayList<KVP<String, String>>();
        String key = parameterName;
        for (int i = 0; i < CHAR_DIAMETER.length; ++i) {
            String calc = CHAR_DIAMETER[i];
            rows.add(new KVP (calc,
                              Resources.getMsg(meta, calc, calc)));
        }

        return rows.toArray(new KVP[rows.size()]);
    }

    @Override
    protected String getLabelFor(CallContext cc, String parameterName,
            String value) throws IllegalArgumentException {

        return Resources.getMsg(cc.getMeta(), value, value);
    }

}
