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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;

import org.dive4elements.artifactdatabase.data.DefaultStateData;
import org.dive4elements.artifactdatabase.state.Facet;
import org.dive4elements.artifactdatabase.state.FacetActivity;
import org.dive4elements.artifactdatabase.state.DefaultOutput;
import org.dive4elements.artifactdatabase.state.State;

import org.dive4elements.artifacts.Artifact;
import org.dive4elements.artifacts.ArtifactFactory;
import org.dive4elements.artifacts.CallMeta;

import org.dive4elements.artifacts.common.ArtifactNamespaceContext;
import org.dive4elements.artifacts.common.utils.XMLUtils;

import org.dive4elements.river.model.Gauge;
import org.dive4elements.river.model.MainValue;
import org.dive4elements.river.model.River;

import org.dive4elements.river.artifacts.access.RiverAccess;
import org.dive4elements.river.artifacts.access.RangeAccess;
import org.dive4elements.river.artifacts.model.Calculation;
import org.dive4elements.river.artifacts.model.FacetTypes;
import org.dive4elements.river.artifacts.model.MainValuesQFacet;
import org.dive4elements.river.artifacts.model.MainValuesWFacet;
import org.dive4elements.river.artifacts.model.NamedDouble;
import org.dive4elements.river.artifacts.model.WstValueTable;
import org.dive4elements.river.artifacts.model.WstValueTableFactory;

import org.dive4elements.river.artifacts.states.StaticState;
import org.dive4elements.river.artifacts.resources.Resources;


/**
 * Artifact to access main and extreme values of a river.
 * This artifact neglects (Static)D4EArtifacts capabilities of interaction
 * with the StateEngine by overriding the getState*-methods.
 */
