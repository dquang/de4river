/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.exports;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class ChartSection extends TypeSection {

    private static Logger log = LogManager.getLogger(ChartSection.class);

    public static final String TITLE_ATTR         = "title";
    public static final String SUBTITLE_ATTR      = "subtitle";
    public static final String DISPLAYGRID_ATTR   = "display-grid";
    public static final String DISPLAYLOGO_ATTR   = "display-logo";
    public static final String LOGOPLACEMENTH_ATTR = "logo-placeh";
    public static final String LOGOPLACEMENTV_ATTR = "logo-placev";


    public ChartSection() {
        super("chart");
    }


    public void setTitle(String title) {
        setStringValue(TITLE_ATTR, title);
    }


    public String getTitle() {
        return getStringValue(TITLE_ATTR);
    }


    public void setSubtitle(String subtitle) {
        setStringValue(SUBTITLE_ATTR, subtitle);
    }


    public String getSubtitle() {
        return getStringValue(SUBTITLE_ATTR);
    }


    /** Get Property-value for display-logo property. */
    public String getDisplayLogo() {
        return getStringValue(DISPLAYLOGO_ATTR);
    }


    /** Set Property-value for display-logo property. */
    public void setDisplayLogo(String logo) {
        log.debug("Setting Display logo string.");
        setChoiceStringValue(DISPLAYLOGO_ATTR, logo, "logo");
    }


    /** Get Property-value for horizontal logo-placement property. */
    public String getLogoHPlacement() {
        return getStringValue(LOGOPLACEMENTH_ATTR);
    }


    /** Set Property-value for horizontal logo-placement property. */
    public void setLogoHPlacement(String place) {
        setChoiceStringValue(LOGOPLACEMENTH_ATTR, place, "placeh");
    }


    /** Get Property-value for vertical logo-placement property. */
    public String getLogoVPlacement() {
        return getStringValue(LOGOPLACEMENTV_ATTR);
    }


    /** Set Property-value for vertical logo-placement property. */
    public void setLogoVPlacement(String place) {
        setChoiceStringValue(LOGOPLACEMENTV_ATTR, place, "placev");
    }


    public void setDisplayGrid(boolean displayGrid) {
        setBooleanValue(DISPLAYGRID_ATTR, displayGrid);
    }


    public Boolean getDisplayGrid() {
        return getBooleanValue(DISPLAYGRID_ATTR);
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
