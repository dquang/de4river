/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model;

import org.dive4elements.river.artifacts.model.FixingsOverview.AndFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.DateFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.DateRangeFilter;

import org.dive4elements.river.artifacts.model.FixingsOverview.Fixing.Filter;

import org.dive4elements.river.artifacts.model.FixingsOverview.IdFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.IdsFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.KmFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.NotFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.OrFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.SectorFilter;
import org.dive4elements.river.artifacts.model.FixingsOverview.SectorRangeFilter;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FixingsFilterBuilder
{
    private static Logger log = LogManager.getLogger(FixingsFilterBuilder.class);

    protected Filter   filter;
    protected Range    range;

    protected Document document;

    public FixingsFilterBuilder() {
    }

    public FixingsFilterBuilder(Document document) {
        this.document = document;
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = buildFilter();
        }
        return filter;
    }

    public Range getRange() {
        if (range == null) {
            range = buildRange();
        }
        return range;
    }

    public Document getDocument() {
        return document;
    }

    protected Range buildRange() {

        NodeList ranges = document.getElementsByTagName("range");

        if (ranges.getLength() < 1) {
            return FixingsOverview.FULL_EXTENT;
        }

        Element range = (Element)ranges.item(0);

        String from = range.getAttribute("from").trim();
        String to   = range.getAttribute("to"  ).trim();

        double start = -Double.MAX_VALUE;
        double end   =  Double.MAX_VALUE;

        if (from.length() > 0) {
            try {
                start = Double.parseDouble(from);
            }
            catch (NumberFormatException nfe) {
                log.warn("Invalid from value: " + from);
            }
        }

        if (to.length() > 0) {
            try {
                end = Double.parseDouble(to);
            }
            catch (NumberFormatException nfe) {
                log.warn("Invalid to value: " + to);
            }
        }

        if (start > end) {
            double t = start;
            start = end;
            end = t;
        }

        return new Range(start, end);
    }

    protected Filter buildFilter() {
        NodeList filters = document.getElementsByTagName("filter");

        return filters.getLength() < 1
            ? FixingsOverview.ACCEPT
            : buildFilter((Element)filters.item(0));
    }

    protected static Filter buildFilter(Element root) {
        List<Filter> filters = buildRecursiveFilter(root);
        switch (filters.size()) {
            case  0: return FixingsOverview.ACCEPT;
            case  1: return filters.get(0);
            default: return new AndFilter(filters);
        }
    }

    protected static final Date parseDate(String text) {
        SimpleDateFormat format =
            new SimpleDateFormat(FixingsOverview.DATE_FORMAT);
        return format.parse(text, new ParsePosition(0));
    }

    protected static List<Filter> buildRecursiveFilter(Element root) {
        List<Filter> filters = new ArrayList<Filter>();

        NodeList children = root.getChildNodes();

        for (int i = 0, N = children.getLength(); i < N; ++i) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element)child;
            String name = element.getLocalName();

            if ("and".equals(name)) {
                filters.add(new AndFilter(buildRecursiveFilter(element)));
            }
            else if ("or".equals(name)) {
                filters.add(new OrFilter(buildRecursiveFilter(element)));
            }
            else if ("not".equals(name)) {
                List<Filter> childrenFilters = buildRecursiveFilter(element);
                if (!childrenFilters.isEmpty()) {
                    filters.add(new NotFilter(childrenFilters.get(0)));
                }
            }
            else if ("column".equals(name)) {
                String cid = element.getAttribute("cid").trim();
                if (cid.length() > 0) {
                    try {
                        filters.add(new IdFilter(Integer.parseInt(cid)));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(nfe);
                    }
                }
            }
            else if ("columns".equals(name)) {
                String cidsS = element.getAttribute("cids").trim();
                String [] parts = cidsS.split("\\s+");
                List<Integer> ids = new ArrayList<Integer>();
                for (String part: parts) {
                    try {
                        ids.add(Integer.valueOf(part));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(nfe);
                    }
                }
                int [] cids = new int[ids.size()];
                for (int j = 0; j < cids.length; ++j) {
                    cids[j] = ids.get(j);
                }
                filters.add(new IdsFilter(cids));
            }
            else if ("date".equals(name)) {
                String when = element.getAttribute("when").trim();
                if (when.length() > 0) {
                    Date date = parseDate(when);
                    if (date != null) {
                        filters.add(new DateFilter(date));
                    }
                }
            }
            else if ("date-range".equals(name)) {
                String from = element.getAttribute("from").trim();
                String to   = element.getAttribute("to"  ).trim();
                if (from.length() > 0 && to.length() > 0) {
                    Date start = parseDate(from);
                    Date end   = parseDate(to);
                    if (start != null && end != null) {
                        filters.add(new DateRangeFilter(start, end));
                    }
                }
            }
            else if ("sector-range".equals(name)) {
                String from = element.getAttribute("from").trim();
                String to   = element.getAttribute("to"  ).trim();
                if (from.length() > 0 && to.length() > 0) {
                    try {
                        filters.add(new SectorRangeFilter(
                            Integer.parseInt(from),
                            Integer.parseInt(to)));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(nfe);
                    }
                }
            }
            else if ("sector".equals(name)) {
                String value = element.getAttribute("value").trim();
                if (value.length() > 0) {
                    try {
                        filters.add(new SectorFilter(Integer.parseInt(value)));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(nfe);
                    }
                }
            }
            else if ("position".equals(name)) {
                String km = element.getAttribute("km").trim();
                if (km.length() > 0) {
                    try {
                        filters.add(new KmFilter(Double.parseDouble(km)));
                    }
                    catch (NumberFormatException nfe) {
                        log.warn(nfe);
                    }
                }
            }
        }

        return filters;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
