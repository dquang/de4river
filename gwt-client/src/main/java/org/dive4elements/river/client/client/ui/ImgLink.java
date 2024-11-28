/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.HTMLPane;


/** An image wrapped in a clickable link. */
public class ImgLink extends HTMLPane {

    protected int width;
    protected int height;

    protected String href;
    protected String imgUrl;

    protected boolean newTab;


    public ImgLink(String imgUrl, String href, int width, int height) {
        super();

        this.width  = width;
        this.height = height;
        this.href   = href;
        this.imgUrl = imgUrl;
        this.newTab = false;

        update();
    }


    public ImgLink(String imgUrl, String href, int w, int h, boolean newTab) {
        this(imgUrl, href, w, h);
        this.newTab = newTab;

        update();
    }


    protected void update() {
        String target = newTab ? "_blank" : "_self";

        setContents("<a target='" + target
            + "' href='" + href + "'><img src='" + imgUrl + "'></a>");
        setWidth(width);
        setHeight(height);
        setOverflow(Overflow.VISIBLE);
    }


    public void setSource(String href) {
        this.href = href;
        update();
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
