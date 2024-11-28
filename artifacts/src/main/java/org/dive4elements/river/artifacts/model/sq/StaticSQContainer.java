/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.util.ArrayList;
import java.util.List;


public class StaticSQContainer
{
    private String description;
    private String stationName;
    private double km;

    private List<StaticSQRelation> relations;


    public StaticSQContainer() {
        relations = new ArrayList<StaticSQRelation>();
    }

    public StaticSQContainer(
        String stationName,
        String description,
        double km
    ) {
        this.stationName = stationName;
        this.description = description;
        this.km = km;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    public List<StaticSQRelation> getSQRelations() {
        return relations;
    }

    public void setSQRelations(List<StaticSQRelation> relations) {
        this.relations = relations;
    }

    public void addSQRelation(StaticSQRelation relation) {
        this.relations.add(relation);
    }

    public StaticSQRelation getSQRelation(int ndx) {
        return this.relations.get(ndx);
    }

    public int size() {
        return this.relations.size();
    }

    public List<StaticSQRelation> getRelationsByParameter(
        StaticSQRelation.Parameter parameter
    ) {
        List<StaticSQRelation> result = new ArrayList<StaticSQRelation>();
        for (StaticSQRelation relation : relations) {
            if (relation.getParameter() == parameter) {
                result.add(relation);
            }
        }
        return result;
    }
}
