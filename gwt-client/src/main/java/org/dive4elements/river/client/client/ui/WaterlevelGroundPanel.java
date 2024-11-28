/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.form.validator.FloatRangeValidator;

public class WaterlevelGroundPanel extends DistancePanel {

    private static final long serialVersionUID = 6598703468619862469L;

    public static final String FIELD_LOWER = "diff_from";
    public static final String FIELD_UPPER = "diff_to";
    public static final String FIELD_STEP  = "diff_diff";


    public WaterlevelGroundPanel() {
        super("left");

        FloatRangeValidator frv = new FloatRangeValidator();
        frv.setMin(0f);
        frv.setMax(Float.MAX_VALUE);
        frv.setValidateOnChange(true);
        distancePanel.getToItem().setValidators(frv);
    }


    @Override
    protected String getLowerField() {
        return FIELD_LOWER;
    }


    @Override
    protected String getUpperField() {
        return FIELD_UPPER;
    }


    @Override
    protected String getStepField() {
        return FIELD_STEP;
    }


    @Override
    protected String getLabel() {
        return MSG.waterlevel_ground_state();
    }


    @Override
    protected String labelFrom() {
        return getLabelFrom() + " [" + getUnitFrom() + "]";
    }


    @Override
    protected String getLabelFrom() {
        return MSG.wgLabelFrom();
    }


    @Override
    protected String getUnitFrom() {
        return MSG.wgUnitFrom();
    }


    @Override
    protected String labelTo() {
        return getLabelTo() + " [" + getUnitTo() + "]";
    }


    @Override
    protected String getLabelTo() {
        return MSG.wgLabelTo();
    }


    @Override
    protected String getUnitTo() {
        return MSG.wgUnitTo();
    }


    @Override
    protected String labelStep() {
        return getLabelStep() + " [" + getUnitStep() + "]";
    }


    @Override
    protected String getLabelStep() {
        return MSG.wgLabelStep();
    }


    @Override
    protected String getUnitStep() {
        return MSG.wgUnitStep();
    }


    @Override
    protected double getDefaultFrom() {
        return 0;
    }


    @Override
    protected double getDefaultTo() {
        return 2;
    }


    @Override
    protected double getDefaultStep() {
        return 0.5;
    }


    @Override
    protected void initHelperPanel() {
        // We don't need a helper panel here. But we have to override this
        // method to avoid the table creation in the parent class.
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
