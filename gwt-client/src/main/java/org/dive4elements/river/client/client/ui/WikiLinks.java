/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.smartgwt.client.types.FormMethod;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.HiddenItem;
import com.smartgwt.client.widgets.form.fields.LinkItem;
import com.smartgwt.client.widgets.form.fields.events.ClickEvent;
import com.smartgwt.client.widgets.form.fields.events.ClickHandler;

import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.shared.model.User;

public class WikiLinks
{
    public static String imageLinkForm(
        FLYS instance,
        String url,
        String imageUrl,
        String formName
    ) {
        String saml = null;
        if (instance != null && instance.getCurrentUser() != null) {
            saml = instance.getCurrentUser().getSamlXMLBase64();
        }
        String quotedUrl = SafeHtmlUtils.htmlEscape(url);
        String quotedImage = SafeHtmlUtils.htmlEscape(imageUrl);

        if (saml != null) {
            return "<form method=\"POST\" target=\"_blank\" action=\""
                + quotedUrl + "\" " + "name=\"" + formName + "\">"
                + "<input type=\"hidden\" name=\"saml\" value=\""
                + SafeHtmlUtils.htmlEscape(saml) + "\">"
                + "<input type=\"image\" src=\""+ quotedImage + "\">"
                + "</form>";
        }
        else {
            return "<a href=\"" + quotedUrl
                + "\"><img src=\"" + quotedImage + "\"></a>";
        }
    }

    public static DynamicForm linkDynamicForm(
        FLYS flys,
        String url,
        String text
    ) {
        User currentUser = flys.getCurrentUser();
        String quotedUrl = SafeHtmlUtils.htmlEscape(url);
        String quotedText = SafeHtmlUtils.htmlEscape(text);

        if (currentUser != null) {
            String saml = currentUser.getSamlXMLBase64();
            if (saml != null) {
                final DynamicForm form = new DynamicForm();
                form.setMethod(FormMethod.POST);
                form.setTarget("_blank");
                form.setAction(quotedUrl);
                form.setCanSubmit(true);
                LinkItem item = new LinkItem("saml");
                item.setTextBoxStyle("font-size: large;");
                item.setShowTitle(false);
                item.setLinkTitle(quotedText);
                item.setValue(SafeHtmlUtils.htmlEscape(saml));
                item.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        form.submitForm();
                    }
                });
                form.setFields(item);
                return form;
            }
        }
        DynamicForm form = new DynamicForm();
        LinkItem item = new LinkItem(quotedText);
        item.setShowTitle(false);
        item.setLinkTitle(quotedText);
        item.setTarget(quotedUrl);
        form.setItems(item);
        return form;
    }

    public static DynamicForm dynamicForm(FLYS flys, String url) {
        User currentUser = flys.getCurrentUser();
        String quotedUrl = SafeHtmlUtils.htmlEscape(url);

        if (currentUser != null) {
            String saml = currentUser.getSamlXMLBase64();
            if (saml != null) {
                saml = SafeHtmlUtils.htmlEscape(saml);
                GWT.log("saml=" + saml);
                DynamicForm form = new DynamicForm();
                form.setID("wikiDynamicForm");
                form.setMethod(FormMethod.POST);
                form.setTarget("_blank");
                form.setAction(quotedUrl);
                form.setCanSubmit(true);
                HiddenItem item = new HiddenItem("saml");
                item.setDefaultValue(saml);
                item.setValue(saml);
                form.setFields(item);
                //form.setValue("saml", saml);
                return form;
            }
        }
        DynamicForm form = new DynamicForm();
        form.setTarget("_blank");
        form.setAction(quotedUrl);
        return form;
    }
}
