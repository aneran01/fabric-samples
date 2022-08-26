package com.example.restservice;

import com.example.restservice.Claim;
import com.example.restservice.Customer;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.CommitStatusException;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.hyperledger.fabric.client.Gateway.Builder;
import org.hyperledger.fabric.client.Network;

import java.io.IOException;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;
import java.util.List;
import java.time.Instant;


@RestController
public class ChainCodeController {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	private static final String mspID = "Org1MSP";
	private static final String channelName = "mychannel";
	private static final String chaincodeName = "basic";

	// Path to crypto materials.
	private static final Path cryptoPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org1.example.com");
	// Path to user certificate.
	private static final Path certPath = cryptoPath.resolve(Paths.get("users", "User1@org1.example.com", "msp", "signcerts", "cert.pem"));
	// Path to user private key directory.
	private static final Path keyDirPath = cryptoPath.resolve(Paths.get("users", "User1@org1.example.com", "msp", "keystore"));
	// Path to peer tls certificate.
	private static final Path tlsCertPath = cryptoPath.resolve(Paths.get("peers", "peer0.org1.example.com", "tls", "ca.crt"));

	// Gateway peer end point.
	private static final String peerEndpoint = "localhost:7051";
	private static final String overrideAuth = "peer0.org1.example.com";

	private Contract contract;	
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private void initializeGrpcCall() throws Exception {
		// The gRPC client connection should be shared by all Gateway connections to
		// this endpoint.
		ManagedChannel channel = newGrpcConnection();

		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private static ManagedChannel newGrpcConnection() throws IOException, CertificateException {
		BufferedReader tlsCertReader = Files.newBufferedReader(tlsCertPath);
		X509Certificate tlsCert = Identities.readX509Certificate(tlsCertReader);
		System.out.println("Peer Endpoint connected to: "+peerEndpoint);
		System.out.println("overrideAuth: "+overrideAuth);
		return NettyChannelBuilder.forTarget(peerEndpoint)
				.sslContext(GrpcSslContexts.forClient().trustManager(tlsCert).build()).overrideAuthority(overrideAuth)
				.build();
	}

	private static Identity newIdentity() throws IOException, CertificateException {
		BufferedReader certReader = Files.newBufferedReader(certPath);
		X509Certificate certificate = Identities.readX509Certificate(certReader);

		return new X509Identity(mspID, certificate);
	}

	private static Signer newSigner() throws IOException, InvalidKeyException {
		BufferedReader keyReader = Files.newBufferedReader(getPrivateKeyPath());
		PrivateKey privateKey = Identities.readPrivateKey(keyReader);

		return Signers.newPrivateKeySigner(privateKey);
	}

	private static Path getPrivateKeyPath() throws IOException {
		try (Stream<Path> keyFiles = Files.list(keyDirPath)) {
			return keyFiles.findFirst().orElseThrow();
		}
	}

	private Claim readClaimById(String claimId) throws Exception {
		System.out.println("\n--> Evaluate Transaction: ReadClaim, function returns claim attributes");
		System.out.println("Claim ID: "+claimId);
		ManagedChannel channel = newGrpcConnection();
		byte[] evaluateResult;
		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
				evaluateResult = contract.evaluateTransaction("ReadClaim", claimId);
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}		
		Gson gson = new Gson();
		Claim response = gson.fromJson(prettyJson(evaluateResult), Claim.class);
		System.out.println("*** Result:" + prettyJson(evaluateResult));
		return response;
	}

