/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;

import org.dive4elements.river.client.client.Config;
import org.dive4elements.river.client.client.FLYS;
import org.dive4elements.river.client.client.FLYSConstants;
import org.dive4elements.river.client.client.services.UserService;
import org.dive4elements.river.client.client.services.UserServiceAsync;
import org.dive4elements.river.client.shared.model.User;


/**
 * Header of the FLYS webpage/app.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class FLYSHeader extends HLayout {

    /** The interface that provides the message resources. */
    private final FLYSConstants MESSAGES = GWT.create(FLYSConstants.class);

    /** The height used for this header.*/
    public static final int HEIGHT = 56;

    /** The height used for the images.*/
    public static final int IMG_HEIGHT = 50;

    /** The user that is currently logged in. */
    private User currentUser;

    /** The label that displays the current logged in user. */
    private final Label userText;

    /** The button to log the current user out.*/
    private final Button logout;

    /** The button to open the project list.*/
    private final Button projectList;

    /** The button to switch between the english and german version.*/
    private final Button language;

    /** The button to open an info panel.*/
    private final Button info;

    private final UserServiceAsync userService =
        GWT.create(UserService.class);

    /** An instance to FLYS.*/
    private final FLYS flys;


    public FLYSHeader(FLYS flys) {
        this.flys     = flys;

        String guest = MESSAGES.user() + " " + MESSAGES.guest();

        userText    = new Label(guest);
        projectList = new Button(MESSAGES.manage_projects());
        logout      = new Button(MESSAGES.logout());
        language    = new Button(MESSAGES.switch_language());
        info        = new Button(MESSAGES.info());

        projectList.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GWT.log("Clicked 'Open ProjectList' button.");
                getFlys().openProjectList();
            }
        });

        logout.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                GWT.log("Clicked 'logout' button.");
                userService.logoutCurrentUser(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(Void result) {
                        /* Just reload the page. GGInAFilter is going
                         * to redirect
                         * to the correct login page */
                        Window.Location.reload();
                    }
                });

            }
        });

        language.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LocaleInfo info            = LocaleInfo.getCurrentLocale();
                final String currentLocale = info.getLocaleName();
                final String newLocale     = currentLocale.equals("de")
                    ? "en"
                    : "de";

                SC.confirm(MESSAGES.warning(), MESSAGES.warning_language(),
                    new BooleanCallback() {
                        @Override
                        public void execute(Boolean value) {
                            if (value) {
                                switchLanguage(currentLocale, newLocale);
                            }
                        }
                    });
            }
        });

        info.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String wikiLink = Config.getInstance().getWikiUrl() + "/Info";

                // Create a form which contains the SAML session
                // for the user which is currently logged in
                String html = WikiLinks.imageLinkForm(
                        getFlys(), wikiLink, "", "wikiLinkForm");
                HTML htmlObj = new HTML(html);
                info.addChild(htmlObj);
                fireWikiLinkSubmit();
                htmlObj.removeFromParent();
            }
        });
        init();
    }

    /**
     * Calls the JS submit() function on the dynamically added
     * wikiLinkForm. This is a workaround for a SmartGWT issue(?) that
     * clears all form fields when using DynamicForm.submit() or .submitForm().
     */
    protected native void fireWikiLinkSubmit() /*-{
        $doc.wikiLinkForm.submit();
    }-*/;

    public void init() {
        setStyleName("header");
        setWidth100();
        setHeight(HEIGHT);
        setBackgroundColor("#ffffff");
        setLayoutLeftMargin(5);
        setLayoutRightMargin(5);

        String baseUrl = GWT.getHostPageBaseURL();

        Img flys = new Img(
            baseUrl + MESSAGES.flysLogo(),
            50,
            IMG_HEIGHT);

        Img bfg = new Img(
            baseUrl + MESSAGES.bfgLogoSmall(),
            98,
            IMG_HEIGHT);

        Label fullname = new Label(MESSAGES.fullname());
        fullname.setHeight(HEIGHT - IMG_HEIGHT);
        fullname.setStyleName("fontBlackMid");

        HLayout left = new HLayout();
        left.setDefaultLayoutAlign(VerticalAlignment.CENTER);
        left.setMembersMargin(3);
        left.addMember(flys);
        left.addMember(fullname);

        HLayout right = new HLayout();
        right.setAlign(Alignment.RIGHT);
        right.setDefaultLayoutAlign(Alignment.RIGHT);
        right.setDefaultLayoutAlign(VerticalAlignment.CENTER);
        right.setMembersMargin(3);
        right.setLayoutRightMargin(5);

        projectList.setStyleName("manageProjects");
        userText.setStyleName("fontBlackSmall");
        logout.setStyleName("fontLightSmall");
        language.setStyleName("fontLightSmall");
        info.setStyleName("fontLightSmall");

        userText.setAlign(Alignment.RIGHT);
        userText.setWidth(200);
        logout.setWidth(70);
        info.setWidth(40);
        language.setWidth(70);

        left.addMember(projectList);
        if (this.flys.isProjectListVisible()) {
            hideProjectButton();
        }
        else {
            showProjectButton();
        }

        right.addMember(userText);
        if (!Config.getInstance().getHideLogout()) {
            right.addMember(logout);
        }
        right.addMember(language);
        right.addMember(info);
        right.addMember(bfg);

        addMember(left);
        addMember(right);
    }

    /**
     * Returns the FLYS instance stored in this class.
     *
     * @return the flys instance.
     */
    private FLYS getFlys() {
        return flys;
    }

    /**
     * This method triggers the language switch between the <i>currentLocale</i>
     * and the <i>newLocale</i>. The switch is done by replacing a "locale="
     * parameter in the url of the application. We could use the GWT UrlBuilder
     * class to create a new URL, but - in my eyes - this class is a bit
     * inconsistens in its implementation.
     *
     * @param currentLocale The current locale string (e.g. "en").
     * @param newLocale The new locale string (e.g. "de").
     */
    private void switchLanguage(String currentLocale, String newLocale) {
        String newLocation = Window.Location.getHref();

        if (newLocation.endsWith("/")) {
            newLocation = newLocation.substring(0, newLocation.length()-1);
        }

        String replace     = null;
        String replaceWith = null;

        if (newLocation.indexOf("&locale=") >= 0) {
            replace = currentLocale.equals("de")
                ? "&locale=de"
                : "&locale=en";

            replaceWith = "&locale=" + newLocale;
        }
        else if (newLocation.indexOf("?locale=") >= 0) {
            replace = currentLocale.equals("de")
                ? "?locale=de"
                : "?locale=en";

            replaceWith = "?locale=" + newLocale;
        }
        else {
            newLocation += newLocation.indexOf("?") >= 0
                ? "&locale=" + newLocale
                : "?locale=" + newLocale;
        }

        if (replace != null && replaceWith != null) {
            newLocation = newLocation.replace(replace, replaceWith);
        }

        Window.open(newLocation, "_self", "");
    }

    /**
     * Update the text field that shows the current user. If no user is
     * currently logged in, the text will display {@link FLYSConstants.guest()}.
     */
    private void updateCurrentUser() {
        String name = currentUser != null
            ? currentUser.getName()
            : MESSAGES.guest();

        GWT.log("Update the current user: " + name);

        String username = MESSAGES.user() + " " + name;
        userText.setContents(username);
    }

    /**
     * Set the current {@link User} and call {@link updateCurrentUser()}
     * afterwards.
     *
     * @param user the new user.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;

        updateCurrentUser();
    }

    public void hideProjectButton() {
        this.projectList.hide();
    }

    public void showProjectButton() {
        this.projectList.show();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
