namespace Kios.Service.Sdk.Test;

using Client;
using Types;

public class ClientTests
{
    private Client client = new(NetWorkType.ACC_TestNet);

    [SetUp]
    public void Setup()
    {
    }

    [Test]
    public async Task Test01_GetChainId()
    {
        var chainId = await client.GetChainId();
        Assert.That(chainId, Is.EqualTo(215115));
    }

    [Test]
    public async Task Test02_GetBalanceAccount()
    {
        var balance = await client.GetBalanceAccount("0x20eB9941Df5b95b1b1AfAc1193c6a075B6191563");
        Assert.Multiple(() =>
        {
            Assert.That(balance.Point.Balance.ToString(), Is.EqualTo("5000000000000000000000000"));
            Assert.That(balance.Token.Balance.ToString(), Is.EqualTo("100000000000000000000000"));
        });
    }

    [Test]
    public async Task Test03_GetBalancePhone()
    {
        var balance = await client.GetBalancePhone("+82 10-1000-2099");
        Assert.Multiple(() =>
        {
            Assert.That(balance.Point.Balance.ToString(), Is.EqualTo("5000000000000000000000000"));
            Assert.That(balance.Token.Balance.ToString(), Is.EqualTo("0"));
        });
    }

    [Test]
    public async Task Test04_GetBalancePhoneHash()
    {
        var balance =
            await client.GetBalancePhoneHash("0x6e2f492102956a83a350152070be450b44fa19c08455c74b3aa79cc74195d3ba");

        Assert.Multiple(() =>
        {
            Assert.That(balance.Point.Balance.ToString(), Is.EqualTo("5000000000000000000000000"));
            Assert.That(balance.Token.Balance.ToString(), Is.EqualTo("0"));
        });
    }

    [Test]
    public async Task Test05_GetLedgerNonceOf()
    {
        var nonce = await client.GetLedgerNonceOf("0x20eB9941Df5b95b1b1AfAc1193c6a075B6191563");
        Assert.Multiple(() => { Assert.That(nonce, Is.GreaterThanOrEqualTo(0)); });
    }

    [Test]
    public async Task Test06_GetPhoneHash()
    {
        var info = await client.GetPhoneHash("+82 10-9000-5000");
        Assert.That(info.PhoneHash,
            Is.EqualTo("0x8f01f960fa3bacb03c4217e254a031bd005b1685002a1826141a90f1692ca2c4"));
    }

    [Test]
    public async Task Test07_GetPhoneHash2()
    {
        var info = await client.GetPhoneHash("");
        Assert.That(info.PhoneHash,
            Is.EqualTo("0x32105b1d0b88ada155176b58ee08b45c31e4f2f7337475831982c313533b880c"));
    }
}