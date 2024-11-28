/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.StepForwardEvent;
import org.dive4elements.river.client.client.services.LoadArtifactService;
import org.dive4elements.river.client.client.services.LoadArtifactServiceAsync;
import org.dive4elements.river.client.shared.model.Artifact;
import org.dive4elements.river.client.shared.model.Collection;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;
import org.dive4elements.river.client.shared.model.Recommendation;
import org.dive4elements.river.client.shared.model.Recommendation.Facet;
import org.dive4elements.river.client.shared.model.Recommendation.Filter;
import org.dive4elements.river.client.shared.model.ToLoad;
import org.dive4elements.river.client.shared.model.User;

import java.util.List;
import java.util.Map;
import java.util.Set;


public class WspDatacagePanel extends DatacagePanel {

    private static final long serialVersionUID = 2494432743877141135L;

    public static final String WATERLEVEL_OUTS = "waterlevels_panel";

    public static final FLYSConstants MSG = GWT.create(FLYSConstants.class);


    protected LoadArtifactServiceAsync loadService =
        GWT.create(LoadArtifactService.class);

    protected Recommendation recommendation;
    protected Artifact       artifact;


    public WspDatacagePanel() {
        super();
    }


    public WspDatacagePanel(User user) {
        super(user);
    }


    @Override
    public String getOuts() {
        return WATERLEVEL_OUTS;
    }


    @Override
    protected void createWidget() {
        super.createWidget();
        widget.setIsMutliSelectable(false);
    }


    /**
     * We need to override this method (defined in AbstractUIProvider) because
     * we have to create a new Artifact specified by the Datacage selection via
     * Async request.
     *
     * @param e The ClickEvent.
     */
    @Override
    public void onClick(ClickEvent e) {
        List<String> errors = validate();
        if (errors == null || errors.isEmpty()) {
            // 1) Fetch selected recommendation.
            Config config           = Config.getInstance();
            final  String locale    = config.getLocale();
            final  Collection c     = this.collection;
            final  Recommendation r = getSelectedRecommendation();


            if (r == null) {
                SC.warn(MSG.warning_no_wsp_selected());
                return;
            }

            // TODO: This could eventually be handled server-side.
            // 2) Create, load Artifact and fire event.
            loadService.load(
                c, r, r.getFactory(), locale,
                new AsyncCallback<Artifact>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        GWT.log("WspDatacagePanel", caught);
                        SC.warn(FLYS.getExceptionString(MSG, caught));
                    }

                    @Override
                    public void onSuccess(Artifact newArtifact) {
                        GWT.log("Created new artifact.");
                        fireStepForwardEvent(new StepForwardEvent(
                            getData(r, newArtifact)));
                    }
                }
            );
        }
        else {
            showErrors(errors);
        }
    }


    protected Recommendation getSelectedRecommendation() {
        ToLoad toLoad = widget.getSelection();
        List<Recommendation> recoms = toLoad.toRecommendations();

        return recoms.size() > 0 ? recoms.get(0) : null;
    }


    /**
     * Nothing is done in this method. It returns null, because we serve the
     * Data another way!
     *
     * @return always null!
     */
    @Override
    protected Data[] getData() {
        // do nothing here, the Data is fetched on another way in this panel.
        return null;
    }


    /** Returns a Data Array with one default item. */
    protected Data[] getData(Recommendation r, Artifact newArtifact) {
        String uuid = newArtifact.getUuid();
        r.setMasterArtifact(uuid);

        String value = createDataString(uuid, r);

        DataItem item = new DefaultDataItem(dataName, dataName, value);
        return new Data[] { new DefaultData(
            dataName, null, null, new DataItem[] { item }) };
    }


    protected String createDataString(
        String artifact,
        Recommendation recommendation
    ) {
        Facet f = null;

        // The filter will only be available or previous calculation artifacts.
        Filter filter = recommendation.getFilter();

        if (filter != null) {
            Map<String, List<Facet>>               outs = filter.getOuts();
            Set<Map.Entry<String, List<Facet>>> entries = outs.entrySet();

            for (Map.Entry<String, List<Facet>> entry: entries) {
                List<Facet> fs = entry.getValue();

                f = fs.get(0);
                if (f != null) {
                    break;
                }
            }

            return "[" + artifact + ";" + f.getName() + ";"
                + f.getIndex() + "]";
        }
        else {
            return "[" + artifact + ";"
                + recommendation.getFactory() + ";" + 0 + "]";
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
