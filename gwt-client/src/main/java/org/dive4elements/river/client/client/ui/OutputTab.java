/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.tab.Tab;

import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.OutputMode;


public class OutputTab extends Tab {

    /** The OutputMode that should be displayed in this tab.*/
    protected OutputMode mode;

    /** The Collection that should be displayed in this tab.*/
    protected Collection collection;

    /** The CollectionView containing this tab. */
    protected CollectionView collectionView;


    /**
     * The default constructor that creates a new Tab for displaying a specific
     * OutputMode of a Collection.
     *
     * @param title The title of the tab.
     * @param collection The collection that need to be displayed.
     * @param outputmode The OutputMode that need to be displayed.
     */
    public OutputTab(
        String         title,
        Collection     collection,
        CollectionView collectionView,
        OutputMode     mode
    ) {
        super(title);

        this.collection     = collection;
        this.mode           = mode;
        this.collectionView = collectionView;

        setPane(new Label("Implement concrete subclasses to vary the output."));
    }


    public CollectionView getCollectionView() {
        return collectionView;
    }


    public String getOutputName() {
        return mode.getName();
    }


    public Artifact getArtifact() {
        return getCollectionView().getArtifact();
    }


    public Collection getCollection() {
        return collection;
    }


    public OutputMode getMode() {
        return mode;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
