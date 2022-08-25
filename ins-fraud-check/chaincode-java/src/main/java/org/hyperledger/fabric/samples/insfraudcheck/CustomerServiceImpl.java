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
                title = "Customer Service",
                description = "The hyperlegendary customer processing",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "aniket.anerao@lexisnexisrisk.com",
                        name = "Aniket Anerao",
                        url = "https://github.com/aneran01")))
@Default
public final class CustomerServiceImpl implements ContractInterface {

    private final Genson genson = new Genson();

    private enum CustomerCreationErrors {
        CUSTOMER_NOT_FOUND,
        CUSTOMER_ALREADY_EXISTS
    }

    /**
     * Creates some initial customers on the ledger.
     *
     * @param ctx the transaction context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {                
        CreateCustomer(ctx, "1", "Aniket", "Prakash", "Anerao", "Sant Namdeo Path No 2", "Gograsswadi", "A-6", "Dombivli", "Maharashtra", "421201", "PANCARD", "ABDCA4364R");
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

        if (CustomerExists(ctx, custId)) {
            String errorMessage = String.format("Customer %s already exists", custId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerCreationErrors.CUSTOMER_ALREADY_EXISTS.toString());
        }

        Customer customer = new Customer(custId, custFirstName, custMiddleName, custLastName, streetNo, streetName, aptSuiteUnitNo, city, state, pincode, proofType, proofNo);
        //Use Genson to convert the Customer into string, sort it alphabetically and serialize it into a json string
        String sortedJson = genson.serialize(customer);
        stub.putStringState(custId, sortedJson);

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

}
