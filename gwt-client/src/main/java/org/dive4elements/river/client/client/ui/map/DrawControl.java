/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.SelectionType;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.utils.EnableDisableCmd;

import java.util.LinkedHashMap;

import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.Style;
import org.gwtopenmaps.openlayers.client.control.Control;
import org.gwtopenmaps.openlayers.client.control.DrawFeature;
import org.gwtopenmaps.openlayers.client.event.VectorFeatureAddedListener;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.handler.Handler;
import org.gwtopenmaps.openlayers.client.handler.PathHandler;
import org.gwtopenmaps.openlayers.client.handler.PolygonHandler;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.util.Attributes;

/**
 * Widget that handles the drawing of pipes and dikes
 * in the DigitizePanel (MapPanel).
 */
public class DrawControl
    extends HLayout implements VectorFeatureAddedListener {

    public static final String BARRIER_PIPE1    = "pipe1";
    public static final String BARRIER_PIPE2    = "pipe2";
    public static final String BARRIER_DITCH    = "ditch";
    public static final String BARRIER_DAM      = "dam";
    public static final String BARRIER_RINGDIKE = "ring_dike";

    // FIXME: i18n
    public static final String BARRIER_PIPE1_VALUE    = "Rohr 1";
    public static final String BARRIER_PIPE2_VALUE    = "Rohr 2";
    public static final String BARRIER_DITCH_VALUE    = "Graben";
    public static final String BARRIER_DAM_VALUE      = "Damm";
    public static final String BARRIER_RINGDIKE_VALUE = "Ringdeich";

    public static final String FIELD_BARRIER_TYPE = "field_barrier_type";


    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected EnableDisableCmd cmd;

    protected ImgButton   button;
    protected DynamicForm form;

    protected Map    map;
    protected Vector layer;

    protected Control control;


    public DrawControl(Map map, Vector layer, EnableDisableCmd cmd) {
        this.map   = map;
        this.layer = layer;
        this.cmd   = cmd;

        initialize();
    }


    protected void initialize() {
        setWidth(100);
        setMembersMargin(0);

        button = new ImgButton();

        final String baseUrl = GWT.getHostPageBaseURL();
        button.setSrc(baseUrl + MSG.digitize());
        button.setActionType(SelectionType.CHECKBOX);
        button.setSize(20);
        button.setShowRollOver(false);
        button.setSelected(false);
        button.setTooltip(MSG.digitizeObjects());

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent e) {
                if (button.isSelected()) {
                    cmd.enable();
                }
                else {
                    cmd.disable();
                }
            }
        });

        form = new DynamicForm();
        form.setWidth(100);
        form.setTitlePrefix("");
        form.setTitleSuffix("");

        final LinkedHashMap<String, String> map =
            new LinkedHashMap<String, String>();
        map.put(BARRIER_PIPE1, MSG.getString(BARRIER_PIPE1));
        map.put(BARRIER_PIPE2, MSG.getString(BARRIER_PIPE2));
        map.put(BARRIER_DITCH, MSG.getString(BARRIER_DITCH));
        map.put(BARRIER_DAM, MSG.getString(BARRIER_DAM));
        map.put(BARRIER_RINGDIKE, MSG.getString(BARRIER_RINGDIKE));

        final LinkedHashMap<String, String> ics =
            new LinkedHashMap<String, String>();
        ics.put(BARRIER_PIPE1, BARRIER_PIPE1);
        ics.put(BARRIER_PIPE2, BARRIER_PIPE2);
        ics.put(BARRIER_DITCH, BARRIER_DITCH);
        ics.put(BARRIER_DAM, BARRIER_DAM);
        ics.put(BARRIER_RINGDIKE, BARRIER_RINGDIKE);

        final SelectItem box = new SelectItem(FIELD_BARRIER_TYPE);
        box.setTitle("");
        box.setWidth(100);
        box.setValueMap(map);
        box.setImageURLSuffix(".png");
        box.setValueIcons(ics);

        box.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent e) {
                setSelectedControl();
            }
        });

        form.setFields(box);

        addMember(button);
        addMember(form);

        layer.addVectorFeatureAddedListener(this);

        activate(false);
    }


    protected String getSelectedType() {
        return form.getValueAsString(FIELD_BARRIER_TYPE);
    }


    @Override
    public void onFeatureAdded(FeatureAddedEvent evt) {
        setCurrentType(evt.getVectorFeature());
    }


    protected void setCurrentType(VectorFeature feature) {
        final Attributes attrs = feature.getAttributes();
        String     type  = attrs.getAttributeAsString("typ");

        if (type == null || type.length() == 0) {
            type = getSelectedType();

            final Style style = FloodMap.getStyle(type);
            if (style != null) {
                feature.setStyle(style);
            }

            if (type.equals(BARRIER_PIPE1)) {
                attrs.setAttribute("typ", BARRIER_PIPE1_VALUE);
            }
            else if (type.equals(BARRIER_PIPE2)) {
                attrs.setAttribute("typ", BARRIER_PIPE2_VALUE);
            }
            else if (type.equals(BARRIER_DAM)) {
                attrs.setAttribute("typ", BARRIER_DAM_VALUE);
            }
            else if (type.equals(BARRIER_DITCH)) {
                attrs.setAttribute("typ", BARRIER_DITCH_VALUE);
            }
            else if (type.equals(BARRIER_RINGDIKE)) {
                attrs.setAttribute("typ", BARRIER_RINGDIKE_VALUE);
            }

            layer.redraw();
        }
    }


    protected void removeControl() {
        if (control != null) {
            control.deactivate();
            map.removeControl(control);
        }
    }


    protected void setSelectedControl() {
        removeControl();

        final String type = getSelectedType();

        if (type == null || type.length() == 0) {
            return;
        }

        if (type.equalsIgnoreCase(BARRIER_RINGDIKE)) {
            control = createDrawPolygonControl();
        }
        else {
            control = createDrawLineControl();
        }

        map.addControl(control);
        control.activate();

        // Make sure the barrier layer is on top;
        // sometime it looses it z-index...
        layer.setZIndex(1000);
    }


    protected Control createDrawControl(Handler handler) {
        return new DrawFeature(layer, handler);
    }


    protected Control createDrawPolygonControl() {
        return createDrawControl(new PolygonHandler());
    }


    protected Control createDrawLineControl() {
        return createDrawControl(new PathHandler());
    }


    public void activate(boolean activate) {
        final FormItem item = form.getField(FIELD_BARRIER_TYPE);

        if (activate) {
            button.select();
            item.enable();
            setSelectedControl();
        }
        else {
            removeControl();
            button.deselect();
            item.disable();
        }
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
