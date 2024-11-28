/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.util.Date;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import com.smartgwt.client.widgets.grid.ListGridRecord;


/**
 * The CollectionRecord is a wrapper to put Collection objects into a ListGrid.
 *
 * @author <a href="mailto:ingo.weinzierl@intevation.de">Ingo Weinzierl</a>
 */
public class CollectionRecord extends ListGridRecord {

    /** The artifact collection. */
    protected Collection collection;


    /**
     * The default constructor.
     *
     * @param collection The artifact collection.
     */
    public CollectionRecord(Collection collection) {
        this.collection = collection;

        setCreationTime(collection.getCreationTime());

        String name = collection.getName();
        setName(name != null && name.length() > 0
            ? name
            : collection.identifier());

        setTTL(collection.getTTL());
    }


    /**
     * Sets the creation time.
     *
     * @param creationTime The creation time.
     */
    public void setCreationTime(Date creationTime) {
        setAttribute("creationTime", creationTime);
    }


    /**
     * Returns the date of the creation.
     *
     * @return the creation time.
     */
    public Date getCreationTime() {
        return getAttributeAsDate("creationTime");
    }


    /**
     * Returns the name of the collection.
     *
     * @return the name of the collection.
     */
    public void setName(String name) {
        SafeHtmlBuilder b = new SafeHtmlBuilder();
        b.appendEscaped(name);

        SafeHtml html = b.toSafeHtml();

        setAttribute("name", html.asString());
    }


    /**
     * Returns the name of the collection or the uuid if no name is specified.
     *
     * @return the name of the collection.
     */
    public String getName() {
        return getAttributeAsString("name");
    }


    public void setTTL(long ttl) {
        if (ttl == 0) {
            setAttribute("ttl", "star_gold");
        }
        else {
            setAttribute("ttl", "star_silver");
        }
    }


    public String getTTL() {
        return getAttribute("ttl");
    }


    /**
     * Returns the collection objects itself.
     *
     * @return the collection object.
     */
    public Collection getCollection() {
        return collection;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
