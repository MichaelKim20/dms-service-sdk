import { BigNumber } from "@ethersproject/bignumber";
import { HTTPClient } from "../network/HTTPClient";
import { NetWorkType, ShopData, ShopRefundableData } from "../types";
import { CommonUtils } from "../utils/CommonUtils";
import { Client } from "./Client";

import { Wallet } from "@ethersproject/wallet";

import URI from "urijs";

/**
 *
 */
export class SettlementClient extends Client {
    /**
     * Settlement manager or agent's wallet
     */
    private readonly _wallet: Wallet;
    /**
     * Settlement Manager's shop ID
     */
    private readonly _shopId: string;

    /**
     * Constructor
     * @param network Type of network (mainnet, testnet, localhost)
     * @param privateKey The private key of settlement manager or agent's wallet
     * @param shopId Settlement Manager's shop ID
     */
    constructor(network: NetWorkType, privateKey: string, shopId: string) {
        super(network);
        this._wallet = new Wallet(privateKey);
        this._shopId = shopId;
    }

    public get wallet(): Wallet {
        return this._wallet;
    }

    public get address(): string {
        return this._wallet.address;
    }

    public get shopId(): string {
        return this._shopId;
    }

    public async getSettlementClientLength(): Promise<number> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/shop/settlement/client/length/").filename(this.shopId).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.length;
    }

    public async getSettlementClientList(startIndex: number, endIndex: number): Promise<string[]> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay)
                .directory("/v1/shop/settlement/client/list/")
                .filename(this.shopId)
                .addQuery("startIndex", startIndex)
                .addQuery("endIndex", endIndex)
                .toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.clients;
    }

    public async collectSettlementAmountMultiClient(clients: string[]): Promise<string> {
        const client = new HTTPClient({});
        const nonce = await this.getShopNonceOf(this.wallet.address);
        const chainId = await this.getChainId();
        const message = CommonUtils.getCollectSettlementAmountMultiClientMessage(this.shopId, clients, nonce, chainId);
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await client.post(
            URI(this.endpoints.relay).directory("/v1/shop/settlement/collect").toString(),
            {
                shopId: this.shopId,
                account: this.wallet.address,
                clients: clients.join(","),
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.txHash;
    }

    public async getShopInfo(): Promise<ShopData> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/shop/info/").filename(this.shopId).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        const shopInfo = response.data.data;
        const shopData: ShopData = {
            shopId: shopInfo.shopId,
            name: shopInfo.name,
            currency: shopInfo.currency,
            account: shopInfo.account,
            delegator: shopInfo.delegator,
            providedAmount: BigNumber.from(shopInfo.providedAmount),
            usedAmount: BigNumber.from(shopInfo.usedAmount),
            collectedAmount: BigNumber.from(shopInfo.collectedAmount),
            settledAmount: BigNumber.from(0),
            refundedAmount: BigNumber.from(shopInfo.refundedAmount),
            status: shopInfo.status,
        };

        shopData.settledAmount = shopData.collectedAmount.add(shopData.usedAmount).gt(shopData.providedAmount)
            ? shopData.collectedAmount.add(shopData.usedAmount).sub(shopData.providedAmount)
            : BigNumber.from(0);

        return shopData;
    }

    public async getAccountOfShopOwner(): Promise<string> {
        const info = await this.getShopInfo();
        return info.account;
    }

    public async getRefundable(): Promise<ShopRefundableData> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/shop/refundable/").filename(this.shopId).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return {
            refundableAmount: BigNumber.from(response.data.data.refundableAmount),
            refundableToken: BigNumber.from(response.data.data.refundableToken),
        };
    }

    public async refund(amount: BigNumber): Promise<string> {
        const account = this.wallet.address;
        const adjustedAmount = CommonUtils.zeroGWEI(amount);
        const nonce = await this.getShopNonceOf(account);
        const chainId = await this.getChainId();
        const message = CommonUtils.getShopRefundMessage(this.shopId, adjustedAmount, nonce, chainId);
        const signature = await CommonUtils.signMessage(this.wallet, message);

        const param = {
            shopId: this.shopId,
            account,
            amount: adjustedAmount.toString(),
            signature,
        };

        const agent = new HTTPClient({});
        const response = await agent.post(URI(this.endpoints.relay).directory("/v1/shop/refund").toString(), param);
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.txHash;
    }

    public async withdraw(amount: BigNumber): Promise<string> {
        const account = await this.getAccountOfShopOwner();
        const chainInfo = await this.getChainInfoOfSideChain();
        const adjustedAmount = CommonUtils.zeroGWEI(amount);

        const nonce = await this.getLedgerNonceOf(this.wallet.address);
        const expiry = CommonUtils.getTimeStamp() + 1800;
        const message = CommonUtils.getTransferMessage(
            chainInfo.network.chainId,
            chainInfo.contract.token,
            account,
            chainInfo.contract.loyaltyBridge,
            adjustedAmount,
            nonce,
            expiry
        );
        const signature = await CommonUtils.signMessage(this.wallet, message);

        const param = {
            account,
            amount: adjustedAmount.toString(),
            expiry,
            signature,
        };

        const client = new HTTPClient({});
        const response = await client.post(
            URI(this.endpoints.relay).directory("/v1/ledger/withdraw_via_bridge").toString(),
            param
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.txHash;
    }
}
