/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.artifacts.model.sq;

import java.io.Serializable;
import java.util.Date;


public class StaticSQRelation implements Serializable{

    private Date startTime;
    private Date stopTime;
    private String type;
    private Parameter parameter;
    private double a;
    private double b;
    private double qmax;

    public static enum Parameter {
        A, B, C, D, E, F
    }


    public StaticSQRelation() {
    }

    public StaticSQRelation(
        Date startTime,
        Date stopTime,
        String type,
        Parameter parameter,
        double a,
        double b
    ) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.type = type;
        this.parameter = parameter;
        this.a = a;
        this.b = b;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public void setStopTime(Date stopTime) {
        this.stopTime = stopTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public void setParameter(String parameter) {
        if (parameter == null) {
            return;
        }
        this.parameter = Parameter.valueOf(parameter);
    }

    public double getA() {
        return a;
    }

    public void setA(double a) {
        this.a = a;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getQmax() {
        return qmax;
    }

    public void setQmax(double qmax) {
        this.qmax = qmax;
    }
}
