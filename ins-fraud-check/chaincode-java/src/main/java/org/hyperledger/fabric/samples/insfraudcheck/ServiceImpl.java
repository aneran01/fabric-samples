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
public final class ServiceImpl implements ContractInterface {

    private final Genson genson = new Genson();
    private final String claimIdentifier = "claim-";
    private final String custIdentifier = "cust-";
    private final String vehinsIdentifier = "vehins-";

    private enum ClaimProcessingErrors {
        CLAIM_NOT_FOUND,
        CLAIM_ALREADY_EXISTS
    }

    private enum CustomerCreationErrors {
        CUSTOMER_NOT_FOUND,
        CUSTOMER_ALREADY_EXISTS
    }

    private enum VehInsuranceCreationErrors {
        INSURANCE_NOT_FOUND,
        INSURANCE_ALREADY_EXISTS
    }

    /**
     * Creates some initial claims on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {
        //ChaincodeStub stub = ctx.getStub();
        CreateCustomer(ctx, "1", "Aniket", "Prakash", "Anerao", "Sant Namdeo Path No 2", "Gograsswadi", "A-6", "Dombivli", "Maharashtra", "421201", "PANCARD", "ABDCA4364R");
        CreateInsurance(ctx, "1", "cust-1", "comp-1", "1", "2023-12-12", "COMPREHENSIVE", "SUV", "Tata", "2022");
        CreateInsurance(ctx, "2", "cust-1", "comp-2", "1", "2023-12-12", "COMPREHENSIVE", "SUV", "Tata", "2022");
        CreateClaim(ctx, "1", "vehins-1", 3000, "NEW_CLAIM");
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

        String ID = claimIdentifier+claimID;
        

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

    /**
     * Checks the existence of the Customer on the ledger
     *
     * @param ctx the transaction context
     * @param custId the ID of the customer
     * @return boolean indicating the existence of the Customer
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean CustomerExists(final Context ctx, final String custId) {
        ChaincodeStub stub = ctx.getStub();
        String customerJSON = stub.getStringState(custId);

        return (customerJSON != null && !customerJSON.isEmpty());
    }


    /**
     * Creates a new Customer on the ledger.
     *
     * @param ctx the transaction context
     * @param custId the ID of the new customer
     * @param custFirstName the insuranceId of the new customer
     * @param custMiddleName the custMiddleName of the new customer
     * @param custLastName the custLastName of the new customer
     * @param streetNo the streetNo of the new customer
     * @param streetName the streetName of the new customer
     * @param aptSuiteUnitNo the aptSuiteUnitNo of the new customer
     * @param city the city of the new customer
     * @param state the state of the new customer
     * @param pincode the pincode of the new customer
     * @param proofType the proofType of the new customer
     * @param proofNo the proofNo of the new customer
     * @return the created customer
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Customer CreateCustomer(
        final Context ctx,
        final String custId,
        final String custFirstName,
        final String custMiddleName,
        final String custLastName,
        final String streetNo,
        final String streetName,
        final String aptSuiteUnitNo,
        final String city,
        final String state,
        final String pincode,
        final String proofType,
        final String proofNo) {
        ChaincodeStub stub = ctx.getStub();

        String ID = custIdentifier+custId;

        if (CustomerExists(ctx, ID)) {
            String errorMessage = String.format("Customer %s already exists", ID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerCreationErrors.CUSTOMER_ALREADY_EXISTS.toString());
        }

        Customer customer = new Customer(ID, custFirstName, custMiddleName, custLastName, streetNo, streetName, aptSuiteUnitNo, city, state, pincode, proofType, proofNo);
        //Use Genson to convert the Customer into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(customer);
        stub.putStringState(ID, sortedJson);

        return customer;
    }

    /**
     * Retrieves an customer with the specified ID from the ledger.
     *
     * @param ctx the transaction context
     * @param custId the ID of the customer
     * @return the customer found on the ledger if there was one
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Customer ReadCustomer(final Context ctx, final String custId) {
        ChaincodeStub stub = ctx.getStub();
        String customerJSON = stub.getStringState(custId);

        if (customerJSON == null || customerJSON.isEmpty()) {
            String errorMessage = String.format("Customer %s does not exist", custId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerCreationErrors.CUSTOMER_NOT_FOUND.toString());
        }

        Customer customer = genson.deserialize(customerJSON, Customer.class);
        return customer;
    }


    /**
     * Retrieves all customers from the ledger.
     *
     * @param ctx the transaction context
     * @return array of customers found on the ledger
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllCustomers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Customer> queryResults = new ArrayList<Customer>();

        // To retrieve all customers from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'customer0', endKey = 'customer9' ,
        // then getStateByRange will retrieve customer with keys between customer0 (inclusive) and customer9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Customer customer = genson.deserialize(result.getStringValue(), Customer.class);
            System.out.println(customer);
            queryResults.add(customer);
        }

        final String response = genson.serialize(queryResults);

        return response;
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
        String ID = vehinsIdentifier + insuranceId;

        if (InsuranceExists(ctx, ID)) {
            String errorMessage = String.format("Insurance %s already exists", ID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, VehInsuranceCreationErrors.INSURANCE_ALREADY_EXISTS.toString());
        }

        VehInsurance insurance = new VehInsurance(ID, custId, companyId, vin, expDate, insType, vehicleMake, vehicleModel, vehicleModelYear);
        //Use Genson to convert the Insurance into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(insurance);
        stub.putStringState(ID, sortedJson);

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
