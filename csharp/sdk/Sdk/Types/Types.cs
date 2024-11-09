using System.Numerics;
using Newtonsoft.Json.Linq;

namespace Acc.Service.Sdk.Types;

public enum NetWorkType
{
    MainNet,
    TestNet,
    LocalHost
}

public class BalanceData(BigInteger balance, BigInteger value)
{
    public BigInteger Balance = balance;
    public BigInteger Value = value;
}

public class UserBalanceData(JObject point, JObject token)
{
    public readonly BalanceData Point = new(BigInteger.Parse(point["balance"]!.ToString()),
        BigInteger.Parse(point["value"]!.ToString()));

    public readonly BalanceData Token = new(BigInteger.Parse(token["balance"]!.ToString()),
        BigInteger.Parse(token["value"]!.ToString()));
}

public class PurchaseDetail(string productId, string amount, long providePercent)
{
    public readonly string ProductId = productId;
    public readonly string Amount = amount;
    public readonly long ProvidePercent = providePercent;
}

public class ResponseSavePurchase(int type, string sequence, string purchaseId)
{
    public readonly int Type = type;
    public readonly string Sequence = sequence;
    public readonly string PurchaseId = purchaseId;

    public static ResponseSavePurchase FromJObject(JObject data)
    {
        var tx = data.GetValue("tx")!.ToObject<JObject>();
        if (tx != null)
            return new ResponseSavePurchase(
                Convert.ToInt32(tx["type"]!.ToString()),
                tx["sequence"]!.ToString(),
                tx["purchaseId"]!.ToString());
        else
            return new ResponseSavePurchase(0, "0", "");
    }
}

public class SaveCancelOthers(long timestamp, long waiting)
{
    public readonly long Timestamp = timestamp;
    public readonly long Waiting = waiting;
}

public class SaveCancelPurchase(
    string purchaseId,
    string sender,
    string purchaseSignature)
{
    public readonly string PurchaseId = purchaseId;
    public readonly string Sender = sender;
    public string PurchaseSignature = purchaseSignature;
}

public class SaveNewDetail(string productId, BigInteger amount, BigInteger providePercent)
{
    public readonly string ProductId = productId;
    public readonly BigInteger Amount = amount;
    public readonly BigInteger ProvidePercent = providePercent;
}

public class SaveNewOthers(BigInteger totalAmount, long timestamp, long waiting)
{
    public BigInteger TotalAmount = totalAmount;
    public readonly long Timestamp = timestamp;
    public readonly long Waiting = waiting;
}

public class SaveNewPurchase(
    string purchaseId,
    BigInteger cashAmount,
    BigInteger loyalty,
    string currency,
    string shopId,
    string userAccount,
    string userPhoneHash,
    string sender,
    string purchaseSignature)
{
    public readonly string PurchaseId = purchaseId;
    public BigInteger CashAmount = cashAmount;
    public BigInteger Loyalty = loyalty;
    public readonly string Currency = currency;
    public readonly string ShopId = shopId;
    public readonly string UserAccount = userAccount;
    public readonly string UserPhoneHash = userPhoneHash;
    public readonly string Sender = sender;
    public string PurchaseSignature = purchaseSignature;
}

public class PaymentInfo(
    string account,
    BigInteger amount,
    string currency,
    BigInteger balance,
    BigInteger balanceValue,
    BigInteger paidPoint,
    BigInteger paidValue,
    BigInteger feePoint,
    BigInteger feeValue,
    BigInteger totalPoint,
    BigInteger totalValue)
{
    public string Account = account;
    public BigInteger Amount = amount;
    public string Currency = currency;
    public BigInteger Balance = balance;
    public BigInteger BalanceValue = balanceValue;
    public BigInteger PaidPoint = paidPoint;
    public BigInteger PaidValue = paidValue;
    public BigInteger FeePoint = feePoint;
    public BigInteger FeeValue = feeValue;
    public BigInteger TotalPoint = totalPoint;
    public BigInteger TotalValue = totalValue;
}

