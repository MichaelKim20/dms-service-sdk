import { BOACoin, NetWorkType, ProviderClient } from "../src";

import * as assert from "assert";

import { BigNumber } from "@ethersproject/bignumber";
import { AddressZero } from "@ethersproject/constants";

describe("Test of ProviderClient", function () {
    this.timeout(1000 * 60 * 5);
    let providerClient: ProviderClient;
    let agentClient: ProviderClient;
    const network: NetWorkType = NetWorkType.testnet;
    const AccessKeys: Map<number, string> = new Map([
        [NetWorkType.testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276"],
        [NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342"],
    ]);

    before(() => {
        providerClient = new ProviderClient(
            network,
            "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c"
        );
        agentClient = new ProviderClient(network, "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab");
    });

    it("Check Provider", async () => {
        const isProvider = await providerClient.isProvider(providerClient.address);
        assert.ok(isProvider);
    });

    it("Test getBalanceAccount", async () => {
        const res = await providerClient.getBalanceAccount(providerClient.address);
        assert.ok(res.point.balance.gt(BigNumber.from(0)));
        assert.ok(res.token.balance.gt(BigNumber.from(0)));
    });

    it("Test getBalancePhone", async () => {
        const res = await providerClient.getBalancePhone("+82 10-1000-2000");
        assert.ok(res.point.balance.gt(BigNumber.from(0)));
    });

    it("Clear agent", async () => {
        await providerClient.setAgent(AddressZero);
        assert.deepStrictEqual(await providerClient.getAgent(), AddressZero);
    });

    it("Provider to Address", async () => {
        const isProvider = await providerClient.isProvider(providerClient.address);
        const receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
        if (isProvider) {
            const res1 = await providerClient.getBalanceAccount(receiver);
            const oldBalance = res1.point.balance;

            const amount = BOACoin.make(100).value;
            await providerClient.provideToAddress(providerClient.address, receiver, amount);
            const res2 = await providerClient.getBalanceAccount(receiver);

            assert.deepStrictEqual(res2.point.balance, oldBalance.add(amount));
        }
    });

    it("Provider to Phone", async () => {
        const isProvider = await providerClient.isProvider(providerClient.address);
        const phoneNumber = "+82 10-9000-5000";
        if (isProvider) {
            const res1 = await providerClient.getBalancePhone(phoneNumber);
            const oldBalance = res1.point.balance;

            const amount = BOACoin.make(100).value;
            await providerClient.provideToPhone(providerClient.address, phoneNumber, amount);

            const res2 = await providerClient.getBalancePhone(phoneNumber);
            assert.deepStrictEqual(res2.point.balance, oldBalance.add(amount));
        }
    });

    it("Set agent", async () => {
        await providerClient.setAgent(agentClient.address);
        assert.deepStrictEqual(await providerClient.getAgent(), agentClient.address);
    });

    it("Provider to Address by agent", async () => {
        const isProvider = await providerClient.isProvider(providerClient.address);
        const receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
        if (isProvider) {
            const res1 = await providerClient.getBalanceAccount(receiver);
            const oldBalance = res1.point.balance;

            const amount = BOACoin.make(100).value;
            await agentClient.provideToAddress(providerClient.address, receiver, amount);

            const res2 = await providerClient.getBalanceAccount(receiver);
            assert.deepStrictEqual(res2.point.balance, oldBalance.add(amount));
        }
    });

    it("Provider to Phone by agent", async () => {
        const isProvider = await providerClient.isProvider(providerClient.address);
        const phoneNumber = "+82 10-9000-5000";
        if (isProvider) {
            const res1 = await providerClient.getBalancePhone(phoneNumber);
            const oldBalance = res1.point.balance;

            const amount = BOACoin.make(100).value;
            await agentClient.provideToPhone(providerClient.address, phoneNumber, amount);

            const res2 = await providerClient.getBalancePhone(phoneNumber);
            assert.deepStrictEqual(res2.point.balance, oldBalance.add(amount));
        }
    });
});
