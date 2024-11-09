using Nethereum.Signer;

namespace Acc.Service.Sdk.Types;

public class ShopData(string shopId, string privateKey)
{
    private readonly EthECKey _keyPair = new(privateKey);
    public string ShopId = shopId;
    public string PrivateKey => _keyPair.GetPrivateKey();
    public string Address => _keyPair.GetPublicAddress();
}

public class UserData(string phoneNumber, string privateKey)
{
    private readonly EthECKey _keyPair = new(privateKey);
    public string PhoneNumber = phoneNumber;
    public string PrivateKey => _keyPair.GetPrivateKey();
    public string Address => _keyPair.GetPublicAddress();
}
