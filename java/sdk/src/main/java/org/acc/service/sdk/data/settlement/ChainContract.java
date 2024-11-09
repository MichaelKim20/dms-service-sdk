package org.acc.service.sdk.data.settlement;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ChainContract {
    public String token;
    public String chainBridge;
    public String loyaltyBridge;


    public ChainContract(
            String token,
            String chainBridge,
            String loyaltyBridge
    ) {
        this.token = token;
        this.chainBridge = chainBridge;
        this.loyaltyBridge = loyaltyBridge;
    }


    @NotNull
    @Contract("_ -> new")
    public static ChainContract fromJSONObject(JSONObject data) {
        return new ChainContract(
                data.getString("token"),
                data.getString("chainBridge"),
                data.getString("loyaltyBridge")
        );
    }

    public ChainContract cloneTaskItem() {
        return new ChainContract(
                this.token,
                this.chainBridge,
                this.loyaltyBridge
        );
    }
}