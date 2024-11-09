using System.Text;
using System.Net;
using Newtonsoft.Json.Linq;
using Nethereum.Signer;
using System.Numerics;

namespace Acc.Service.Sdk.Client;

using Utils;
using Types;

public class ProviderClient(NetWorkType network, string privateKey) : Client(network)
{
    private readonly EthECKey _keyPair = new(privateKey);
    public string Address => _keyPair.GetPublicAddress();

    /**
     * Provide a nonce corresponding to the user's wallet address. It provides a nonce corresponding to the user's wallet address.
     * This ensures that the same signature is not repeated. And this value is recorded in Contract and automatically increases by 1.
     * @param account User's wallet address
     */
    public async Task<bool> IsProvider(string account)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/provider/status/{account.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return Convert.ToBoolean(jObject.GetValue("enable")!.ToString());
    }

    /**
     * Register the address of the assistant who directly delivers points for the registered wallet(this.wallet).
     * The assistant's wallet can be registered and used on the server.
     * The assistant does not have the authority to deposit and withdraw, only has the authority to provide points.
     * @param account Address of wallet for the agent
     */
    public async Task<string> SetAgent(string account)
    {
        var nonce = await GetLedgerNonceOf(Address);
        var message = CommonUtils.GetRegisterAgentMessage(
            Address,
            account,
            nonce,
            await GetChainId()
        );
        var signature = CommonUtils.SignMessage(_keyPair, message);

        var body = new StringContent(new JObject
        {
            { "provider", Address },
            { "assistant", account },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response =
            await PostAsync($"{RelayEndpoint}/v1/provider/assistant/register", body);

        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }

    /**
     * Provide the agent's address for the registered wallet(this.wallet)
     * @param provider This is provider's wallet address
     */
    public async Task<string> GetAgent(string provider)
    {
        var response = await GetAsync($"{RelayEndpoint}/v1/provider/assistant/{provider.Trim()}");
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("assistant")!.ToString();
    }

    /**
     * Provide the agent's address for the registered wallet(this.wallet)
     * @param provider This is provider's wallet address
     */
    public async Task<string> GetAgent()
    {
        return await GetAgent(Address);
    }

    /**
     * Points are provided to the specified address.
     * Registered wallets are used for signatures. Registered wallet(this.wallet) may be providers or helpers.
     * @param provider - wallet address of the resource provider
     * @param receiver - wallet address of the person who will receive the points
     * @param amount - amount of points
     */
    public async Task<string> ProvideToAddress(string provider, string receiver, BigInteger amount)
    {
        var message =
            CommonUtils.GetProvidePointToAddressMessage(provider, receiver, amount, await GetLedgerNonceOf(Address),
                await GetChainId());
        var signature = CommonUtils.SignMessage(_keyPair, message);

        var body = new StringContent(new JObject
        {
            { "provider", provider },
            { "receiver", receiver },
            { "amount", amount.ToString() },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/provider/send/account", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }

    /**
     * Points are provided to the specified phone number.
     * Registered wallets are used for signatures. Registered wallet(this.wallet) may be providers or helpers.
     * @param provider - wallet address of the resource provider
     * @param receiver - phone number of the person who will receive the points
     * @param amount - amount of points
     */
    public async Task<string> ProvideToPhone(string provider, string receiver, BigInteger amount)
    {
        var phoneHash = await GetPhoneHash(receiver);
        var message =
            CommonUtils.GetProvidePointToPhoneMessage(provider, phoneHash, amount, await GetLedgerNonceOf(Address),
                await GetChainId());
        var signature = CommonUtils.SignMessage(_keyPair, message);

        var body = new StringContent(new JObject
        {
            { "provider", provider },
            { "receiver", phoneHash },
            { "amount", amount.ToString() },
            { "signature", signature }
        }.ToString(), Encoding.UTF8, "application/json");

        var response = await PostAsync($"{RelayEndpoint}/v1/provider/send/phoneHash", body);
        var jObject = ParseResponseToJObject(response);
        return jObject.GetValue("txHash")!.ToString();
    }
}
