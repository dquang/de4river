/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


public class CalculationMessage implements Serializable {

    protected int    steps;
    protected int    currentStep;
    protected String message;


    public CalculationMessage() {
    }


    public CalculationMessage(int steps, int currentStep, String message) {
        this.steps       = steps;
        this.currentStep = currentStep;
        this.message     = message;
    }


    public int getSteps() {
        return steps;
    }


    public int getCurrentStep() {
        return currentStep;
    }


    public String getMessage() {
        return message;
    }
}
