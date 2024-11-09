package org.acc.service.sdk.client;

import org.acc.service.sdk.data.NetWorkType;
import org.acc.service.sdk.data.settlement.ShopData;
import org.acc.service.sdk.data.settlement.ShopRefundableData;
import org.acc.service.sdk.utils.CommonUtils;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class SettlementClient extends Client {
    /**
     * Message repeater's wallet for providing
     */
    protected final Credentials credentials;

    protected String shopId;

    public SettlementClient(NetWorkType network, String privateKey, String shopId) {
        super(network);
        this.credentials = Credentials.create(ECKeyPair.create(new BigInteger(Numeric.cleanHexPrefix(privateKey), 16)));
        this.shopId = shopId;
    }

    public String getAddress() {
        return this.credentials.getAddress();
    }

    public String getShopId() {
        return this.shopId;
    }

    public long getSettlementClientLength() throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/shop/settlement/client/length/" + getShopId());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getInt("length");
    }

    public String[] getSettlementClientList(long startIndex, long endIndex) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/shop/settlement/client/list/" + getShopId() + "?start=" + startIndex + "&end=" + endIndex);
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        var clients = data.getJSONArray("clients");
        var clientList = new String[clients.length()];
        for (var i = 0; i < clients.length(); i++) {
            clientList[i] = clients.getString(i);
        }
        return clientList;
    }

    public String collectSettlementAmountMultiClient(String[] clients) throws Exception {
        long nonce = this.getLedgerNonceOf(getAddress());
        byte[] message = CommonUtils.getCollectSettlementAmountMultiClientMessage(
                getShopId(),
                clients,
                nonce,
                this.getChainId()
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/shop/settlement/collect", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("shopId", getShopId());
        body.put("account", getAddress());
        body.put("clients", String.join(",", clients));
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    public ShopData getShopInfo() throws Exception {
        URI uri = new URI(String.format("%s/v1/shop/info/%s", relayEndpoint, getShopId()));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return ShopData.fromJSONObject(data);
    }

    public String getAccountOfShopOwner(String provider) throws Exception {
        ShopData info = getShopInfo();
        return info.account;
    }

    public ShopRefundableData getRefundable() throws Exception {
        URI uri = new URI(String.format("%s/v1/shop/refundable/%s", relayEndpoint, getShopId()));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return ShopRefundableData.fromJSONObject(data);
    }

    public String refund(BigInteger amount) throws Exception {
        var adjustedAmount = CommonUtils.zeroGWEI(amount);
        long nonce = this.getLedgerNonceOf(this.credentials.getAddress());
        long chainId = this.getChainId();
        byte[] message = CommonUtils.getShopRefundMessage(getShopId(), adjustedAmount, nonce, chainId);
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/shop/refund", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("shopId", getShopId());
        body.put("account", getAddress());
        body.put("amount", adjustedAmount.toString());
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    public String withdraw(BigInteger amount) throws Exception {
        var chainInfo = getChainInfoOfSideChain();
        var adjustedAmount = CommonUtils.zeroGWEI(amount);
        var expiry = CommonUtils.getTimeStamp() + 1800;
        long nonce = this.getLedgerNonceOf(getAddress());
        byte[] message = CommonUtils.getTransferMessage(
                chainInfo.network.chainId,
                chainInfo.contract.token,
                getAddress(),
                chainInfo.contract.loyaltyBridge,
                adjustedAmount,
                nonce,
                expiry);
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/ledger/withdraw_via_bridge", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("account", getAddress());
        body.put("amount", adjustedAmount.toString());
        body.put("expiry", expiry);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }
}
