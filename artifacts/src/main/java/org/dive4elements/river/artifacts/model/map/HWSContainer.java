/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.map;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class HWSContainer
{
    private static Logger log = LogManager.getLogger(HWSContainer.class);
    private String river;
    private HWS.TYPE type;
    private List<HWS> hws;

    public HWSContainer() {
        river = null;
        hws = new ArrayList<HWS>();
    }

    public HWSContainer(String river, HWS.TYPE type, List<HWS> hws) {
        this.river = river;
        this.hws = hws;
        this.type = type;
    }

    public void setRiver(String river) {
        this.river = river;
    }

    public String getRiver() {
        return this.river;
    }

    public HWS.TYPE getType() {
        return type;
    }

    public void setType(HWS.TYPE type) {
        this.type = type;
    }

    public List<HWS> getHws() {
        return hws;
    }

    public void addHws(HWS hws) {
        log.debug("add hws: " + hws.getName());
        this.hws.add(hws);
    }

    public void addHws(List<HWS> hws) {
        this.hws.addAll(hws);
    }

    public List<HWS> getOfficialHWS() {
        if (hws == null || hws.size() == 0) {
            return new ArrayList<HWS>();
        }
        List<HWS> results = new ArrayList<HWS>();
        for (HWS h: hws) {
            if (h.isOfficial()) {
                results.add(h);
            }
        }
        return results;
    }

    public List<HWS> getHws(String name) {
        log.debug("find: " + name + " in " + hws.size() + " elements");
        if (hws == null || hws.size() == 0) {
            return new ArrayList<HWS>();
        }
        List<HWS> results = new ArrayList<HWS>();
        for (HWS h: hws) {
            if (h.getName().equals(name)) {
                results.add(h);
            }
        }
        log.debug("found: " + results.size());
        return results;
    }

    public List<HWS> getHws(List<String> list) {
        if (hws == null || hws.size() == 0) {
            return new ArrayList<HWS>();
        }
        List<HWS> results = new ArrayList<HWS>();
        for (String name : list) {
            results.addAll(getHws(name));
        }
        return results;
    }
}
