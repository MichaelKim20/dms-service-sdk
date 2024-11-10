package org.acc.service.sdk.client;

import org.acc.service.sdk.data.NetWorkType;
import org.acc.service.sdk.utils.CommonUtils;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class SettlementClientForShop extends SettlementClient {
    protected String shopId;

    public SettlementClientForShop(NetWorkType network, String privateKey, String shopId) {
        super(network, privateKey, shopId);

        this.shopId = shopId;
    }

    public String getSettlementManager() throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/shop/settlement/manager/get/" + getShopId());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("managerId");
    }

    public String setSettlementManager(String managerId) throws Exception {
        byte[] message = CommonUtils.getSetSettlementManagerMessage(
                getShopId(),
                managerId,
                this.getShopNonceOf(getAddress()),
                this.getChainId()
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/shop/settlement/manager/set", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("shopId", getShopId());
        body.put("account", getAddress());
        body.put("managerId", managerId);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    public String removeSettlementManager() throws Exception {
        byte[] message = CommonUtils.getRemoveSettlementManagerMessage(
                getShopId(),
                this.getShopNonceOf(getAddress()),
                this.getChainId()
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/shop/settlement/manager/remove", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("shopId", getShopId());
        body.put("account", getAddress());
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    public String getAgentOfRefund() throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/agent/refund/" + getShopId());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("agent");
    }

    public String setAgentOfRefund(String agent) throws Exception {
        byte[] message = CommonUtils.getRegisterAgentMessage(
                getAddress(),
                agent,
                this.getLedgerNonceOf(getAddress()),
                this.getChainId()
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/agent/refund", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("account", getAddress());
        body.put("agent", agent);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

    public String getAgentOfWithdrawal() throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/agent/withdrawal/" + getShopId());
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("agent");
    }

    public String setAgentOfWithdrawal(String agent) throws Exception {
        byte[] message = CommonUtils.getRegisterAgentMessage(
                getAddress(),
                agent,
                this.getLedgerNonceOf(getAddress()),
                this.getChainId()
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);

        URI uri = new URI(String.format("%s/v1/agent/withdrawal", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("account", getAddress());
        body.put("agent", agent);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        JSONObject data = getJSONObjectResponse(conn);
        return data.getString("txHash");
    }

}
