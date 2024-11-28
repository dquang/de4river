/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.model.CrossSectionFacet;
import org.dive4elements.river.artifacts.model.FastCrossSectionLineFactory;

import org.dive4elements.river.model.FastCrossSectionLine;

import org.dive4elements.river.model.CrossSection;
import org.dive4elements.river.model.CrossSectionLine;
import org.dive4elements.river.artifacts.model.CrossSectionFactory;

import org.dive4elements.river.artifacts.states.StaticState;

import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.river.artifacts.services.CrossSectionKMService;


/**
 * Artifact describing a cross-section.
 */
public class CrossSectionArtifact extends StaticD4EArtifact {

    /** Name of Artifact. */
    public static final String CS_ARTIFACT_NAME = "cross_section";

    /** Name of state. */
    public static final String STATIC_STATE_NAME = "state.cross_section";

    /** Name of data item keeping the position. */
    public static final String DATA_KM = "cross_section.km";

    /** Name of data item keeping the 'parents' km. */
    public static final String PARENT_KM = "cross_section.parent.km";

    /** Name of data item keeping the database id of this c.s.. */
    public static final String DATA_DBID = "cross_section.dbid";

    /** Name of data item flagging whether we think that we are master. */
    public static final String DATA_IS_MASTER = "cross_section.master?";

    /** Name of data item flagging whether we are the newest. */
    public static final String DATA_IS_NEWEST = "cross_section.newest?";

    /** Name of data item storing the previous possible km. */
    public static final String DATA_PREV_KM = "cross_section.km.previous";

    /** Name of data item storing the next possible km. */
    public static final String DATA_NEXT_KM = "cross_section.km.next";

