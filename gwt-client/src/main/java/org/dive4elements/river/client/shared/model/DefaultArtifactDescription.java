/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.List;

import org.dive4elements.river.client.shared.DoubleUtils;


/**
 * The default implementation of an {@link ArtifactDescription}. This class just
 * implements constructors to create new instances and the necessary methods of
 * the interface.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultArtifactDescription implements ArtifactDescription {

    /** Data that have been inserted in former states.*/
    protected DataList[] oldData;

    /** The Data that is allowed to be inserted in the current state.*/
    protected DataList currentData;

    /** The current state name.*/
    protected String   currentState;

    /** The names of reachable states.*/
    protected String[] reachableStates;

    /** The output modes of this state.*/
    protected OutputMode[] outputModes;

    /** A list of recommendations suggested by the server.*/
    protected Recommendation[] recommendations;


    public DefaultArtifactDescription() {
    }


    /**
     * The default constructor.
     *
     * @param old The data that have been inserted in former states.
     * @param current The data that might be inserted in the current state.
     * @param state The name of the current state.
     * @param reachableStates The names of the reachable states.
     */
    public DefaultArtifactDescription(
        DataList[]   old,
        DataList     current,
        String       state,
        String[]     reachableStates)
    {
        this.oldData         = old;
        this.currentData     = current;
        this.currentState    = state;
        this.reachableStates = reachableStates;
    }


    /**
     * The default constructor.
     *
     * @param old The data that have been inserted in former states.
     * @param current The data that might be inserted in the current state.
     * @param state The name of the current state.
     * @param reachableStates The names of the reachable states.
     * @param outputModes The available output modes of this artifact.
     */
    public DefaultArtifactDescription(
        DataList[]       old,
        DataList         current,
        String           state,
        String[]         reachableStates,
        OutputMode[]     outputModes,
        Recommendation[] recommendations)
    {
        this(old, current, state, reachableStates);
        this.outputModes     = outputModes;
        this.recommendations = recommendations;
    }


    public DataList[] getOldData() {
        return oldData;
    }


    public DataList getCurrentData() {
        return currentData;
    }


    public String getCurrentState() {
        return currentState;
    }


    public String[] getReachableStates() {
        return reachableStates;
    }


    public OutputMode[] getOutputModes() {
        return outputModes;
    }


    public Recommendation[] getRecommendations() {
        return recommendations;
    }


    public String getRiver() {
        return getDataValueAsString("river");
    }


    /** Get [min,max] of data items. */
    public double[] getKMRange() {
        Double[] mm = new Double[2];

        for (DataList list: oldData) {
            List<Data> dataList = list.getAll();

            for (Data data: dataList) {
                String dataName = data.getLabel();
                DataItem item   = data.getItems()[0];

                if (dataName.equals("ld_from") || dataName.equals("from")) {
                    Double d = DoubleUtils.getDouble(item.getStringValue());

                    if (d != null) {
                        mm[0] = d;
                    }
                }
                else if (dataName.equals("ld_to") || dataName.equals("to")) {
                    Double d = DoubleUtils.getDouble(item.getStringValue());

                    if (d != null) {
                        mm[1] = d;
                    }
                }
                else if (dataName.equals("ld_locations")) {
                    return DoubleUtils.getMinMax(item.getStringValue());
                }
            }

            if (mm[0] != null && mm[1] != null) {
                return new double[] { mm[0], mm[1] };
            }
        }

        return null;
    }


    public String getReferenceGauge() {
        return getDataValueAsString("reference_gauge");
    }


    public String getDataValueAsString(String name) {
        if (oldData == null) {
            return null;
        }
        for (DataList list: oldData) {
            List<Data> dataList = list.getAll();

            for (Data d: dataList) {
                String dataName = d.getLabel();
                DataItem item   = d.getItems()[0];

                if (dataName.equals(name)) {
                    return item.getStringValue();
                }
            }
        }

        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