	private Customer readCustomerById(String customerId) throws Exception {
		System.out.println("\n--> Evaluate Transaction: ReadCustomer, function returns Customer KYC attributes");
		System.out.println("Customer ID: "+customerId);
		ManagedChannel channel = newGrpcConnection();
		byte[] evaluateResult;
		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
				evaluateResult = contract.evaluateTransaction("ReadCustomer", customerId);
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}		
		Gson gson = new Gson();
		Customer response = gson.fromJson(prettyJson(evaluateResult), Customer.class);
		System.out.println("*** Result:" + prettyJson(evaluateResult));
		return response;
	}

	private List<Customer> readAllCustomers() throws Exception {
		System.out.println("\n--> Evaluate Transaction: GetAllCustomers, function returns all Customers KYC attributes");
		ManagedChannel channel = newGrpcConnection();
		byte[] evaluateResult;
		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
				evaluateResult = contract.evaluateTransaction("GetAllCustomers");
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}		
		Gson gson = new Gson();
		//Customer response = gson.fromJson(prettyJson(evaluateResult), Customer.class);		
		List<Customer> customerList = gson.fromJson(prettyJson(evaluateResult), new TypeToken<List<Customer>>(){}.getType());
		System.out.println("*** Result:" + prettyJson(evaluateResult));
		return customerList;
	}

	private void addCustomer(Customer customer) throws Exception {
		System.out.println("\n--> Evaluate Transaction: CreateCustomer, function to save Customer attributes in blockchain ledger");
		String CustId = String.valueOf(Instant.now().toEpochMilli());
		System.out.println("Customer ID: "+CustId);
		ManagedChannel channel = newGrpcConnection();
		byte[] evaluateResult;
		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
				contract.submitTransaction("CreateCustomer", 
					CustId, 
					customer.getCustFirstName(), 
					customer.getCustMiddleName(),
					customer.getCustLastName(),
					customer.getStreetNo(),
					customer.getStreetName(),
					customer.getAptSuiteUnitNo(),
					customer.getCity(),
					customer.getState(),
					customer.getPincode(),
					customer.getProofType(),
					customer.getProofNo()
					);
				System.out.println("*** Customer Registration completed successfully");
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}						
	}

	private void addInsurance(VehInsurance insurance) throws Exception {
		System.out.println("\n--> Evaluate Transaction: CreateInsurance, function to save Insurance attributes in blockchain ledger");
		String insuranceId = String.valueOf(Instant.now().toEpochMilli());
		System.out.println("Insurance ID: "+insuranceId);
		ManagedChannel channel = newGrpcConnection();
		byte[] evaluateResult;
		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
				contract.submitTransaction("CreateInsurance", 
					insuranceId, 
					insurance.getCustId(),
					insurance.getCompanyId(),
					insurance.getVin(),
					insurance.getExpDate(),
					insurance.getInsType(),
					insurance.getVehicleMake(),
					insurance.getVehicleModel(),
					insurance.getVehicleModelYear()
					);
				System.out.println("*** Insurance Registration for "+insurance.getCustId()+" completed successfully");
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}						
	}

	private void addClaim(Claim claim) throws Exception {
		System.out.println("\n--> Evaluate Transaction: CreateClaim, function to save Insurance Claim attributes in blockchain ledger");
		String claimID = String.valueOf(Instant.now().toEpochMilli());
		System.out.println("Claim ID: "+claimID);
		ManagedChannel channel = newGrpcConnection();
		byte[] evaluateResult;
		Builder builder = Gateway.newInstance().identity(newIdentity()).signer(newSigner()).connection(channel)
				// Default timeouts for different gRPC calls
				.evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
				.submitOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
				.commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES));

		try (Gateway gateway = builder.connect()) {
				Network network = gateway.getNetwork(channelName);
				// Get the smart contract from the network.
				contract = network.getContract(chaincodeName);
				contract.submitTransaction("CreateClaim", 
					claimID, 
					claim.getInsuranceId(),
					String.valueOf(claim.getClaimAmount()),
					claim.getClaimStatus()
					);
				System.out.println("*** Insurance Claim Applied for "+claim.getInsuranceId()+" completed successfully");
		} finally {
			channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
		}						
	}

	private String prettyJson(final byte[] json) {
		return prettyJson(new String(json, StandardCharsets.UTF_8));
	}

	private String prettyJson(final String json) {
		JsonElement parsedJson = JsonParser.parseString(json);
		return gson.toJson(parsedJson);
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@GetMapping("/listClaimById")
	public Claim listClaimById(@RequestParam String id) throws Exception {
		return readClaimById(id);		
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@GetMapping("/listCustomerById")
	public Customer listCustomerById(@RequestParam String id) throws Exception {
		return readCustomerById(id);		
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@PostMapping(value = "/registerCustomer", consumes = "application/json")
	public void registerCustomer(@RequestBody Customer customer) throws Exception {
		System.out.println("Registering Customer..");
		addCustomer(customer);
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@PostMapping(value = "/registerInsurance", consumes = "application/json")
	public void registerInsurance(@RequestBody VehInsurance insurance) throws Exception {
		System.out.println("Registering Insurance for customer - "+insurance.getCustId());
		addInsurance(insurance);
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@PostMapping(value = "/registerClaim", consumes = "application/json")
	public void registerClaim(@RequestBody Claim claim) throws Exception {
		System.out.println("Registering Claim for Insurance no - "+claim.getInsuranceId());
		addClaim(claim);
	}

	@CrossOrigin(origins = {"http://66.241.32.168:3000", "http://20.115.96.244", "http://localhost:3000"})
	@GetMapping("/listAllCustomersKYC")
	public List<Customer> listAllCustomersKYC() throws Exception {
		return readAllCustomers();		
	}

}
