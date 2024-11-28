/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import org.dive4elements.river.client.client.ui.fixation.FixEventSelect;
import org.dive4elements.river.client.client.ui.fixation.FixFunctionSelect;
import org.dive4elements.river.client.client.ui.fixation.FixGaugeSelectPanel;
import org.dive4elements.river.client.client.ui.fixation.FixLocationPanel;
import org.dive4elements.river.client.client.ui.fixation.FixMultiPeriodPanel;
import org.dive4elements.river.client.client.ui.fixation.FixPeriodPanel;
import org.dive4elements.river.client.client.ui.fixation.FixQSelectPanel;
import org.dive4elements.river.client.client.ui.minfo.BedHeightsDatacagePanel;
import org.dive4elements.river.client.client.ui.minfo.BedMultiPeriodPanel;
import org.dive4elements.river.client.client.ui.minfo.CheckboxPanel;
import org.dive4elements.river.client.client.ui.minfo.SedLoadDistancePanel;
import org.dive4elements.river.client.client.ui.minfo.SedLoadEpochPanel;
import org.dive4elements.river.client.client.ui.minfo.SedLoadPeriodPanel;
import org.dive4elements.river.client.client.ui.minfo.SedLoadSQTiPanel;
import org.dive4elements.river.client.client.ui.sq.SQPeriodPanel;
import org.dive4elements.river.client.shared.model.User;

/**
 * Depending on the provider the state declared, return a UIProvider.
 *
 * A UIProvider provides widgets and input helpers to guide input to an
 * artifacts state.
 */
public class UIProviderFactory {

    private UIProviderFactory() {
    }

    /** @param uiProvider uiprovider demanded for by state. */
    public static UIProvider getProvider(String uiProvider, User user) {
        if (uiProvider == null || uiProvider.equals("")) {
            return new SelectProvider();
        }
        else if (uiProvider.equals("select_with_map")) {
            return new MapSelection();
        }
        else if (uiProvider.equals("location_distance_panel")) {
            return new LocationDistancePanel();
        }
        else if (uiProvider.equals("location_panel")) {
            return new SingleLocationPanel();
        }
        else if (uiProvider.equals("multi_location_panel")) {
            return new MultipleLocationPanel();
        }
        else if (uiProvider.equals("distance_panel")) {
            return new DistancePanel();
        }
        else if (uiProvider.equals("distance_only_panel")) {
            return new DistanceOnlyPanel();
        }
        else if (uiProvider.equals("waterlevel_ground_panel")) {
            return new WaterlevelGroundPanel();
        }
        else if (uiProvider.equals("wq_panel")) {
            return new WQInputPanel();
        }
        else if (uiProvider.equals("wq_panel_adapted")) {
            return new WQAdaptedInputPanel();
        }
        else if (uiProvider.equals("wq_panel_adapted_fixing")) {
            return new WQAdaptedFixingInputPanel();
        }
        else if (uiProvider.equals("q_segmented_panel")) {
            return new QSegmentedInputPanel();
        }
        else if (uiProvider.equals("river_panel")) {
            return new LinkSelection();
        }
        else if (uiProvider.equals("continue")) {
            return new ContinuePanel();
        }
        else if (uiProvider.equals("wsp_datacage_panel")) {
            return new WspDatacagePanel(user);
        }
        else if (uiProvider.equals("dgm_datacage_panel")) {
            return new DemDatacagePanel(user);
        }
        else if (uiProvider.equals("datacage_twin_panel")) {
            return new DatacageTwinPanel(user);
        }
        else if (uiProvider.equals("auto_integer")) {
            return new AutoIntegerPanel();
        }
        else if (uiProvider.equals("boolean_panel")) {
            return new BooleanPanel();
        }
        else if (uiProvider.equals("noinput")) {
            return new NoInputPanel();
        }
        else if (uiProvider.equals("map_digitize")) {
            return new DigitizePanel();
        }
        else if (uiProvider.equals("timerange")) {
            return new IntegerRangePanel();
        }
        else if (uiProvider.equals("wq_simple_array")) {
            return new WQSimpleArrayPanel();
        }
        else if (uiProvider.equals("gaugetimerange")) {
            return new GaugeTimeRangePanel();
        }
        else if (uiProvider.equals("fix.location_panel")) {
            return new FixLocationPanel();
        }
        else if (uiProvider.equals("fix.period_panel")) {
            return new FixPeriodPanel();
        }
        else if (uiProvider.equals("fix.period_ref_panel")) {
            return new FixPeriodPanel("ref_start", "ref_end");
        }
        else if (uiProvider.equals("fix.period_ana_panel")) {
            return new FixMultiPeriodPanel();
        }
        else if (uiProvider.equals("fix.qselect_panel")) {
            return new FixQSelectPanel();
        }
        else if (uiProvider.equals("fix.gaugeselect_panel")) {
            return new FixGaugeSelectPanel();
        }
        else if (uiProvider.equals("fix.event_panel")) {
            return new FixEventSelect();
        }
        else if (uiProvider.equals("fix.preprocessing_panel")) {
            return new BooleanPanel();
        }
        else if (uiProvider.equals("fix.functionselect")) {
            return new FixFunctionSelect();
        }
        else if (uiProvider.equals("period_select")) {
            return new PeriodPanel();
        }
        else if (uiProvider.equals("periods_select")) {
            return new MultiPeriodPanel();
        }
        else if (uiProvider.equals("sq.period.select")) {
            return new SQPeriodPanel();
        }
        else if (uiProvider.equals("outliers_input")) {
            return new DoubleInputPanel();
        }
        else if (uiProvider.equals("percent_input")) {
            return new DoubleInputPanel("percent");
        }
        else if (uiProvider.equals("parameter-matrix")) {
            return new ParameterMatrixPanel();
        }
        else if (uiProvider.equals("minfo.bed.year_epoch")) {
            return new RadioPanel();
        }
        else if (uiProvider.equals("bedquality_periods_select")) {
            return new BedMultiPeriodPanel();
        }
        else if (uiProvider.equals("bedheights_twin_panel")) {
            return new BedHeightsDatacagePanel(user);
        }
        else if (uiProvider.equals("minfo.bed.char_diameter")) {
            return new CheckboxPanel();
        }
        else if (uiProvider.equals("minfo.sedimentload_unit_select")) {
            return new RadioPanel();
        }
        else if (uiProvider.equals("static_data")) {
            return new StaticDataPanel();
        }
        else if (uiProvider.equals("minfo.sedimentload_distance_select")) {
            return new SedLoadDistancePanel();
        }
        else if (uiProvider.equals("minfo.sedimentload_year_select")) {
            return new SedLoadPeriodPanel();
        }
        else if (uiProvider.equals("minfo.sedimentload_epoch_select")) {
            return new SedLoadEpochPanel();
        }
        else if (uiProvider.equals("minfo.sedimentload_sqti_select")) {
            return new SedLoadSQTiPanel();
        }
        else if (uiProvider.equals("hws_datacage_panel")) {
            return new HWSDatacagePanel(user);
        }
        else if (uiProvider.equals("user_rgd_panel")) {
            return new UserRGDProvider();
        }
        else if (uiProvider.equals("static_sqrelation")) {
            return new StaticDataPanel();
        }
        else {
            //GWT.log("Picked default provider.");
            return new SelectProvider();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :

