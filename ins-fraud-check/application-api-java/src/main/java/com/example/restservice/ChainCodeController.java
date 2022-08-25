package com.example.restservice;

import com.example.restservice.Claim;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
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
	//private final String ID = String.valueOf(Instant.now().toEpochMilli());
	//private final String claimFullID = "claim-" + ID;
	//private final String claimID = ID;
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

	private Claim readClaimById(String claimId) throws GatewayException {
		System.out.println("\n--> Evaluate Transaction: ReadClaim, function returns claim attributes");

		byte[] evaluateResult = contract.evaluateTransaction("ReadClaim", "claim-1");
		Gson gson = new Gson();
		Claim response = gson.fromJson(prettyJson(evaluateResult), Claim.class);
		System.out.println("*** Result:" + prettyJson(evaluateResult));
		return response;
	}

	private String prettyJson(final byte[] json) {
		return prettyJson(new String(json, StandardCharsets.UTF_8));
	}

	private String prettyJson(final String json) {
		JsonElement parsedJson = JsonParser.parseString(json);
		return gson.toJson(parsedJson);
	}

	@GetMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@GetMapping("/listClaimById")
	public Claim listClaimById(@RequestParam(value = "claim-1", defaultValue = "claim-2") String claimId) throws GatewayException {
		initializeGrpcCall();
		return readClaimById(claimId);		
	}
}
