package org.kios.service.sdk.client;

import org.kios.service.sdk.data.NetWorkType;
import org.kios.service.sdk.data.ShopData;
import org.kios.service.sdk.data.UserBalance;
import org.kios.service.sdk.data.UserData;
import org.kios.service.sdk.data.payment.PaymentTaskItem;
import org.kios.service.sdk.data.payment.PaymentTaskItemShort;
import org.kios.service.sdk.data.purchase.PurchaseDetail;
import org.kios.service.sdk.data.purchase.ResponseSavePurchase;
import org.kios.service.sdk.data.settlement.ChainInfo;
import org.kios.service.sdk.data.settlement.ShopRefundableData;
import org.kios.service.sdk.utils.Amount;
import org.kios.service.sdk.utils.CommonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettlementClientUsingAgentTest {
    private final NetWorkType network = NetWorkType.acc_testnet;
    private Hashtable<NetWorkType, String> AccessKeys;
    private Hashtable<NetWorkType, String> AssetAddresses;
    private ArrayList<ShopData> shops;
    private ArrayList<UserData> users;
    private ArrayList<ShopData> activeShops;

    private String purchaseShopId;
    private String userAccount;

    private SettlementClient settlementClient;
    private SettlementClientForShop settlementClientForManager;

    private SettlementClient refundAgent;
    private SettlementClient withdrawalAgent;

    @Test
    void RunTest() {
        AccessKeys = new Hashtable<NetWorkType, String>();
        AssetAddresses = new Hashtable<NetWorkType, String>();

        AccessKeys.put(NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        AccessKeys.put(NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342");

        AssetAddresses.put(NetWorkType.acc_testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714");
        AssetAddresses.put(NetWorkType.localhost, "0x4501F7aF010Cef3DcEaAfbc7Bfb2B39dE57df54d");

        shops = new ArrayList<ShopData>();
        users = new ArrayList<UserData>();
        shops.add(new ShopData("0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874", "0xa237d68cbb66fd5f76e7b321156c46882546ad87d662dec8b82703ac31efbf0a"));
        shops.add(new ShopData("0x00015f59d6b480ff5a30044dcd7fe3b28c69b6d0d725ca469d1b685b57dfc105", "0x05152ad8d5b14d3f65539e0e42131bc72cbdd16c486cb215d60b7dc113ca1ebd"));
        shops.add(new ShopData("0x000108f12f827f0521be34e7563948dc778cb80f7498cebb57cb1a62840d96eb", "0xf4b8aa615834c57d1e4836c683c8d3460f8ff232667dc317f82844e674ee4f26"));
        shops.add(new ShopData("0x0001befa86be32da60a87a843bf3e63e77092040ee044f854e8d318d1eb18d20", "0xe58b3ae0e68a04996d6c13c9f9cb65b2d88ada662f28edd67db8c8e1ef45eed4"));
        shops.add(new ShopData("0x00013ecc54754b835d04ee5b4df7d0d0eb4e0eafc33ac8de4d282d641f7f054d", "0x1f2246394971c643d371a2b2ab9176d34b98c0a84a6aa5e4e53f73ab6119dcc1"));
        shops.add(new ShopData("0x0001548b7faa282b8721218962e3c1ae43608009534663de91a1548e37cc1c69", "0x49d28e02787ca6f2827065c83c9c4de2369b4d18d132505d3c01ba35a4558214"));
        shops.add(new ShopData("0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86", "0xd72fb7fe49fd18f92481cbee186050816631391b4a25d579b7cff7efdf7099d3"));
        shops.add(new ShopData("0x000104ef11be936f49f6388dd20d062e43170fd7ce9e968e51426317e284b930", "0x90ee852d612e080fb99914d40e0cd75edf928ca895bdda8b91be4b464c55edfc"));
        shops.add(new ShopData("0x00016bad0e0f6ad0fdd7660393b45f452a0eca3f6f1f0eeb25c5902e46a1ffee", "0x8bfcb398c9cb1c7c11790a2293f6d4d8c0adc5f2bd3620561dd81e2db2e9a83e"));
        shops.add(new ShopData("0x00012a23595cf31762a61502546e8b9f947baf3bd55040d9bd535f8afdbff409", "0x77962b6be5cd2ab0c692fe500f50965b5051822d91fece18dcd256dc79182305"));

        activeShops = new ArrayList<ShopData>();
        activeShops.add(new ShopData("0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874", "0xa237d68cbb66fd5f76e7b321156c46882546ad87d662dec8b82703ac31efbf0a"));
        activeShops.add(new ShopData("0x00015f59d6b480ff5a30044dcd7fe3b28c69b6d0d725ca469d1b685b57dfc105", "0x05152ad8d5b14d3f65539e0e42131bc72cbdd16c486cb215d60b7dc113ca1ebd"));
        activeShops.add(new ShopData("0x000108f12f827f0521be34e7563948dc778cb80f7498cebb57cb1a62840d96eb", "0xf4b8aa615834c57d1e4836c683c8d3460f8ff232667dc317f82844e674ee4f26"));
        activeShops.add(new ShopData("0x0001befa86be32da60a87a843bf3e63e77092040ee044f854e8d318d1eb18d20", "0xe58b3ae0e68a04996d6c13c9f9cb65b2d88ada662f28edd67db8c8e1ef45eed4"));
        activeShops.add(new ShopData("0x00013ecc54754b835d04ee5b4df7d0d0eb4e0eafc33ac8de4d282d641f7f054d", "0x1f2246394971c643d371a2b2ab9176d34b98c0a84a6aa5e4e53f73ab6119dcc1"));
        activeShops.add(new ShopData("0x0001548b7faa282b8721218962e3c1ae43608009534663de91a1548e37cc1c69", "0x49d28e02787ca6f2827065c83c9c4de2369b4d18d132505d3c01ba35a4558214"));

        users.add(new UserData("+82 10-1000-2000", "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c"));
        users.add(new UserData("+82 10-1000-2001", "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab"));
        users.add(new UserData("+82 10-1000-2002", "0x11855bdc610b27e6b98de50715c947dc7acd95166d70d66b773f06b328ee8b5c"));
        users.add(new UserData("+82 10-1000-2003", "0x2e981a3691ff15706d808b457699ddd46fb9ebf8d824295fb585e71e1db3c4c1"));
        users.add(new UserData("+82 10-1000-2004", "0xb93f43bdafc9efee5b223735a0dd4efef9522f2db5b9f70889d6fa6fcead50c4"));

        purchaseShopId = shops.get(5).shopId;
        userAccount = users.get(0).address;

        SavePurchase();
        Payment();
        Settlement();
    }

    void SavePurchase() {
        SavePurchaseClient savePurchaseClient = new SavePurchaseClient(network, AccessKeys.get(network), AssetAddresses.get(network));

        try {
            // Check Balance
            System.out.println("[ Check Balance ]");
            UserBalance balance1 = savePurchaseClient.getBalanceAccount(userAccount);
            System.out.printf("  - Balance: %s\n", new Amount(balance1.point.balance).toAmountString());

            // Save New Purchase
            System.out.println("[ Save New Purchase ]");
            ResponseSavePurchase res1 = savePurchaseClient.saveNewPurchase(
                    CommonUtils.getSamplePurchaseId(),
                    CommonUtils.getTimeStamp(),
                    0,
                    "100000000",
                    "100000000",
                    "php",
                    purchaseShopId,
                    userAccount,
                    "",
                    new PurchaseDetail[]{new PurchaseDetail("2020051310000000", "100000000", 10)}
            );
            System.out.printf("  - type: %d, sequence: %s, purchaseId: %s\n", res1.type, res1.sequence, res1.purchaseId);

            // Waiting...
            System.out.println("[ Waiting for providing... ]");
            long t1 = CommonUtils.getTimeStamp();
            while (true) {
                UserBalance balance2 = savePurchaseClient.getBalanceAccount(userAccount);
                if (balance2.point.balance.equals(balance1.point.balance.add(Amount.make("10000000").getValue()))) {
                    break;
                } else if (CommonUtils.getTimeStamp() - t1 > 120) {
                    System.out.println("Time out for providing... ");
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Payment() {
        try {
            // Create User Client
            System.out.println("[ Create User Client ]");
            PaymentClientForUser userClient = new PaymentClientForUser(network, users.get(0).privateKey);

            // Create Client
            System.out.println("[ Create Client ]");
            PaymentClient client = new PaymentClient(network, AccessKeys.get(network));

            String terminalID = "POS001";
            PaymentTaskItem paymentItem;

            for (ShopData shop : activeShops) {

                // Create Temporary Account
                System.out.println("[ Create Temporary Account ]");
                String temporaryAccount = userClient.getTemporaryAccount();
                System.out.printf("  - Temporary Account: %s\n", temporaryAccount);

                // Open New Payment
                System.out.println("[ Open New Payment ]");
                String purchaseId = CommonUtils.getSamplePurchaseId();
                paymentItem = client.openNewPayment(
                        purchaseId,
                        temporaryAccount,
                        Amount.make("1_000").getValue(),
                        "php",
                        shop.shopId,
                        terminalID
                );
                assertEquals(paymentItem.purchaseId, purchaseId);
                assertEquals(paymentItem.account.toLowerCase(), userClient.getAddress().toLowerCase());

                // Waiting...
                System.out.println("[ Waiting... ]");
                Thread.sleep(1000);

                // Approval New Payment
                System.out.println("[ Approval New Payment ]");
                PaymentTaskItemShort res = userClient.approveNewPayment(
                        paymentItem.paymentId,
                        paymentItem.purchaseId,
                        paymentItem.amount,
                        paymentItem.currency,
                        paymentItem.shopId,
                        true
                );
                assertEquals(res.paymentId, paymentItem.paymentId);

                // Waiting...
                System.out.println("[ Waiting... ]");
                Thread.sleep(3000);

                // Close New Payment
                System.out.println("[ Close New Payment ]");
                PaymentTaskItem res2 = client.closeNewPayment(paymentItem.paymentId, true);
                assertEquals(res2.paymentId, paymentItem.paymentId);

                // Waiting...f
                System.out.println("[ Waiting... ]");
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement() {
        try {
            Settlement01_CreateSettlementClient();
            Settlement02_RemoveManager();
            Settlement03_SetManager();
            Settlement04_Check();
            Settlement05_CollectSettlementAmount();
            Settlement06_Waiting();
            Settlement07_CheckRefund();
            Settlement08_RefundOfManager();
            Settlement09_Waiting();
            Settlement10_Withdrawal();
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement01_CreateSettlementClient() {
        System.out.println("[ Settlement01_CreateSettlementClient ]");
        try {
            settlementClientForManager = new SettlementClientForShop(network, shops.get(6).privateKey, shops.get(6).shopId);
            settlementClient = new SettlementClient(network, shops.get(6).privateKey, shops.get(6).shopId);
            refundAgent = new SettlementClient(network, users.get(1).privateKey, shops.get(6).shopId);
            withdrawalAgent = new SettlementClient(network, users.get(2).privateKey, shops.get(6).shopId);

            settlementClientForManager.setAgentOfRefund(refundAgent.getAddress());
            settlementClientForManager.setAgentOfWithdrawal(withdrawalAgent.getAddress());
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement02_RemoveManager() {
        System.out.println("[ Settlement02_RemoveManager ]");
        try {
            for (ShopData shop : shops) {
                SettlementClientForShop shopClient = new SettlementClientForShop(network, shop.privateKey, shop.shopId);
                shopClient.removeSettlementManager();
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement03_SetManager() {
        System.out.println("[ Settlement03_SetManager ]");
        try {
            for (ShopData shop : activeShops) {
                SettlementClientForShop shopClient = new SettlementClientForShop(network, shop.privateKey, shop.shopId);
                shopClient.setSettlementManager(settlementClientForManager.getShopId());
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement04_Check() {
        System.out.println("[ Settlement04_Check ]");
        try {
            long length = settlementClient.getSettlementClientLength();
            Assertions.assertEquals(6, length);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement05_CollectSettlementAmount() {
        System.out.println("[ Settlement05_CollectSettlementAmount ]");
        try {
            long clientLength = refundAgent.getSettlementClientLength();
            ArrayList<String> clientList = refundAgent.getSettlementClientList(0, clientLength);
            refundAgent.collectSettlementAmountMultiClient(clientList);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement06_Waiting() {
        System.out.println("[ Settlement06_Waiting ]");
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement07_CheckRefund() {
        System.out.println("[ Settlement07_CheckRefund ]");
        try {
            for (ShopData shop : activeShops) {
                SettlementClientForShop shopClient = new SettlementClientForShop(network, shop.privateKey, shop.shopId);
                ShopRefundableData res = shopClient.getRefundable();
                Assertions.assertEquals(BigInteger.ZERO, res.refundableAmount);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement08_RefundOfManager() {
        System.out.println("[ Settlement08_RefundOfManager ]");
        try {
            ShopRefundableData refundableData = settlementClient.getRefundable();
            BigInteger refundableAmount = refundableData.refundableAmount;
            BigInteger refundableToken = refundableData.refundableToken;

            String accountOfShop = settlementClient.getAccountOfShopOwner();
            UserBalance res1 = settlementClient.getBalanceAccount(accountOfShop);

            refundAgent.refund(refundableAmount);

            UserBalance res2 = settlementClient.getBalanceAccount(accountOfShop);

            Assertions.assertEquals(res2.token.balance, res1.token.balance.add(refundableToken));
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement09_Waiting() {
        System.out.println("[ Settlement09_Waiting ]");
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Settlement10_Withdrawal() {
        System.out.println("[ Settlement10_Withdrawal ]");
        try {
            ChainInfo chainInfo = settlementClient.getChainInfoOfSideChain();
            String accountOfShop = settlementClient.getAccountOfShopOwner();
            UserBalance res2 = settlementClient.getBalanceAccount(accountOfShop);
            BigInteger balanceOfToken = res2.token.balance;
            BigInteger balanceMainChain1 = settlementClient.getBalanceOfMainChainToken(accountOfShop);
            withdrawalAgent.withdraw(balanceOfToken);

            long t1 = CommonUtils.getTimeStamp();
            while (true) {
                BigInteger balanceMainChain2 = settlementClient.getBalanceOfMainChainToken(accountOfShop);
                if (balanceMainChain2.equals(balanceMainChain1.add(balanceOfToken).subtract(chainInfo.network.loyaltyBridgeFee))) {
                    break;
                } else if (CommonUtils.getTimeStamp() - t1 > 120) {
                    System.out.println("Time out for providing... ");
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }
}
