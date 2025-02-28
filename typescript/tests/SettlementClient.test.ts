import { AddressZero, HashZero } from "@ethersproject/constants";
import { expect } from "chai";
import fs from "fs";
import { BOACoin, CommonUtils, IPaymentTaskItem, NetWorkType, PaymentClient, SavePurchaseClient } from "../src";

import * as assert from "assert";

import { BigNumber } from "@ethersproject/bignumber";
import { SettlementClient } from "../src/client/SettlementClient";
import { PaymentClientForShop } from "./helper/PaymentClientForShop";
import { PaymentClientForUser } from "./helper/PaymentClientForUser";
import { SettlementClientForShop } from "./helper/SettlementClientForShop";

interface IShopData {
    shopId: string;
    name: string;
    currency: string;
    address: string;
    privateKey: string;
}

interface IUserData {
    phone: string;
    address: string;
    privateKey: string;
}

let _purchaseId = 0;
function getPurchaseId(): string {
    const randomIdx = Math.floor(Math.random() * 1000);
    const res = "P" + _purchaseId.toString().padStart(10, "0") + randomIdx.toString().padStart(4, "0");
    _purchaseId++;
    return res;
}

describe("Test of SettlementClient - Not using agent", function () {
    this.timeout(1000 * 60 * 5);
    const network: NetWorkType = NetWorkType.acc_testnet;
    const AccessKeys: Map<number, string> = new Map([
        [NetWorkType.kios_testnet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee"],
        [NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276"],
        [NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342"],
    ]);
    const AssetAddresses: Map<number, string> = new Map([
        [NetWorkType.kios_testnet, "0x153f2340807370855092D04E0e0abe4f2b634240"],
        [NetWorkType.acc_testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714"],
        [NetWorkType.localhost, "0x4501F7aF010Cef3DcEaAfbc7Bfb2B39dE57df54d"],
    ]);

    let savePurchaseClient: SavePurchaseClient;
    before(() => {
        savePurchaseClient = new SavePurchaseClient(network, privateKeyOfCollector, addressOfAsset);
    });

    it("Check Previous Point Balance...", async () => {
        const res = await savePurchaseClient.getBalanceAccount(users[0].address);
        balance0 = res.point.balance;
    });

    it("Save New Purchase 1", async () => {
        await savePurchaseClient.saveNewPurchase(
            getPurchaseId(),
            CommonUtils.getTimeStampBigInt(),
            0n,
            100_000_000,
            100_000_000,
            "php",
            purchaseShopId,
            userAccount,
            "",
            [
                {
                    productId: "2020051310000000",
                    amount: 100_000_000,
                    providePercent: 10,
                },
            ]
        );
    });

    it("Check Point Balance...", async () => {
        const t1 = CommonUtils.getTimeStamp();
        while (true) {
            const res1 = await savePurchaseClient.getBalanceAccount(userAccount);
            balance1 = res1.point.balance;
            if (balance1.toString() === balance0.add(BOACoin.make(10_000_000).value).toString()) break;
            else if (CommonUtils.getTimeStamp() - t1 > 120) break;
            await CommonUtils.delay(1000);
        }
        const res2 = await savePurchaseClient.getBalanceAccount(userAccount);
        balance1 = res2.point.balance;
        assert.deepStrictEqual(balance1.toString(), balance0.add(BOACoin.make(10_000_000).value).toString());
    });

    let settlementClientForManager: SettlementClientForShop;
    let settlementClient: SettlementClient;
    const privateKeyOfCollector = AccessKeys.get(network) || "";
    const addressOfAsset = AssetAddresses.get(network) || "";
    const shops: IShopData[] = JSON.parse(fs.readFileSync("./tests/data/shops.json", "utf8"));
    const users: IUserData[] = JSON.parse(fs.readFileSync("./tests/data/users.json", "utf8"));
    const settlementClientForShop = shops.map((m) => new SettlementClientForShop(network, m.privateKey, m.shopId));
    let balance0: BigNumber;
    let balance1: BigNumber;

    const purchaseShopId = shops[5].shopId;
    const managerId = shops[6].shopId;
    const userAccount = users[0].address;

    before(async () => {
        settlementClient = new SettlementClient(network, shops[6].privateKey, shops[6].shopId);
        settlementClientForManager = new SettlementClientForShop(network, shops[6].privateKey, shops[6].shopId);
        await settlementClientForManager.setAgentOfRefund(AddressZero);
        await settlementClientForManager.setAgentOfWithdrawal(AddressZero);
    });

    it("Remove Manager...", async () => {
        for (const settlementClientFirShop of settlementClientForShop) {
            await settlementClientFirShop.removeSettlementManager();
        }
    });

    it("Set Manager...", async () => {
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const settlementClientFirShop = settlementClientForShop[shopIndex];
            await settlementClientFirShop.setSettlementManager(managerId);
        }
    });

    it("Check getSettlementClientLength.", async () => {
        expect(await settlementClient.getSettlementClientLength()).to.be.equal(6);
    });

    it("Check getSettlementClientList.", async () => {
        const ids: string[] = [];
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = shops[shopIndex];
            ids.push(shop.shopId);
        }
        expect(await settlementClient.getSettlementClientList(0, 6)).to.deep.equal(ids);
    });

    let paymentClient: PaymentClient;
    let userClient: PaymentClientForUser;
    let shopClient: PaymentClientForShop;
    let temporaryAccount: string = "";
    let paymentItem: IPaymentTaskItem;
    const terminalID: string = "POS001";

    before("Create Client for Payment", async () => {
        const privateKeyForPayment = AccessKeys.get(network) || "";
        paymentClient = new PaymentClient(network, privateKeyForPayment);
        userClient = new PaymentClientForUser(network, users[0].privateKey);
    });

    it("Use Point", async () => {
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = shops[shopIndex];

            shopClient = new PaymentClientForShop(network, shop.privateKey, shop.shopId);
            console.log(`[0. Shop ID - ${shopClient.getShopId()}`);

            console.log(`[1. Begin - Temporary Account] temporaryAccount`);
            temporaryAccount = await userClient.getTemporaryAccount();
            console.log(`[1. End - Temporary Account] temporaryAccount: ${temporaryAccount}`);

            console.log(`[2. Begin - Open New Payment] paymentId`);
            const purchaseId = getPurchaseId();
            paymentItem = await paymentClient.openNewPayment(
                purchaseId,
                temporaryAccount,
                BOACoin.make(100_000).value,
                "php",
                shopClient.getShopId(),
                terminalID
            );
            assert.deepStrictEqual(paymentItem.purchaseId, purchaseId);
            assert.deepStrictEqual(paymentItem.account, userClient.address);
            console.log(`[2. End - Open New Payment] paymentId: ${paymentItem.paymentId}`);

            console.log(`[3. Begin - Approval New Payment]`);
            const res1 = await userClient.approveNewPayment(
                paymentItem.paymentId,
                paymentItem.purchaseId,
                paymentItem.amount,
                paymentItem.currency,
                paymentItem.shopId,
                true
            );
            assert.deepStrictEqual(res1.paymentId, paymentItem.paymentId);
            console.log(`[3. End - Approval New Payment]`);

            await CommonUtils.delay(2000);

            console.log(`[4. Begin - Close New Payment]`);
            const res2 = await paymentClient.closeNewPayment(paymentItem.paymentId, true);
            assert.deepStrictEqual(res2.paymentId, paymentItem.paymentId);
            console.log(`[4. End - Close New Payment]`);
        }
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(3000);
    });

    it("Collect settlement amount", async () => {
        const ids: string[] = [];
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = shops[shopIndex];
            ids.push(shop.shopId);
        }
        await settlementClient.collectSettlementAmountMultiClient(ids);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(3000);
    });

    it("Check refundable amount...", async () => {
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const settlementClientFirShop = settlementClientForShop[shopIndex];
            const res = await settlementClientFirShop.getRefundable();
            expect(res.refundableAmount).to.deep.equal(BigNumber.from(0));
        }
    });

    it("Refund of manager", async () => {
        const refundableData = await settlementClient.getRefundable();
        const refundableAmount = refundableData.refundableAmount;
        const refundableToken = refundableData.refundableToken;

        const accountOfShop = await settlementClient.getAccountOfShopOwner();
        const res1 = await settlementClient.getBalanceAccount(accountOfShop);

        await settlementClient.refund(refundableAmount);

        const res2 = await settlementClient.getBalanceAccount(accountOfShop);

        expect(res2.token.balance).to.deep.equal(res1.token.balance.add(refundableToken));
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(3000);
    });

    it("Withdrawal", async () => {
        const chainInfo = await settlementClient.getChainInfoOfSideChain();
        const accountOfShop = await settlementClient.getAccountOfShopOwner();
        const res2 = await settlementClient.getBalanceAccount(accountOfShop);
        const balanceOfToken = res2.token.balance;
        const balanceMainChain1 = await settlementClient.getBalanceOfMainChainToken(accountOfShop);
        await settlementClient.withdraw(balanceOfToken);

        const t1 = CommonUtils.getTimeStamp();
        while (true) {
            const balanceMainChain2 = await settlementClient.getBalanceOfMainChainToken(accountOfShop);
            if (
                balanceMainChain2.toString() ===
                balanceMainChain1.add(balanceOfToken).sub(chainInfo.network.loyaltyBridgeFee).toString()
            )
                break;
            else if (CommonUtils.getTimeStamp() - t1 > 60) throw new Error("Timeout Withdrawal");
            await CommonUtils.delay(1000);
        }
    });
});

