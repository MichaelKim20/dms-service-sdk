import { BOACoin, CommonUtils, NetWorkType, SavePurchaseClient } from "../src";

import * as assert from "assert";

import { BigNumber } from "@ethersproject/bignumber";

let _purchaseId = 0;

function getPurchaseId(): string {
    const randomIdx = Math.floor(Math.random() * 1000);
    const res = "P" + _purchaseId.toString().padStart(10, "0") + randomIdx.toString().padStart(4, "0");
    _purchaseId++;
    return res;
}

describe("Test of ProviderClient", function () {
    this.timeout(1000 * 60 * 5);
    const network: NetWorkType = NetWorkType.localhost;
    const AccessKeys: Map<number, string> = new Map([
        [NetWorkType.testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276"],
        [NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342"],
    ]);
    const AssetAddresses: Map<number, string> = new Map([
        [NetWorkType.testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714"],
        [NetWorkType.localhost, "0x4501F7aF010Cef3DcEaAfbc7Bfb2B39dE57df54d"],
    ]);

    let savePurchaseClient: SavePurchaseClient;
    const privateKeyOfCollector = AccessKeys.get(network) || "";
    const addressOfAsset = AssetAddresses.get(network) || "";
    const shopId = "0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874";
    const userAccount = "0x64D111eA9763c93a003cef491941A011B8df5a49";
    const userPhone = "";
    let balance0: BigNumber;
    let balance1: BigNumber;
    let balance2: BigNumber;

    before(() => {
        savePurchaseClient = new SavePurchaseClient(network, privateKeyOfCollector, addressOfAsset);
    });

    it("Waiting...", async () => {
        const res = await savePurchaseClient.getBalanceAccount(userAccount);
        balance0 = res.point.balance;
    });

    it("Save New Purchase 1", async () => {
        await savePurchaseClient.saveNewPurchase(
            getPurchaseId(),
            CommonUtils.getTimeStampBigInt(),
            0n,
            10_000,
            10_000,
            "php",
            shopId,
            userAccount,
            userPhone,
            [
                {
                    productId: "2020051310000000",
                    amount: 10_000,
                    providePercent: 10,
                },
            ]
        );
    });

    it("Check...", async () => {
        const t1 = CommonUtils.getTimeStamp();
        while (true) {
            const res1 = await savePurchaseClient.getBalanceAccount(userAccount);
            balance1 = res1.point.balance;
            if (balance1.toString() === balance0.add(BOACoin.make(1000).value).toString()) break;
            else if (CommonUtils.getTimeStamp() - t1 > 120) break;
            await CommonUtils.delay(1000);
        }
        const res2 = await savePurchaseClient.getBalanceAccount(userAccount);
        balance1 = res2.point.balance;
        assert.deepStrictEqual(balance1.toString(), balance0.add(BOACoin.make(1000).value).toString());
    });

    it("Save New Purchase and Cancel", async () => {
        const purchaseId = getPurchaseId();
        const timestamp = CommonUtils.getTimeStampBigInt();
        await savePurchaseClient.saveNewPurchase(
            purchaseId,
            timestamp,
            60n,
            10_000,
            10_000,
            "php",
            shopId,
            userAccount,
            userPhone,
            [
                {
                    productId: "2020051310000000",
                    amount: 10_000,
                    providePercent: 10,
                },
            ]
        );
        await CommonUtils.delay(1000);
        await savePurchaseClient.saveCancelPurchase(purchaseId, timestamp, 60n);
    });

    it("Waiting 2...", async () => {
        await CommonUtils.delay(50000);
    });

    it("Check 2...", async () => {
        const res = await savePurchaseClient.getBalanceAccount(userAccount);
        balance2 = res.point.balance;
        assert.deepStrictEqual(balance1.toString(), balance2.toString());
    });
});
