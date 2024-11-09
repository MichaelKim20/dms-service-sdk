package org.acc.service.sdk.data.settlement;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class ChainInfo {
    public String url;
    public ChainNetwork network;
    public ChainContract contract;

    public ChainInfo(
            String url,
            ChainNetwork network,
            ChainContract contract
    ) {
        this.url = url;
        this.network = network;
        this.contract = contract;
    }

    @NotNull
    @Contract("_ -> new")
    public static ChainInfo fromJSONObject(@NotNull JSONObject data) {
        return new ChainInfo(
                data.getString("url"),
                ChainNetwork.fromJSONObject(data.getJSONObject("network")),
                ChainContract.fromJSONObject(data.getJSONObject("contract"))
        );
    }

}
