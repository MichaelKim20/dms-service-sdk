using System.Text;
using System.Net;
using Newtonsoft.Json.Linq;
using Nethereum.Signer;
using System.Numerics;

namespace Acc.Service.Sdk.Client;

using Utils;
using Types;

public class SettlementClient(NetWorkType network, string privateKey, string shopId) : Client(network)
{
    protected readonly EthECKey KeyPair = new(privateKey);
    public string Address => KeyPair.GetPublicAddress();
    public string ShopId { get; } = shopId;

    public async Task<int> GetSettlementClientLength()
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/shop/settlement/client/length/{ShopId}");
        var jObject = ParseResponseToJObject(response);
        return Convert.ToInt32(jObject.GetValue("length")!.ToString());
    }

    public async Task<string[]> GetSettlementClientList(int startIndex, int endIndex)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/shop/settlement/client/list/{ShopId}?startIndex={startIndex}&endIndex={endIndex}&endIndex={endIndex}");
        var jObject = ParseResponseToJObject(response);
        var clients = jObject.GetValue("clients")!.ToObject<JArray>();
        if (clients == null) throw new Exception("Internal Error : clients is null");
        var clientList = new string[clients.Count];
        for (var i = 0; i < clients.Count; i++)
        {
            clientList[i] = clients[i].ToString();  
        }
        return clientList;
    }

    public async Task<string> CollectSettlementAmountMultiClient(string[] clients)
    {
        var message =
            CommonUtils.GetCollectSettlementAmountMultiClientMessage(ShopId, clients, await GetShopNonceOf(Address), await GetChainId());
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "shopId", ShopId },
            { "account", Address },
            { "clients", string.Join(",", clients) },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/shop/settlement/collect", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }

    public async Task<ShopData> GetShopInfo()
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/shop/info/{ShopId}");
        var jObject = ParseResponseToJObject(response);
        return ShopData.FromJObject(jObject);
    }

    public async Task<string> GetAccountOfShopOwner()
    {
        var info = await GetShopInfo();
        return info.Account;
    }

    public async Task<ShopRefundableData> GetRefundable()
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/shop/refundable/{ShopId}");
        var jObject = ParseResponseToJObject(response);
        return ShopRefundableData.FromJObject(jObject);
    }

    public async Task<string> Refund(BigInteger amount)
    {
        var adjustedAmount = CommonUtils.ZeroGwei(amount);
        var message = CommonUtils.GetShopRefundMessage(ShopId, adjustedAmount, await GetShopNonceOf(Address), await GetChainId());
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "shopId", ShopId },
            { "account", Address },
            { "amount", adjustedAmount.ToString() },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/shop/refund", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }

    public async Task<string> Withdraw(BigInteger amount)
    {
        var chainInfo = await GetChainInfoOfSideChain();
        var adjustedAmount = CommonUtils.ZeroGwei(amount);
        var expiry = CommonUtils.GetTimeStamp() + 1800;
        var message = CommonUtils.GetTransferMessage(
            chainInfo.Network.ChainId,
            chainInfo.Contract.Token,
            Address,
            chainInfo.Contract.LoyaltyBridge,
            adjustedAmount,
            await GetLedgerNonceOf(Address),
            expiry);
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "account", Address },
            { "amount", adjustedAmount.ToString() },
            { "expiry", expiry },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/ledger/withdraw_via_bridge", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }
}
