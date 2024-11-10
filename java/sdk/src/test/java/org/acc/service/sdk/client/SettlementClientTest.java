package org.acc.service.sdk.client;

import org.acc.service.sdk.data.NetWorkType;
import org.acc.service.sdk.data.ShopData;
import org.acc.service.sdk.data.UserData;
import org.acc.service.sdk.data.UserBalance;
import org.acc.service.sdk.data.payment.PaymentTaskItem;
import org.acc.service.sdk.data.purchase.PurchaseDetail;
import org.acc.service.sdk.data.purchase.ResponseSavePurchase;
import org.acc.service.sdk.utils.Amount;
import org.acc.service.sdk.utils.CommonUtils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettlementClientTest {
    private final NetWorkType network = NetWorkType.localhost;
    private Hashtable<NetWorkType, String> AccessKeys;
    private Hashtable<NetWorkType, String> AssetAddresses;
    private ArrayList<ShopData> shops;
    private ArrayList<UserData> users;

    private String purchaseShopId;
    private String managerId;
    private String userAccount;
    private String userPhone;

    private SettlementClient settlementClient;
    private SettlementClientForShop settlementClientForManager;

    @Test
    void Settlement(){
        AccessKeys = new Hashtable<NetWorkType, String>();
        AssetAddresses = new Hashtable<NetWorkType, String>();

        AccessKeys.put(NetWorkType.testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        AccessKeys.put(NetWorkType.localhost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342");

        AssetAddresses.put(NetWorkType.testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714");
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

        users.add(new UserData("+82 10-1000-2000", "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c"));
        users.add(new UserData("+82 10-1000-2001", "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab"));
        users.add(new UserData("+82 10-1000-2002", "0x11855bdc610b27e6b98de50715c947dc7acd95166d70d66b773f06b328ee8b5c"));
        users.add(new UserData("+82 10-1000-2003", "0x2e981a3691ff15706d808b457699ddd46fb9ebf8d824295fb585e71e1db3c4c1"));
        users.add(new UserData("+82 10-1000-2004", "0xb93f43bdafc9efee5b223735a0dd4efef9522f2db5b9f70889d6fa6fcead50c4"));

        purchaseShopId = shops.get(5).shopId;
        userAccount = users.get(0).address;

        savePurchase();
        payment();
        settlement();
    }

    void savePurchase() {
        SavePurchaseClient savePurchaseClient = new SavePurchaseClient(NetWorkType.localhost, AccessKeys.get(network), AssetAddresses.get(network));

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
                new PurchaseDetail[]{ new PurchaseDetail("2020051310000000", "100000000", 10) }
            );
            System.out.printf("  - type: %d, sequence: %s, purchaseId: %s\n", res1.type, res1.sequence, res1.purchaseId);

            // Waiting...
            System.out.println("[ Waiting for providing... ]");
            long t1 = CommonUtils.getTimeStamp();
            while(true) {
                UserBalance balance2 = savePurchaseClient.getBalanceAccount(userAccount);
                if (balance2.point.balance.equals(balance1.point.balance.add(Amount.make("10000000").getValue()))) {
                    break;
                }
                else if (CommonUtils.getTimeStamp() - t1 > 120) {
                    System.out.println("Time out for providing... ");
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void payment() {
        try {
            // Create User Client
            System.out.println("[ Create User Client ]");
            PaymentClientForUser userClient = new PaymentClientForUser(network, users.getFirst().privateKey);

            // Create Client
            System.out.println("[ Create Client ]");
            PaymentClient client = new PaymentClient(network, AccessKeys.get(network));

            String terminalID = "POS001";
            PaymentTaskItem paymentItem;

            for (int i = 0; i < 6; i++) {

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
                        shops.get(i).shopId,
                        terminalID
                );
                assertEquals(paymentItem.purchaseId, purchaseId);
                assertEquals(paymentItem.account.toLowerCase(), userClient.getAddress().toLowerCase());

                // Waiting...
                System.out.println("[ Waiting... ]");
                Thread.sleep(1000);

                // Approval New Payment
                System.out.println("[ Approval New Payment ]");
                var res = userClient.approveNewPayment(
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
                var res2 = client.closeNewPayment(paymentItem.paymentId, true);
                assertEquals(res2.paymentId, paymentItem.paymentId);

                // Waiting...
                System.out.println("[ Waiting... ]");
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void settlement(){
        try {
            Test05_CreateSettlementClient();
            Test06_RemoveManager();
            Test07_SetManager();
            Test08_Check();
            Test09_CollectSettlementAmount();
            Test10_Waiting();
            Test11_CheckRefund();
            Test12_RefundOfManager();
            Test13_Waiting();
            Test14_Withdrawal();
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test05_CreateSettlementClient(){
        System.out.println("[ Test05_CreateSettlementClient ]");
        try {
            settlementClientForManager = new SettlementClientForShop(network, shops.get(6).privateKey, shops.get(6).shopId);
            settlementClient = new SettlementClient(network, shops.get(6).privateKey, shops.get(6).shopId);
            settlementClientForManager.setAgentOfRefund("0x0000000000000000000000000000000000000000");
            settlementClientForManager.setAgentOfWithdrawal("0x0000000000000000000000000000000000000000");
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test06_RemoveManager(){
        System.out.println("[ Test06_RemoveManager ]");
        try {
            for (ShopData shop : shops)
            {
                var shopClient = new SettlementClientForShop(network, shop.privateKey, shop.shopId);
                shopClient.removeSettlementManager();
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test07_SetManager(){
        System.out.println("[ Test07_SetManager ]");
        try {
            for (int i = 0; i < 6; i++)
            {
                var shopClient = new SettlementClientForShop(network, shops.get(i).privateKey, shops.get(i).shopId);
                shopClient.setSettlementManager(settlementClientForManager.getShopId());
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test08_Check(){
        System.out.println("[ Test08_Check ]");
        try {
            var length = settlementClient.getSettlementClientLength();
            assertEquals(length, 6);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test09_CollectSettlementAmount()
    {
        System.out.println("[ Test09_CollectSettlementAmount ]");
        try {
            var ids = new ArrayList<String>();
            for (int i = 0; i < 6; i++)
            {
                ids.add(shops.get(i).shopId);
            }
            settlementClient.collectSettlementAmountMultiClient(ids);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test10_Waiting()
    {
        System.out.println("[ Test10_Waiting ]");
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test11_CheckRefund()
    {
        System.out.println("[ Test11_CheckRefund ]");
        try {
            for (int i = 0; i < 6; i++)
            {
                var shopClient = new SettlementClientForShop(network, shops.get(i).privateKey, shops.get(i).shopId);
                var res = shopClient.getRefundable();
                assertEquals(res.refundableAmount, BigInteger.ZERO);
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }


    void Test12_RefundOfManager()
    {
        System.out.println("[ Test09_RefundOfManager ]");
        try {
            var refundableData =  settlementClient.getRefundable();
            var refundableAmount = refundableData.refundableAmount;
            var refundableToken = refundableData.refundableToken;

            var accountOfShop = settlementClient.getAccountOfShopOwner();
            var res1 = settlementClient.getBalanceAccount(accountOfShop);

            settlementClient.refund(refundableAmount);

            var res2 = settlementClient.getBalanceAccount(accountOfShop);

            assertEquals(res2.token.balance, res1.token.balance.add(refundableToken));
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test13_Waiting()
    {
        System.out.println("[ Test10_Waiting ]");
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    void Test14_Withdrawal()
    {
        System.out.println("[ Test11_Withdrawal ]");
        try {
            var chainInfo = settlementClient.getChainInfoOfSideChain();
            var accountOfShop = settlementClient.getAccountOfShopOwner();
            var res2 = settlementClient.getBalanceAccount(accountOfShop);
            var balanceOfToken = res2.token.balance;
            var balanceMainChain1 = settlementClient.getBalanceOfMainChainToken(accountOfShop);
            settlementClient.withdraw(balanceOfToken);

            long t1 = CommonUtils.getTimeStamp();
            while(true) {
                BigInteger balanceMainChain2 = settlementClient.getBalanceOfMainChainToken(accountOfShop);
                if (balanceMainChain2.equals(balanceMainChain1.add(balanceOfToken).subtract(chainInfo.network.loyaltyBridgeFee))) {
                    break;
                }
                else if (CommonUtils.getTimeStamp() - t1 > 120) {
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
