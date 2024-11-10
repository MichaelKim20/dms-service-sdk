package org.acc.service.sdk.data;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class ShopData {
    protected final Credentials credentials;
    public String shopId;
    public String address;
    public String privateKey;

    public ShopData(String shopId, String privateKey) {
        this.credentials = Credentials.create(ECKeyPair.create(new BigInteger(Numeric.cleanHexPrefix(privateKey), 16)));
        this.shopId = shopId;
        this.address = this.credentials.getAddress();
        this.privateKey = privateKey;
    }
}
