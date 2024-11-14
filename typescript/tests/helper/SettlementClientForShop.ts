import { CommonUtils } from "../../src";
import { SettlementClient } from "../../src/client/SettlementClient";
import { HTTPClient } from "../../src/network/HTTPClient";

import URI from "urijs";

/**
 *
 */
export class SettlementClientForShop extends SettlementClient {
    public async getSettlementManager(): Promise<string> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/shop/settlement/manager/get").filename(this.shopId).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.managerId;
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

        return response.data.data.txHash;
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
}
