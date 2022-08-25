/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.insfraudcheck;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Customer {

    @Property()
    private final String custId;
    @Property()
    private final String custFirstName;
    @Property()
    private final String custMiddleName;
    @Property()
    private final String custLastName;
    @Property()
    private final String streetNo;
    @Property()
    private final String streetName;
    @Property()
    private final String aptSuiteUnitNo;
    @Property()
    private final String city;
    @Property()
    private final String state;
    @Property()
    private final String pincode;
    @Property()
    private final String proofType;
    @Property()
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
