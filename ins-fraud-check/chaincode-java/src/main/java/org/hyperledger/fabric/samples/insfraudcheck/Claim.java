/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.insfraudcheck;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import com.owlike.genson.annotation.JsonProperty;

@DataType()
public final class Claim {

    @Property()
    private final String claimID;
    @Property()
    private final String insuranceId;
    @Property()
    private final int claimAmount;
    @Property()
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + claimAmount;
        result = prime * result + ((claimID == null) ? 0 : claimID.hashCode());
        result = prime * result + ((insuranceId == null) ? 0 : insuranceId.hashCode());
        result = prime * result + ((claimStatus == null) ? 0 : claimStatus.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Claim other = (Claim) obj;
        if (claimAmount != other.claimAmount) {
            return false;
        }
        if (claimID == null) {
            if (other.claimID != null) {
                return false;
            }
        } else if (!claimID.equals(other.claimID)) {
            return false;
        }
        if (insuranceId == null) {
            if (other.insuranceId != null) {
                return false;
            }
        } else if (!insuranceId.equals(other.insuranceId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "InsClaim [claimAmount=" + claimAmount + ", claimID=" + claimID + ", claimStatus=" + claimStatus + ", insuranceId="
                + insuranceId + "]";
    }

}
