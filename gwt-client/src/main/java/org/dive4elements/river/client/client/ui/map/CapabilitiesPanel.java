/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui.map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Grid;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.layout.VLayout;

import org.dive4elements.river.client.shared.model.Capabilities;
import org.dive4elements.river.client.shared.model.ContactInformation;
import org.dive4elements.river.client.client.FLYSConstants;


public class CapabilitiesPanel extends VLayout {

    protected FLYSConstants MSG = GWT.create(FLYSConstants.class);

    protected Capabilities capabilites;


    public CapabilitiesPanel(Capabilities capabilites) {
        super();
        this.capabilites = capabilites;

        initLayout();
    }


    protected void initLayout() {
        setMargin(5);
        setOverflow(Overflow.AUTO);
        initContent();
    }


    protected void initContent() {
        Grid grid = new Grid(10, 2);
        grid.setCellPadding(10);

        grid.setText(0, 0, MSG.capabilitiesTitle() + ":");
        grid.setText(0, 1, capabilites.getTitle());
        grid.setText(1, 0, MSG.capabilitiesURL() + ":");
        grid.setText(1, 1, capabilites.getOnlineResource());
        grid.setText(2, 0, MSG.capabilitiesAccessConstraints() + ":");
        grid.setText(2, 1, capabilites.getAccessConstraints());
        grid.setText(3, 0, MSG.capabilitiesFees() + ":");
        grid.setText(3, 1, capabilites.getFees());

        int row = 4;

        ContactInformation ci = capabilites.getContactInformation();

        grid.setText(row, 0, MSG.capabilitiesContactInformation() + ":");

        String person = ci.getPerson();
        if (person != null && person.length() > 0) {
            grid.setText(row++, 1, person);
        }

        String organization = ci.getOrganization();
        if (organization != null && organization.length() > 0) {
            grid.setText(row++, 1, organization);
        }

        String address = ci.getAddress();
        if (address != null && address.length() > 0) {
            grid.setText(row++, 1, address);
        }

        String pc = ci.getPostcode();
        String c  = ci.getCity();
        if ((pc != null && pc.length() > 0) || (c != null && c.length() > 0)) {
            grid.setText(row++, 1, pc + " " + c);
        }

        String email = ci.getEmail();
        if (email != null && email.length() > 0) {
            grid.setText(row++, 1, MSG.capabilitiesEmail() + ": " + email);
        }

        String phone = ci.getPhone();
        if (phone != null && phone.length() > 0) {
            grid.setText(row++, 1, MSG.capabilitiesPhone() + ": " + phone);
        }

        Label title = new Label(MSG.capabilitiesHint());
        title.setHeight(25);
        title.setStyleName("capabilities-info-title");

        addMember(title);
        addMember(grid);
    }


    protected Layout createRow(Label title, Label content) {
        title.setWidth(100);

        HLayout layout = new HLayout();
        layout.addMember(title);
        layout.addMember(content);

        return layout;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
