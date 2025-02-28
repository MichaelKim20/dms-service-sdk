using Kios.Service.Sdk.Client;
using Kios.Service.Sdk.Types;
using Kios.Service.Sdk.Utils;
using Kios.Service.Sdk.Event;

namespace Kios.Service.Sdk.Sample;

public class PaymentClientSample
{
    private readonly PaymentClient _paymentClient;
    private readonly PaymentClientForUser _userClient;
    private readonly PaymentClientForShop _shopClient;
    private readonly TaskEventCollector _collector;
    private string _temporaryAccount = "";
    private readonly string _terminalId = "POS001";
    private PaymentTaskItem? _paymentItem = null;

    public PaymentClientSample()
    {
        var privateKeyForPayment = "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276";
        _paymentClient = new PaymentClient(NetWorkType.ACC_TestNet, privateKeyForPayment);
        var listener = new TestEventListener();
        _collector = new TaskEventCollector(_paymentClient, listener);
        _collector.Start();
        _userClient = new PaymentClientForUser(NetWorkType.ACC_TestNet,
            "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
        _shopClient = new PaymentClientForShop(NetWorkType.ACC_TestNet,
            "0xa237d68cbb66fd5f76e7b321156c46882546ad87d662dec8b82703ac31efbf0a",
            "0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874");
    }

    private async Task Test01_Waiting()
    {
        Console.WriteLine("Test01_Waiting");
        await Task.Delay(3000);
    }

    private async Task Test02_CreateTemporaryAccount()
    {
        Console.WriteLine("Test02_CreateTemporaryAccount");
        _temporaryAccount = await _userClient.GetTemporaryAccount();
        Console.WriteLine($"Temporary Account: {_temporaryAccount}");
    }

    private async Task Test03_OpenNewPayment()
    {
        Console.WriteLine("Test03_OpenNewPayment");
        var purchaseId = CommonUtils.GetSamplePurchaseId();
        _paymentItem = await _paymentClient.OpenNewPayment(
            purchaseId,
            _temporaryAccount,
            Amount.Make("1_000").Value,
            "php",
            _shopClient.ShopId,
            _terminalId
        );
        if (!_paymentItem.PurchaseId.Equals(purchaseId)) Console.WriteLine("Error");
        if (!_paymentItem.Account.Equals(_userClient.Address)) Console.WriteLine("Error");
    }

    private async Task Test04_Waiting()
    {
        Console.WriteLine("Test04_Waiting");
        await Task.Delay(1000);
    }

    private async Task Test05_ApproveNewPayment()
    {
        Console.WriteLine("Test05_ApproveNewPayment");
        var res = await _userClient.ApproveNewPayment(
            _paymentItem.PaymentId,
            _paymentItem.PurchaseId,
            _paymentItem.Amount,
            _paymentItem.Currency,
            _paymentItem.ShopId,
            true
        );
        if (!res.PaymentId.Equals(_paymentItem.PaymentId)) Console.WriteLine("Error");
    }

    private async Task Test06_Waiting()
    {
        Console.WriteLine("Test06_Waiting");
        await Task.Delay(3000);
    }

    private async Task Test07_CloseNewPayment()
    {
        Console.WriteLine("Test07_CloseNewPayment");
        var res = await _paymentClient.CloseNewPayment(
            _paymentItem.PaymentId,
            true
        );
        if (!res.PaymentId.Equals(_paymentItem.PaymentId)) Console.WriteLine("Error");
    }

    private async Task Test08_Waiting()
    {
        Console.WriteLine("Test08_Waiting");
        await Task.Delay(3000);
    }

    private async Task Test09_OpenCancelPayment()
    {
        Console.WriteLine("Test09_OpenCancelPayment");
        var res = await _paymentClient.OpenCancelPayment(
            _paymentItem.PaymentId,
            _terminalId
        );
        if (!res.PurchaseId.Equals(_paymentItem.PurchaseId)) Console.WriteLine("Error");
        if (!res.Account.Equals(_userClient.Address)) Console.WriteLine("Error");
    }

    private async Task Test10_Waiting()
    {
        Console.WriteLine("Test10_Waiting");
        await Task.Delay(1000);
    }

    private async Task Test11_ApproveCancelPayment()
    {
        Console.WriteLine("Test11_ApproveCancelPayment");
        var res = await _shopClient.ApproveCancelPayment(
            _paymentItem.PaymentId,
            _paymentItem.PurchaseId,
            true
        );
        if (!res.PaymentId.Equals(_paymentItem.PaymentId)) Console.WriteLine("Error");
    }

    private async Task Test12_Waiting()
    {
        Console.WriteLine("Test12_Waiting");
        await Task.Delay(3000);
    }

    private async Task Test13_CloseCancelPayment()
    {
        Console.WriteLine("Test13_CloseCancelPayment");
        var res = await _paymentClient.CloseCancelPayment(
            _paymentItem.PaymentId,
            true
        );
        if (!res.PaymentId.Equals(_paymentItem.PaymentId)) Console.WriteLine("Error");
    }

    private async Task Test14_Waiting()
    {
        Console.WriteLine("Test14_Waiting");
        await Task.Delay(3000);
        _collector.Stop();
    }

    public async Task TestAll()
    {
        await Test01_Waiting();
        await Test02_CreateTemporaryAccount();
        await Test03_OpenNewPayment();
        await Test04_Waiting();
        await Test05_ApproveNewPayment();
        await Test06_Waiting();
        await Test07_CloseNewPayment();
        await Test08_Waiting();
        await Test09_OpenCancelPayment();
        await Test10_Waiting();
        await Test11_ApproveCancelPayment();
        await Test12_Waiting();
        await Test13_CloseCancelPayment();
        await Test14_Waiting();
    }
}