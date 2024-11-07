// tslint:disable-next-line:no-implicit-dependencies
import { defaultAbiCoder } from "@ethersproject/abi";
// tslint:disable-next-line:no-implicit-dependencies
import { Signer } from "@ethersproject/abstract-signer";
// tslint:disable-next-line:no-implicit-dependencies
import { BigNumber, BigNumberish } from "@ethersproject/bignumber";
// tslint:disable-next-line:no-implicit-dependencies
import { arrayify, BytesLike } from "@ethersproject/bytes";
import { HashZero } from "@ethersproject/constants";
// tslint:disable-next-line:no-implicit-dependencies
import { keccak256 } from "@ethersproject/keccak256";

import { PhoneNumberFormat, PhoneNumberUtil } from "google-libphonenumber";

export class CommonUtils {
    public static getTimeStamp(): number {
        return Math.floor(new Date().getTime() / 1000);
    }

    public static getTimeStampBigInt(): bigint {
        return BigInt(new Date().getTime()) / 1000n;
    }

    public static delay(interval: number): Promise<void> {
        return new Promise<void>((resolve, _) => {
            setTimeout(resolve, interval);
        });
    }

    public static zeroGWEI(value: BigNumber): BigNumber {
        return value.div(1000000000).mul(1000000000);
    }

    // region Phone Link
    public static getPhoneHash(phone: string): string {
        const encodedResult = defaultAbiCoder.encode(["string", "string"], ["BOSagora Phone Number", phone]);
        return keccak256(encodedResult);
    }

    // endregion

    // region Provider
    public static getProvidePointToAddressMessage(
        provider: string,
        receiver: string,
        amount: BigNumberish,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["address", "address", "uint256", "uint256", "uint256"],
            [provider, receiver, amount, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getProvidePointToPhoneMessage(
        provider: string,
        receiver: BytesLike,
        amount: BigNumberish,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["address", "bytes32", "uint256", "uint256", "uint256"],
            [provider, receiver, amount, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getRegisterAgentMessage(
        account: string,
        agent: string,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["address", "address", "uint256", "uint256"],
            [account, agent, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getAccountMessage(account: string, nonce: BigNumberish, chainId: BigNumberish): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(["address", "uint256", "uint256"], [account, chainId, nonce]);
        return arrayify(keccak256(encodedResult));
    }

    // endregion

    // region Payment
    public static getOpenNewPaymentMessage(
        purchaseId: string,
        amount: BigNumberish,
        currency: string,
        shopId: string,
        account: string,
        terminalId: string
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "string", "uint256", "string", "bytes32", "address", "string"],
            ["OpenNewPayment", purchaseId, amount, currency, shopId, account, terminalId]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getCloseNewPaymentMessage(paymentId: string, confirm: boolean): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "string", "uint256"],
            ["CloseNewPayment", paymentId, confirm ? 1 : 0]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getOpenCancelPaymentMessage(paymentId: string, terminalId: string): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "string", "string"],
            ["OpenCancelPayment", paymentId, terminalId]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getCloseCancelPaymentMessage(paymentId: string, confirm: boolean): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "string", "uint256"],
            ["CloseCancelPayment", paymentId, confirm ? 1 : 0]
        );
        return arrayify(keccak256(encodedResult));
    }

    // endregion

    // region SavePurchase
    public static getNewPurchaseDataMessage(
        purchaseId: string,
        amount: BigNumberish,
        loyalty: BigNumberish,
        currency: string,
        shopId: BytesLike,
        account: string,
        phone: BytesLike,
        sender: string,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "uint256", "uint256", "string", "bytes32", "address", "bytes32", "address", "uint256"],
            [purchaseId, amount, loyalty, currency, shopId, account, phone, sender, chainId]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getCancelPurchaseDataMessage(purchaseId: string, sender: string, chainId: BigNumberish): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(["string", "address", "uint256"], [purchaseId, sender, chainId]);
        return arrayify(keccak256(encodedResult));
    }

    // endregion

    public static getCollectSettlementAmountMultiClientMessage(
        managerShopId: BytesLike,
        clientShopIds: BytesLike[],
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "bytes32", "bytes32[]", "uint256", "uint256"],
            ["CollectSettlementAmountMultiClient", managerShopId, clientShopIds, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static async signMessage(signer: Signer, message: Uint8Array): Promise<string> {
        return signer.signMessage(message);
    }

    /**
     * Converting phone numbers to international notation
     * @param phoneNumber
     */
    public static getInternationalPhoneNumber(phoneNumber: string): string {
        const phoneUtil = PhoneNumberUtil.getInstance();
        const number = phoneUtil.parseAndKeepRawInput(phoneNumber, "ZZ");
        if (!phoneUtil.isValidNumber(number)) {
            throw new Error("Invalid Phone Number");
        }
        return phoneUtil.format(number, PhoneNumberFormat.INTERNATIONAL);
    }

    public static getSetSettlementManagerMessage(
        shopId: BytesLike,
        managerId: BytesLike,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "bytes32", "bytes32", "uint256", "uint256"],
            ["SetSettlementManager", shopId, managerId, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getRemoveSettlementManagerMessage(
        shopId: BytesLike,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["string", "bytes32", "bytes32", "uint256", "uint256"],
            ["RemoveSettlementManager", shopId, HashZero, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getShopRefundMessage(
        shopId: BytesLike,
        amount: BigNumberish,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["bytes32", "uint256", "uint256", "uint256"],
            [shopId, amount, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static getTransferMessage(
        chainId: BigNumberish,
        tokenAddress: string,
        from: string,
        to: string,
        amount: BigNumberish,
        nonce: BigNumberish,
        expiry: number
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["uint256", "address", "address", "address", "uint256", "uint256", "uint256"],
            [chainId, tokenAddress, from, to, amount, nonce, expiry]
        );
        return arrayify(keccak256(encodedResult));
    }
}

(BigInt.prototype as any).toJSON = function () {
    return this.toString();
};

(BigNumber.prototype as any).toJSON = function () {
    return this.toString();
};
