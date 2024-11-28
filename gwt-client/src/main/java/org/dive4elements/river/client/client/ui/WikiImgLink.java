/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.client.ui;

import com.smartgwt.client.types.Overflow;

import org.dive4elements.river.client.client.FLYS;

public class WikiImgLink extends ImgLink {

    protected FLYS instance;

    public WikiImgLink(
        String imgUrl,
        String href,
        int width,
        int height,
        FLYS instance
    ) {
        super(imgUrl, href, width, height, false);
        this.instance = instance;
        update();
    }

    @Override
    protected void update() {
        setContents(WikiLinks.imageLinkForm(
                instance, href, imgUrl, "wikiImgLink" + toString()));
        setWidth(width);
        setHeight(height);
        setOverflow(Overflow.VISIBLE);
    }
}
