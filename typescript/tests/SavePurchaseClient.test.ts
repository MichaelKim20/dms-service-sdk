import { BOACoin, CommonUtils, NetWorkType, SavePurchaseClient } from "../src";

import * as assert from "assert";

import { BigNumber } from "@ethersproject/bignumber";
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

describe("Test of ProviderClient", function () {
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

    const shops: IShopData[] = JSON.parse(fs.readFileSync("./tests/data/shops.json", "utf8"));
    const users: IUserData[] = JSON.parse(fs.readFileSync("./tests/data/users.json", "utf8"));
    let savePurchaseClient: SavePurchaseClient;
    const privateKeyOfCollector = AccessKeys.get(network) || "";
    const addressOfAsset = AssetAddresses.get(network) || "";
    const shopId = shops[0].shopId;
    const userAccount = users[0].address;
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
