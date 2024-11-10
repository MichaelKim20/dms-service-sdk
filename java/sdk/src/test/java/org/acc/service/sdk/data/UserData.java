package org.acc.service.sdk.data;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

public class UserData {
    protected final Credentials credentials;
    public String phoneNumber;
    public String address;
    public String privateKey;

    public UserData(String phoneNumber, String privateKey) {
        this.credentials = Credentials.create(ECKeyPair.create(new BigInteger(Numeric.cleanHexPrefix(privateKey), 16)));
        this.phoneNumber = phoneNumber;
        this.address = this.credentials.getAddress();
        this.privateKey = privateKey;
    }
}
