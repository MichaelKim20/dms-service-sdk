import { HTTPClient } from "../network/HTTPClient";
import { NetWorkType } from "../types";
import { CommonUtils } from "../utils/CommonUtils";
import { Client } from "./Client";

import { BigNumber } from "@ethersproject/bignumber";
import { Wallet } from "@ethersproject/wallet";

import URI from "urijs";

/**
 * The client that is needed to provide a point to the user.
 * First, you must obtain permission from the loyalty system and register the address of the wallet to be used in the loyalty system.
 */
export class ProviderClient extends Client {
    /**
     * Message repeater's wallet for providing
     */
    private readonly wallet: Wallet;

    /**
     * Constructor
     * @param network Type of network (mainnet, testnet, localhost)
     * @param privateKey The private key used in the providing
     */
    constructor(network: NetWorkType, privateKey: string) {
        super(network);
        this.wallet = new Wallet(privateKey);
    }

    public get address(): string {
        return this.wallet.address;
    }

    /**
     * Check if the `account` can provide points
     * @param account Wallet address
     */
    public async isProvider(account: string): Promise<boolean> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/provider/status/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.enable;
    }

    /**
     * Register the address of the assistant who directly delivers points for the registered wallet(this.wallet).
     * The assistant's wallet can be registered and used on the server.
     * The assistant does not have the authority to deposit and withdraw, only has the authority to provide points.
     * @param agent
     */
    public async setAgent(agent: string): Promise<string> {
        const client = new HTTPClient({});
        const nonce = await this.getLedgerNonceOf(this.wallet.address);
        const message = CommonUtils.getRegisterAgentMessage(this.wallet.address, agent, nonce, await this.getChainId());
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await client.post(
            URI(this.endpoints.relay).directory("/v1/provider/assistant/register").toString(),
            {
                provider: this.wallet.address,
                assistant: agent,
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.txHash;
    }

    /**
     * Provide the agent's address for the registered wallet(this.wallet)
     * @param provider Provider's wallet address
     */
    public async getAgent(provider?: string): Promise<string> {
        if (provider === undefined) provider = this.wallet.address;
        const client = new HTTPClient({});
        const response = await client.get(
            URI(this.endpoints.relay).directory("/v1/provider/assistant/").filename(provider).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.assistant;
    }

    /**
     * Points are provided to the specified address.
     * Registered wallets are used for signatures. Registered wallet(this.wallet) may be providers or helpers.
     * @param provider - wallet address of the resource provider
     * @param receiver - wallet address of the person who will receive the points
     * @param amount - amount of points
     */
    public async provideToAddress(provider: string, receiver: string, amount: BigNumber): Promise<string> {
        const client = new HTTPClient({});
        const nonce = await this.getLedgerNonceOf(this.wallet.address);
        const chainId = await this.getChainId();
        const message = CommonUtils.getProvidePointToAddressMessage(provider, receiver, amount, nonce, chainId);
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await client.post(
            URI(this.endpoints.relay).directory("/v1/provider/send/account").toString(),
            {
                provider,
                receiver,
                amount: amount.toString(),
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.txHash;
    }

    /**
     * Points are provided to the specified phone number.
     * Registered wallets are used for signatures. Registered wallet(this.wallet) may be providers or helpers.
     * @param provider - wallet address of the resource provider
     * @param receiver - phone number of the person who will receive the points
     * @param amount - amount of points
     */
    public async provideToPhone(provider: string, receiver: string, amount: BigNumber): Promise<string> {
        const client = new HTTPClient({});
        const nonce = await this.getLedgerNonceOf(this.wallet.address);
        const chainId = await this.getChainId();
        const phoneHash = CommonUtils.getPhoneHash(CommonUtils.getInternationalPhoneNumber(receiver));
        const message = CommonUtils.getProvidePointToPhoneMessage(provider, phoneHash, amount, nonce, chainId);
        const signature = await CommonUtils.signMessage(this.wallet, message);
        const response = await client.post(
            URI(this.endpoints.relay).directory("/v1/provider/send/phoneHash").toString(),
            {
                provider,
                receiver: phoneHash,
                amount: amount.toString(),
                signature,
            }
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return response.data.data.txHash;
    }
}
