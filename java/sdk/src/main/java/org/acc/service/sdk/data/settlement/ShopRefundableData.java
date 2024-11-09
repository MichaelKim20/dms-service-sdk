package org.acc.service.sdk.data.settlement;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.math.BigInteger;

public class ShopRefundableData {
    public BigInteger refundableAmount;
    public BigInteger refundableToken;

    public ShopRefundableData(
            BigInteger refundableAmount,
            BigInteger refundableToken
    ) {
        this.refundableAmount = refundableAmount;
        this.refundableToken = refundableToken;
    }

    @NotNull
    @Contract("_ -> new")
    public static ShopRefundableData fromJSONObject(@NotNull JSONObject data) {
        return new ShopRefundableData(
                data.getBigInteger("refundableAmount"),
                data.getBigInteger("refundableToken")
        );
    }

}
