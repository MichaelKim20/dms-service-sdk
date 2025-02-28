package org.kios.service.sdk.client;

import org.kios.service.sdk.data.NetWorkType;
import org.kios.service.sdk.data.payment.PaymentInfo;
import org.kios.service.sdk.data.payment.PaymentTaskItem;
import org.kios.service.sdk.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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
 * The client that processes payments using points.
 * It has the role of delivering input from the KIOSK to the loyalty system.
 */
public class PaymentClient extends Client {
    /**
     * Message repeater's wallet for payment
     */
    protected final Credentials credentials;

    /**
     * Constructor
     * @param network Type of network (mainnet, testnet, localhost)
     * @param privateKey The private key used in the payment
     */
    public PaymentClient(NetWorkType network, String privateKey) {
        super(network);
        this.credentials = Credentials.create(ECKeyPair.create(new BigInteger(Numeric.cleanHexPrefix(privateKey), 16)));
    }

    public String getAddress() {
        return this.credentials.getAddress();
    }

    /**
     * It calculates the amount required for payment.
     * @param account   User's wallet address or temporary address
     * @param amount    Purchase amount (info. decimals are 18)
     * @param currency  Currency symbol
     */
    public PaymentInfo getPaymentInfo(@NotNull String account, BigInteger amount, String currency) throws Exception {
        URI uri = new URI(String.format("%s/v2/payment/info?account=%s&amount=%s&currency=%s", relayEndpoint, account.trim(), amount.toString(), currency.trim()));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return new PaymentInfo(
                data.getString("account"),
                new BigInteger(data.getString("amount"), 10),
                data.getString("currency"),
                new BigInteger(data.getString("balance"), 10),
                new BigInteger(data.getString("balanceValue"), 10),
                new BigInteger(data.getString("paidPoint"), 10),
                new BigInteger(data.getString("paidValue"), 10),
                new BigInteger(data.getString("feePoint"), 10),
                new BigInteger(data.getString("feeValue"), 10),
                new BigInteger(data.getString("totalPoint"), 10),
                new BigInteger(data.getString("totalValue"), 10)
        );
    }

    /**
     * Start a new payment
     * @param purchaseId    Purchase ID
     * @param account       User's wallet address or temporary address
     * @param amount        Purchase amount
     * @param currency      Currency symbol (case letter)
     * @param shopId        Shop ID
     * @param terminalId    Terminal ID
     */
    public PaymentTaskItem openNewPayment(String purchaseId, String account, BigInteger amount, String currency, String shopId, String terminalId) throws Exception {
        byte[] message = CommonUtils.getOpenNewPaymentMessage(
                purchaseId,
                amount,
                currency,
                shopId,
                account,
                terminalId
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);
        URI uri = new URI(String.format("%s/v2/payment/new/open", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("purchaseId", purchaseId);
        body.put("amount", amount.toString());
        body.put("currency", currency);
        body.put("shopId", shopId);
        body.put("account", account);
        body.put("terminalId", terminalId);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        return PaymentTaskItem.fromJSONObject(getJSONObjectResponse(conn));
    }

    /**
     * Close the new payment
     * @param paymentId Payment ID
     * @param confirm If this value is true, the payment will be terminated normally, otherwise the payment will be canceled.
     */
    public PaymentTaskItem closeNewPayment(String paymentId, Boolean confirm) throws Exception {
        byte[] message = CommonUtils.getCloseNewPaymentMessage(
                paymentId,
                confirm
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);
        URI uri = new URI(String.format("%s/v2/payment/new/close", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("paymentId", paymentId);
        body.put("confirm", confirm);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        return PaymentTaskItem.fromJSONObject(getJSONObjectResponse(conn));
    }

    /**
     * Start processing cancellation of previously completed new payments
     * @param paymentId  Payment ID
     * @param terminalId Terminal ID
     */
    public PaymentTaskItem openCancelPayment(String paymentId, String terminalId) throws Exception {
        byte[] message = CommonUtils.getOpenCancelPaymentMessage(
                paymentId,
                terminalId
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);
        URI uri = new URI(String.format("%s/v2/payment/cancel/open", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("paymentId", paymentId);
        body.put("terminalId", terminalId);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        return PaymentTaskItem.fromJSONObject(getJSONObjectResponse(conn));
    }

    /**
     * Close the cancellation payment
     * @param paymentId Payment ID
     * @param confirm If this value is true, the payment will be terminated normally, otherwise the payment will be canceled.
     */
    public PaymentTaskItem closeCancelPayment(String paymentId, Boolean confirm) throws Exception {
        byte[] message = CommonUtils.getCloseCancelPaymentMessage(
                paymentId,
                confirm
        );
        String signature = CommonUtils.signMessage(this.credentials.getEcKeyPair(), message);
        URI uri = new URI(String.format("%s/v2/payment/cancel/close", relayEndpoint));
        HttpURLConnection conn = getHttpURLConnection(uri, "POST");

        JSONObject body = new JSONObject();
        body.put("paymentId", paymentId);
        body.put("confirm", confirm);
        body.put("signature", signature);

        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));
        }

        return PaymentTaskItem.fromJSONObject(getJSONObjectResponse(conn));
    }

    /**
     * Provide detailed information on the payment
     * @param paymentId Payment ID
     */
    public PaymentTaskItem getPaymentItem(@NotNull String paymentId) throws Exception {
        URI uri = new URI(String.format("%s/v2/payment/item?paymentId=%s", relayEndpoint, paymentId.trim()));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");

        return PaymentTaskItem.fromJSONObject(getJSONObjectResponse(conn));
    }

    public long getLatestTaskSequence() throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/task/sequence/latest");
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        JSONObject data = getJSONObjectResponse(conn);
        return data.getLong("sequence");
    }

    public JSONArray getTasks(long sequence) throws Exception {
        URI uri = new URI(relayEndpoint + "/v1/task/list/" + String.valueOf(sequence));
        HttpURLConnection conn = getHttpURLConnection(uri, "GET");
        return getJSONArrayResponse(conn);
    }
}
