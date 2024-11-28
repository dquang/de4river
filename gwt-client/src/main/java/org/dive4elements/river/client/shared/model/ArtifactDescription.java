/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


/**
 * The artifact description describes a state of an artifact. There are
 * operations defined that return former inserted data, possible input values
 * and output targets that are available in the current state of the artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface ArtifactDescription extends Serializable {

    /**
     * Returns the data that have been inserted in former states of the
     * artifact.
     *
     * @return the old data of former states.
     */
    public DataList[] getOldData();


    /**
     * Returns the data with all its options that might be inserted in the
     * current state of the artifact.
     *
     * @return the current data.
     */
    public DataList getCurrentData();


    /**
     * Returns the current state as string.
     *
     * @return the current state.
     */
    public String getCurrentState();


    /**
     * Returns the reachable states as string.
     *
     * @return the reachable states.
     */
    public String[] getReachableStates();


    /**
     * Returns the name of the selected river.
     *
     * @return the selected river.
     */
    public String getRiver();


    /**
     * Returns the selected min and max kilomter if existing otherwise null.
     *
     * @return an array of [min-km, max-km] if existing otherwise null.
     */
    public double[] getKMRange();


    /**
     * Returns the selected reference gauge (which needs to be a data named
     * 'reference_gauge'.
     *
     * @return the selected reference gauge (which needs to be a data named
     * 'reference_gauge'.
     */
    public String getReferenceGauge();


    /**
     * Returns the string value of a data object with name <i>dataName</i>.
     *
     * @return the string value of a data object with name <i>dataName</i>.
     */
    public String getDataValueAsString(String dataName);


    /**
     * Returns the available output modes.
     *
     * @return the available output modes.
     */
    public OutputMode[] getOutputModes();


    /**
     * Returns the recommended artifacts suggested by the server.
     *
     * @return the recommended artifacts.
     */
    public Recommendation[] getRecommendations();
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
