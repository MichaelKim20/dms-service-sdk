package org.acc.service.sdk.utils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.jetbrains.annotations.NotNull;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;

public class CommonUtils {
    @NotNull
    public static String getPhoneHash(String phone) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("BOSagora Phone Number"),
                        new Utf8String(phone)
                )
        );
        return Numeric.toHexString(Hash.sha3(Numeric.hexStringToByteArray(value)));
    }

    public static long getTimeStamp() {
        return new java.sql.Timestamp(System.currentTimeMillis()).getTime() / 1000;
    }

    public static byte[] getProvidePointToAddressMessage(
            String provider,
            String receiver,
            BigInteger amount,
            long nonce,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Address(provider),
                        new Address(receiver),
                        new Uint256(amount),
                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getProvidePointToPhoneMessage(
            String provider,
            String receiver,
            BigInteger amount,
            long nonce,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Address(provider),
                        new Bytes32(Numeric.hexStringToByteArray(receiver)),
                        new Uint256(amount),
                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getRegisterAgentMessage(
            String provider,
            String assistance,
            long nonce,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Address(provider),
                        new Address(assistance),
                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getAccountMessage(
            String account,
            long nonce,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Address(account),
                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getOpenNewPaymentMessage(
            String purchaseId,
            BigInteger amount,
            String currency,
            String shopId,
            String account,
            String terminalId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("OpenNewPayment"),
                        new Utf8String(purchaseId),
                        new Uint256(amount),
                        new Utf8String(currency),
                        new Bytes32(Numeric.hexStringToByteArray(shopId)),
                        new Address(account),
                        new Utf8String(terminalId)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getCloseNewPaymentMessage(
            String paymentId,
            Boolean confirm
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("CloseNewPayment"),
                        new Utf8String(paymentId),
                        new Uint256(confirm ? BigInteger.ONE : BigInteger.ZERO)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getOpenCancelPaymentMessage(
            String paymentId,
            String terminalId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("OpenCancelPayment"),
                        new Utf8String(paymentId),
                        new Utf8String(terminalId)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getCloseCancelPaymentMessage(
            String paymentId,
            Boolean confirm
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("CloseCancelPayment"),
                        new Utf8String(paymentId),
                        new Uint256(confirm ? BigInteger.ONE : BigInteger.ZERO)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getNewPurchaseDataMessage(
            String purchaseId,
            BigInteger amount,
            BigInteger loyalty,
            String currency,
            String shopId,
            String account,
            String phone,
            String sender,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String(purchaseId),
                        new Uint256(amount),
                        new Uint256(loyalty),
                        new Utf8String(currency),
                        new Bytes32(Numeric.hexStringToByteArray(shopId)),
                        new Address(account),
                        new Bytes32(Numeric.hexStringToByteArray(phone)),
                        new Address(sender),
                        new Uint256(chainId)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getCancelPurchaseDataMessage(
            String purchaseId,
            String sender,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String(purchaseId),
                        new Address(sender),
                        new Uint256(chainId)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getCollectSettlementAmountMultiClientMessage(
            String managerShopId,
            String[] clientShopIds,
            long nonce,
            long chainId
    ) {
        var clients = new ArrayList<Bytes32>();
        for (int i = 0; i < clientShopIds.length; i++) {
            clients.add(new Bytes32(Numeric.hexStringToByteArray(clientShopIds[i])));
        }
        DynamicArray<Bytes32> clientDynamicArray;
        clientDynamicArray = new DynamicArray<Bytes32>(Bytes32.class, clients.stream().toList());

        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("CollectSettlementAmountMultiClient"),
                        new Bytes32(Numeric.hexStringToByteArray(managerShopId)),
                        clientDynamicArray,
                        new Uint256(nonce),
                        new Uint256(chainId)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getShopRefundMessage(
            String shopId,
            BigInteger amount,
            long nonce,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Bytes32(Numeric.hexStringToByteArray(shopId)),
                        new Uint256(amount),
                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getTransferMessage(
            long chainId,
            String tokenAddress,
            String from,
            String to,
            BigInteger amount,
            long nonce,
            long expiry
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Uint256(chainId),
                        new Address(tokenAddress),
                        new Address(from),
                        new Address(to),
                        new Uint256(amount),
                        new Uint256(nonce),
                        new Uint256(expiry)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getSetSettlementManagerMessage(
            String shopId,
            String managerId,
            long nonce,
            long chainId
    ) {
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("SetSettlementManager"),
                        new Bytes32(Numeric.hexStringToByteArray(shopId)),
                        new Bytes32(Numeric.hexStringToByteArray(managerId)),
                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    public static byte[] getRemoveSettlementManagerMessage(
            String shopId,
            long nonce,
            long chainId
    ) {
        String managerId = "0x0000000000000000000000000000000000000000000000000000000000000000";
        String value = TypeEncoder.encode(
                new DynamicStruct(
                        new Utf8String("RemoveSettlementManager"),
                        new Bytes32(Numeric.hexStringToByteArray(shopId)),
                        new Bytes32(Numeric.hexStringToByteArray(managerId)),                        new Uint256(chainId),
                        new Uint256(nonce)
                )
        );
        return Hash.sha3(Numeric.hexStringToByteArray(value));
    }

    @NotNull
    public static String signMessage(
            ECKeyPair keyPair,
            byte[] message
    ) {
        Sign.SignatureData signature = Sign.signPrefixedMessage(message, keyPair);
        byte[] value = new byte[65];
        System.arraycopy(signature.getR(), 0, value, 0, 32);
        System.arraycopy(signature.getS(), 0, value, 32, 32);
        System.arraycopy(signature.getV(), 0, value, 64, 1);
        return Numeric.toHexString(value);
    }

    public static String padLeftZeros(@NotNull String inputString, int length) {
        if (inputString.length() >= length) {
            return inputString;
        }
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length - inputString.length()) {
            sb.append('0');
        }
        sb.append(inputString);

        return sb.toString();
    }

    public static String getInternationalPhoneNumber(String phoneNumber) throws Exception {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber pn = phoneUtil.parseAndKeepRawInput(phoneNumber, "ZZ");
        if (!phoneUtil.isValidNumber(pn)) {
            throw new Exception("Invalid Phone Number");
        }
        return phoneUtil.format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    @NotNull
    public static BigInteger zeroGWEI(BigInteger value) {
        BigInteger unit = BigInteger.valueOf(1000000000);
        return value.divide(unit).multiply(unit);
    }

    static int purchaseId = 1000000;
    @NotNull
    public static String getSamplePurchaseId() {
        int randomIdx = (int) Math.floor(Math.random() * 1000);
        String res = String.format("P%s%s", CommonUtils.padLeftZeros(String.valueOf(purchaseId), 10), CommonUtils.padLeftZeros(String.valueOf(randomIdx), 4));
        purchaseId++;
        return res;
    }
}
