/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.insfraudcheck;

import com.owlike.genson.annotation.JsonProperty;

public final class VehInsurance {

    private final String insuranceId;
    private final String custId;
    private final String companyId;
    private final String vin;
    private final String expDate;
    private final String insType;
    private final String vehicleMake;
    private final String vehicleModel;
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

    public VehInsurance(@JsonProperty("insuranceId") final String insuranceId,
            @JsonProperty("custId") final String custId,
            @JsonProperty("companyId") final String companyId,
            @JsonProperty("vin") final String vin,
            @JsonProperty("expDate") final String expDate,
            @JsonProperty("insType") final String insType,
            @JsonProperty("vehicleMake") final String vehicleMake,
            @JsonProperty("vehicleModel") final String vehicleModel,
            @JsonProperty("vehicleModelYear") final String vehicleModelYear) {
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
