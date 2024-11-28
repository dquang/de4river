/* Copyright (C) 2011, 2012, 2013 by Bundesanstalt für Gewässerkunde
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU AGPL (>=v3)
 * and comes with ABSOLUTELY NO WARRANTY! Check out the
 * documentation coming with Dive4Elements River for details.
 */

package org.dive4elements.river.client.shared.model;

import java.io.Serializable;


public class ContactInformation implements Serializable {

    protected String person;
    protected String organization;
    protected String address;
    protected String city;
    protected String postcode;
    protected String phone;
    protected String email;


    public ContactInformation() {
    }


    public void setPerson(String person) {
        this.person = person;
    }


    public String getPerson() {
        return person;
    }


    public void setOrganization(String organization) {
        this.organization = organization;
    }


    public String getOrganization() {
        return organization;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public String getAddress() {
        return address;
    }


    public void setCity(String city) {
        this.city = city;
    }


    public String getCity() {
        return city;
    }


    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }


    public String getPostcode() {
        return postcode;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getPhone() {
        return phone;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getEmail() {
        return email;
    }
}
// vim:set ts=4 sw=4 si et sta sts=4 fenc=utf8 :
