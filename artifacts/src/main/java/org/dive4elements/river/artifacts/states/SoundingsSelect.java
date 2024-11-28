/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.states;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.CallContext;
import org.dive4elements.artifacts.common.model.KVP;
import org.dive4elements.artifacts.common.utils.XMLUtils;
import org.dive4elements.artifacts.common.utils.XMLUtils.ElementCreator;
import org.dive4elements.river.model.BedHeight;
import org.dive4elements.river.model.River;
import org.dive4elements.river.artifacts.D4EArtifact;
import org.dive4elements.river.artifacts.resources.Resources;
import org.dive4elements.river.utils.Formatter;
import org.dive4elements.river.utils.RiverUtils;
import org.w3c.dom.Element;


public class SoundingsSelect extends DefaultState {

    public static final String SOUNDINGS = "soundings";

    public static final String PREFIX_SINGLE = "single-";

    /** Private log. */
    private static final Logger log = LogManager.getLogger(SoundingsSelect.class);


    @Override
    public String getUIProvider() {
        return "parameter-matrix";
    }

    @Override
    protected void appendItems(
        Artifact       artifact,
        ElementCreator creator,
        String         name,
        CallContext    context,
        Element        select
    ) {
        try {
            creator.addAttr(select, "type", "multiattribute", true);

            appendMeta(creator, select);

            getOptions(artifact, name, context, creator, select);
        }
        catch (IllegalArgumentException iae) {
            log.warn("Illegal argument", iae);
        }
    }

    private static Element order(
        ElementCreator creator,
        String name,
        String order
    ) {
        Element element = creator.create(name);
        creator.addAttr(element, "order", order, false);
        return element;
    }

    private void appendMeta(ElementCreator creator, Element select) {

        Element meta = creator.create("meta");

        meta.appendChild(order(creator, "year",           "0"));
        meta.appendChild(order(creator, "value",          "1"));
        meta.appendChild(order(creator, "analyzed_range", "2"));
        meta.appendChild(order(creator, "label",          "3"));
        meta.appendChild(order(creator, "minfo_type",     "4"));

        select.appendChild(meta);

    }

    protected KVP<String, String>[] getOptions(
        Artifact artifact,
        String   parameterName,
        CallContext context,
        ElementCreator creator,
        Element select
    )
    throws IllegalArgumentException
    {
        log.debug("Get options for parameter: '" + parameterName + "'");

        if (!testParameterName(parameterName)) {
            throw new IllegalArgumentException(
                "Invalid parameter for state: '" + parameterName + "'");
        }

        River river = RiverUtils.getRiver((D4EArtifact) artifact);
        double lo = ((D4EArtifact) artifact).getDataAsDouble("ld_from");
        double hi = ((D4EArtifact) artifact).getDataAsDouble("ld_to");

        double kmLo = Math.min(lo, hi);
        double kmHi = Math.max(lo, hi);

        appendSingles(river, kmLo, kmHi, creator, select, context);

        List<KVP<String, String>> kvp =
            Collections.<KVP<String, String>>emptyList();
        return kvp.toArray(new KVP[kvp.size()]);
    }


    protected void appendSingles(
        River river,
        double kmLo,
        double kmHi,
        ElementCreator creator,
        Element select,
        CallContext context
    ) {
        List<BedHeight> singles =
            BedHeight.getBedHeights(river, kmLo, kmHi);

        if (singles != null) {
            int size = singles.size();

            log.debug("Found " + size + " singles.");

            NumberFormat nf = Formatter.getCalculationKm(context.getMeta());
            for (int i = 0; i < size; i++) {
                BedHeight s = singles.get(i);

                String id    = PREFIX_SINGLE + s.getId();
                String value = s.getDescription();

                Integer year = s.getYear();
                Element item = creator.create("item");
                creator.addAttr(item, "label", value, true);
                creator.addAttr(item, "value", id, true);
                creator.addAttr(item, "analyzed_range",
                    nf.format(s.getRange().getA()) +
                    " - " +
                    nf.format(s.getRange().getB()));
                creator.addAttr(item, "year",
                    year != null ? s.getYear().toString() : "");
                creator.addAttr(item, "minfo_type", s.getType().getName());
                select.appendChild(item);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("appended singles");
            log.debug(XMLUtils.toString(select));
        }
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
        Element data = creator.create("data");
        creator.addAttr(data, "name",  name, true);
        creator.addAttr(data, "type",  type, true);
        creator.addAttr(data, "label",
            Resources.getMsg(cc.getMeta(), name, name), true);

        String[] values = value.split(";");

        for (String val: values) {
            Element item = creator.create("item");
            creator.addAttr(item, "value", val, true);
            creator.addAttr(item, "label", getLabelFor(cc, name, val), true);

            data.appendChild(item);
        }

        return data;
    }

    protected String getLabelFor(
        CallContext cc,
        String      parameterName,
        String      value
    ) throws IllegalArgumentException
    {
        if (!testParameterName(parameterName)) {
            throw new IllegalArgumentException(
                "Invalid parameter for state: '" + parameterName + "'");
        }

        if (value.indexOf(PREFIX_SINGLE) >= 0) {
            return getLabelForSingle(cc, value);
        }
        return value;
    }


    protected String getLabelForSingle(CallContext cc, String value) {
        String id = value.replace(PREFIX_SINGLE, "");
        try {
            BedHeight s = BedHeight.getBedHeightById(
                Integer.parseInt(id));

            if (s != null) {
                return s.getDescription();
            }
            else {
                return "no value for '" + id + "'";
            }
        }
        catch (NumberFormatException nfe) {
            log.warn("Could not parse id from string '" + id + "'", nfe);
        }

        return "n.A.";
    }


    /**
     * This method might be used to test, if a parameter name
     * is handled by this state.
     *
     * @param parameterName The name of a parameter.
     *
     * @return true, if parameterName is one of <i>MAIN_CHANNEL</i> or
     * <i>TOTAL_CHANNEL</i>. Otherwise false.
     */
    protected boolean testParameterName(String parameterName) {
        if (parameterName == null || parameterName.length() == 0) {
            return false;
        }
        else if (parameterName.equals(SOUNDINGS)) {
            return true;
        }
        else {
            return false;
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
