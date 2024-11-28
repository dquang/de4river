/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.VisibilityChangedEvent;
import com.smartgwt.client.widgets.events.VisibilityChangedHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.events.ItemHoverEvent;
import com.smartgwt.client.widgets.form.fields.events.ItemHoverHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

import java.util.HashMap;
import java.util.Map;

import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.event.StepForwardEvent;
import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataItem;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.DefaultData;
import org.dive4elements.river.client.shared.model.DefaultDataItem;


/**
 * This UIProvider displays the DataItems of the Data object in a list of links.
 *
 * @author <a href="mailto:raimund.renkert@intevation.de">Raimund Renkert</a>
 */
public class LinkSelection
extends      MapSelection
implements   VisibilityChangedHandler
{

    private static final long serialVersionUID = -7138270638349711024L;

    /** The message class that provides i18n strings.*/
    protected FLYSConstants messages = GWT.create(FLYSConstants.class);

    /** The selected river*/
    protected Data river;
    private static Map<String, Img> riverHighlight;
    private static Map<String, HLayout> riverList;
    private static String selected;
    private static HLayout columns;

    private static Trigger trigger;

    private Canvas module;


    private class Trigger {

        private final LinkSelection ls;

        public Trigger(LinkSelection ls) {
            this.ls = ls;
        }

        public void trigger(String name) {
            DataItem item = new DefaultDataItem(
                name,
                null,
                name);

            river = new DefaultData(
                "river",
                null,
                null,
                new DataItem [] {item});
            this.ls.fireStepForwardEvent (new StepForwardEvent (getData()));
        }
    }

    /**
     * This method currently returns a
     * {@link com.smartgwt.client.widgets.form.DynamicForm} that contains all
     * data items in a list of links stored in <i>data</i>.
     *
     * @param data The {@link Data} object.
     *
     * @return a combobox.
     */
    @Override
    public Canvas create(DataList data) {
        trigger = new Trigger(this);
        createCallback();
        riverHighlight = new HashMap<String, Img>();
        riverList = new HashMap<String, HLayout>();

        VLayout v = new VLayout();
        v.setMembersMargin(10);
        v.setAlign(VerticalAlignment.TOP);
        if (data.getState() == null) {
            module = super.createWidget(data);
            v.addMember(module);
        }
        else {
            module = null;
        }
        Canvas content = createWidget(data);
        v.addMember(content);

        return v;
    }


    @Override
    public Canvas createOld(DataList dataList) {
        HLayout layout  = new HLayout();
        VLayout vLayout = new VLayout();
        layout.setWidth("400px");

        Label label = new Label(dataList.getLabel());
        label.setWidth("200px");

        int size = dataList.size();
        for (int i = 0; i < size; i++) {
            Data data        = dataList.get(i);
            DataItem[] items = data.getItems();

            for (DataItem item: items) {
                HLayout hLayout = new HLayout();

                hLayout.addMember(label);
                hLayout.addMember(new Label(item.getLabel()));

                vLayout.addMember(hLayout);
                vLayout.setWidth("130px");
            }
        }

        layout.addMember(label);
        layout.addMember(vLayout);

        return layout;
    }


    /**
     * This method creates the content of the widget.
     *
     * @param data The {@link Data} object.
     *
     * @return a list of links
     */
    @Override
    protected Canvas createWidget(DataList data) {
        GWT.log("LinkSelection - create()");

        VLayout layout   = new VLayout();
        layout.setAlign(VerticalAlignment.TOP);
        // XXX: This an evil hack because of misuse of static vars!
        layout.setHeight(25);
        columns = new HLayout();
        VLayout formLayout1 = new VLayout();
        VLayout formLayout2 = new VLayout();

        formLayout1.setLayoutLeftMargin(60);

        int size = data.size();

        for (int i = 0; i < size; i++) {
            Data d = data.get(i);

            Label label = new Label(d.getDescription());
            label.setValign(VerticalAlignment.TOP);
            label.setHeight(20);
            label.setWidth(400);

            int counter = 0;
            for (DataItem item: d.getItems()) {
                HLayout row = new HLayout();
                row.setTitle(item.getLabel());
                LinkItem link = new LinkItem("river");
                link.setHoverDelay(0);
                link.setLinkTitle(item.getLabel());
                link.setValue(item.getStringValue());
                link.setShowTitle(false);
                Img img = new Img();
                img.setShowTitle(false);
                img.setSrc("symbol_selected.png");
                img.setWidth(18);
                img.setHeight(18);
                img.setVisible(false);
                Canvas container = new Canvas();
                container.addChild(img);
                container.setHeight(20);
                container.setWidth(20);
                DynamicForm f = new DynamicForm();
                riverList.put(item.getStringValue(), row);
                riverHighlight.put(item.getStringValue(), img);
                f.setItems(link);
                row.addMember(container);
                row.addMember(f);
                row.addVisibilityChangedHandler(this);
                link.setColSpan(20);
                if (counter < d.getItems().length/2) {
                    formLayout1.addMember(row);
                }
                else {
                    formLayout2.addMember(row);
                }
                counter++;
                link.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        DataItem item = new DefaultDataItem(
                            ((LinkItem)event.getItem()).getLinkTitle(),
                            null,
                            ((LinkItem)event.getItem()).getLinkTitle());

                            river = new DefaultData(
                                "river",
                                null,
                                null,
                                new DataItem [] {item});
                        fireStepForwardEvent (new StepForwardEvent (getData()));
                    }
                });
                link.addItemHoverHandler(new ItemHoverHandler() {
                    @Override
                    public void onItemHover(ItemHoverEvent event) {
                        String river =
                            ((LinkItem)event.getItem()).getValue().toString();
                        Img item = riverHighlight.get(river);
                        if (item != null) {
                            item.setVisible(true);
                            if (selected != null && !selected.equals(river)) {
                                riverHighlight.get(selected).setVisible(false);
                            }
                            callHighlightRiver(river);
                            selected = river;
                        }
                    }
                });
            }

            if (module != null) {
                getModuleSelection().setRivers(riverList);
            }
            label.setWidth(50);

            layout.addMember(label);
            columns.addMember(formLayout1);
            columns.addMember(formLayout2);
            layout.addMember(columns);

        }
        HTMLPane map = new HTMLPane();
        map.setContentsURL("images/FLYS_Karte_interactive.html");

        helperContainer.addMember(map);
        return layout;
    }


    @Override
    protected Data[] getData() {
        Data[] module = super.getData();
        if (module != null) {
            return new Data[] {module[0], river};
        }
        else {
            return new Data[] {river};
        }
    }

    private native void createCallback() /*-{
        $wnd.highlightRiver = @org.dive4elements.river.client.client.ui.LinkSelection::highlightCallback(Ljava/lang/String;);
        $wnd.unHighlightRiver = @org.dive4elements.river.client.client.ui.LinkSelection::unHighlightCallback(Ljava/lang/String;);
        $wnd.selectRiver = @org.dive4elements.river.client.client.ui.LinkSelection::selectCallback(Ljava/lang/String;);
        $wnd.availableRiver = @org.dive4elements.river.client.client.ui.LinkSelection::availableRiver(Ljava/lang/String;);
    }-*/;

    private static void highlightCallback(String name) {
        if (riverHighlight.containsKey(name)) {
            riverHighlight.get(name).setVisible(true);
            if(selected != null && !selected.equals(name)) {
                riverHighlight.get(selected).setVisible(false);
            }
            selected = name;
        }
    }

    private static void unHighlightCallback(String name) {
        if (riverHighlight.containsKey(name)) {
            riverHighlight.get(name).setVisible(false);
            selected = null;
        }
    }

    private static boolean availableRiver(String river) {
        HLayout row = riverList.get(river);
        return row != null && row.isVisible();
    }

    private static void selectCallback(String name) {
        if (availableRiver(name)) {
            String river = riverList.get(name).getTitle();
            trigger.trigger(river);
        }
    }

    private native void callHighlightRiver(String name) /*-{
        $wnd.highlight(name);
    }-*/;


    @Override
    public void onVisibilityChanged(VisibilityChangedEvent event) {
        for (Map.Entry<String, Img> entry: riverHighlight.entrySet()) {
            entry.getValue().setVisible(false);
        }
        selected = null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
