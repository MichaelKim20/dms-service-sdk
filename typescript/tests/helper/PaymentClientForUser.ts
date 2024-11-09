import { CommonUtils } from "../../src";
import { Client } from "../../src/client/Client";
import { HTTPClient } from "../../src/network/HTTPClient";
import { IPaymentTaskItemShort, NetWorkType } from "../../src/types";

import { BigNumber, BigNumberish } from "@ethersproject/bignumber";
import { Wallet } from "@ethersproject/wallet";

// @ts-ignore
import URI from "urijs";

import { defaultAbiCoder } from "@ethersproject/abi";
import { Signer } from "@ethersproject/abstract-signer";
import { arrayify, BytesLike } from "@ethersproject/bytes";
import { keccak256 } from "@ethersproject/keccak256";

export class PaymentClientForUser extends Client {
    private readonly userWallet: Wallet;

    constructor(network: NetWorkType, privateKey: string) {
        super(network);
        this.userWallet = new Wallet(privateKey);
    }

    public get address(): string {
        return this.userWallet.address;
    }

    public static getLoyaltyNewPaymentMessage(
        address: string,
        paymentId: BytesLike,
        purchaseId: string,
        amount: BigNumberish,
        currency: string,
        shopId: BytesLike,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Uint8Array {
        const encodedResult = defaultAbiCoder.encode(
            ["bytes32", "string", "uint256", "string", "bytes32", "address", "uint256", "uint256"],
            [paymentId, purchaseId, amount, currency, shopId, address, chainId, nonce]
        );
        return arrayify(keccak256(encodedResult));
    }

    public static async signLoyaltyNewPayment(
        signer: Signer,
        paymentId: BytesLike,
        purchaseId: string,
        amount: BigNumberish,
        currency: string,
        shopId: BytesLike,
        nonce: BigNumberish,
        chainId: BigNumberish
    ): Promise<string> {
        const message = PaymentClientForUser.getLoyaltyNewPaymentMessage(
            await signer.getAddress(),
            paymentId,
            purchaseId,
            amount,
            currency,
            shopId,
            nonce,
            chainId
        );
        return signer.signMessage(message);
    }

    public async getTemporaryAccount(): Promise<string> {
        const client = new HTTPClient({});
        const account = this.userWallet.address;
        const nonce = await this.getLedgerNonceOf(account);
        const message = CommonUtils.getAccountMessage(account, nonce, await this.getChainId());
        const signature = await CommonUtils.signMessage(this.userWallet, message);
        const response = await client.post(
            URI(this.endpoints.relay).directory("/v1/payment/account/temporary").toString(),
            {
                account,
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.temporaryAccount;
    }

    public async approveNewPayment(
        paymentId: string,
        purchaseId: string,
        amount: BigNumber,
        currency: string,
        shopId: string,
        approval: boolean
    ): Promise<IPaymentTaskItemShort> {
        const account: string = this.userWallet.address;
        const nonce = await this.getLedgerNonceOf(account);
        const signature = await PaymentClientForUser.signLoyaltyNewPayment(
            this.userWallet,
            paymentId,
            purchaseId,
            amount,
            currency,
            shopId,
            nonce,
            await this.getChainId()
        );
        const client = new HTTPClient({});
        const response = await client.post(URI(this.endpoints.relay).directory("/v1/payment/new/approval").toString(), {
            paymentId,
            approval,
            signature,
        });
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return {
            paymentId: response.data.data.paymentId,
            purchaseId: response.data.data.purchaseId,
            amount: BigNumber.from(response.data.data.amount),
            currency: response.data.data.currency,
            shopId: response.data.data.shopId,
            account: response.data.data.account,
            terminalId: response.data.data.terminalId,
            paymentStatus: Number(response.data.data.paymentStatus),
        };
    }
}
