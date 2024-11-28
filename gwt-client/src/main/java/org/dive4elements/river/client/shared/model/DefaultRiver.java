/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;


/**
 * The simpliest default implementation of a River that just stores a name.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class DefaultRiver implements River {

    /** The name of the river.*/
    protected String name;

    /** The model uuid of the river */
    protected String modelUuid;

    /**
     * The default constructor that creates empty river objects.
     */
    public DefaultRiver() {
    }


    /**
     * This constructor should be used to create new rivers.
     *
     * @param name The name of the river.
     */
    public DefaultRiver(String name, String modelUuid) {
        this.name = name;
        this.modelUuid = modelUuid;
    }


    /**
     * Sets the name of the river.
     *
     * @param name The name of the river.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Returns the name of the river.
     *
     * @return the name of the river.
     */
    public String getName() {
        return name;
    }



    /**
     * @return the modelUuid
     */
    public String getModelUuid() {
        return modelUuid;
    }



    /**
     * @param modelUuid the modelUuid to set
     */
    public void setModelUuid(String modelUuid) {
        this.modelUuid = modelUuid;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
