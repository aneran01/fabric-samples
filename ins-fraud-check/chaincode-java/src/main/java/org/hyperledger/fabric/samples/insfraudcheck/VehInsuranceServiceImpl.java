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
                title = "Vehicle Insurance Service",
                description = "The hyperlegendary Insurance processing",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "aniket.anerao@lexisnexisrisk.com",
                        name = "Aniket Anerao",
                        url = "https://github.com/aneran01")))
@Default
public final class VehInsuranceServiceImpl implements ContractInterface {

    private final Genson genson = new Genson();

    private enum VehInsuranceCreationErrors {
        INSURANCE_NOT_FOUND,
        INSURANCE_ALREADY_EXISTS
    }

    /**
     * Creates some initial vehicle insurance on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {                
        CreateInsurance(ctx, "1", "1", "1", "1", "2023-12-12", "COMPREHENSIVE", "SUV", "Tata", "2022");
    }

    /**
     * Checks the existence of the Insurance on the ledger
     *
     * @param ctx the transaction context
     * @param insuranceId the ID of the insurance
     * @return boolean indicating the existence of the Insurance
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean InsuranceExists(final Context ctx, final String insuranceId) {
        ChaincodeStub stub = ctx.getStub();
        String insuranceJSON = stub.getStringState(insuranceId);

        return (insuranceJSON != null && !insuranceJSON.isEmpty());
    }

    /**
     * Creates a new Insurance on the ledger.
     *
     * @param ctx the transaction context
     * @param insuranceId the ID of the new Insurance
     * @param custId the ID of the customer
     * @param companyId the companyId of the Insurance
     * @param vin the vin of the new Insurance
     * @param expDate the expDate of the new Insurance
     * @param insType the insType of the Insurance
     * @param vehicleMake the vehicleMake of the new Insurance
     * @param vehicleModel the vehicleModel of the new Insurance
     * @param vehicleModelYear the vehicleModelYear of the new Insurance
     * @return the created Insurance
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public VehInsurance CreateInsurance(
        final Context ctx,
        final String insuranceId,
        final String custId,
        final String companyId,
        final String vin,
        final String expDate,
        final String insType,
        final String vehicleMake,
        final String vehicleModel,
        final String vehicleModelYear) {
        ChaincodeStub stub = ctx.getStub();

        if (InsuranceExists(ctx, insuranceId)) {
            String errorMessage = String.format("Insurance %s already exists", custId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VehInsuranceCreationErrors.INSURANCE_ALREADY_EXISTS.toString());
        }

        VehInsurance insurance = new VehInsurance(insuranceId, custId, companyId, vin, expDate, insType, vehicleMake, vehicleModel, vehicleModelYear);
        //Use Genson to convert the Insurance into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(insurance);
        stub.putStringState(insuranceId, sortedJson);

        return insurance;
    }

    /**
     * Retrieves an Insurance with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param custId the ID of the Insurance
     * @return the Insurance found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public VehInsurance ReadInsurance(final Context ctx, final String insuranceId) {
        ChaincodeStub stub = ctx.getStub();
        String insuranceJSON = stub.getStringState(insuranceId);

        if (insuranceJSON == null || insuranceJSON.isEmpty()) {
            String errorMessage = String.format("Insurance %s does not exist", insuranceId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VehInsuranceCreationErrors.INSURANCE_NOT_FOUND.toString());
        }

        VehInsurance insurance = genson.deserialize(insuranceJSON, VehInsurance.class);
        return insurance;
    }


    /**
     * Retrieves all Insurances from the ledger.
     *
     * @param ctx the transaction context
     * @return array of Insurances found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllInsurance(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<VehInsurance> queryResults = new ArrayList<VehInsurance>();

        // To retrieve all Insurances from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'Insurances0', endKey = 'Insurances9' ,
        // then getStateByRange will retrieve Insurances with keys between Insurances0 (inclusive) and Insurances9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            VehInsurance insurance = genson.deserialize(result.getStringValue(), VehInsurance.class);
            System.out.println(insurance);
            queryResults.add(insurance);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
