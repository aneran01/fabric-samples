/*
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.restservice;

import com.owlike.genson.annotation.JsonProperty;

public final class Claim {


    private final String claimID;
    private final String insuranceId;
    private final int claimAmount;
    private final String claimStatus;


    public String getClaimID() {
        return claimID;
    }

    public String getInsuranceId() {
        return insuranceId;
    }

    public int getClaimAmount() {
        return claimAmount;
    }

    public String getClaimStatus() {
        return claimStatus;
    }


    public Claim(@JsonProperty("claimID") final String claimID,
            @JsonProperty("insuranceId") final String insuranceId,
            @JsonProperty("claimAmount") final int claimAmount,
            @JsonProperty("claimStatus") final String claimStatus) {
        this.claimID = claimID;
        this.insuranceId = insuranceId;
        this.claimAmount = claimAmount;
        this.claimStatus = claimStatus;
    }

    @Override
    public String toString() {
        return "InsClaim [claimAmount=" + claimAmount + ", claimID=" + claimID + ", claimStatus=" + claimStatus + ", insuranceId="
                + insuranceId + "]";
    }

}
