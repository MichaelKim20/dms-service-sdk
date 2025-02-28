import { HTTPClient } from "../network/HTTPClient";
import { IChainInfo, IEndpoints, IUserBalance, NetWorkType } from "../types";

import URI from "urijs";

import { BigNumber } from "@ethersproject/bignumber";

/**
 * The client class of decentralized loyalty services
 */
export class Client {
    /**
     * Endpoint of API
     * @protected
     */
    protected endpoints: IEndpoints;

    /**
     * Chain ID of side chain
     * @private
     */
    private chainId: number = 0;

    /**
     * Constructor
     * @param network Type of network (acc_mainnet, acc_testnet, kios_mainnet, kios_testnet, localhost)
     */
    constructor(network: NetWorkType) {
        if (network === NetWorkType.localhost) {
            this.endpoints = {
                relay: "http://127.0.0.1:7070",
                save: "http://127.0.0.1:3030",
            };
        } else if (network === NetWorkType.acc_mainnet) {
            this.endpoints = {
                relay: "https://relay.main.acccoin.io",
                save: "https://save.main.acccoin.io",
            };
        } else if (network === NetWorkType.acc_testnet) {
            this.endpoints = {
                relay: "https://relay.test.acccoin.io",
                save: "https://save.test.acccoin.io",
            };
        } else if (network === NetWorkType.kios_mainnet) {
            this.endpoints = {
                relay: "https://relay.main.kioscoin.io",
                save: "https://save.main.kioscoin.io",
            };
        } else if (network === NetWorkType.kios_testnet) {
            this.endpoints = {
                relay: "https://relay.test.kioscoin.io",
                save: "https://save.test.kioscoin.io",
            };
        } else {
            this.endpoints = {
                relay: "https://relay.main.kioscoin.io",
                save: "https://save.main.kioscoin.io",
            };
        }
    }

    /**
     * Provide the ID of the chain
     */
    public async getChainId(): Promise<number> {
        if (this.chainId === 0) {
            const agent = new HTTPClient({});
            const response = await agent.get(URI(this.endpoints.relay).directory("/v1/chain/side/id").toString());

            if (response.data.code !== 0) {
                throw new Error(response.data.error?.message);
            }

            this.chainId = Number(response.data.data.chainId);
        }
        return this.chainId;
    }

