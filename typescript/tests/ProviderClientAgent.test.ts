import { BOACoin, NetWorkType, ProviderClient } from "../src";

import * as assert from "assert";
import fs from "fs";

interface IUserData {
    phone: string;
    address: string;
    privateKey: string;
}

describe("Test of ProviderClient", function () {
    this.timeout(1000 * 60 * 5);
    const users: IUserData[] = JSON.parse(fs.readFileSync("./tests/data/users.json", "utf8"));
    let agentClient: ProviderClient;
    const providerAddress = users[0].address;
    const network: NetWorkType = NetWorkType.acc_testnet;

    before(() => {
        agentClient = new ProviderClient(network, users[1].privateKey);
    });

    it("Provider to Address by agent", async () => {
        const isProvider = await agentClient.isProvider(providerAddress);
        const receiver = "0xCf44157e9df307c90FC7762933Ac1e2921e8b39E";
        if (isProvider) {
            const res1 = await agentClient.getBalanceAccount(receiver);
            const oldBalance = res1.point.balance;

            const amount = BOACoin.make(200).value;
            await agentClient.provideToAddress(providerAddress, receiver, amount);

            const res2 = await agentClient.getBalanceAccount(receiver);
            assert.deepStrictEqual(res2.point.balance, oldBalance.add(amount));
        } else {
            console.log("Not Provider");
        }
    });

    it("Provider to Phone by agent", async () => {
        const isProvider = await agentClient.isProvider(providerAddress);
        const phoneNumber = "+82 10-9000-5000";
        if (isProvider) {
            const res1 = await agentClient.getBalancePhone(phoneNumber);
            const oldBalance = res1.point.balance;

            const amount = BOACoin.make(100).value;
            await agentClient.provideToPhone(providerAddress, phoneNumber, amount);

            const res2 = await agentClient.getBalancePhone(phoneNumber);
            assert.deepStrictEqual(res2.point.balance, oldBalance.add(amount));
        } else {
            console.log("Not Provider");
        }
    });
});
