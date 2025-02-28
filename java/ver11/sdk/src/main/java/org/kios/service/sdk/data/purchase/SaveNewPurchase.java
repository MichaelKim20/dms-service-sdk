package org.kios.service.sdk.data.purchase;

import java.math.BigInteger;

public class SaveNewPurchase {
    public String purchaseId;
    public BigInteger cashAmount;
    public BigInteger loyalty;
    public String currency;
    public String shopId;
    public String userAccount;
    public String userPhone;
    public String sender;
    public String purchaseSignature;

    public SaveNewPurchase(
            String purchaseId,
            BigInteger cashAmount,
            BigInteger loyalty,
            String currency,
            String shopId,
            String userAccount,
            String userPhone,
            String sender,
            String purchaseSignature
    ) {
        this.purchaseId = purchaseId;
        this.cashAmount = cashAmount;
        this.loyalty = loyalty;
        this.currency = currency;
        this.shopId = shopId;
        this.userAccount = userAccount;
        this.userPhone = userPhone;
        this.sender = sender;
        this.purchaseSignature = purchaseSignature;
    }
}