public class MainValuesArtifact
extends      StaticD4EArtifact
implements   FacetTypes
{
    /** The log for this class. */
    private static Logger log = LogManager.getLogger(MainValuesArtifact.class);

    /** The name of the artifact. */
    public static final String ARTIFACT_NAME = "mainvalue";

    /** The name of the static state for this artifact. */
    public static final String STATIC_STATE_NAME = "state.mainvalue.static";

    /** One and only state to be in. */
    protected transient State state = null;


    static {
        // TODO: Move to configuration.
        FacetActivity.Registry.getInstance().register(
            ARTIFACT_NAME,
            new FacetActivity() {
                @Override
                public Boolean isInitialActive(
                    Artifact artifact,
                    Facet    facet,
                    String   outputName
                ) {
                    if (facet.getName().equals(MAINVALUES_Q) ||
                        facet.getName().equals(MAINVALUES_W)) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
            });
    }


    /**
     * Trivial Constructor.
     */
    public MainValuesArtifact() {
        log.debug("MainValuesArtifact.MainValuesartifact()");
    }


    /**
     * Gets called from factory, to set things up.
     */
    @Override
    public void setup(
        String          identifier,
        ArtifactFactory factory,
        Object          context,
        CallMeta        callMeta,
        Document        data,
        List<Class>     loadFacets)
    {
        log.debug("MainValuesArtifact.setup");
        state = new StaticState(STATIC_STATE_NAME);

        initFromGaugeDoc(data, callMeta);

        List<Facet> fs = new ArrayList<Facet>();
        addFacets(state.getID(), fs);
        spawnState();
        String restriction = getDatacageIDValue(data);
        log.debug("mainvalue restriction " + restriction);
        boolean restricted = restriction.endsWith("q")
            || restriction.endsWith("w");
        if (!restricted || restriction.endsWith("q")) {
            fs.add(new MainValuesQFacet(
                    MAINVALUES_Q,
                    Resources.getMsg(
                        callMeta,
                        "facet.discharge_curves.mainvalues.q")));
        }
        if (!restricted || restriction.endsWith("w")) {
            fs.add(new MainValuesWFacet(
                    MAINVALUES_W,
                    Resources.getMsg(
                        callMeta,
                        "facet.discharge_curves.mainvalues.w")));
        }
        super.setup(identifier, factory, context, callMeta, data, loadFacets);
    }


    /**
     * The MainValueArtifact can be set up with a document giving the
     * river and gauge.  This happens in context of GaugeDischargeArtifact.
     * In that case, initalize() is not called.
     */
    private void initFromGaugeDoc(Document data, CallMeta callMeta) {
        String gaugeref = XMLUtils.xpathString(
                data, GaugeDischargeCurveArtifact.XPATH_GAUGE,
                ArtifactNamespaceContext.INSTANCE);
        String rivername = XMLUtils.xpathString(
                data, GaugeDischargeCurveArtifact.XPATH_RIVER,
                ArtifactNamespaceContext.INSTANCE);

        if (rivername == null || gaugeref == null || rivername.equals("")
            || gaugeref.equals("")) {
            log.debug("Not setting MainValuesArtifact up from gauge doc.");
            return;
        }

        addData("river", new DefaultStateData("river",
                    Resources.getMsg(callMeta,
                        "facet.gauge_discharge_curve.river",
                        "Name of the river"),
                    "String", rivername));

        try {
            Long officialNumber = Long.valueOf(gaugeref);
            Gauge gauge = Gauge.getGaugeByOfficialNumber(officialNumber);
            addData(
                "ld_locations",
                new DefaultStateData("ld_locations", null, null,
                    String.valueOf(gauge.getStation()))
            );
        } catch (NumberFormatException nfe) {
            log.debug("MainValuesArtifact could not parse gaugeref from doc.");
        }
    }


    /**
     * Create "the" (one possible) state.
     */
    protected State spawnState() {
        state = new StaticState(STATIC_STATE_NAME);
        List<Facet> fs = (List<Facet>) getFacets(STATIC_STATE_NAME);

        DefaultOutput mainValuesOutput = new DefaultOutput(
            "computed_discharge_curve",
            "output.computed_discharge_curve", "image/png",
            fs,
            "chart");

        state.getOutputs().add(mainValuesOutput);
        return state;
    }


    /** Get important data from the 'calling' artifact. */
    @Override
    protected void initialize(
        Artifact artifact,
        Object context,
        CallMeta meta
    ) {
        log.debug("MainValuesArtifact.initialize");
        D4EArtifact winfo = (D4EArtifact) artifact;
        River river = new RiverAccess(winfo).getRiver();
        double [] locations = new RangeAccess(winfo).getKmRange();

        if (locations != null) {
            double location = locations[0];
            addData(
                "ld_locations",
                new DefaultStateData("ld_locations", null, null,
                    String.valueOf(location))
            );
        }
        else {
            log.error("No location for mainvalues given.");
        }
        importData(winfo, "river");
        // In the case of DischargeWQCurves, there are no locations,
        // but a gauge.
        if (getDataAsString("ld_locations") == null) {
            // TODO its a tad difficult to remodel Range/Gauge-Access to
            // do this.
            String refGaugeID = winfo.getDataAsString("reference_gauge");
            if (refGaugeID != null) {
                Gauge g = Gauge.getGaugeByOfficialNumber(
                    Integer.parseInt(refGaugeID));
                addData(
                    "ld_locations",
                    new DefaultStateData("ld_locations", null, null,
                        String.valueOf(g.getStation()))
                );
            }
            else {
                log.error("MainValuesArtifact: No location/gauge.");
            }
        }
    }


    /**
     * Get a list containing the one and only State.
     * @param  context ignored.
     * @return list with one and only state.
     */
    @Override
    protected List<State> getStates(Object context) {
        ArrayList<State> states = new ArrayList<State>();
        states.add(getState());
        return states;
    }


    /**
     * Get the "current" state.
     * @param cc ignored.
     * @return the "current" state.
     */
    @Override
    public State getCurrentState(Object cc) {
        return getState();
    }


    /**
     * Get the only possible state.
     * @return the state.
     */
    protected State getState() {
        return getState(null, null);
    }


    /**
     * Get the state.
     * @param context ignored.
     * @param stateID ignored.
     * @return the state.
     */
    @Override
    protected State getState(Object context, String stateID) {
        if (state != null)
            return state;
        else
            return spawnState();
    }

    /**
     * Access the Gauge that the mainvalues are taken from.
     * @return Gauge that main values are taken from or null in case of
     *         invalid parameterization.
     */
    protected Gauge getGauge(double km) {
        River river = new RiverAccess((D4EArtifact)this).getRiver();

        if (river == null) {
            log.error("River is null");
            return null;
        }

        return river.determineGaugeByPosition(km);
    }

    /**
     * Get current location.
     * @return the location.
     */
    public double getLocation() {
        double location = Double.parseDouble(getDataAsString("ld_locations"));
        return location;
    }


    /**
     * Get a list of "Q" main values.
     * @param Array of length 1 (isn't it lovely?) giving the station for
     *        which the main values should be returned
     * @return list of Q main values.
     */
    public List<NamedDouble> getMainValuesQ(double[] kms, Object pnpObject) {
        if (kms.length > 1) {
            log.error("How did you dare to give an array of lenght >1! " +
                "DAS GEHT GARNICHT!!!! (we'll just take the first value)");
        }
        List<NamedDouble> filteredList = new ArrayList<NamedDouble>();
        Gauge gauge = getGauge(kms[0]);
        River river = new RiverAccess((D4EArtifact)this).getRiver();
        WstValueTable interpolator = WstValueTableFactory.getTable(river);
        Calculation c = new Calculation();
        double w_out[] = {0.0f};
        double q_out[] = {0.0f};
        double pnp     = Double.NaN;

        if (gauge != null) {
            double gaugeStation = gauge.getStation().doubleValue();
            List<MainValue> orig = gauge.getMainValues();
            if (pnpObject instanceof Number) {
                pnp = Double.valueOf(pnpObject.toString());
            }

            for (MainValue mv : orig) {
                if (mv.getMainValue().getType().getName().equals("Q")) {
                    if (pnpObject instanceof Number) {
                        q_out[0] = mv.getValue().doubleValue();
                    }
                    else {
                        interpolator.interpolate(mv.getValue().doubleValue(),
                            gaugeStation, kms, w_out, q_out, c);
                    }
                    filteredList.add(new NamedDouble(
                                mv.getMainValue().getName(),
                                q_out[0]
                                ));
                }
            }
        }
        return filteredList;
    }

    /**
     * Get a list of "Q" main values.
     * @return list of Q main values.
     */
    public List<NamedDouble> getMainValuesQ(Object pnpObject) {
        double kms[] = {getLocation()};
        return getMainValuesQ(kms, pnpObject);
    }


    public List<NamedDouble> getMainValuesW(double[] kms, Object pnpObject) {
        List<NamedDouble> filteredList = new ArrayList<NamedDouble>();
        Gauge gauge = getGauge(kms[0]);
        River river = new RiverAccess((D4EArtifact)this).getRiver();
        WstValueTable interpolator = WstValueTableFactory.getTable(river);
        Calculation c = new Calculation();
        double w_out[] = {0.0f};
        double q_out[] = {0.0f};
        double pnp     = Double.NaN;

        if (gauge != null) {
            double gaugeStation = gauge.getStation().doubleValue();
            List<MainValue> orig = gauge.getMainValues();
            if (pnpObject instanceof Number) {
                pnp = Double.valueOf(pnpObject.toString());
            }

            for (MainValue mv : orig) {
                Gauge g = river.determineGaugeAtStation(kms[0]);
                if (pnpObject instanceof Number) {
                    if (mv.getMainValue().getType().getName().equals("W")) {
                        filteredList.add(new NamedDouble(
                                mv.getMainValue().getName(),
                                mv.getValue().doubleValue()/100 + pnp
                            ));
                    }
                }
                else if (!(pnpObject instanceof Number) &&
                    g != null &&
                    !"duration_curve".equals(getBoundToOut())
                ) {
                    if (mv.getMainValue().getType().getName().equals("W")) {
                        filteredList.add(new NamedDouble(
                                mv.getMainValue().getName(),
                                mv.getValue().doubleValue()
                            ));
                    }
                }
                else {
                    // We cannot interpolate the W values, so derive them
                    // from given Q values.
                    if (mv.getMainValue().getType().getName().equals("Q")) {
                        interpolator.interpolate(mv.getValue().doubleValue(),
                                gaugeStation, kms, w_out, q_out, c);

                        filteredList.add(new NamedDouble(
                                    "W(" + mv.getMainValue().getName() +")",
                                    w_out[0]
                                    ));
                    }
                }
            }
        }
        return filteredList;
    }


    /**
     * Get a list of "W" main values.
     * @return list of W main values.
     */
    public List<NamedDouble> getMainValuesW(Object pnpObject) {
        return getMainValuesW(new double[] {getLocation()}, pnpObject);
    }


    /**
     * Returns the name of this artifact ('mainvalue').
     *
     * @return 'mainvalue'
     */
    public String getName() {
        return ARTIFACT_NAME;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
