package org.acc.service.sdk.client;

import org.acc.service.sdk.data.NetWorkType;
import org.acc.service.sdk.data.UserBalance;
import org.acc.service.sdk.data.settlement.ChainInfo;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * The client class of decentralized loyalty services
 */
public class Client {
    /**
     * The endpoint of the relay API server
     */
    protected final String relayEndpoint;
    /**
     * The endpoint of the save purchase API server
     */
    protected final String saveEndpoint;
    /**
     * The Chain ID of side chain
     */
    protected int chainId;

    /**
     * Constructor
     * @param network Type of network (mainnet, testnet, localhost)
     */
    public Client(NetWorkType network) {
        if (network == NetWorkType.localhost) {
            relayEndpoint = "http://127.0.0.1:7070";
            saveEndpoint = "http://127.0.0.1:3030";
        } else if (network == NetWorkType.mainnet) {
            relayEndpoint = "https://relay.main.acccoin.io";
            saveEndpoint = "https://save.main.acccoin.io";
        } else {
            relayEndpoint = "https://relay.test.acccoin.io";
            saveEndpoint = "https://save.test.acccoin.io";
        }
        chainId = 0;

    }

    /**
     * Translate HTTP response data into JSON objects and deliver
     * @param conn HttpURLConnection
     * @return JSON objects
     * @throws Exception Error during HTTP communication
     */
    @NotNull
    protected static JSONObject getResponse(@NotNull HttpURLConnection conn) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

        String inputLine;
        StringBuilder sb = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
        br.close();

        JSONObject jObject = new JSONObject(sb.toString());
        int code = jObject.getInt("code");
        if (code != 0) {
            String errorMessage;
            try {
                JSONObject error = jObject.getJSONObject("error");
                errorMessage = String.format("%s (%d)", error.getString("message"), jObject.getInt("code"));
            } catch (Exception e) {
                errorMessage = String.format("%d", jObject.getInt("code"));
            }
            throw new Exception("Internal Error : " + errorMessage);
        }
        return jObject;
    }

    /**
     * The data inside the JSON object that was responded is extracted as an object.
     * @param conn HttpURLConnection
     * @return JSON objects
     * @throws Exception Error during HTTP communication
     */
    protected static JSONObject getJSONObjectResponse(@NotNull HttpURLConnection conn) throws Exception {
        return getResponse(conn).getJSONObject("data");
    }

    /**
     * The data inside the JSON object that was responded is extracted as an array.
     * @param conn HttpURLConnection
     * @return JSON objects
     * @throws Exception Error during HTTP communication
     */
    protected static JSONArray getJSONArrayResponse(@NotNull HttpURLConnection conn) throws Exception {
        return getResponse(conn).getJSONArray("data");
    }

    /**
     * Create an HTTP connection
     * @param uri URL
     * @param method GET or POST
     * @return HttpURLConnection
     * @throws Exception Exception while creating HTTP connection
     */
    protected HttpURLConnection getHttpURLConnection(@NotNull URI uri, String method) throws Exception  {
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        return conn;
    }

    /**
     * Provide the ID of the chain
     * @return chain ID
     *
     * @throws Exception Error during HTTP communication
     */
    public long getChainId() throws Exception {
        if (chainId != 0) {
            return chainId;
        }
        URI uri = new URI(relayEndpoint + "/v1/chain/side/id");
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        chainId = data.getInt("chainId");
        return chainId;
    }


    /**
     * Provide the user's points and token balance information
     * @param phoneNumber User's phone number
     * @return UserBalance
     *
     * @throws Exception Error during HTTP communication
     */
    public UserBalance getBalancePhone(@NotNull String phoneNumber) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/ledger/balance/phone/" + phoneNumber.trim().replace(" ", "%20"));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return new UserBalance(data.getJSONObject("point"), data.getJSONObject("token"));
    }

    /**
     * Provide the user's points and token balance information
     * @param phoneHash User's phone number hash
     * @return UserBalance
     *
     * @throws Exception Error during HTTP communication
     */
    public UserBalance getBalancePhoneHash(@NotNull String phoneHash) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/ledger/balance/phoneHash/" + phoneHash.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return new UserBalance(data.getJSONObject("point"), data.getJSONObject("token"));
    }

    /**
     * Provide the user's points and token balance information
     * @param account User's wallet address
     * @return UserBalance
     *
     * @throws Exception Error during HTTP communication
     */
    public UserBalance getBalanceAccount(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/ledger/balance/account/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return new UserBalance(data.getJSONObject("point"), data.getJSONObject("token"));
    }

    /**
     * Provide a nonce corresponding to the user's wallet address. It provides a nonce corresponding to the user's wallet address.
     * This ensures that the same signature is not repeated. And this value is recorded in Contract and automatically increases by 1.
     * @param account User's wallet address
     * @return the nonce
     *
     * @throws Exception Error during HTTP communication
     */
    public long getLedgerNonceOf(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/ledger/nonce/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getInt("nonce");
    }

    private ChainInfo _mainChainInfo;
    private ChainInfo _sideChainInfo;

    public long getShopNonceOf(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/shop/nonce/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getInt("nonce");
    }

    public ChainInfo getChainInfoOfMainChain() throws Exception {
        if (_mainChainInfo != null) return _mainChainInfo;
        URI uri = new URI(relayEndpoint + "/v1/chain/main/info/");
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        _mainChainInfo = ChainInfo.fromJSONObject(data);
        return _mainChainInfo;
    }

    public long getChainIdOfMainChain() throws Exception {
        ChainInfo info = getChainInfoOfMainChain();
        return info.network.chainId;
    }

    public long getNonceOfMainChainToken(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/token/main/nonce/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getInt("nonce");
    }

    public BigInteger getBalanceOfMainChainToken(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/token/main/balance/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getBigInteger("balance");
    }

    public ChainInfo getChainInfoOfSideChain() throws Exception {
        if (_sideChainInfo != null) return _sideChainInfo;
        URI uri = new URI(relayEndpoint + "/v1/chain/side/info/");
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        _sideChainInfo = ChainInfo.fromJSONObject(data);
        return _sideChainInfo;
    }

    public long getChainIdOfSideChain() throws Exception {
        ChainInfo info = getChainInfoOfSideChain();
        return info.network.chainId;
    }

    public long getNonceOfSideChainToken(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/token/side/nonce/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getInt("nonce");
    }

    public BigInteger getBalanceOfSideChainToken(@NotNull String account) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/token/side/balance/" + account.trim());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getBigInteger("balance");
    }
}
