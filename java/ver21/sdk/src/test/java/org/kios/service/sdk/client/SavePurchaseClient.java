package org.kios.service.sdk.client;

import org.kios.service.sdk.data.NetWorkType;
import org.kios.service.sdk.data.UserBalance;
import org.kios.service.sdk.data.purchase.PurchaseDetail;
import org.kios.service.sdk.data.purchase.ResponseSavePurchase;
import org.kios.service.sdk.utils.Amount;
import org.kios.service.sdk.utils.CommonUtils;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SavePurchaseClientTest {
    @Test
    void SavePurchase() {

        // 포인트를 사용하여 구매를 하기 위해 필요한 키 네트워크 별로 가지고 있어야 한다
        // 메인넷의 키는 담당자에게 직접요청하여야 함
        //---------------------------------------------------------------------------------------
        Map<NetWorkType, String> keysOfCollector = new HashMap<>();
        keysOfCollector.put(NetWorkType.kios_testnet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee");
        keysOfCollector.put(NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        //---------------------------------------------------------------------------------------

        // 포인트를 자산을 소유한 주소
        //---------------------------------------------------------------------------------------
        Map<NetWorkType, String> addressOfAsset = new HashMap<>();
        addressOfAsset.put(NetWorkType.acc_testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714");
        addressOfAsset.put(NetWorkType.acc_mainnet, "0xCB2e8ebBF4013164161d7F2297be25d4A9dC6b17");
        addressOfAsset.put(NetWorkType.kios_testnet, "0x153f2340807370855092D04E0e0abe4f2b634240");
        addressOfAsset.put(NetWorkType.kios_mainnet, "0xf077c9CfFa387E35de72b68448ceD5382CbC5D7D");
        //---------------------------------------------------------------------------------------

        //---------------------------------------------------------------------------------------
        // 키오스크의 상점 아이디
        String shopId = "0x0003be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874";

        // 테스트를 할 네트워크, 상점아이디에 따른 네트워크를 선택한다
        NetWorkType network = CommonUtils.getNetWorkType(shopId);

        // 키오스크에 표시되었던 환률 심벌
        String currency = CommonUtils.getDefaultCurrencySymbol(network);

        // 구매 아이디
        String purchaseId = CommonUtils.getSamplePurchaseId();

        // 구매 발생 timestamp
        long timestamp = CommonUtils.getTimeStamp();

        // 사용자앱에서 키오스크로 전달받은 지갑주소 (처음에는 임시주소이나, 서버에 정보요청 후 정상주소로 변환해야 함)
        // 입력되지 않았다면 ""
        String userAccount = "0x64D111eA9763c93a003cef491941A011B8df5a49";

        // 사용자가 전화번호를 입력했을 때 사용되며, 입력되지 않았다면 ""
        String userPhone = "";

        // 전체 결제 금액
        String totalAmount = "10000";

        // 포인트 사용금액
        String cacheAmount = "10000";

        // 포인트 지급까지의 대기시간, 단위는 초이다.
        // 0이면 블록생성과 기타 작업등으로 인해 테스트넷은 10초내외 메인넷은 30초 정도 후에 포인트가 제공된다.
        long waiting = 0;
        //---------------------------------------------------------------------------------------


        // 구매데이터를 전송하는 클라이언트를 생성한다
        //---------------------------------------------------------------------------------------
        SavePurchaseClient savePurchaseClient = new SavePurchaseClient(network, keysOfCollector.get(network), addressOfAsset.get(network));
        //---------------------------------------------------------------------------------------

        try {
            // 초기 잔고를 화인한다. 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Check Balance ]");
            UserBalance balance1 = savePurchaseClient.getBalanceAccount(userAccount);
            System.out.printf("  - Balance: %s\n", new Amount(balance1.point.balance).toAmountString());

            // 신규결제에 대한 구매데이터 전송입니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Save New Purchase ]");
            ResponseSavePurchase res1 = savePurchaseClient.saveNewPurchase(
                    purchaseId,
                    timestamp,
                    waiting,
                    totalAmount,
                    cacheAmount,
                    currency,
                    shopId,
                    userAccount,
                    userPhone,
                    new PurchaseDetail[]{new PurchaseDetail("2020051310000000", "10000", 10)}
            );
            System.out.printf("  - type: %d, sequence: %s, purchaseId: %s\n", res1.type, res1.sequence, res1.purchaseId);

            // 구매정보가 전송된 후 포인트가 지급될 때 까지 대기합니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting for providing... ]");
            long t1 = CommonUtils.getTimeStamp();
            while (true) {
                UserBalance balance2 = savePurchaseClient.getBalanceAccount(userAccount);
                if (balance2.point.balance.equals(balance1.point.balance.add(Amount.make("1000").getValue()))) {
                    break;
                } else if (CommonUtils.getTimeStamp() - t1 > 120) {
                    System.out.println("Time out for providing... ");
                    break;
                }
                Thread.sleep(1000);
            }
            //---------------------------------------------------------------------------------------

            // 이전 잔고와 비교하여 증가된것을 확인 할 수 있습니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Check Balance ]");
            UserBalance balance3 = savePurchaseClient.getBalanceAccount(userAccount);
            assertEquals(balance3.point.balance, balance1.point.balance.add(Amount.make("1000").getValue()));
            System.out.printf("  - Balance: %s\n", new Amount(balance3.point.balance).toAmountString());
            //---------------------------------------------------------------------------------------
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

    @Test
    void SaveCancelPurchase() {

        // 포인트를 사용하여 구매를 하기 위해 필요한 키 네트워크 별로 가지고 있어야 한다
        // 메인넷의 키는 담당자에게 직접요청하여야 함
        //---------------------------------------------------------------------------------------
        Map<NetWorkType, String> keysOfCollector = new HashMap<>();
        keysOfCollector.put(NetWorkType.kios_testnet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee");
        keysOfCollector.put(NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
        //---------------------------------------------------------------------------------------

        // 포인트를 자산을 소유한 주소
        //---------------------------------------------------------------------------------------
        Map<NetWorkType, String> addressOfAsset = new HashMap<>();
        addressOfAsset.put(NetWorkType.acc_testnet, "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714");
        addressOfAsset.put(NetWorkType.acc_mainnet, "0xCB2e8ebBF4013164161d7F2297be25d4A9dC6b17");
        addressOfAsset.put(NetWorkType.kios_testnet, "0x153f2340807370855092D04E0e0abe4f2b634240");
        addressOfAsset.put(NetWorkType.kios_mainnet, "0xf077c9CfFa387E35de72b68448ceD5382CbC5D7D");
        //---------------------------------------------------------------------------------------

        //---------------------------------------------------------------------------------------
        // 키오스크의 상점 아이디
        String shopId = "0x0003be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874";

        // 테스트를 할 네트워크, 상점아이디에 따른 네트워크를 선택한다
        NetWorkType network = CommonUtils.getNetWorkType(shopId);

        // 키오스크에 표시되었던 환률 심벌
        String currency = CommonUtils.getDefaultCurrencySymbol(network);

        // 구매 아이디
        String purchaseId = CommonUtils.getSamplePurchaseId();

        // 구매 발생 timestamp
        long timestamp = CommonUtils.getTimeStamp();

        // 사용자앱에서 키오스크로 전달받은 지갑주소 (처음에는 임시주소이나, 서버에 정보요청 후 정상주소로 변환해야 함)
        // 입력되지 않았다면 ""
        String userAccount = "0x64D111eA9763c93a003cef491941A011B8df5a49";

        // 사용자가 전화번호를 입력했을 때 사용되며, 입력되지 않았다면 ""
        String userPhone = "";

        // 전체 결제 금액
        String totalAmount = "10000";

        // 포인트 사용금액
        String cacheAmount = "10000";
        //---------------------------------------------------------------------------------------


        // 구매데이터를 전송하는 클라이언트를 생성한다
        //---------------------------------------------------------------------------------------
        SavePurchaseClient savePurchaseClient = new SavePurchaseClient(network, keysOfCollector.get(network), addressOfAsset.get(network));
        //---------------------------------------------------------------------------------------


        try {
            // 초기 잔고를 화인한다.
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Check Balance ]");
            UserBalance balance1 = savePurchaseClient.getBalanceAccount(userAccount);
            System.out.printf("  - Balance: %s\n", new Amount(balance1.point.balance).toAmountString());
            //---------------------------------------------------------------------------------------

            // 신규결제에 대한 구매데이터 전송입니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Save New Purchase and Cancel ]");
            ResponseSavePurchase res2 = savePurchaseClient.saveNewPurchase(
                    purchaseId,
                    timestamp,
                    60,
                    totalAmount,
                    cacheAmount,
                    currency,
                    shopId,
                    userAccount,
                    userPhone,
                    new PurchaseDetail[]{new PurchaseDetail("2020051310000000", "10000", 10)}
            );
            //---------------------------------------------------------------------------------------
            System.out.printf("  - type: %d, sequence: %s, purchaseId: %s\n", res2.type, res2.sequence, res2.purchaseId);

            // 잠시 대기 합니다.
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);
            //---------------------------------------------------------------------------------------

            // 취소결제에 대한 데이터 전송입니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Save Cancel ]");
            ResponseSavePurchase res3 = savePurchaseClient.saveCancelPurchase(purchaseId, timestamp, 0);
            System.out.printf("  - type: %d, sequence: %s, purchaseId: %s\n", res3.type, res3.sequence, res3.purchaseId);
            //---------------------------------------------------------------------------------------

            // 잠시 대기 합니다.
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(50000);
            //---------------------------------------------------------------------------------------

            // 이전 잔고와 비교하여 변경되지 않은 것을 확인 할 수 있습니다.
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Check Balance ]");
            UserBalance balance2 = savePurchaseClient.getBalanceAccount(userAccount);
            assertEquals(balance2.point.balance, balance1.point.balance);
            System.out.printf("  - Balance: %s\n", new Amount(balance2.point.balance).toAmountString());
            //---------------------------------------------------------------------------------------

        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }
}
