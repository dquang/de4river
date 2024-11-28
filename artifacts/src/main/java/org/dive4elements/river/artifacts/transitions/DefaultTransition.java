/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.transitions;

import org.w3c.dom.Node;

import org.dive4elements.artifacts.Artifact;

import org.dive4elements.artifactdatabase.state.State;
import org.dive4elements.artifactdatabase.transition.Transition;


/**
 * The default implementation of a <code>Transition</code>.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultTransition implements Transition {

    /** The ID of the current state */
    protected String from;

    /** The ID of the target state */
    protected String to;


    /**
     * The default constructor.
     */
    public DefaultTransition() {
    }


    /**
     * The default constructor.
     *
     * @param from The current state.
     * @param to The target state.
     */
    public DefaultTransition(String from, String to) {
        this.from = from;
        this.to   = to;
    }


    public void init(Node config) {
        // nothing to do in the default transition
    }


    /**
     * Returns the current state ID.
     *
     * @return the current state ID.
     */
    public String getFrom() {
        return from;
    }


    /**
     * Returns the target state ID.
     *
     * @return the target state ID.
     */
    public String getTo() {
        return to;
    }


    /**
     * Set the current state ID.
     *
     * @param from current state ID.
     */
    public void setFrom(String from) {
        this.from = from;
    }


    /**
     * Set the target state ID.
     *
     * @param to the target state ID.
     */
    public void setTo(String to) {
        this.to = to;
    }


    /**
     * Determines if its valid to step from state <i>a</i> of an artifact
     * <i>artifact</i> to state <i>b</i>. This method always returns true - no
     * validation takes place.
     *
     * @param artifact The owner artifact of state a and b.
     * @param a The current state.
     * @param b The target state.
     *
     * @return true, if it is valid to step from a to b, otherwise false.
     */
    public boolean isValid(Artifact artifact, State a, State b) {
        return true;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
