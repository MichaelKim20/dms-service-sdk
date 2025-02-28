namespace Kios.Service.Sdk.Test;

using Client;
using Types;
using Utils;
using System.Numerics;

public class SavePurchaseClientTests
{
    private NetWorkType network = NetWorkType.ACC_TestNet;
    private Dictionary<NetWorkType, string> AccessKeys;
    private Dictionary<NetWorkType, string> AssetAddresses;
    private SavePurchaseClient client;
    private string shopId;
    private string userAccount;
    private string userPhone;

    private UserBalanceData balance1;
    private UserBalanceData balance2;

    private string purchaseId;
    private long timestamp;

    public SavePurchaseClientTests()
    {
        AccessKeys = new Dictionary<NetWorkType, string>();
        AccessKeys.Add(NetWorkType.KIOS_TestNet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee");
        AccessKeys.Add(NetWorkType.ACC_TestNet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        AccessKeys.Add(NetWorkType.LocalHost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342");

        AssetAddresses = new Dictionary<NetWorkType, string>();
        AssetAddresses.Add(NetWorkType.ACC_TestNet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714");
        AssetAddresses.Add(NetWorkType.LocalHost, "0x4501F7aF010Cef3DcEaAfbc7Bfb2B39dE57df54d");

        client = new SavePurchaseClient(
            network,
            AccessKeys[network],
            AssetAddresses[network]
        );
        shopId = "0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874";
        userAccount = "0x64D111eA9763c93a003cef491941A011B8df5a49";
        userPhone = "";
    }

    [SetUp]
    public void Setup()
    {
    }

    [Test]
    public async Task Test01_CheckBalance()
    {
        balance1 = await client.GetBalanceAccount(userAccount);
        Console.WriteLine($"Balance: {new Amount(balance1.Point.Balance).ToAmountString()}");
    }

    [Test]
    public async Task Test02_SaveNewPurchase()
    {
        var res1 = await client.SaveNewPurchase(
            CommonUtils.GetSamplePurchaseId(),
            CommonUtils.GetTimeStamp(),
            0,
            "10000",
            "10000",
            "php",
            shopId,
            userAccount,
            "",
            [new("2020051310000000", "10000", 10)]
        );
        Console.WriteLine($"type: {res1.Type}, sequence: {res1.Sequence}, purchaseId: {res1.PurchaseId}");
    }

    [Test]
    public async Task Test03_Waiting()
    {
        var t1 = CommonUtils.GetTimeStamp();
        while (true)
        {
            var balance = await client.GetBalanceAccount(userAccount);
            if (balance.Point.Balance.Equals(BigInteger.Add(balance1.Point.Balance, Amount.Make("1000").Value)))
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
        balance2 = await client.GetBalanceAccount(userAccount);
        Assert.That(balance2.Point.Balance,
            Is.EqualTo(BigInteger.Add(balance1.Point.Balance, Amount.Make("1000").Value)));
        Console.WriteLine($"Balance: {new Amount(balance2.Point.Balance).ToAmountString()}");
    }

    [Test]
    public async Task Test05_SaveNewPurchase()
    {
        purchaseId = CommonUtils.GetSamplePurchaseId();
        timestamp = CommonUtils.GetTimeStamp();
        var res2 = await client.SaveNewPurchase(
            purchaseId,
            timestamp,
            60,
            "10000",
            "10000",
            "php",
            shopId,
            userAccount,
            "",
            new PurchaseDetail[] { new("2020051310000000", "10000", 10) }
        );
        Console.WriteLine($"type: {res2.Type}, sequence: {res2.Sequence}, purchaseId: {res2.PurchaseId}");
    }

    [Test]
    public async Task Test06_Waiting()
    {
        await Task.Delay(100);
    }

    [Test]
    public async Task Test07_SaveCancelPurchase()
    {
        var res3 = await client.SaveCancelPurchase(purchaseId, timestamp, 3600);
        Console.WriteLine($"type: {res3.Type}, sequence: {res3.Sequence}, purchaseId: {res3.PurchaseId}");
    }

    [Test]
    public async Task Test08_Waiting()
    {
        await Task.Delay(50000);
    }

    [Test]
    public async Task Test09_CheckBalance()
    {
        var balance4 = await client.GetBalanceAccount(userAccount);
        Console.WriteLine($"Balance: {balance4.Point.Balance.ToString()}");

        Assert.That(balance4.Point.Balance,
            Is.EqualTo(balance2.Point.Balance));
    }
}