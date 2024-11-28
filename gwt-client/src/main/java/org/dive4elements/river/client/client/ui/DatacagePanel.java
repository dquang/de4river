/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Data;
import org.dive4elements.river.client.shared.model.DataList;
import org.dive4elements.river.client.shared.model.User;

import java.util.ArrayList;
import java.util.List;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public abstract class DatacagePanel extends TextProvider {

    private static final long serialVersionUID = 6937994648371673222L;

    protected String dataName;

    protected User user;

    protected DatacageWidget widget;


    public DatacagePanel() {
        super();
    }


    public DatacagePanel(User user) {
        super();
        this.user = user;
    }


    @Override
    public Canvas create(DataList dataList) {
        Data   data   = dataList.get(0);
        this.dataName = data.getLabel();

        createWidget();

        Canvas label   = new Label(data.getDescription());
        Canvas submit  = getNextButton();
        VLayout layout = new VLayout();
        label.setHeight(25);

        layout.addMember(label);
        layout.addMember(submit);
        layout.setMembersMargin(10);

        return layout;
    }


    protected void createWidget() {
        widget = new DatacageWidget(
            artifact,
            getUser(),
            getOuts(),
            getParameters(),
            false);

        widget.setHeight100();

        helperContainer.addMember(widget);
    }


    @Override
    public List<String> validate() {
        return new ArrayList<String>();
    }


    public User getUser() {
        return user;
    }


    public String getOuts() {
        return null;
    }


    public String getParameters() {
        return null;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
