using Kios.Service.Sdk.Event;
using Nethereum.Util;

namespace Kios.Service.Sdk.Test;

using Client;
using Types;
using Utils;

public class PaymentClientTest
{
    private NetWorkType network = NetWorkType.ACC_TestNet;
    private Dictionary<NetWorkType, string> AccessKeys;
    private PaymentClient paymentClient;
    private PaymentClientForUser userClient;
    private PaymentClientForShop shopClient;
    private TestEventListener listener;
    private TaskEventCollector collector;
    private string temporaryAccount;
    private string terminalID = "POS001";
    private PaymentTaskItem paymentItem;

    public PaymentClientTest()
    {
        AccessKeys = new Dictionary<NetWorkType, string>();
        AccessKeys.Add(NetWorkType.KIOS_TestNet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee");
        AccessKeys.Add(NetWorkType.ACC_TestNet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        AccessKeys.Add(NetWorkType.LocalHost, "0x2c93e943c0d7f6f1a42f53e116c52c40fe5c1b428506dc04b290f2a77580a342");
        paymentClient = new PaymentClient(network, AccessKeys[network]);
        listener = new TestEventListener();
        collector = new TaskEventCollector(paymentClient, listener);
        collector.Start();
        userClient = new PaymentClientForUser(network,
            "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
        shopClient = new PaymentClientForShop(network,
            "0xa237d68cbb66fd5f76e7b321156c46882546ad87d662dec8b82703ac31efbf0a",
            "0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874");
    }

    [SetUp]
    public void Setup()
    {
    }

    [Test]
    public async Task Test01_Waiting()
    {
        await Task.Delay(3000);
    }

    [Test]
    public async Task Test02_CreateTemporaryAccount()
    {
        temporaryAccount = await userClient.GetTemporaryAccount();
        Console.WriteLine($"Temporary Account: {temporaryAccount}");
    }

    [Test]
    public async Task Test03_OpenNewPayment()
    {
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
    }

    [Test]
    public async Task Test04_Waiting()
    {
        await Task.Delay(1000);
    }

    [Test]
    public async Task Test05_ApproveNewPayment()
    {
        var res = await userClient.ApproveNewPayment(
            paymentItem.PaymentId,
            paymentItem.PurchaseId,
            paymentItem.Amount,
            paymentItem.Currency,
            paymentItem.ShopId,
            true
        );
        Assert.That(res.PaymentId, Is.EqualTo(paymentItem.PaymentId));
    }

    [Test]
    public async Task Test06_Waiting()
    {
        await Task.Delay(3000);
    }

    [Test]
    public async Task Test07_CloseNewPayment()
    {
        var res = await paymentClient.CloseNewPayment(
            paymentItem.PaymentId,
            true
        );
        Assert.That(res.PaymentId, Is.EqualTo(paymentItem.PaymentId));
    }

    [Test]
    public async Task Test08_Waiting()
    {
        await Task.Delay(3000);
    }

    [Test]
    public async Task Test09_OpenCancelPayment()
    {
        var res = await paymentClient.OpenCancelPayment(
            paymentItem.PaymentId,
            terminalID
        );
        Assert.That(res.PurchaseId, Is.EqualTo(paymentItem.PurchaseId));
        Assert.That(res.Account, Is.EqualTo(userClient.Address));
    }

    [Test]
    public async Task Test10_Waiting()
    {
        await Task.Delay(1000);
    }

    [Test]
    public async Task Test11_ApproveCancelPayment()
    {
        var res = await shopClient.ApproveCancelPayment(
            paymentItem.PaymentId,
            paymentItem.PurchaseId,
            true
        );
        Assert.That(res.PaymentId, Is.EqualTo(paymentItem.PaymentId));
    }

    [Test]
    public async Task Test12_Waiting()
    {
        await Task.Delay(3000);
    }

    [Test]
    public async Task Test13_CloseCancelPayment()
    {
        var res = await paymentClient.CloseCancelPayment(
            paymentItem.PaymentId,
            true
        );
        Assert.That(res.PaymentId, Is.EqualTo(paymentItem.PaymentId));
    }

    [Test]
    public async Task Test14_Waiting()
    {
        await Task.Delay(3000);
        collector.Stop();
    }
}