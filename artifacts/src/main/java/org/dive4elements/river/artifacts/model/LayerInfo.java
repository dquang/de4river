/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;


public class LayerInfo {

    protected String name;
    protected String type;
    protected String directory;
    protected String data;
    protected String connection;
    protected String connectionType;
    protected String extent;
    protected String srid;
    protected String group;
    protected String groupTitle;
    protected String title;
    protected String style;
    protected String filter;
    protected String labelItem;


    public LayerInfo() {
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public void setType(String type) {
        this.type = type;
    }


    public String getType() {
        return type;
    }


    public void setDirectory(String directory) {
        this.directory = directory;
    }


    public String getDirectory() {
        return directory;
    }


    public void setData(String data) {
        this.data = data;
    }


    public String getData() {
        return data;
    }


    public void setConnection(String connection) {
        this.connection = connection;
    }


    public String getConnection() {
        return connection;
    }


    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }


    public String getConnectionType() {
        return connectionType;
    }


    public void setGroup(String group) {
        this.group = group;
    }


    public String getGroup() {
        return group;
    }


    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }


    public String getGroupTitle() {
        return groupTitle;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getTitle() {
        return title;
    }


    public void setExtent(String extent) {
        this.extent = extent;
    }


    public String getExtent() {
        return extent;
    }


    public void setSrid(String srid) {
        this.srid = srid;
    }


    public String getSrid() {
        return srid;
    }


    public void setStyle(String style) {
        this.style = style;
    }


    public String getStyle() {
        return style;
    }


    public void setFilter(String filter) {
        this.filter = filter;
    }


    public String getFilter() {
        return filter;
    }

    public void setLabelItem(String labelItem) {
        this.labelItem = labelItem;
    }

    public String getLabelItem() {
        return labelItem;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
