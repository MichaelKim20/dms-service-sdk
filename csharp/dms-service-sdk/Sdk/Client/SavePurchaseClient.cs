using System.Text;
using System.Net;
using Newtonsoft.Json.Linq;
using Nethereum.Signer;
using System.Numerics;
using Nethereum.Util;

namespace Dms.Service.Sdk.Client;

using Utils;
using Types;

public class SavePurchaseClient(NetWorkType network, string privateKey, string assetAddress)
    : Client(network)
{
    private readonly EthECKey _keyPair = new(privateKey);
    public string Address => _keyPair.GetPublicAddress();

    private BigInteger GetLoyaltyInTransaction(
        BigInteger cashAmount,
        BigInteger totalAmount,
        List<SaveNewDetail> details
    )
    {
        if (totalAmount.Equals(BigInteger.Zero)) return BigInteger.Zero;
        if (cashAmount.Equals(BigInteger.Zero)) return BigInteger.Zero;
        var sum = details.Aggregate(BigInteger.Zero,
            (current, elem) => BigInteger.Add(current, BigInteger.Multiply(elem.Amount, elem.ProvidePercent)));
        return CommonUtils.ZeroGwei(
            BigInteger.Divide(BigInteger.Divide(BigInteger.Multiply(sum, cashAmount), totalAmount),
                new BigInteger(10000)));
    }

    /**
     * Save purchase data
     * @param purchaseId is PurchaseId ID
     * @param timestamp Purchase Time
     * @param waiting Wait time (in seconds) for points to be provided
     * @param totalAmount Total Purchase Amount
     * @param cacheAmount Amount purchased in cash
     * @param currency is Currency symbol (case letter)
     * @param shopId Shop ID
     * @param userAccount User's wallet address
     * @param userPhone User's phone number
     * @param details Unit price and accumulated rate of purchased goods
     */
    public ResponseSavePurchase SaveNewPurchase(
        string purchaseId,
        long timestamp,
        long waiting,
        string totalAmount,
        string cacheAmount,
        string currency,
        string shopId,
        string userAccount,
        string userPhone,
        PurchaseDetail[] details
    )
    {
        var adjustedUserAccount =
            userAccount.Trim().Equals(string.Empty) ? AddressUtil.ZERO_ADDRESS : userAccount.Trim();

        var adjustedPurchase = new SaveNewPurchase(
            purchaseId,
            Amount.Make(cacheAmount).Value,
            BigInteger.Zero,
            currency,
            shopId,
            adjustedUserAccount,
            GetPhoneHash(userPhone),
            assetAddress,
            ""
        );
        var adjustedOthers = new SaveNewOthers(Amount.Make(totalAmount).Value, timestamp, waiting);
        var saveDetails = details.Select(detail => new SaveNewDetail(detail.ProductId, Amount.Make(detail.Amount).Value,
            BigInteger.Multiply(detail.ProvidePercent, new BigInteger(100)))).ToList();

        adjustedPurchase.Loyalty =
            GetLoyaltyInTransaction(adjustedPurchase.CashAmount, adjustedOthers.TotalAmount, saveDetails);

        var message = CommonUtils.GetNewPurchaseDataMessage(
            adjustedPurchase.PurchaseId,
            adjustedPurchase.CashAmount,
            adjustedPurchase.Loyalty,
            adjustedPurchase.Currency,
            adjustedPurchase.ShopId,
            adjustedPurchase.UserAccount,
            adjustedPurchase.UserPhoneHash,
            adjustedPurchase.Sender,
            GetChainId()
        );
        adjustedPurchase.PurchaseSignature = CommonUtils.SignMessage(_keyPair, message);

        var request =
            GetHttpRequest($"{SaveEndpoint}/v2/tx/purchase/new", "POST");

        var purchaseObj = new JObject
        {
            { "purchaseId", adjustedPurchase.PurchaseId },
            { "cashAmount", adjustedPurchase.CashAmount.ToString() },
            { "loyalty", adjustedPurchase.Loyalty.ToString() },
            { "currency", adjustedPurchase.Currency },
            { "shopId", adjustedPurchase.ShopId },
            { "userAccount", adjustedPurchase.UserAccount },
            { "userPhoneHash", adjustedPurchase.UserPhoneHash },
            { "sender", adjustedPurchase.Sender },
            { "purchaseSignature", adjustedPurchase.PurchaseSignature }
        };

        var othersObj = new JObject
        {
            { "totalAmount", adjustedOthers.TotalAmount.ToString() },
            { "timestamp", adjustedOthers.Timestamp.ToString() },
            { "waiting", adjustedOthers.Waiting.ToString() }
        };

        var detailsObj = new JArray();
        foreach (var elemObj in saveDetails.Select(elem => new JObject
                 {
                     { "productId", elem.ProductId },
                     { "amount", elem.Amount.ToString() },
                     { "providePercent", elem.ProvidePercent.ToString() }
                 }))
            detailsObj.Add(elemObj);

        var json = new JObject
        {
            { "purchase", purchaseObj },
            { "others", othersObj },
            { "details", detailsObj }
        };
        var body = Encoding.UTF8.GetBytes(json.ToString());
        using (var stream = request.GetRequestStream())
        {
            stream.Write(body, 0, body.Length);
        }

        var response = (HttpWebResponse)request.GetResponse();
        return ResponseSavePurchase.FromJObject(GetJObjectResponse(response));
    }

    /**
     * Cancellation process for payments that have already been completed
     * @param purchaseId is PurchaseId ID
     * @param timestamp Purchase Time
     * @param waiting Wait time (in seconds) for points to be provided
     */
    public ResponseSavePurchase SaveCancelPurchase(
        string purchaseId,
        long timestamp,
        long waiting
    )
    {
        var adjustedPurchase = new SaveCancelPurchase(
            purchaseId,
            assetAddress,
            ""
        );
        var message = CommonUtils.GetCancelPurchaseDataMessage(
            adjustedPurchase.PurchaseId,
            adjustedPurchase.Sender,
            GetChainId()
        );
        adjustedPurchase.PurchaseSignature = CommonUtils.SignMessage(_keyPair, message);
        var adjustedOthers = new SaveCancelOthers(timestamp, waiting);

        var request =
            GetHttpRequest($"{SaveEndpoint}/v2/tx/purchase/cancel", "POST");

        var body = Encoding.UTF8.GetBytes(new JObject
        {
            {
                "purchase", new JObject()
                {
                    { "purchaseId", adjustedPurchase.PurchaseId },
                    { "sender", adjustedPurchase.Sender },
                    { "purchaseSignature", adjustedPurchase.PurchaseSignature }
                }
            },
            {
                "others", new JObject()
                {
                    { "timestamp", adjustedOthers.Timestamp },
                    { "waiting", adjustedOthers.Waiting }
                }
            }
        }.ToString());

        using (var stream = request.GetRequestStream())
        {
            stream.Write(body, 0, body.Length);
        }

        var response = (HttpWebResponse)request.GetResponse();
        return ResponseSavePurchase.FromJObject(GetJObjectResponse(response));
    }
}