describe("Test of SettlementClient - Using agent", function () {
    this.timeout(1000 * 60 * 5);
    const network: NetWorkType = NetWorkType.acc_testnet;
    const AccessKeys: Map<number, string> = new Map([
        [NetWorkType.kios_testnet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee"],
        [NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276"],
        [NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342"],
    ]);
    const AssetAddresses: Map<number, string> = new Map([
        [NetWorkType.kios_testnet, "0x153f2340807370855092D04E0e0abe4f2b634240"],
        [NetWorkType.acc_testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714"],
        [NetWorkType.localhost, "0x4501F7aF010Cef3DcEaAfbc7Bfb2B39dE57df54d"],
    ]);

    let savePurchaseClient: SavePurchaseClient;
    before(() => {
        savePurchaseClient = new SavePurchaseClient(network, privateKeyOfCollector, addressOfAsset);
    });

    it("Check Previous Point Balance...", async () => {
        const res = await savePurchaseClient.getBalanceAccount(users[0].address);
        balance0 = res.point.balance;
    });

    it("Save New Purchase 1", async () => {
        await savePurchaseClient.saveNewPurchase(
            getPurchaseId(),
            CommonUtils.getTimeStampBigInt(),
            0n,
            100_000_000,
            100_000_000,
            "php",
            purchaseShopId,
            userAccount,
            "",
            [
                {
                    productId: "2020051310000000",
                    amount: 100_000_000,
                    providePercent: 10,
                },
            ]
        );
    });

    it("Check Point Balance...", async () => {
        const t1 = CommonUtils.getTimeStamp();
        while (true) {
            const res1 = await savePurchaseClient.getBalanceAccount(userAccount);
            balance1 = res1.point.balance;
            if (balance1.toString() === balance0.add(BOACoin.make(10_000_000).value).toString()) break;
            else if (CommonUtils.getTimeStamp() - t1 > 120) break;
            await CommonUtils.delay(1000);
        }
        const res2 = await savePurchaseClient.getBalanceAccount(userAccount);
        balance1 = res2.point.balance;
        assert.deepStrictEqual(balance1.toString(), balance0.add(BOACoin.make(10_000_000).value).toString());
    });

    let settlementClientForManager: SettlementClientForShop;
    let settlementClient: SettlementClient;
    let refundAgent: SettlementClient;
    let withdrawalAgent: SettlementClient;
    const privateKeyOfCollector = AccessKeys.get(network) || "";
    const addressOfAsset = AssetAddresses.get(network) || "";
    const shops: IShopData[] = JSON.parse(fs.readFileSync("./tests/data/shops.json", "utf8"));
    const users: IUserData[] = JSON.parse(fs.readFileSync("./tests/data/users.json", "utf8"));
    const settlementClientForShop = shops.map((m) => new SettlementClientForShop(network, m.privateKey, m.shopId));
    let balance0: BigNumber;
    let balance1: BigNumber;

    const purchaseShopId = shops[5].shopId;
    const managerId = shops[6].shopId;
    const userAccount = users[0].address;

    before(async () => {
        settlementClient = new SettlementClient(network, shops[6].privateKey, shops[6].shopId);
        refundAgent = new SettlementClient(network, users[1].privateKey, shops[6].shopId);
        withdrawalAgent = new SettlementClient(network, users[2].privateKey, shops[6].shopId);

        settlementClientForManager = new SettlementClientForShop(network, shops[6].privateKey, shops[6].shopId);
        await settlementClientForManager.setAgentOfRefund(refundAgent.address);
        await settlementClientForManager.setAgentOfWithdrawal(withdrawalAgent.address);
    });

    it("Remove Manager...", async () => {
        for (const settlementClientFirShop of settlementClientForShop) {
            await settlementClientFirShop.removeSettlementManager();
        }
    });

    it("Set Manager...", async () => {
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const settlementClientFirShop = settlementClientForShop[shopIndex];
            await settlementClientFirShop.setSettlementManager(managerId);
        }
    });

    it("Check getSettlementClientLength.", async () => {
        expect(await settlementClient.getSettlementClientLength()).to.be.equal(6);
    });

    it("Check getSettlementClientList.", async () => {
        const ids: string[] = [];
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = shops[shopIndex];
            ids.push(shop.shopId);
        }
        expect(await settlementClient.getSettlementClientList(0, 6)).to.deep.equal(ids);
    });

    let paymentClient: PaymentClient;
    let userClient: PaymentClientForUser;
    let shopClient: PaymentClientForShop;
    let temporaryAccount: string = "";
    let paymentItem: IPaymentTaskItem;
    const terminalID: string = "POS001";

    before("Create Client for Payment", async () => {
        const privateKeyForPayment = AccessKeys.get(network) || "";
        paymentClient = new PaymentClient(network, privateKeyForPayment);
        userClient = new PaymentClientForUser(network, users[0].privateKey);
    });

    it("Use Point", async () => {
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = shops[shopIndex];

            shopClient = new PaymentClientForShop(network, shop.privateKey, shop.shopId);
            console.log(`[0. Shop ID - ${shopClient.getShopId()}`);

            console.log(`[1. Begin - Temporary Account] temporaryAccount`);
            temporaryAccount = await userClient.getTemporaryAccount();
            console.log(`[1. End - Temporary Account] temporaryAccount: ${temporaryAccount}`);

            console.log(`[2. Begin - Open New Payment] paymentId`);
            const purchaseId = getPurchaseId();
            paymentItem = await paymentClient.openNewPayment(
                purchaseId,
                temporaryAccount,
                BOACoin.make(100_000).value,
                "php",
                shopClient.getShopId(),
                terminalID
            );
            assert.deepStrictEqual(paymentItem.purchaseId, purchaseId);
            assert.deepStrictEqual(paymentItem.account, userClient.address);
            console.log(`[2. End - Open New Payment] paymentId: ${paymentItem.paymentId}`);

            console.log(`[3. Begin - Approval New Payment]`);
            const res1 = await userClient.approveNewPayment(
                paymentItem.paymentId,
                paymentItem.purchaseId,
                paymentItem.amount,
                paymentItem.currency,
                paymentItem.shopId,
                true
            );
            assert.deepStrictEqual(res1.paymentId, paymentItem.paymentId);
            console.log(`[3. End - Approval New Payment]`);

            await CommonUtils.delay(2000);

            console.log(`[4. Begin - Close New Payment]`);
            const res2 = await paymentClient.closeNewPayment(paymentItem.paymentId, true);
            assert.deepStrictEqual(res2.paymentId, paymentItem.paymentId);
            console.log(`[4. End - Close New Payment]`);
        }
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(3000);
    });

    it("Collect settlement amount", async () => {
        const ids: string[] = [];
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = shops[shopIndex];
            ids.push(shop.shopId);
        }
        await refundAgent.collectSettlementAmountMultiClient(ids);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(3000);
    });

    it("Check refundable amount...", async () => {
        for (let shopIndex = 0; shopIndex < 6; shopIndex++) {
            const shop = settlementClientForShop[shopIndex];
            const res = await shop.getRefundable();
            expect(res.refundableAmount.toString()).to.deep.equal(BigNumber.from(0).toString());
        }
    });

    it("Refund of manager", async () => {
        const refundableData = await settlementClient.getRefundable();
        const refundableAmount = refundableData.refundableAmount;
        const refundableToken = refundableData.refundableToken;

        const accountOfShop = await settlementClient.getAccountOfShopOwner();
        const res1 = await settlementClient.getBalanceAccount(accountOfShop);

        await refundAgent.refund(refundableAmount);

        const res2 = await settlementClient.getBalanceAccount(accountOfShop);

        expect(res2.token.balance).to.deep.equal(res1.token.balance.add(refundableToken));
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(3000);
    });

    it("Withdrawal", async () => {
        const chainInfo = await settlementClient.getChainInfoOfSideChain();
        const accountOfShop = await settlementClient.getAccountOfShopOwner();
        const res2 = await settlementClient.getBalanceAccount(accountOfShop);

        const balanceOfToken = res2.token.balance;
        const balanceMainChain1 = await settlementClient.getBalanceOfMainChainToken(accountOfShop);
        await withdrawalAgent.withdraw(balanceOfToken);

        const t1 = CommonUtils.getTimeStamp();
        while (true) {
            const balanceMainChain2 = await settlementClient.getBalanceOfMainChainToken(accountOfShop);
            if (
                balanceMainChain2.toString() ===
                balanceMainChain1.add(balanceOfToken).sub(chainInfo.network.loyaltyBridgeFee).toString()
            )
                break;
            else if (CommonUtils.getTimeStamp() - t1 > 60) throw new Error("Timeout Withdrawal");
            await CommonUtils.delay(1000);
        }
    });
});
