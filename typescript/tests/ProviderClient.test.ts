import { BOACoin, NetWorkType, ProviderClient } from "../src";

import * as assert from "assert";

import { BigNumber } from "@ethersproject/bignumber";
import { AddressZero } from "@ethersproject/constants";
import fs from "fs";

interface IUserData {
    phone: string;
    address: string;
    privateKey: string;
}

describe("Test of ProviderClient", function () {
    this.timeout(1000 * 60 * 5);
    let providerClient: ProviderClient;
    let agentClient: ProviderClient;
    const network: NetWorkType = NetWorkType.acc_testnet;

    const users: IUserData[] = JSON.parse(fs.readFileSync("./tests/data/users.json", "utf8"));

    before(() => {
        providerClient = new ProviderClient(network, users[0].privateKey);
        agentClient = new ProviderClient(network, users[1].privateKey);
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

    it("Convert", async () => {
        const amount = BOACoin.make(1).value;
        const amount2 = await agentClient.convert(amount, "usd", "point");
        console.log(amount2.toString());
    });
});
