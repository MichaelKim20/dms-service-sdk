using Nethereum.Util;
using System.Numerics;
using Kios.Service.Sdk.Client;
using Kios.Service.Sdk.Types;
using Kios.Service.Sdk.Utils;

namespace Kios.Service.Sdk.Sample;

public class ProviderClientSample
{
    private ProviderClient providerClient;
    private ProviderClient agentClient;

    public ProviderClientSample()
    {
        providerClient = new ProviderClient(NetWorkType.ACC_TestNet,
            "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
        agentClient = new ProviderClient(NetWorkType.ACC_TestNet,
            "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab");
    }

    private async Task Test01_IsProvider()
    {
        Console.WriteLine("Test01_IsProvider");
        var value = await providerClient.IsProvider(providerClient.Address);
        if (!value) Console.WriteLine("Error");
    }

    private async Task Test02_ClearAgent()
    {
        Console.WriteLine("Test02_ClearAgent");
        await providerClient.SetAgent(AddressUtil.ZERO_ADDRESS);
        if (!(await providerClient.GetAgent()).Equals(AddressUtil.ZERO_ADDRESS)) Console.WriteLine("Error");
    }

    private async Task Test03_ProvideToAddress()
    {
        Console.WriteLine("Test03_ProvideToAddress");
        var receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
        var res1 = await providerClient.GetBalanceAccount(receiver);
        var oldBalance = res1.Point.Balance;

        var amount = Amount.Make("100").Value;
        await providerClient.ProvideToAddress(providerClient.Address, receiver, amount);
        var res2 = await providerClient.GetBalanceAccount(receiver);

        if (!res2.Point.Balance.Equals(BigInteger.Add(oldBalance, amount))) Console.WriteLine("Error");
    }

    private async Task Test04_ProvideToPhone()
    {
        Console.WriteLine("Test04_ProvideToPhone");
        var phoneNumber = "+82 10-9000-5000";
        var res1 = await providerClient.GetBalancePhone(phoneNumber);
        var oldBalance = res1.Point.Balance;
        var amount = Amount.Make("100").Value;

        await providerClient.ProvideToPhone(providerClient.Address, phoneNumber, amount);
        var res2 = await providerClient.GetBalancePhone(phoneNumber);

        if (!res2.Point.Balance.Equals(BigInteger.Add(oldBalance, amount))) Console.WriteLine("Error");
    }

    private async Task Test05_SetNewAgent()
    {
        Console.WriteLine("Test05_SetNewAgent");
        await providerClient.SetAgent(agentClient.Address);
        if (!(await providerClient.GetAgent()).Equals(agentClient.Address)) Console.WriteLine("Error");
    }

    private async Task Test06_ProvideToAddress()
    {
        Console.WriteLine("Test06_ProvideToAddress");
        var receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
        var res1 = await providerClient.GetBalanceAccount(receiver);
        var oldBalance = res1.Point.Balance;

        var amount = Amount.Make("100").Value;
        await agentClient.ProvideToAddress(providerClient.Address, receiver, amount);
        var res2 = await providerClient.GetBalanceAccount(receiver);

        if (!res2.Point.Balance.Equals(BigInteger.Add(oldBalance, amount))) Console.WriteLine("Error");
    }

    private async Task Test07_ProvideToPhone()
    {
        Console.WriteLine("Test07_ProvideToPhone");
        var phoneNumber = "+82 10-9000-5000";
        var res1 = await providerClient.GetBalancePhone(phoneNumber);
        var oldBalance = res1.Point.Balance;
        var amount = Amount.Make("100").Value;

        await agentClient.ProvideToPhone(providerClient.Address, phoneNumber, amount);
        var res2 = await providerClient.GetBalancePhone(phoneNumber);

        if (!res2.Point.Balance.Equals(BigInteger.Add(oldBalance, amount))) Console.WriteLine("Error");
    }

    public async Task TestAll()
    {
        await Test01_IsProvider();
        await Test02_ClearAgent();
        await Test03_ProvideToAddress();
        await Test04_ProvideToPhone();
        await Test05_SetNewAgent();
        await Test06_ProvideToAddress();
        await Test07_ProvideToPhone();
    }
}