/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.ToLoad;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.List;


public class DemDatacagePanel extends DatacagePanel {

    private static final long serialVersionUID = -2301633938080411687L;

    public static final String OUT        = "floodmap_dem_panel";
    public static final String PARAMETERS = "dem:true";


    public DemDatacagePanel() {
        super();
    }


    public DemDatacagePanel(User user) {
        super(user);
    }


    @Override
    protected void createWidget() {
        super.createWidget();
        widget.setIsMutliSelectable(false);
    }


    @Override
    public String getOuts() {
        return OUT;
    }


    @Override
    public String getParameters() {
        return PARAMETERS;
    }


    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        Recommendation r = getSelectedRecommendation();
        if (r == null) {
            errors.add(MSG.requireDGM());
        }

        return errors;
    }


    @Override
    protected Data[] getData() {
        Recommendation r = getSelectedRecommendation();

        DataItem item = new DefaultDataItem(dataName, dataName, r.getIDs());
        return new Data[] { new DefaultData(
            dataName, null, null, new DataItem[] { item }) };
    }


    protected Recommendation getSelectedRecommendation() {
        ToLoad toLoad = widget.getSelection();
        List<Recommendation> recoms = toLoad.toRecommendations();

        return recoms != null && recoms.size() >= 1 ? recoms.get(0) : null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