public class PaymentTaskItem(
    string paymentId,
    string purchaseId,
    BigInteger amount,
    string currency,
    string shopId,
    string account,
    BigInteger paidPoint,
    BigInteger paidValue,
    BigInteger feePoint,
    BigInteger feeValue,
    BigInteger totalPoint,
    BigInteger totalValue,
    string terminalId,
    int paymentStatus)
{
    public readonly string PaymentId = paymentId;
    public readonly string PurchaseId = purchaseId;
    public readonly BigInteger Amount = amount;
    public readonly string Currency = currency;
    public readonly string ShopId = shopId;
    public readonly string Account = account;
    public readonly BigInteger PaidPoint = paidPoint;
    public readonly BigInteger PaidValue = paidValue;
    public readonly BigInteger FeePoint = feePoint;
    public readonly BigInteger FeeValue = feeValue;
    public readonly BigInteger TotalPoint = totalPoint;
    public readonly BigInteger TotalValue = totalValue;
    public readonly string TerminalId = terminalId;
    public readonly int PaymentStatus = paymentStatus;

    public static PaymentTaskItem FromJObject(JObject data)
    {
        return new PaymentTaskItem(
            data["paymentId"]!.ToString(),
            data["purchaseId"]!.ToString(),
            BigInteger.Parse(data["amount"]!.ToString()),
            data["currency"]!.ToString(),
            data["shopId"]!.ToString(),
            data["account"]!.ToString(),
            BigInteger.Parse(data["paidPoint"]!.ToString()),
            BigInteger.Parse(data["paidValue"]!.ToString()),
            BigInteger.Parse(data["feePoint"]!.ToString()),
            BigInteger.Parse(data["feeValue"]!.ToString()),
            BigInteger.Parse(data["totalPoint"]!.ToString()),
            BigInteger.Parse(data["totalValue"]!.ToString()),
            data["terminalId"]!.ToString(),
            Convert.ToInt32(data["paymentStatus"]!.ToString())
        );
    }

    public PaymentTaskItem CloneTaskItem()
    {
        return new PaymentTaskItem(
            PaymentId,
            PurchaseId,
            Amount,
            Currency,
            ShopId,
            Account,
            PaidPoint,
            PaidValue,
            FeePoint,
            FeeValue,
            TotalPoint,
            TotalValue,
            TerminalId,
            PaymentStatus
        );
    }
}

public class PaymentTaskItemShort(
    string paymentId,
    string purchaseId,
    BigInteger amount,
    string currency,
    string shopId,
    string account,
    string terminalId,
    int paymentStatus)
{
    public readonly string PaymentId = paymentId;
    public readonly string PurchaseId = purchaseId;
    public readonly BigInteger Amount = amount;
    public readonly string Currency = currency;
    public readonly string ShopId = shopId;
    public readonly string Account = account;
    public readonly string TerminalId = terminalId;
    public readonly int PaymentStatus = paymentStatus;

    public static PaymentTaskItemShort FromJObject(JObject data)
    {
        return new PaymentTaskItemShort(
            data["paymentId"]!.ToString(),
            data["purchaseId"]!.ToString(),
            BigInteger.Parse(data["amount"]!.ToString()),
            data["currency"]!.ToString(),
            data["shopId"]!.ToString(),
            data["account"]!.ToString(),
            data["terminalId"]!.ToString(),
            Convert.ToInt32(data["paymentStatus"]!.ToString())
        );
    }

    public PaymentTaskItemShort CloneTaskItem()
    {
        return new PaymentTaskItemShort(
            PaymentId,
            PurchaseId,
            Amount,
            Currency,
            ShopId,
            Account,
            TerminalId,
            PaymentStatus
        );
    }
}

public class ShopTaskItem(
    string taskId,
    string shopId,
    string name,
    string currency,
    int status,
    string account,
    string terminalId,
    int taskStatus)
{
    public readonly string TaskId = taskId;
    public readonly string ShopId = shopId;
    public readonly string Name = name;
    public readonly string Currency = currency;
    public readonly int Status = status;
    public readonly string Account = account;
    public readonly string TerminalId = terminalId;
    public readonly int TaskStatus = taskStatus;

    public static ShopTaskItem FromJObject(JObject data)
    {
        return new ShopTaskItem(
            data["taskId"]!.ToString(),
            data["shopId"]!.ToString(),
            data["name"]!.ToString(),
            data["currency"]!.ToString(),
            Convert.ToInt32(data["status"]!.ToString()),
            data["account"]!.ToString(),
            data["terminalId"]!.ToString(),
            Convert.ToInt32(data["taskStatus"]!.ToString())
        );
    }

    public ShopTaskItem CloneTaskItem()
    {
        return new ShopTaskItem(
            TaskId,
            ShopId,
            Name,
            Currency,
            Status,
            Account,
            TerminalId,
            TaskStatus
        );
    }
}

