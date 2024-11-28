/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.events.CloseClickEvent;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.event.RedrawRequestHandler;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.User;


public abstract class Toolbar extends HLayout {

    protected OutputTab outputTab;


    public Toolbar(OutputTab outputTab) {
        super();

        // Set overflow to hidden in order to prevent nasty scrollbars in IE
        setOverflow(Overflow.HIDDEN);

        this.outputTab = outputTab;
    }


    public OutputTab getOutputTab() {
        return outputTab;
    }


    public Artifact getArtifact() {
        return outputTab.getCollectionView().getArtifact();
    }


    public User getUser() {
        return outputTab.getCollectionView().getUser();
    }


    protected void openDatacageWindow(RedrawRequestHandler handler) {
        Artifact artifact = getArtifact();
        User     user     = getUser();

        String outs = getOutputTab().getOutputName();

        final DatacageWindow dc = new DatacageWindow(
            artifact, user, outs, outputTab.getCollectionView());
        dc.addRedrawRequestHandler(handler);
        dc.addCloseClickHandler(new CloseClickHandler() {
            @Override
            public void onCloseClick(CloseClickEvent event) {
                dc.destroy();
            }
        });
        dc.show();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
