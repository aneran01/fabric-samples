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
    
    public Customer(@JsonProperty("custId") String custId, 
            @JsonProperty("custFirstName") String custFirstName, 
            @JsonProperty("custMiddleName") String custMiddleName, 
            @JsonProperty("custLastName") String custLastName, 
            @JsonProperty("streetNo") String streetNo,
            @JsonProperty("streetName") String streetName, 
            @JsonProperty("aptSuiteUnitNo") String aptSuiteUnitNo, 
            @JsonProperty("city") String city, 
            @JsonProperty("state") String state, 
            @JsonProperty("pincode") String pincode, 
            @JsonProperty("proofType") String proofType,
            @JsonProperty("proofNo") String proofNo) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aptSuiteUnitNo == null) ? 0 : aptSuiteUnitNo.hashCode());
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        result = prime * result + ((custFirstName == null) ? 0 : custFirstName.hashCode());
        result = prime * result + ((custId == null) ? 0 : custId.hashCode());
        result = prime * result + ((custLastName == null) ? 0 : custLastName.hashCode());
        result = prime * result + ((custMiddleName == null) ? 0 : custMiddleName.hashCode());
        result = prime * result + ((pincode == null) ? 0 : pincode.hashCode());
        result = prime * result + ((proofNo == null) ? 0 : proofNo.hashCode());
        result = prime * result + ((proofType == null) ? 0 : proofType.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((streetName == null) ? 0 : streetName.hashCode());
        result = prime * result + ((streetNo == null) ? 0 : streetNo.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Customer [aptSuiteUnitNo=" + aptSuiteUnitNo + ", city=" + city + ", custFirstName=" + custFirstName
                + ", custId=" + custId + ", custLastName=" + custLastName + ", custMiddleName=" + custMiddleName
                + ", pincode=" + pincode + ", proofNo=" + proofNo + ", proofType=" + proofType + ", state=" + state
                + ", streetName=" + streetName + ", streetNo=" + streetNo + "]";
    }

}
