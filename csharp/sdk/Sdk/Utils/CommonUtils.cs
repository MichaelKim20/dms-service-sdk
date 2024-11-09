using System.Numerics;
using System.Text;
using Nethereum.ABI;
using Nethereum.Signer;
using Org.BouncyCastle.Crypto.Digests;
using Nethereum.Hex.HexConvertors.Extensions;

namespace Acc.Service.Sdk.Utils;

public static class CommonUtils
{
    public static string ConvertByteToHexString(byte[] bytes)
    {
        return bytes.ToHex(true);
    }

    public static byte[] ConvertHexStringToByte(string hexString)
    {
        return hexString.HexToByteArray();
    }

    public static string GetPhoneHash(string phone)
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "BOSagora Phone Number"),
            new ABIValue("string", phone)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return ConvertByteToHexString(message);
    }

    public static long GetTimeStamp()
    {
        return ((DateTimeOffset)DateTime.UtcNow).ToUnixTimeSeconds();
    }

    public static BigInteger ZeroGwei(BigInteger value)
    {
        return BigInteger.Multiply(BigInteger.Divide(value, 1000000000), 1000000000);
    }

    // region Provider

    public static byte[] GetProvidePointToAddressMessage(
        string provider,
        string receiver,
        BigInteger amount,
        long nonce,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("address", provider),
            new ABIValue("address", receiver),
            new ABIValue("uint256", amount),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetProvidePointToPhoneMessage(
        string provider,
        string receiver,
        BigInteger amount,
        long nonce,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("address", provider),
            new ABIValue("bytes32", ConvertHexStringToByte(receiver)),
            new ABIValue("uint256", amount),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );

        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetRegisterAgentMessage(
        string provider,
        string assistance,
        long nonce,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("address", provider),
            new ABIValue("address", assistance),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetAccountMessage(string account, long nonce, long chainId)
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("address", account),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }
    // endregion

    // region Payment
    public static byte[] GetOpenNewPaymentMessage(
        string purchaseId,
        BigInteger amount,
        string currency,
        string shopId,
        string account,
        string terminalId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "OpenNewPayment"),
            new ABIValue("string", purchaseId),
            new ABIValue("uint256", amount),
            new ABIValue("string", currency),
            new ABIValue("bytes32", ConvertHexStringToByte(shopId)),
            new ABIValue("address", account),
            new ABIValue("string", terminalId)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetCloseNewPaymentMessage(string paymentId, bool confirm)
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "CloseNewPayment"),
            new ABIValue("string", paymentId),
            new ABIValue("uint256", confirm ? 1 : 0)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetOpenCancelPaymentMessage(string paymentId, string terminalId)
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "OpenCancelPayment"),
            new ABIValue("string", paymentId),
            new ABIValue("string", terminalId)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetCloseCancelPaymentMessage(string paymentId, bool confirm)
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "CloseCancelPayment"),
            new ABIValue("string", paymentId),
            new ABIValue("uint256", confirm ? 1 : 0)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    // endregion

    // region SavePurchase
    public static byte[] GetNewPurchaseDataMessage(
        string purchaseId,
        BigInteger amount,
        BigInteger loyalty,
        string currency,
        string shopId,
        string account,
        string phone,
        string sender,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", purchaseId),
            new ABIValue("uint256", amount),
            new ABIValue("uint256", loyalty),
            new ABIValue("string", currency),
            new ABIValue("bytes32", ConvertHexStringToByte(shopId)),
            new ABIValue("address", account),
            new ABIValue("bytes32", ConvertHexStringToByte(phone)),
            new ABIValue("address", sender),
            new ABIValue("uint256", chainId)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetCancelPurchaseDataMessage(string purchaseId, string sender, long chainId)
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", purchaseId),
            new ABIValue("address", sender),
            new ABIValue("uint256", chainId)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }
    // endregion
    
    // region
    public static byte[] GetCollectSettlementAmountMultiClientMessage(
        string managerShopId,
        string[] clientShopIds,
        long nonce,
        long chainId
    )
    {
        var ids = clientShopIds.Select(ConvertHexStringToByte).ToArray();
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "CollectSettlementAmountMultiClient"),
            new ABIValue("bytes32", ConvertHexStringToByte(managerShopId)),
            new ABIValue("bytes32[]", ids),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetShopRefundMessage(
        string shopId,
        BigInteger amount,
        long nonce,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("bytes32", ConvertHexStringToByte(shopId)),
            new ABIValue("uint256", amount),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetTransferMessage(
        long chainId,
        string tokenAddress,
        string from,
        string to,
        BigInteger amount,
        long nonce,
        long expiry
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("uint256", chainId),
            new ABIValue("address", tokenAddress),
            new ABIValue("address", from),
            new ABIValue("address", to),
            new ABIValue("uint256", amount),
            new ABIValue("uint256", nonce),
            new ABIValue("uint256", expiry)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetSetSettlementManagerMessage(
        string shopId,
        string managerId,
        long nonce,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "SetSettlementManager"),
            new ABIValue("bytes32", ConvertHexStringToByte(shopId)),
            new ABIValue("bytes32", ConvertHexStringToByte(managerId)),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }

    public static byte[] GetRemoveSettlementManagerMessage(
        string shopId,
        long nonce,
        long chainId
    )
    {
        var abiEncode = new ABIEncode();
        var encodedBytes = abiEncode.GetABIEncoded(
            new ABIValue("string", "RemoveSettlementManager"),
            new ABIValue("bytes32", ConvertHexStringToByte(shopId)),
            new ABIValue("bytes32", ConvertHexStringToByte("0x0000000000000000000000000000000000000000000000000000000000000000")),
            new ABIValue("uint256", chainId),
            new ABIValue("uint256", nonce)
        );
        var message = new byte[32];
        var digest = new KeccakDigest(256);
        digest.BlockUpdate(encodedBytes, 0, encodedBytes.Length);
        digest.DoFinal(message, 0);
        return message;
    }


    // endregion

    public static string SignMessage(EthECKey key, byte[] message)
    {
        var signer = new EthereumMessageSigner();
        return signer.Sign(message, key);
    }

    private static int _purchaseId = 1000;
    private static readonly Random Rand = new();

    public static string GetSamplePurchaseId()
    {
        var res = "P" + Convert.ToString(_purchaseId).PadLeft(4, '0') +
                  Convert.ToString(Rand.Next() % 100000000).PadLeft(8, '0');
        _purchaseId++;
        return res;
    }
}
