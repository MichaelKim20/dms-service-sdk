package org.kios.service.sdk.client;

import org.junit.jupiter.api.Test;
import org.kios.service.sdk.data.NetWorkType;
import org.kios.service.sdk.data.payment.PaymentInfo;
import org.kios.service.sdk.data.payment.PaymentTaskItem;
import org.kios.service.sdk.data.payment.ShopTaskItem;
import org.kios.service.sdk.event.ITaskEventListener;
import org.kios.service.sdk.event.TaskEventCollector;
import org.kios.service.sdk.utils.Amount;
import org.kios.service.sdk.utils.CommonUtils;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentClientTest {
    @Test
    void PaymentClient() {
        try {
            // 포인트를 사용하여 구매를 하기 위해 필요한 키 네트워크 별로 가지고 있어야 한다
            // 메인넷의 키는 담당자에게 직접요청하여야 함
            //---------------------------------------------------------------------------------------
            Map<NetWorkType, String> keysOfPayment = new HashMap<>();
            keysOfPayment.put(NetWorkType.kios_testnet, "0xa0dcffca22f13363ab5d109f3a51ca99754cff4ce4c71dccc0c5df7f6492beee");
            keysOfPayment.put(NetWorkType.kios_mainnet, "0x0000000000000000000000000000000000000000000000000000000000000000");
            keysOfPayment.put(NetWorkType.acc_testnet, "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276");
            keysOfPayment.put(NetWorkType.acc_mainnet, "0x0000000000000000000000000000000000000000000000000000000000000000");
            //---------------------------------------------------------------------------------------

            // 사용자앱의 정보
            // 이것은 사용자 모바일앱을 대신해서 테스트 코드에서 신규 결제 승인을 하기 위해 필요한 것임
            // 키오스크 서버에는 구현할 필요 없음
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            // 사용자 앱의 지갑의 비밀키
            String userPrivateKey = "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c";
            //---------------------------------------------------------------------------------------

            // 상점앱의 정보
            // 이것은 사용자 모바일앱을 대신해서 테스트 코드에서 신규 결제 승인을 하기 위해 필요한 것임
            // 키오스크 서버에는 구현할 필요 없음
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            // 상정앱의 지갑의 비밀키
            String shopPrivateKey = "0xa237d68cbb66fd5f76e7b321156c46882546ad87d662dec8b82703ac31efbf0a";
            // 상정앱의 상점아이디
            String shopId = "0x0003be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874";
            //---------------------------------------------------------------------------------------


            // 테스트를 할 네트워크
            NetWorkType network = CommonUtils.getNetWorkType(shopId);

            // 키오스크의 고유번호 (옵션, ""로 처리되어도 괜찮음)
            String terminalID = "POS001";
            // 키오스크에 표시되었던 포인트 구매금액
            BigInteger amount = Amount.make("1_000").getValue();
            // 키오스크에 표시되었던 환률 심벌
            String currency = CommonUtils.getDefaultCurrencySymbol(network);


            // Create Client
            // 키오스크는 이 객체를 가지고 있어야 함
            //---------------------------------------------------------------------------------------
            System.out.println("[ Create Client ]");
            PaymentClient client = new PaymentClient(network, keysOfPayment.get(network));
            //---------------------------------------------------------------------------------------



            // Create Event Collector
            // 키오스크 또는 서버는 이 객체를 가지고 있어야 함
            //---------------------------------------------------------------------------------------
            System.out.println("[ Create Event Collector ]");
            TaskEventListener listener = new TaskEventListener();
            TaskEventCollector collector = new TaskEventCollector(client, listener);
            //---------------------------------------------------------------------------------------



            // Start Event Collector
            // 키오스크 또는 서버는 이 객체를 가지고 있어야 함
            //---------------------------------------------------------------------------------------
            collector.start();
            Thread.sleep(3000);  // 정상적으로 시작될 때 까지 대기 한다
            //---------------------------------------------------------------------------------------



            // Create User Client
            // 이것은 사용자 모바일앱을 대신해서 테스트 코드에서 신규 결제 승인을 하기 위해 필요한 것임
            // 키오스크 서버에는 구현할 필요 없음
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Create User Client ]");
            PaymentClientForUser userClient = new PaymentClientForUser(network, userPrivateKey);
            //---------------------------------------------------------------------------------------



            // Create Shop Client
            // 이것은 상점용 모바일앱을 대신해서 테스트 코드에서 취소 결제 승인을 하기 위해 필요한 것임
            // 키오스크 서버에는 구현할 필요 없음
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Create Shop Client ]");
            PaymentClientForShop shopClient = new PaymentClientForShop(network, shopPrivateKey, shopId);
            //---------------------------------------------------------------------------------------



            // Create Temporary Account
            // 사용자 모바일 앱에서 실행되는 내용임
            // 키오스크 서버에는 구현할 필요 없음
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Create Temporary Account ]");
            String temporaryAccount = userClient.getTemporaryAccount();
            System.out.printf("  - Temporary Account: %s\n", temporaryAccount);
            //---------------------------------------------------------------------------------------



            // Get Payment Info
            // 키오스크가 사용자앱에서 QR 코드를 입력받은 후 사용자의 포인트잔고를 확인하는 과정, 결제금액에 해당하는 포인트와 수수료등을 계산하여
            // 총사용량을 계산해준다.
            //---------------------------------------------------------------------------------------
            PaymentInfo info = client.getPaymentInfo(temporaryAccount, amount, currency);
            assertEquals(info.account.toLowerCase(), userClient.getAddress());
            //---------------------------------------------------------------------------------------



            // Open New Payment (키오스크 시스템에서 실행해야 한다)
            // 결제를 오픈한다.
            // 키오스크 서버에서 실행해야 한다
            //---------------------------------------------------------------------------------------
            System.out.println("[ Open New Payment ]");
            PaymentTaskItem paymentItem = client.openNewPayment(
                    CommonUtils.getSamplePurchaseId(),          // 구매아이디는 서버에서 생성한다.
                    temporaryAccount,                           // 사용자의 임시주소는 사용자용 앱의 QR 코드를 키오스크를 통해 전달한다.
                    amount,                                     // 키오스크에 표시되었던 포인트 구매금액
                    currency,                                   // 해당 상점의 키오스크 결제에 사용되는 환률심벌
                    shopClient.getShopId(),                     // 키오스크와 매핑된 상점 아이디
                    terminalID                                  // 키오스크의 고유번호
            );
            //---------------------------------------------------------------------------------------



            // Waiting...
            // 결제가 오픈 될 때까지 잠시 대기한다
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);
            //---------------------------------------------------------------------------------------



            // Approval New Payment (사용자 앱에서 실행되기 때문 실제에는 필요없음)
            // 실제는 사용자가 푸쉬메세지를 받고 사용자용 앱에서 승인을 하나,
            // 이 코드에서는 사용자앱이 없기 때문에 테스트를 위해 수동을 승인한다
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Approval New Payment ]");
            userClient.approveNewPayment(
                    paymentItem.paymentId,
                    paymentItem.purchaseId,
                    paymentItem.amount,
                    paymentItem.currency,
                    paymentItem.shopId,
                    true
            );
            //---------------------------------------------------------------------------------------



            // Waiting...
            // 결제가 승인 완료 될 때까지 잠시 대기한다
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);
            //---------------------------------------------------------------------------------------



            // Close New Payment (키오스크 시스템에서 실행해야 한다)
            // 결제를 닫는다
            //---------------------------------------------------------------------------------------
            System.out.println("[ Close New Payment ]");
            client.closeNewPayment(paymentItem.paymentId, true);
            //---------------------------------------------------------------------------------------



            // Waiting...
            // 직전 프로세스가 완료되기 까지 대기
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);
            //---------------------------------------------------------------------------------------



            // Open Cancel Payment (키오스크 시스템에서 실행해야 한다)
            // 결제를 취소를 오픈한다
            // 키오스크 서버에서 실행해야 한다
            //---------------------------------------------------------------------------------------
            System.out.println("[ Open Cancel Payment ]");
            client.openCancelPayment(paymentItem.paymentId, terminalID);
            //---------------------------------------------------------------------------------------



            // Waiting...
            // 직전 프로세스가 완료되기 까지 대기 (테스트를 위한 용도)
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);
            //---------------------------------------------------------------------------------------



            // Approval Cancel Payment
            // 실제는 상점주가 푸쉬메세지를 받고 상점용 앱에서 승인을 하나,
            // 이 코드에서는 상점용 앱이 없기 때문에 테스트를 위해 수동을 승인한다
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Approval Cancel Payment ]");
            shopClient.approveCancelPayment(
                    paymentItem.paymentId,
                    paymentItem.purchaseId,
                    true
            );
            //---------------------------------------------------------------------------------------



            // Waiting...
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            //---------------------------------------------------------------------------------------
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);
            //---------------------------------------------------------------------------------------




            // Close Cancel Payment (키오스크 시스템에서 실행해야 한다)
            // 결제를 취소를 닫는다
            // 키오스크 서버에서 실행해야 한다
            //---------------------------------------------------------------------------------------
            System.out.println("[ Close Cancel Payment ]");
            client.closeCancelPayment(paymentItem.paymentId, true);
            //---------------------------------------------------------------------------------------


            PaymentTaskItem paymentTaskItem = client.getPaymentItem(paymentItem.paymentId);
            assertEquals(paymentTaskItem.paymentId, paymentItem.paymentId);

            // Waiting...
            // 테스트 목적입니다. 실제코드에는 제거해도 됩니다.
            System.out.println("[ Waiting... ]");
            Thread.sleep(3000);

            // Stop Event Collector
            collector.stop();

        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }
}

class TaskEventListener implements ITaskEventListener {
    public void onNewPaymentEvent(
            String type,
            int code,
            String message,
            long sequence,
            PaymentTaskItem paymentTaskItem
    ) {
        System.out.printf("  -> onNewPaymentEvent %s - %d - %s - %d\n", type, code, message, sequence);
    }

    public void onNewShopEvent(
            String type,
            int code,
            String message,
            long sequence,
            ShopTaskItem shopTaskItem
    ) {
        System.out.printf("  -> onNewShopEvent %s - %d - %s - %d\n", type, code, message, sequence);
    }
}
