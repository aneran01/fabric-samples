/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.restservice;

import com.owlike.genson.annotation.JsonProperty;

public final class Customer {


    private final String custId;
    private final String custFirstName;
    private final String custMiddleName;
    private final String custLastName;
    private final String streetNo;
    private final String streetName;
    private final String aptSuiteUnitNo;
    private final String city;
    private final String state;
    private final String pincode;
    private final String proofType;
    private final String proofNo;

    public String getCustId() {
        return custId;
    }
    public String getCustFirstName() {
        return custFirstName;
    }
    public String getCustMiddleName() {
        return custMiddleName;
    }
    public String getCustLastName() {
        return custLastName;
    }
    public String getStreetNo() {
        return streetNo;
    }
    public String getStreetName() {
        return streetName;
    }
    public String getAptSuiteUnitNo() {
        return aptSuiteUnitNo;
    }
    public String getCity() {
        return city;
    }
    public String getState() {
        return state;
    }
    public String getPincode() {
        return pincode;
    }
    public String getProofType() {
        return proofType;
    }
    public String getProofNo() {
        return proofNo;
    }

    public Customer(@JsonProperty("custId") final String custId,
            @JsonProperty("custFirstName") final String custFirstName,
            @JsonProperty("custMiddleName") final String custMiddleName,
            @JsonProperty("custLastName") final String custLastName,
            @JsonProperty("streetNo") final String streetNo,
            @JsonProperty("streetName") final String streetName,
            @JsonProperty("aptSuiteUnitNo") final String aptSuiteUnitNo,
            @JsonProperty("city") final String city,
            @JsonProperty("state") final String state,
            @JsonProperty("pincode") final String pincode,
            @JsonProperty("proofType") final String proofType,
            @JsonProperty("proofNo") final String proofNo) {
        this.custId = custId;
        this.custFirstName = custFirstName;
        this.custMiddleName = custMiddleName;
        this.custLastName = custLastName;
        this.streetNo = streetNo;
        this.streetName = streetName;
        this.aptSuiteUnitNo = aptSuiteUnitNo;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.proofType = proofType;
        this.proofNo = proofNo;
    }

    @Override
    public String toString() {
        return "Customer [aptSuiteUnitNo=" + aptSuiteUnitNo + ", city=" + city + ", custFirstName=" + custFirstName
                + ", custId=" + custId + ", custLastName=" + custLastName + ", custMiddleName=" + custMiddleName
                + ", pincode=" + pincode + ", proofNo=" + proofNo + ", proofType=" + proofType + ", state=" + state
                + ", streetName=" + streetName + ", streetNo=" + streetNo + "]";
    }

}
