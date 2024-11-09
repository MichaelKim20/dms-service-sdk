using System.Text;
using System.Net;
using Newtonsoft.Json.Linq;
using Nethereum.Signer;
using System.Numerics;
using Nethereum.ABI;
using Nethereum.Signer;
using Org.BouncyCastle.Crypto.Digests;
using Nethereum.Hex.HexConvertors.Extensions;

namespace Acc.Service.Sdk.Client;

using Utils;
using Types;

public class SettlementClientForShop(NetWorkType network, string privateKey, string shopId) : SettlementClient(network, privateKey, shopId)
{
    public async Task<string> GetSettlementManager()
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/shop/settlement/manager/get/{ShopId}");
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("managerId")!.ToString();
    }

    public async Task<string> SetSettlementManager(string managerId)
    {
        var message =
            CommonUtils.GetSetSettlementManagerMessage(
                ShopId, 
                managerId, 
                await GetShopNonceOf(Address), 
                await GetChainId());
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "shopId", ShopId },
            { "account", Address },
            { "managerId", managerId },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/shop/settlement/manager/set", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }

    public async Task<string> RemoveSettlementManager()
    {
        var message =
            CommonUtils.GetRemoveSettlementManagerMessage(
                ShopId, 
                await GetShopNonceOf(Address), 
                await GetChainId());
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "shopId", ShopId },
            { "account", Address },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/shop/settlement/manager/remove", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }
    
    public async Task<string> GetAgentOfRefund()
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/agent/refund/{Address}");
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("agent")!.ToString();
    }

    public async Task<string> SetAgentOfRefund(string agent)
    {
        var message =
            CommonUtils.GetRegisterAgentMessage(
                Address, 
                agent, 
                await GetLedgerNonceOf(Address), 
                await GetChainId());
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "account", Address },
            { "agent", agent },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/agent/refund", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }

    public async Task<string> GetAgentOfWithdrawal()
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/agent/withdrawal/{Address}");
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("agent")!.ToString();
    }

    public async Task<string> SetAgentOfWithdrawal(string agent)
    {
        var message =
            CommonUtils.GetRegisterAgentMessage(
                Address, 
                agent, 
                await GetLedgerNonceOf(Address), 
                await GetChainId());
        var signature = CommonUtils.SignMessage(KeyPair, message);

        var body = new StringContent(new JObject
        {
            { "account", Address },
            { "agent", agent },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/agent/withdrawal", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }
}
