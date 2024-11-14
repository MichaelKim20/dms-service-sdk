using Nethereum.ABI.Util;

namespace Acc.Service.Sdk.Test;

using Client;
using Types;
using Utils;
using System.Numerics;

public class SettlementClientUsingAgentTests
{
    private NetWorkType network = NetWorkType.TestNet;
    private Dictionary<NetWorkType, string> AccessKeys;
    private Dictionary<NetWorkType, string> AssetAddresses;
    private SavePurchaseClient savePurchaseClient;
    private string shopId;
    private string userAccount;
    private string userPhone;

    private UserBalanceData balance1;
    private UserBalanceData balance2;

    private string purchaseId;
    private long timestamp;

    private string purchaseShopId;
    private string managerId;

    private List<ShopData> shops;
    private List<UserData> users;

    private SettlementClient settlementClient;
    private SettlementClientForShop settlementClientForManager;
    private List<SettlementClientForShop> settlementClientForShops;

    private SettlementClient refundAgent;
    private SettlementClient withdrawalAgent;

    public SettlementClientUsingAgentTests()
    {
        AccessKeys = new Dictionary<NetWorkType, string>();
        AccessKeys.Add(NetWorkType.TestNet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        AccessKeys.Add(NetWorkType.LocalHost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342");

        AssetAddresses = new Dictionary<NetWorkType, string>();
        AssetAddresses.Add(NetWorkType.TestNet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714");
        AssetAddresses.Add(NetWorkType.LocalHost, "0x4501F7aF010Cef3DcEaAfbc7Bfb2B39dE57df54d");

        savePurchaseClient = new SavePurchaseClient(
            network,
            AccessKeys[network],
            AssetAddresses[network]
        );
    }

    [SetUp]
    public void Setup()
    {
        shops = new List<ShopData>();
        users = new List<UserData>();

        shops.Add(new ShopData("0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874",
            "0xa237d68cbb66fd5f76e7b321156c46882546ad87d662dec8b82703ac31efbf0a"));
        shops.Add(new ShopData("0x00015f59d6b480ff5a30044dcd7fe3b28c69b6d0d725ca469d1b685b57dfc105",
            "0x05152ad8d5b14d3f65539e0e42131bc72cbdd16c486cb215d60b7dc113ca1ebd"));
        shops.Add(new ShopData("0x000108f12f827f0521be34e7563948dc778cb80f7498cebb57cb1a62840d96eb",
            "0xf4b8aa615834c57d1e4836c683c8d3460f8ff232667dc317f82844e674ee4f26"));
        shops.Add(new ShopData("0x0001befa86be32da60a87a843bf3e63e77092040ee044f854e8d318d1eb18d20",
            "0xe58b3ae0e68a04996d6c13c9f9cb65b2d88ada662f28edd67db8c8e1ef45eed4"));
        shops.Add(new ShopData("0x00013ecc54754b835d04ee5b4df7d0d0eb4e0eafc33ac8de4d282d641f7f054d",
            "0x1f2246394971c643d371a2b2ab9176d34b98c0a84a6aa5e4e53f73ab6119dcc1"));
        shops.Add(new ShopData("0x0001548b7faa282b8721218962e3c1ae43608009534663de91a1548e37cc1c69",
            "0x49d28e02787ca6f2827065c83c9c4de2369b4d18d132505d3c01ba35a4558214"));
        shops.Add(new ShopData("0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86",
            "0xd72fb7fe49fd18f92481cbee186050816631391b4a25d579b7cff7efdf7099d3"));
        shops.Add(new ShopData("0x000104ef11be936f49f6388dd20d062e43170fd7ce9e968e51426317e284b930",
            "0x90ee852d612e080fb99914d40e0cd75edf928ca895bdda8b91be4b464c55edfc"));
        shops.Add(new ShopData("0x00016bad0e0f6ad0fdd7660393b45f452a0eca3f6f1f0eeb25c5902e46a1ffee",
            "0x8bfcb398c9cb1c7c11790a2293f6d4d8c0adc5f2bd3620561dd81e2db2e9a83e"));
        shops.Add(new ShopData("0x00012a23595cf31762a61502546e8b9f947baf3bd55040d9bd535f8afdbff409",
            "0x77962b6be5cd2ab0c692fe500f50965b5051822d91fece18dcd256dc79182305"));

        users.Add(
            new UserData("+82 10-1000-2000", "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c"));
        users.Add(
            new UserData("+82 10-1000-2001", "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab"));
        users.Add(
            new UserData("+82 10-1000-2002", "0x11855bdc610b27e6b98de50715c947dc7acd95166d70d66b773f06b328ee8b5c"));
        users.Add(
            new UserData("+82 10-1000-2003", "0x2e981a3691ff15706d808b457699ddd46fb9ebf8d824295fb585e71e1db3c4c1"));
        users.Add(
            new UserData("+82 10-1000-2004", "0xb93f43bdafc9efee5b223735a0dd4efef9522f2db5b9f70889d6fa6fcead50c4"));

        purchaseShopId = shops[5].ShopId;
        userAccount = users[0].Address;
    }

    [Test]
    public async Task Test01_CheckBalance()
    {
        balance1 = await savePurchaseClient.GetBalanceAccount(userAccount);
        Console.WriteLine($"Balance: {new Amount(balance1.Point.Balance).ToAmountString()}");
    }

    [Test]
    public async Task Test02_SaveNewPurchase()
    {
        var res1 = await savePurchaseClient.SaveNewPurchase(
            CommonUtils.GetSamplePurchaseId(),
            CommonUtils.GetTimeStamp(),
            0,
            "100000000",
            "100000000",
            "php",
            purchaseShopId,
            userAccount,
            "",
            new PurchaseDetail[] { new("2020051310000000", "100000000", 10) }
        );
        Console.WriteLine($"type: {res1.Type}, sequence: {res1.Sequence}, purchaseId: {res1.PurchaseId}");
    }

    [Test]
    public async Task Test03_Waiting()
    {
        var t1 = CommonUtils.GetTimeStamp();
        while (true)
        {
            var balance = await savePurchaseClient.GetBalanceAccount(userAccount);
            if (balance.Point.Balance.Equals(BigInteger.Add(balance1.Point.Balance, Amount.Make("10000000").Value)))
            {
                break;
            }
            else if (CommonUtils.GetTimeStamp() - t1 > 120)
            {
                Console.WriteLine("Time out for providing... ");
                break;
            }

            await Task.Delay(1000);
        }
    }

    [Test]
    public async Task Test04_CheckBalance()
    {
        balance2 = await savePurchaseClient.GetBalanceAccount(userAccount);
        Assert.That(balance2.Point.Balance,
            Is.EqualTo(BigInteger.Add(balance1.Point.Balance, Amount.Make("10000000").Value)));
        Console.WriteLine($"Balance: {new Amount(balance2.Point.Balance).ToAmountString()}");
    }

    [Test]
    public async Task Test05_CreateSettlementClient()
    {
        settlementClientForShops = new List<SettlementClientForShop>();
        foreach (var shop in shops)
            settlementClientForShops.Add(new SettlementClientForShop(network, shop.PrivateKey, shop.ShopId));

        settlementClientForManager = new SettlementClientForShop(network, shops[6].PrivateKey, shops[6].ShopId);
        refundAgent = new SettlementClient(network, users[1].PrivateKey, shops[6].ShopId);
        withdrawalAgent = new SettlementClient(network, users[2].PrivateKey, shops[6].ShopId);
        settlementClient = new SettlementClient(network, shops[6].PrivateKey, shops[6].ShopId);
        await settlementClientForManager.SetAgentOfRefund(refundAgent.Address);
        await settlementClientForManager.SetAgentOfWithdrawal(withdrawalAgent.Address);
    }

    [Test]
    public async Task Test06_RemoveManager()
    {
        foreach (var client in settlementClientForShops) await client.RemoveSettlementManager();
    }

    [Test]
    public async Task Test07_SetManager()
    {
        for (var i = 0; i < 6; i++)
            await settlementClientForShops[i].SetSettlementManager(settlementClientForManager.ShopId);
    }

    [Test]
    public async Task Test08_Check()
    {
        var length = await settlementClient.GetSettlementClientLength();
        Assert.That(length, Is.EqualTo(6));
    }


    private PaymentClient paymentClient;
    private PaymentClientForUser userClient;
    private string temporaryAccount = "";
    private PaymentTaskItem paymentItem;
    private string terminalID = "POS001";

    [Test]
    public async Task Test10_CreatePaymentClient()
    {
        paymentClient = new PaymentClient(network, AccessKeys[network]);
        userClient = new PaymentClientForUser(network, users[0].PrivateKey);
    }

    [Test]
    public async Task Test11_UsePoint()
    {
        for (var i = 0; i < 6; i++)
        {
            var shopClient = settlementClientForShops[i];
            Console.WriteLine("[0. Shop ID - " + shopClient.ShopId);

            Console.WriteLine("[1. Begin - Temporary Account]");
            temporaryAccount = await userClient.GetTemporaryAccount();
            Console.WriteLine("[1. End - Temporary Account]");

            Console.WriteLine("[2. Begin - Open New Payment]");
            var purchaseId = CommonUtils.GetSamplePurchaseId();
            paymentItem = await paymentClient.OpenNewPayment(
                purchaseId,
                temporaryAccount,
                Amount.Make("1_000").Value,
                "php",
                shopClient.ShopId,
                terminalID
            );
            Assert.That(paymentItem.PurchaseId, Is.EqualTo(purchaseId));
            Assert.That(paymentItem.Account, Is.EqualTo(userClient.Address));
            Console.WriteLine("[2. End - Open New Payment]");

            await Task.Delay(1000);

            Console.WriteLine("[3. Begin - Approval New Payment]");
            var res = await userClient.ApproveNewPayment(
                paymentItem.PaymentId,
                paymentItem.PurchaseId,
                paymentItem.Amount,
                paymentItem.Currency,
                paymentItem.ShopId,
                true
            );
            Assert.That(res.PaymentId, Is.EqualTo(paymentItem.PaymentId));
            Console.WriteLine("[3. End - Approval New Payment]");

            await Task.Delay(3000);

            Console.WriteLine("[4. Begin - Close New Payment]");
            var res2 = await paymentClient.CloseNewPayment(
                paymentItem.PaymentId,
                true
            );
            Assert.That(res.PaymentId, Is.EqualTo(paymentItem.PaymentId));
            Console.WriteLine("[4. END - Close New Payment]");
        }
    }

    [Test]
    public async Task Test12_CollectSettlementAmount()
    {
        var count = await refundAgent.GetSettlementClientLength();
        Assert.That(count, Is.EqualTo(6));
        var clients = await refundAgent.GetSettlementClientList(0, 6);
        await refundAgent.CollectSettlementAmountMultiClient(clients);
    }

    [Test]
    public async Task Test13_Waiting()
    {
        await Task.Delay(3000);
    }

    [Test]
    public async Task Test14_CheckRefund()
    {
        for (var i = 0; i < 6; i++)
        {
            var shopClient = settlementClientForShops[i];

            var res = await shopClient.GetRefundable();
            Assert.That(res.RefundableAmount, Is.EqualTo(BigInteger.Zero));
        }
    }

    [Test]
    public async Task Test15_RefundOfManager()
    {
        var refundableData = await settlementClient.GetRefundable();
        var refundableAmount = refundableData.RefundableAmount;
        var refundableToken = refundableData.RefundableToken;

        var accountOfShop = await settlementClient.GetAccountOfShopOwner();
        var res1 = await settlementClient.GetBalanceAccount(accountOfShop);

        await refundAgent.Refund(refundableAmount);

        var res2 = await settlementClient.GetBalanceAccount(accountOfShop);

        Assert.That(res2.Token.Balance, Is.EqualTo(BigInteger.Add(res1.Token.Balance, refundableToken)));
    }

    [Test]
    public async Task Test16_Waiting()
    {
        await Task.Delay(3000);
    }

    [Test]
    public async Task Test17_Withdrawal()
    {
        var chainInfo = await settlementClient.GetChainInfoOfSideChain();
        var accountOfShop = await settlementClient.GetAccountOfShopOwner();
        var res2 = await settlementClient.GetBalanceAccount(accountOfShop);
        var balanceOfToken = res2.Token.Balance;
        var balanceMainChain1 = await settlementClient.GetBalanceOfMainChainToken(accountOfShop);
        await withdrawalAgent.Withdraw(balanceOfToken);

        var t1 = CommonUtils.GetTimeStamp();
        while (true)
        {
            var balanceMainChain2 = await settlementClient.GetBalanceOfMainChainToken(accountOfShop);
            if (balanceMainChain2.Equals(BigInteger.Subtract(
                    BigInteger.Add(
                        balanceMainChain1,
                        balanceOfToken),
                    chainInfo.Network.LoyaltyBridgeFee)))
            {
                break;
            }
            else if (CommonUtils.GetTimeStamp() - t1 > 120)
            {
                Console.WriteLine("Time out for providing... ");
                break;
            }

            await Task.Delay(1000);
        }
    }
}