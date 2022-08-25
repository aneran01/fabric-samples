package org.hyperledger.fabric.samples.insfraudcheck;

import java.util.ArrayList;
import java.util.List;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

@Contract(
        name = "basic",
        info = @Info(
                title = "Claim Processing",
                description = "The hyperlegendary claim processing",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "aniket.anerao@lexisnexisrisk.com",
                        name = "Aniket Anerao",
                        url = "https://github.com/aneran01")))
@Default
public final class ClaimServiceImpl implements ContractInterface {

    private final Genson genson = new Genson();
    private final String identifier = "claim-";

    private enum ClaimProcessingErrors {
        CLAIM_NOT_FOUND,
        CLAIM_ALREADY_EXISTS
    }

    /**
     * Creates some initial claims on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        //ChaincodeStub stub = ctx.getStub();
        CreateClaim(ctx, "1", "claim1661412450261", 3000, "NEW_CLAIM");

    }

    /**
     * Checks the existence of the Claim on the ledger
     *
     * @param ctx the transaction context
     * @param claimID the ID of the claim
     * @return boolean indicating the existence of the Claim
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean ClaimExists(final Context ctx, final String claimID) {
        ChaincodeStub stub = ctx.getStub();
        String claimJSON = stub.getStringState(claimID);

        return (claimJSON != null && !claimJSON.isEmpty());
    }

    /**
     * Creates a new claim on the ledger.
     *
     * @param ctx the transaction context
     * @param claimID the ID of the new claim
     * @param insuranceId the insuranceId of the new claim
     * @param claimAmount the claimAmount of the new claim
     * @param claimStatus the claimStatus of the new claim
     * @return the created claim
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Claim CreateClaim(final Context ctx, final String claimID,
        final String insuranceId, final int claimAmount, final String claimStatus) {
        ChaincodeStub stub = ctx.getStub();

        String ID = identifier+claimID;
        

        if (ClaimExists(ctx, ID)) {
            String errorMessage = String.format("Claim %s already exists", ID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ClaimProcessingErrors.CLAIM_ALREADY_EXISTS.toString());
        }

        Claim claim = new Claim(ID, insuranceId, claimAmount, claimStatus);
        //Use Genson to convert the Claim into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(claim);
        stub.putStringState(ID, sortedJson);

        return claim;
    }

    /**
     * Retrieves an claim with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param claimID the ID of the claim
     * @return the claim found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Claim ReadClaim(final Context ctx, final String claimID) {
        ChaincodeStub stub = ctx.getStub();

        String claimJSON = stub.getStringState(claimID);

        if (claimJSON == null || claimJSON.isEmpty()) {
            String errorMessage = String.format("Claim %s does not exist", claimID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ClaimProcessingErrors.CLAIM_NOT_FOUND.toString());
        }

        Claim claim = genson.deserialize(claimJSON, Claim.class);
        return claim;
    }


    /**
     * Retrieves all claims from the ledger.
     *
     * @param ctx the transaction context
     * @return array of claims found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllClaims(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Claim> queryResults = new ArrayList<Claim>();

        // To retrieve all claims from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'claim0', endKey = 'claim9' ,
        // then getStateByRange will retrieve claim with keys between claim0 (inclusive) and claim9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Claim claim = genson.deserialize(result.getStringValue(), Claim.class);
            System.out.println(claim);
            queryResults.add(claim);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