    /**
     * Provide the user's points and token balance information
     * @param phoneNumber User's phone number
     */
    public async getBalancePhone(phoneNumber: string): Promise<IUserBalance> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/ledger/balance/phone/").filename(phoneNumber).toString()
        );

        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return {
            point: {
                balance: BigNumber.from(response.data.data.point.balance),
                value: BigNumber.from(response.data.data.point.value),
            },
            token: {
                balance: BigNumber.from(response.data.data.token.balance),
                value: BigNumber.from(response.data.data.token.value),
            },
        };
    }

    /**
     * Provide the user's points and token balance information
     * @param phoneHash User's phone number hash
     */
    public async getBalancePhoneHash(phoneHash: string): Promise<IUserBalance> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/ledger/balance/phoneHash/").filename(phoneHash).toString()
        );

        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return {
            point: {
                balance: BigNumber.from(response.data.data.point.balance),
                value: BigNumber.from(response.data.data.point.value),
            },
            token: {
                balance: BigNumber.from(response.data.data.token.balance),
                value: BigNumber.from(response.data.data.token.value),
            },
        };
    }

    /**
     * Provide the user's points and token balance information
     * @param account User's wallet address
     */
    public async getBalanceAccount(account: string): Promise<IUserBalance> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/ledger/balance/account/").filename(account).toString()
        );

        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }

        return {
            point: {
                balance: BigNumber.from(response.data.data.point.balance),
                value: BigNumber.from(response.data.data.point.value),
            },
            token: {
                balance: BigNumber.from(response.data.data.token.balance),
                value: BigNumber.from(response.data.data.token.value),
            },
        };
    }

    /**
     * Provide a nonce corresponding to the user's wallet address. It provides a nonce corresponding to the user's wallet address.
     * This ensures that the same signature is not repeated. And this value is recorded in Contract and automatically increases by 1.
     * @param account User's wallet address
     */
    public async getLedgerNonceOf(account: string): Promise<number> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/ledger/nonce/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return Number(response.data.data.nonce);
    }

    public async getShopNonceOf(account: string): Promise<number> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/shop/nonce/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return Number(response.data.data.nonce);
    }

    private mainChainInfo: IChainInfo | undefined = undefined;
    public async getChainInfoOfMainChain(): Promise<IChainInfo> {
        if (this.mainChainInfo !== undefined) return this.mainChainInfo;
        const agent = new HTTPClient({});
        const res = await agent.get(URI(this.endpoints.relay).directory("/v1/chain/main/info").toString());
        if (res.data.code !== 0) {
            throw new Error(res.data.error?.message);
        }
        this.mainChainInfo = {
            url: res.data.data.url,
            network: {
                name: res.data.data.network.name,
                chainId: res.data.data.network.chainId,
                ensAddress: res.data.data.network.ensAddress,
                chainTransferFee: BigNumber.from(res.data.data.network.chainTransferFee),
                chainBridgeFee: BigNumber.from(res.data.data.network.chainBridgeFee),
                loyaltyTransferFee: BigNumber.from(res.data.data.network.loyaltyTransferFee),
                loyaltyBridgeFee: BigNumber.from(res.data.data.network.loyaltyBridgeFee),
            },
            contract: {
                token: res.data.data.contract.token,
                chainBridge: res.data.data.contract.chainBridge,
                loyaltyBridge: res.data.data.contract.loyaltyBridge,
            },
        };
        return this.mainChainInfo;
    }

    /**
     * 메인체인의 토큰의 Nonce 를 제공한다.
     */
    public async getNonceOfMainChainToken(account: string): Promise<BigNumber> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/token/main/nonce/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return BigNumber.from(response.data.data.nonce);
    }

    /**
     * 메인체인의 토큰잔고를 제공한다.
     */
    public async getBalanceOfMainChainToken(account: string): Promise<BigNumber> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/token/main/balance/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return BigNumber.from(response.data.data.balance);
    }

    /**
     * 메인체인의 체인아이디를 제공한다.
     */
    public async getChainIdOfMainChain(): Promise<number> {
        const chainInfo = await this.getChainInfoOfMainChain();
        return Number(chainInfo.network.chainId);
    }

    private sideChainInfo: IChainInfo | undefined = undefined;
    public async getChainInfoOfSideChain(): Promise<IChainInfo> {
        if (this.sideChainInfo !== undefined) return this.sideChainInfo;
        const agent = new HTTPClient({});
        const res = await agent.get(URI(this.endpoints.relay).directory("/v1/chain/side/info").toString());
        if (res.data.code !== 0) {
            throw new Error(res.data.error?.message);
        }
        this.sideChainInfo = {
            url: res.data.data.url,
            network: {
                name: res.data.data.network.name,
                chainId: res.data.data.network.chainId,
                ensAddress: res.data.data.network.ensAddress,
                chainTransferFee: BigNumber.from(res.data.data.network.chainTransferFee),
                chainBridgeFee: BigNumber.from(res.data.data.network.chainBridgeFee),
                loyaltyTransferFee: BigNumber.from(res.data.data.network.loyaltyTransferFee),
                loyaltyBridgeFee: BigNumber.from(res.data.data.network.loyaltyBridgeFee),
            },
            contract: {
                token: res.data.data.contract.token,
                chainBridge: res.data.data.contract.chainBridge,
                loyaltyBridge: res.data.data.contract.loyaltyBridge,
            },
        };
        return this.sideChainInfo;
    }

    /**
     * 사이드체인의 체인아이디를 제공한다.
     */
    public async getChainIdOfSideChain(): Promise<number> {
        const chainInfo = await this.getChainInfoOfSideChain();
        return Number(chainInfo.network.chainId);
    }

    /**
     * 사이드체인의 토큰의 Nonce 를 제공한다.
     */
    public async getNonceOfSideChainToken(account: string): Promise<BigNumber> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/token/side/nonce/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return BigNumber.from(response.data.data.nonce);
    }

    /**
     * 사이드체인의 토큰잔고를 제공한다.
     */
    public async getBalanceOfSideChainToken(account: string): Promise<BigNumber> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay).directory("/v1/token/side/balance/").filename(account).toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return BigNumber.from(response.data.data.balance);
    }

    /**
     * 환률변환
     * @param amount
     * @param from
     * @param to
     */
    public async convert(amount: BigNumber, from: string, to: string): Promise<BigNumber> {
        const agent = new HTTPClient({});
        const response = await agent.get(
            URI(this.endpoints.relay)
                .directory("/v1/currency/convert")
                .addQuery("amount", amount.toString())
                .addQuery("from", from)
                .addQuery("to", to)
                .toString()
        );
        if (response.data.code !== 0) {
            throw new Error(response.data.error?.message);
        }
        return BigNumber.from(response.data.data.amount);
    }
}