public class ChainNetwork(
    string name,
    long chainId,
    string ensAddress,
    BigInteger chainTransferFee,
    BigInteger chainBridgeFee,
    BigInteger loyaltyTransferFee,
    BigInteger loyaltyBridgeFee
)
{
    public readonly string Name = name;
    public readonly long ChainId = chainId;
    public readonly string EnsAddress = ensAddress;
    public readonly BigInteger ChainTransferFee = chainTransferFee;
    public readonly BigInteger ChainBridgeFee = chainBridgeFee;
    public readonly BigInteger LoyaltyTransferFee = loyaltyTransferFee;
    public readonly BigInteger LoyaltyBridgeFee = loyaltyBridgeFee;
    public static ChainNetwork FromJObject(JObject data)
    {
        return new ChainNetwork(
            data["name"]!.ToString(),
            Convert.ToInt64(data["chainId"]!.ToString()),
            data["ensAddress"]!.ToString(),
            BigInteger.Parse(data["chainTransferFee"]!.ToString()),
            BigInteger.Parse(data["chainBridgeFee"]!.ToString()),
                BigInteger.Parse(data["loyaltyTransferFee"]!.ToString()),
                    BigInteger.Parse(data["loyaltyBridgeFee"]!.ToString())
        );
    }
}

public class ChainContract(
    string token,
    string chainBridge,
    string loyaltyBridge
)
{
    public readonly string Token = token;
    public readonly string ChainBridge = chainBridge;
    public readonly string LoyaltyBridge = loyaltyBridge;
    public static ChainContract FromJObject(JObject data)
    {
        return new ChainContract(
            data["token"]!.ToString(),
            data["chainBridge"]!.ToString(),
            data["loyaltyBridge"]!.ToString()
        );
    }
}

public class ChainInfo(string url, ChainNetwork network, ChainContract contract)
{
    public string Url = url;
    public ChainNetwork Network = network;
    public ChainContract Contract = contract;
    public static ChainInfo? FromJObject(JObject data)
    {
        var network = data["network"]!.ToObject<JObject>();
        var contract = data["contract"]!.ToObject<JObject>();
        if ((network != null) && (contract != null))
        {
            return new ChainInfo(
                data["url"]!.ToString(),
                ChainNetwork.FromJObject(network),
                ChainContract.FromJObject(contract));
        }
        else
        {
            return null;
        }
    }
}


public class ShopData(
    string shopId,
    string name,
    string currency,
    string account,
    string delegator,
    BigInteger providedAmount,
    BigInteger usedAmount,
    BigInteger collectedAmount,
    BigInteger refundedAmount,
    int status
)
{
    public readonly string ShopId = shopId;
    public readonly string Name = name;
    public readonly string Currency = currency;
    public readonly string Account = account;
    public readonly string Delegator = delegator;
    public readonly BigInteger ProvidedAmount = providedAmount;
    public readonly BigInteger UsedAmount = usedAmount;
    public readonly BigInteger CollectedAmount = collectedAmount;
    public readonly BigInteger RefundedAmount = refundedAmount;
    public readonly int Status = status;
    
    public readonly BigInteger SettledAmount = 
        BigInteger.Add(collectedAmount, usedAmount).CompareTo(providedAmount) > 0 
            ? BigInteger.Subtract(BigInteger.Add(collectedAmount, usedAmount), providedAmount) 
            : BigInteger.Zero;
    
    public static ShopData FromJObject(JObject data)
    {
        {
            return new ShopData(
                data["shopId"]!.ToString(),
                data["name"]!.ToString(),
                data["currency"]!.ToString(),
                data["account"]!.ToString(),
                data["delegator"]!.ToString(),
                BigInteger.Parse(data["providedAmount"]!.ToString()),
                BigInteger.Parse(data["usedAmount"]!.ToString()),
                BigInteger.Parse(data["collectedAmount"]!.ToString()),
                BigInteger.Parse(data["refundedAmount"]!.ToString()),
                Convert.ToInt32(data["status"]!.ToString())
            );
        }
    }
}


public class ShopRefundableData (BigInteger refundableAmount, BigInteger refundableToken)
{
    public readonly BigInteger RefundableAmount = refundableAmount;
    public readonly BigInteger RefundableToken = refundableToken;
    
    public static ShopRefundableData FromJObject(JObject data)
    {
        {
            return new ShopRefundableData(
                BigInteger.Parse(data["refundableAmount"]!.ToString()),
                BigInteger.Parse(data["refundableToken"]!.ToString())
            );
        }
    }
}