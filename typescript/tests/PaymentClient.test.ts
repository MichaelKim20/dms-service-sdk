import {
    BOACoin,
    CommonUtils,
    IPaymentTaskItem,
    IShopTaskItem,
    ITaskEventListener,
    NetWorkType,
    PaymentClient,
    TaskEventCollector,
} from "../src";

import { PaymentClientForShop } from "./helper/PaymentClientForShop";
import { PaymentClientForUser } from "./helper/PaymentClientForUser";

import * as assert from "assert";
import fs from "fs";

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

class PaymentEventListener implements ITaskEventListener {
    public onNewPaymentEvent(type: string, code: number, message: string, sequence: bigint, data: IPaymentTaskItem) {
        console.log(`type: ${type.toString()}`);
        console.log(`code: ${code.toString()}`);
        console.log(`message: ${message}`);
        console.log(`sequence: ${sequence.toString()}`);
        console.log(`data: ${JSON.stringify(data)}`);
    }
    public onNewShopEvent(type: string, code: number, message: string, sequence: bigint, data: IShopTaskItem) {
        console.log(`type: ${type.toString()}`);
        console.log(`code: ${code.toString()}`);
        console.log(`message: ${message}`);
        console.log(`sequence: ${sequence.toString()}`);
        console.log(`data: ${JSON.stringify(data)}`);
    }
}

describe("Test of PaymentClient", function () {
    this.timeout(1000 * 60 * 5);
    let paymentClient: PaymentClient;
    let userClient: PaymentClientForUser;
    let shopClient: PaymentClientForShop;
    let eventCollector: TaskEventCollector;
    const network: NetWorkType = NetWorkType.acc_testnet;
    const AccessKeys: Map<number, string> = new Map([
        [NetWorkType.kios_testnet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee"],
        [NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276"],
        [NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342"],
    ]);
    const shops: IShopData[] = JSON.parse(fs.readFileSync("./tests/data/shops.json", "utf8"));
    const users: IUserData[] = JSON.parse(fs.readFileSync("./tests/data/users.json", "utf8"));

    before("Create Client for Payment", async () => {
        const privateKeyForPayment = AccessKeys.get(network) || "";
        paymentClient = new PaymentClient(network, privateKeyForPayment);
        userClient = new PaymentClientForUser(network, users[0].privateKey);
        shopClient = new PaymentClientForShop(network, shops[0].privateKey, shops[0].shopId);
        eventCollector = new TaskEventCollector(paymentClient, new PaymentEventListener());
    });

    before("Start Payment Client", async () => {
        await eventCollector.start();
    });

    after("Stop Payment Client", async () => {
        await eventCollector.stop();
    });

    const terminalID: string = "POS001";
    let temporaryAccount: string = "";
    let paymentItem: IPaymentTaskItem;

    it("Temporary Account", async () => {
        console.log(`[1. Begin - Temporary Account] temporaryAccount`);
        temporaryAccount = await userClient.getTemporaryAccount();
        console.log(`[1. End - Temporary Account] temporaryAccount: ${temporaryAccount}`);
    });

    it("Open New Payment", async () => {
        console.log(`[2. Begin - Open New Payment] paymentId`);
        const purchaseId = getPurchaseId();
        paymentItem = await paymentClient.openNewPayment(
            purchaseId,
            temporaryAccount,
            BOACoin.make(1000).value,
            "php",
            shopClient.getShopId(),
            terminalID
        );
        assert.deepStrictEqual(paymentItem.purchaseId, purchaseId);
        assert.deepStrictEqual(paymentItem.account, userClient.address);
        console.log(`[2. End - Open New Payment] paymentId: ${paymentItem.paymentId}`);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(2000);
    });

    it("Approval New Payment", async () => {
        console.log(`[3. Begin - Approval New Payment]`);
        const res = await userClient.approveNewPayment(
            paymentItem.paymentId,
            paymentItem.purchaseId,
            paymentItem.amount,
            paymentItem.currency,
            paymentItem.shopId,
            true
        );
        assert.deepStrictEqual(res.paymentId, paymentItem.paymentId);
        console.log(`[3. End - Approval New Payment]`);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(2000);
    });

    it("Close New Payment", async () => {
        console.log(`[4. Begin - Close New Payment]`);
        const res = await paymentClient.closeNewPayment(paymentItem.paymentId, true);
        assert.deepStrictEqual(res.paymentId, paymentItem.paymentId);
        console.log(`[4. End - Close New Payment]`);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(2000);
    });

    it("Open Cancel Payment", async () => {
        console.log(`[5. Begin - Open Cancel Payment]`);
        const res = await paymentClient.openCancelPayment(paymentItem.paymentId, terminalID);
        assert.deepStrictEqual(res.paymentId, paymentItem.paymentId);
        console.log(`[5. End - Open Cancel Payment]`);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(2000);
    });

    it("Approval Cancel Payment", async () => {
        console.log(`[6. Begin - Approval Cancel Payment]`);
        const res = await shopClient.approveCancelPayment(paymentItem.paymentId, paymentItem.purchaseId, true);
        assert.deepStrictEqual(res.paymentId, paymentItem.paymentId);
        console.log(`[6. End - Approval Cancel Payment]`);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(2000);
    });

    it("Close Cancel Payment", async () => {
        console.log(`[7. Begin - Close Cancel Payment]`);
        const res = await paymentClient.closeCancelPayment(paymentItem.paymentId, true);
        assert.deepStrictEqual(res.paymentId, paymentItem.paymentId);
        console.log(`[7. End - Close Cancel Payment]`);
    });

    it("Waiting...", async () => {
        await CommonUtils.delay(2000);
    });
});
