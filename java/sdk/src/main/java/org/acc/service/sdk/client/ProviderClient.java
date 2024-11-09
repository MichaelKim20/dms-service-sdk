package org.acc.service.sdk.client;

import org.acc.service.sdk.data.NetWorkType;
import org.acc.service.sdk.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * The client that is needed to provide a point to the user.
 * First, you must obtain permission from the loyalty system and register the address of the wallet to be used in the loyalty system.
 */
public class ProviderClient extends Client {
    /**
     * Message repeater's wallet for providing
     */
    protected final Credentials credentials;

    /**
     * Constructor
     * @param network Type of network (mainnet, testnet, localhost)
     * @param privateKey The private key used in the providing
     */
    public ProviderClient(NetWorkType network, String privateKey) {
        super(network);
        this.credentials = Credentials.create(ECKeyPair.create(new BigInteger(Numeric.cleanHexPrefix(privateKey), 16)));
    }

    public String getAddress() {
        return this.credentials.getAddress();
    }

    /**
     * Check if the `account` can provide points
     * @param account Wallet address
     */
    public Boolean isProvider(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/provider/status/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getBoolean("enable");
    }

    /**
     * Register the address of the assistant who directly delivers points for the registered wallet(this.wallet).
     * The assistant's wallet can be registered and used on the server.
     * The assistant does not have the authority to deposit and withdraw, only has the authority to provide points.
     * @param account Address of wallet for the agent
     */
    public String setAgent(String account) throws Exception {
        long nonce = this.getLedgerNonceOf(this.credentials.getAddress());
        byte[] message = CommonUtils.getRegisterAgentMessage(
                this.credentials.getAddress(),
                account,
                nonce,
                this.getChainId()
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/provider/assistant/register", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("provider", this.credentials.getAddress());
        body.put("assistant", account);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    /**
     * Provide the agent's address for the registered wallet(this.wallet)
     * @param provider Provider's wallet address
     */
    public String getAgent(String provider) throws Exception {
        URI uri = new URI(String.format("%s/v1/provider/assistant/%s", relayEndpoint, provider));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("assistant");
    }

    /**
     * Provide the agent's address for the registered wallet(this.wallet)
     */
    public String getAgent() throws Exception {
        return this.getAgent(this.credentials.getAddress());
    }

    /**
     * Points are provided to the specified address.
     * Registered wallets are used for signatures. Registered wallet(this.wallet) may be providers or helpers.
     * @param provider - wallet address of the resource provider
     * @param receiver - wallet address of the person who will receive the points
     * @param amount - amount of points
     */
    public String provideToAddress(String provider, String receiver, BigInteger amount) throws Exception {
        long nonce = this.getLedgerNonceOf(this.credentials.getAddress());
        long chainId = this.getChainId();
        byte[] message = CommonUtils.getProvidePointToAddressMessage(provider, receiver, amount, nonce, chainId);
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/provider/send/account", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("provider", provider);
        body.put("receiver", receiver);
        body.put("amount", amount.toString());
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    /**
     * Points are provided to the specified phone number.
     * Registered wallets are used for signatures. Registered wallet(this.wallet) may be providers or helpers.
     * @param provider - wallet address of the resource provider
     * @param receiver - phone number of the person who will receive the points
     * @param amount - amount of points
     */
    public String provideToPhone(String provider, String receiver, BigInteger amount) throws Exception {
        long nonce = this.getLedgerNonceOf(this.credentials.getAddress());
        long chainId = this.getChainId();
        String phoneHash = CommonUtils.getPhoneHash(CommonUtils.getInternationalPhoneNumber(receiver));
        byte[] message = CommonUtils.getProvidePointToPhoneMessage(provider, phoneHash, amount, nonce, chainId);
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/provider/send/phoneHash", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("provider", provider);
        body.put("receiver", phoneHash);
        body.put("amount", amount.toString());
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }
}