    /** Own log. */
    private static final Logger log =
        LogManager.getLogger(CrossSectionArtifact.class);

    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance().register(
            CS_ARTIFACT_NAME,
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   outputName
                ) {
                    if (artifact instanceof D4EArtifact) {
                        D4EArtifact flys = (D4EArtifact)artifact;
                        String data = flys.getDataAsString(DATA_IS_NEWEST);
                        return data != null && data.equals("1");
                    }
                    return null;
                }
            });
    }

    /** Return given name. */
    @Override
    public String getName() {
        return CS_ARTIFACT_NAME;
    }


    /** Store ids, create a CrossSectionFacet. */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.info("CrossSectionArtifact.setup");

        super.setup(identifier, factory, context, callMeta, data, loadFacets);

        String ids = getDatacageIDValue(data);

        if (ids != null && ids.length() > 0) {
            addStringData(DATA_DBID, ids);
            log.debug("CrossSectionArtifacts db-id: " + ids);
        }
        else {
            throw new IllegalArgumentException("No attribute 'ids' found!");
        }

        List<Facet> fs = new ArrayList<Facet>();
        CrossSection cs = CrossSectionFactory.getCrossSection(
            Integer.parseInt(ids));

        List<CrossSectionLine> csls = cs.getLines();
        if (!csls.isEmpty()) {
            CrossSectionLine csl = csls.get(0);
            // Find min-km of cross sections,
            // then set DATA_KM to min(DATA_KM, minCross).
            String dataKmValue = getDataAsString(DATA_KM);
            double dataKm = (dataKmValue != null)
                ? Double.valueOf(dataKmValue)
                : Double.MIN_VALUE;
            if (dataKm < csl.getKm().doubleValue()) {
                addStringData(DATA_KM, csl.getKm().toString());
            }
        }
        fs.add(new CrossSectionFacet(0, cs.getDescription()));

        // Find out if we are newest and become master if so.
        boolean isNewest = cs.shouldBeMaster(getParentKm());
        String newString = (isNewest) ? "1" : "0";
        addStringData(DATA_IS_NEWEST, newString);
        addStringData(DATA_IS_MASTER, newString);

        if (!fs.isEmpty()) {
            addFacets(getCurrentStateId(), fs);
        }
    }


    /** Copy km where master-artifact "starts". */
    @Override
    protected void initialize(
        Artifact master,
        Object   context,
        CallMeta callMeta)
    {
        D4EArtifact masterArtifact = (D4EArtifact) master;

        RangeAccess rangeAccess = new RangeAccess(masterArtifact);
        double[] range = rangeAccess.getKmRange();
        if (range != null && range.length > 0) {
            this.addStringData(DATA_KM, Double.toString(range[0]));
            this.addStringData(PARENT_KM, Double.toString(range[0]));
        }
    }


    /** Returns next possible km for a cross-section. */
    public Double getNextKm() {
        return getDataAsDouble(DATA_NEXT_KM);
    }


    /** Returns previous possible km for a cross-section. */
    public Double getPrevKm() {
        return getDataAsDouble(DATA_PREV_KM);
    }


    /**
     * Create and return a new StaticState with charting output.
     */
    @Override
    public State getCurrentState(Object cc) {
        final List<Facet> fs = getFacets(getCurrentStateId());

        StaticState state = new StaticState(STATIC_STATE_NAME) {
            @Override
            public Object staticCompute(List<Facet> facets) {
                if (facets != null) {
                    facets.addAll(fs);
                }
                return null;
            }
        };

        state.addDefaultChartOutput("cross_section", fs);

        return state;
    }


    /**
     * Get a list containing the one and only State.
     * @param  context ignored.
     * @return list with one and only state.
     */
    @Override
    protected List<State> getStates(Object context) {
        ArrayList<State> states = new ArrayList<State>();
        states.add(getCurrentState(context));

        return states;
    }

    // TODO all data access needs proper caching.

    /**
     * Get a DataItem casted to int (0 if fails).
     */
    public int getDataAsIntNull(String dataName) {
        String val = getDataAsString(dataName);
        try {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e) {
            log.warn("Could not get data " + dataName + " as int", e);
            return 0;
        }
    }


    /** Returns database-id of cross-section (from data). */
    protected int getDBID() {
        return getDataAsIntNull(DATA_DBID);
    }


    /**
     * Return position (km) from parent (initial km), 0 if not found.
     */
    private double getParentKm() {
        String val = getDataAsString(PARENT_KM);
        if (val == null) {
            log.warn("Empty data: " + PARENT_KM);
            return 0;
        }
        try {
            return Double.valueOf(val);
        }
        catch (NumberFormatException e) {
            log.warn("Could not get data " + PARENT_KM + " as double", e);
            return 0;
        }
    }

    /**
     * Return position (km) from data, 0 if not found.
     */
    protected double getKm() {
        String val = getDataAsString(DATA_KM);
        try {
            return Double.valueOf(val);
        }
        catch (NumberFormatException e) {
            log.warn("Could not get data " + DATA_KM + " as double", e);
            return 0;
        }
    }


    /** Returns true if artifact is set to be a "master" (other facets will
     * refer to this). */
    public boolean isMaster() {
        return !getDataAsString(DATA_IS_MASTER).equals("0");
    }


    /**
     * Get points of Profile of cross section at given kilometer.
     *
     * @return an array holding coordinates of points of profile (
     *         in the form {{x1, x2} {y1, y2}} ).
     */
    public double [][] getCrossSectionData() {
        log.info("getCrossSectionData() for cross_section.km "
            + getDataAsString(DATA_KM));
        FastCrossSectionLine line = searchCrossSectionLine();

        return line != null
               ? line.fetchCrossSectionProfile()
               : null;
    }


    /**
     * Get CrossSectionLine spatially closest to what is specified in the data
     * "cross_section.km", null if considered too far.
     *
     * It also adds DataItems to store the next and previous (numerically)
     * values at which cross-section data was recorded.
     *
     * @return CrossSectionLine closest to "cross_section.km", might be null
     *         if considered too far.
     */
    public FastCrossSectionLine searchCrossSectionLine() {
        double TOO_FAR = 1d;
        CrossSection crossSection = CrossSectionFactory
            .getCrossSection(getDBID());

        if (log.isDebugEnabled()) {
            log.debug("dbid " + getDBID() + " : " + crossSection);
        }

        NavigableMap<Double, Integer> kms = CrossSectionKMService
            .getKms(crossSection.getId());

        Double wishKM = getKm();

        Double floor = kms.floorKey(wishKM);
        Double ceil  = kms.ceilingKey(wishKM);

        Double nextKm;
        Double prevKm;

        double floorD = floor != null
            ? Math.abs(floor - wishKM)
            : Double.MAX_VALUE;

        double ceilD = ceil != null
            ? Math.abs(ceil - wishKM)
            : Double.MAX_VALUE;

        double km;
        if (floorD < ceilD) {
            km = floor;
        }
        else {
            km = ceil;
        }

        // If we are too far from the wished km, return null.
        if (Math.abs(km - wishKM) > TOO_FAR) {
            return null;
        }

        // Store next and previous km.
        nextKm = kms.higherKey(km);
        prevKm = kms.lowerKey(km);

        if (prevKm == null) {
            prevKm = -1d;
        }
        if (nextKm == null) {
            nextKm = -1d;
        }

        addStringData(DATA_PREV_KM, prevKm.toString());
        addStringData(DATA_NEXT_KM, nextKm.toString());

        return FastCrossSectionLineFactory
            .getCrossSectionLine(crossSection, km);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
