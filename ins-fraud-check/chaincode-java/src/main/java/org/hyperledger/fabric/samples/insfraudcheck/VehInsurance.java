/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.insfraudcheck;

//import java.util.Objects;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class VehInsurance {

    @Property()
    private final String insuranceId;
    @Property()
    private final String custId;
    @Property()
    private final String companyId;
    @Property()
    private final String vin;
    @Property()
    private final String expDate;
    @Property()
    private final String insType;
    @Property()
    private final String vehicleMake;
    @Property()
    private final String vehicleModel;
    @Property()
    private final String vehicleModelYear;
    
    public String getInsuranceId() {
        return insuranceId;
    }
    public String getCustId() {
        return custId;
    }
    public String getCompanyId() {
        return companyId;
    }
    public String getVin() {
        return vin;
    }
    public String getExpDate() {
        return expDate;
    }
    public String getInsType() {
        return insType;
    }
    public String getVehicleMake() {
        return vehicleMake;
    }
    public String getVehicleModel() {
        return vehicleModel;
    }
    public String getVehicleModelYear() {
        return vehicleModelYear;
    }

    public VehInsurance(@JsonProperty("insuranceId") String insuranceId, 
            @JsonProperty("custId") String custId, 
            @JsonProperty("companyId") String companyId, 
            @JsonProperty("vin") String vin, 
            @JsonProperty("expDate") String expDate, 
            @JsonProperty("insType") String insType,
            @JsonProperty("vehicleMake") String vehicleMake, 
            @JsonProperty("vehicleModel") String vehicleModel, 
            @JsonProperty("vehicleModelYear") String vehicleModelYear) {
        this.insuranceId = insuranceId;
        this.custId = custId;
        this.companyId = companyId;
        this.vin = vin;
        this.expDate = expDate;
        this.insType = insType;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.vehicleModelYear = vehicleModelYear;
    }

    @Override
    public String toString() {
        return "VehInsurance [companyId=" + companyId + ", custId=" + custId + ", expDate=" + expDate + ", insType="
                + insType + ", insuranceId=" + insuranceId + ", vehicleMake=" + vehicleMake + ", vehicleModel="
                + vehicleModel + ", vehicleModelYear=" + vehicleModelYear + ", vin=" + vin + "]";
    }

     
}
