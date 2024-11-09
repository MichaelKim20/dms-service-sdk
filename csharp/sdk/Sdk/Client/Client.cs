using System.Numerics;
using Acc.Service.Sdk.Utils;
using Newtonsoft.Json.Linq;

namespace Acc.Service.Sdk.Client;

using Types;

/// <summary>
/// The client class of decentralized loyalty services
/// </summary>
public class Client
{
    /// <summary>
    /// The endpoint of the relay API server
    /// </summary>
    protected readonly string RelayEndpoint;

    /// <summary>
    /// The endpoint of the save purchase API server
    /// </summary>
    protected readonly string SaveEndpoint;

    /// <summary>
    /// The Chain ID of side chain
    /// </summary>
    private long _chainId;

    /// <summary>
    /// Constructor
    /// </summary>
    /// <param name="network">Type of network (mainnet, testnet, localhost)</param>
    public Client(NetWorkType network)
    {
        switch (network)
        {
            case NetWorkType.MainNet:
                RelayEndpoint = "https://relay.main.acccoin.io";
                SaveEndpoint = "https://save.main.acccoin.io";
                break;
            case NetWorkType.TestNet:
                RelayEndpoint = "https://relay.test.acccoin.io";
                SaveEndpoint = "https://save.test.acccoin.io";
                break;
            case NetWorkType.LocalHost:
                RelayEndpoint = "http://127.0.0.1:7070";
                SaveEndpoint = "http://127.0.0.1:3030";
                break;
            default:
                RelayEndpoint = "https://relay.main.acccoin.io";
                SaveEndpoint = "https://save.main.acccoin.io";
                break;
        }

        _chainId = 0;
    }

    /// <summary>
    /// HTTP Get
    /// </summary>
    /// <param name="url">Endpoint URL</param>
    /// <returns>HttpWebRequest</returns>
    protected static async Task<string> GetAsync(string url)
    {
        var client = new HttpClient();
        using var response = await client.GetAsync(url);

        return await response.Content.ReadAsStringAsync();
    }

    /// <summary>
    /// HTTP Post
    /// </summary>
    /// <param name="url">Endpoint URL</param>
    /// <param name="body">data</param>
    /// <returns>HttpWebRequest</returns>
    protected static async Task<string> PostAsync(string url, StringContent body)
    {
        var client = new HttpClient();
        using var response = await client.PostAsync(url, body);

        return await response.Content.ReadAsStringAsync();
    }

    private static JObject ParseResponse(string data)
    {
        var jObject = JObject.Parse(data);
        var code = Convert.ToInt32(jObject["code"]!.ToString());
        if (code != 0)
        {
            string errorMessage;
            try
            {
                errorMessage = $"{jObject["error"]!["message"]} {code}";
            }
            catch (Exception)
            {
                errorMessage = $"{code}";
            }

            throw new Exception("Internal Error : " + errorMessage);
        }

        return jObject;
    }

    protected static JObject ParseResponseToJObject(string data)
    {
        var jObject = ParseResponse(data);
        var res = jObject.GetValue("data")!.ToObject<JObject>();
        if (res == null) throw new Exception("Internal Error : Response is null");
        return res;
    }
    
    protected static JArray ParseResponseToJArray(string data)
    {
        var jObject = ParseResponse(data);
        var res = jObject.GetValue("data")!.ToObject<JArray>();
        if (res == null) throw new Exception("Internal Error : Response is null");
        return res;
    }

    /**
     * Provide the ID of the chain
     */
    public async Task<long> GetChainId()
    {
        if (_chainId != 0)
            return _chainId;

        var data = await GetAsync($"{RelayEndpoint}/v1/chain/side/id");
        var jObject = ParseResponseToJObject(data);

        _chainId = Convert.ToInt64(jObject["chainId"]!.ToString());
        return _chainId;
    }

    /**
     * Provide the user's points and token balance information
     * @param account User's wallet address
     */
    public async Task<UserBalanceData> GetBalanceAccount(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/ledger/balance/account/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        var point = jObject.GetValue("point")!.ToObject<JObject>();
        var token = jObject.GetValue("token")!.ToObject<JObject>();
        if (point == null || token == null) throw new Exception("Internal Error : Response is null");
        return new UserBalanceData(point, token);
    }

