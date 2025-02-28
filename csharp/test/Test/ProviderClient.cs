using Nethereum.Util;

namespace Kios.Service.Sdk.Test;

using Client;
using Types;
using Utils;
using System.Numerics;

public class ProviderClientTests
{
    private NetWorkType network = NetWorkType.ACC_TestNet;
    private ProviderClient providerClient;
    private ProviderClient agentClient;

    public ProviderClientTests()
    {
        providerClient =
            new ProviderClient(network, "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
        agentClient = new ProviderClient(network, "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab");
    }

    [SetUp]
    public void Setup()
    {
    }

    [Test]
    public async Task Test01_IsProvider()
    {
        var value = await providerClient.IsProvider(providerClient.Address);
        Assert.That(value, Is.EqualTo(true));
    }

    [Test]
    public async Task Test02_ClearAgent()
    {
        await providerClient.SetAgent(AddressUtil.ZERO_ADDRESS);
        Assert.That(await providerClient.GetAgent(), Is.EqualTo(AddressUtil.ZERO_ADDRESS));
    }

    [Test]
    public async Task Test03_ProvideToAddress()
    {
        var receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
        var res1 = await providerClient.GetBalanceAccount(receiver);
        var oldBalance = res1.Point.Balance;

        var amount = Amount.Make("100").Value;
        await providerClient.ProvideToAddress(providerClient.Address, receiver, amount);
        var res2 = await providerClient.GetBalanceAccount(receiver);

        Assert.That(res2.Point.Balance, Is.EqualTo(BigInteger.Add(oldBalance, amount)));
    }

    [Test]
    public async Task Test04_ProvideToPhone()
    {
        var phoneNumber = "+82 10-9000-5000";
        var res1 = await providerClient.GetBalancePhone(phoneNumber);
        var oldBalance = res1.Point.Balance;
        var amount = Amount.Make("100").Value;

        await providerClient.ProvideToPhone(providerClient.Address, phoneNumber, amount);
        var res2 = await providerClient.GetBalancePhone(phoneNumber);

        Assert.That(res2.Point.Balance, Is.EqualTo(BigInteger.Add(oldBalance, amount)));
    }

    [Test]
    public async Task Test05_SetNewAgent()
    {
        await providerClient.SetAgent(agentClient.Address);
        Assert.That(await providerClient.GetAgent(), Is.EqualTo(agentClient.Address));
    }

    [Test]
    public async Task Test06_ProvideToAddress()
    {
        var receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
        var res1 = await providerClient.GetBalanceAccount(receiver);
        var oldBalance = res1.Point.Balance;

        var amount = Amount.Make("100").Value;
        await agentClient.ProvideToAddress(providerClient.Address, receiver, amount);
        var res2 = await providerClient.GetBalanceAccount(receiver);

        Assert.That(res2.Point.Balance, Is.EqualTo(BigInteger.Add(oldBalance, amount)));
    }

    [Test]
    public async Task Test07_ProvideToPhone()
    {
        var phoneNumber = "+82 10-9000-5000";
        var res1 = await providerClient.GetBalancePhone(phoneNumber);
        var oldBalance = res1.Point.Balance;
        var amount = Amount.Make("100").Value;

        await agentClient.ProvideToPhone(providerClient.Address, phoneNumber, amount);
        var res2 = await providerClient.GetBalancePhone(phoneNumber);

        Assert.That(res2.Point.Balance, Is.EqualTo(BigInteger.Add(oldBalance, amount)));
    }
}