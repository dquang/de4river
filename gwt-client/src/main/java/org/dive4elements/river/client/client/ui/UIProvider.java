/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import java.io.Serializable;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;

import org.dive4elements.river.client.client.FLYS;

/**
 * This interface describes a method that creates a Canvas element displaying
 * DataItems for a current state of the artifact.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public interface UIProvider extends Serializable {

    /**
     * This method creates a Canvas element showing the DataItems in
     * <i>data</i>.
     *
     * @param data The DataList object.
     *
     * @return the Canvas showing the Data.
     */
    public Canvas create(DataList data);


    /**
     * This method creates a Canvas element showing the old Data objects in the
     * DataList <i>data</i>.
     *
     * @param dataList The DataList which elements should be displayed.
     *
     * @return a Canvas displaying the Data.
     */
    public Canvas createOld(DataList dataList);


    public Canvas createHelpLink(DataList dataList, Data data, FLYS instance);


    /**
     * This method injects a container that is used to position helper widgets.
     *
     * @param container A container that is used to position helper widgets.
     */
    public void setContainer(VLayout container);


    /**
     * Sets an artifact that contains the status data information for a project.
     *
     * @param artifact The artifact containing status information.
     */
    public void setArtifact(Artifact artifact);


    /**
     * Sets the parent Collection of the Artifact.
     */
    public void setCollection(Collection collection);

    public void setParameterList(ParameterList list);
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
