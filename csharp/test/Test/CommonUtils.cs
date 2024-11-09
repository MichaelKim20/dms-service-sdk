namespace Acc.Service.Sdk.Test;

using Nethereum.Signer;
using Utils;

public class CommonUtilsTest
{
    [SetUp]
    public void Setup()
    {
    }

    [Test]
    public void GetRegisterAgentMessage()
    {
        var message = CommonUtils.GetRegisterAgentMessage(
            "0x64D111eA9763c93a003cef491941A011B8df5a49",
            "0x3FE8D00143bd0eAd2397D48ba0E31E5E1268dBfb",
            45,
            215115);

        Assert.That(CommonUtils.ConvertByteToHexString(message),
            Is.EqualTo("0xd89c684325d02709927db1d839a58f05aa54a8ed21ec4da84fee82427e8286e6"));
    }

    [Test]
    public void SignMessage()
    {
        var message = CommonUtils.GetRegisterAgentMessage(
            "0x64D111eA9763c93a003cef491941A011B8df5a49",
            "0x3FE8D00143bd0eAd2397D48ba0E31E5E1268dBfb",
            45,
            215115);
        Assert.That(CommonUtils.ConvertByteToHexString(message),
            Is.EqualTo("0xd89c684325d02709927db1d839a58f05aa54a8ed21ec4da84fee82427e8286e6"));
        var keyPair = new EthECKey("0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
        var signature = CommonUtils.SignMessage(keyPair, message);
        Assert.That(signature,
            Is.EqualTo(
                "0x16c3db108967fef995b7e6a9439338af06886c568653ee849f5c6511ede9faa2351cd6c89f53f2d400810e07ab8fafeb5d5c19c4b917837b94de2a445ea518281c"));
    }

    [Test]
    public void GetTimeStamp()
    {
        var timeStamp = CommonUtils.GetTimeStamp();
        Assert.That(timeStamp,
            Is.LessThan(2000000000));

    }
}
