import { BigNumber } from "@ethersproject/bignumber";
import { CommonUtils, NetWorkType } from "../../src";
import { Client } from "../../src/client/Client";
import { HTTPClient } from "../../src/network/HTTPClient";

import { Wallet } from "@ethersproject/wallet";

import URI from "urijs";
import { ShopData, ShopRefundableData } from "../../src/types";

/**
 *
 */
export class SettlementClientForShop extends Client {
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

    public async getSettlementManager(): Promise<string> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/shop/settlement/manager/get").filename(this.shopId).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.length;
    }

    public async setSettlementManager(managerId: string): Promise<string> {
        const agent = new HTTPClient({});
        const nonce = await this.getShopNonceOf(this.wallet.address);
        const message = CommonUtils.getSetSettlementManagerMessage(
            this.shopId,
            managerId,
            nonce,
            await this.getChainId()
        );
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await agent.post(
            URI(this.endpoints.relay).directory("/v1/shop/settlement/manager/").filename("set").toString(),
            {
                shopId: this.shopId,
                account: this.wallet.address,
                managerId,
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.length;
    }

    public async removeSettlementManager(): Promise<string> {
        const agent = new HTTPClient({});
        const nonce = await this.getShopNonceOf(this.wallet.address);
        const message = CommonUtils.getRemoveSettlementManagerMessage(this.shopId, nonce, await this.getChainId());
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await agent.post(
            URI(this.endpoints.relay).directory("/v1/shop/settlement/manager/").filename("remove").toString(),
            {
                shopId: this.shopId,
                account: this.wallet.address,
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.length;
    }

    public async setAgentOfRefund(agent: string): Promise<string> {
        const client = new HTTPClient({});
        const nonce = await this.getLedgerNonceOf(this.wallet.address);
        const message = CommonUtils.getRegisterAgentMessage(this.wallet.address, agent, nonce, await this.getChainId());
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await client.post(URI(this.endpoints.relay).directory("/v1/agent/refund").toString(), {
            account: this.wallet.address,
            agent,
            signature,
        });
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.txHash;
    }

    public async getAgentOfRefund(account?: string): Promise<string> {
        if (account === undefined) account = this.wallet.address;
        const client = new HTTPClient({});
        const response = await client.get(
            URI(this.endpoints.relay).directory("/v1/agent/refund/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.agent;
    }

    public async setAgentOfWithdrawal(agent: string): Promise<string> {
        const client = new HTTPClient({});
        const nonce = await this.getLedgerNonceOf(this.wallet.address);
        const message = CommonUtils.getRegisterAgentMessage(this.wallet.address, agent, nonce, await this.getChainId());
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await client.post(URI(this.endpoints.relay).directory("/v1/agent/withdrawal").toString(), {
            account: this.wallet.address,
            agent,
            signature,
        });
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.txHash;
    }

    public async getAgentOfWithdrawal(account?: string): Promise<string> {
        if (account === undefined) account = this.wallet.address;
        const client = new HTTPClient({});
        const response = await client.get(
            URI(this.endpoints.relay).directory("/v1/agent/withdrawal/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return response.data.data.agent;
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
}