    /**
     * Provide the user's points and token balance information
     * @param phoneNumber User's phone number
     */
    public async Task<UserBalanceData> GetBalancePhone(string phoneNumber)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/ledger/balance/phone/{phoneNumber.Trim().Replace(" ", "%20")}");
        var jObject = ParseResponseToJObject(response);
        var point = jObject.GetValue("point")!.ToObject<JObject>();
        var token = jObject.GetValue("token")!.ToObject<JObject>();
        if (point == null || token == null) throw new Exception("Internal Error : Response is null");
        return new UserBalanceData(point, token);
    }

    /**
     * Provide the user's points and token balance information
     * @param phoneHash User's phone number hash
     */
    public async Task<UserBalanceData> GetBalancePhoneHash(string phoneHash)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/ledger/balance/phoneHash/{phoneHash.Trim()}");
        var jObject = ParseResponseToJObject(response);
        var point = jObject.GetValue("point")!.ToObject<JObject>();
        var token = jObject.GetValue("token")!.ToObject<JObject>();
        if (point == null || token == null) throw new Exception("Internal Error : Response is null");
        return new UserBalanceData(point, token);
    }

    /**
     * Provide a nonce corresponding to the user's wallet address. It provides a nonce corresponding to the user's wallet address.
     * This ensures that the same signature is not repeated. And this value is recorded in Contract and automatically increases by 1.
     * @param account User's wallet address
     */
    public async Task<long >GetLedgerNonceOf(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/ledger/nonce/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return Convert.ToInt64(jObject.GetValue("nonce")!.ToString());
    }

    public async Task<long >GetShopNonceOf(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/shop/nonce/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return Convert.ToInt64(jObject.GetValue("nonce")!.ToString());
    }

    public async Task<string> GetPhoneHash(string phone)
    {
        if (phone.Trim().Equals("")) return CommonUtils.GetPhoneHash("");
        var response = await GetAsync($"{RelayEndpoint}/v1/phone/hash/{phone.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("phoneHash")!.ToString();
    }


    private ChainInfo? _mainChainInfo;
    public async Task<ChainInfo >GetChainInfoOfMainChain()
    {
        if (_mainChainInfo != null) return _mainChainInfo;
        
        var response = await GetAsync($"{RelayEndpoint}/v1/chain/main/info/");
        var jObject = ParseResponseToJObject(response);
        var info  = ChainInfo.FromJObject(jObject);
        if (info == null ) throw new Exception("Internal Error : Response is null");

        return _mainChainInfo = info;
    }

    public async Task<long> GetChainIdOfMainChain()
    {
        var info = await GetChainInfoOfMainChain();
        return info.Network.ChainId;
    }

    public async Task<long >GetNonceOfMainChainToken(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/token/main/nonce/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return Convert.ToInt64(jObject.GetValue("nonce")!.ToString());
    }
    
    public async Task<BigInteger >GetBalanceOfMainChainToken(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/token/main/balance/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return BigInteger.Parse(jObject.GetValue("balance")!.ToString());
    }
    
    private ChainInfo? _sideChainInfo;
    public async Task<ChainInfo >GetChainInfoOfSideChain()
    {
        if (_sideChainInfo != null) return _sideChainInfo;
        
        var response = await GetAsync($"{RelayEndpoint}/v1/chain/side/info/");
        var jObject = ParseResponseToJObject(response);
        var info  = ChainInfo.FromJObject(jObject);
        if (info == null ) throw new Exception("Internal Error : Response is null");

        return _sideChainInfo = info;
    }

    public async Task<long> GetChainIdOfSideChain()
    {
        var info = await GetChainInfoOfSideChain();
        return info.Network.ChainId;
    }

    public async Task<long >GetNonceOfSideChainToken(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/token/side/nonce/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return Convert.ToInt64(jObject.GetValue("nonce")!.ToString());
    }
    
    public async Task<BigInteger >GetBalanceOfSideChainToken(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/token/side/balance/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return BigInteger.Parse(jObject.GetValue("balance")!.ToString());
    }
}
