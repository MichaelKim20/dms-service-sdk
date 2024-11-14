# acc-service-sdk for JAVA

---

## 1) Installation

### 1.1) Install gradle

See) https://gradle.org/install

### 1.2) Publish to MavenLocal

```shell
./gradlew clean publishToMavenLocal
```
You can verify that the library is distributed in the storage `~/.m2/repository`.

### 1.3) How to use SDK in your project

Please set the contents of the file `build.gradle` as below

``` 
repositories {
    mavenLocal()
}

dependencies {
    implementation "org.json:json:20231013"
    implementation "com.googlecode.libphonenumber:libphonenumber:8.13.44"
    implementation "org.web3j:core:4.12.1"
    implementation 'org.acc.service.sdk:core:1.0.0-SNAPSHOT'
}
```

---

## 2) Features

A standard development kit provided for interworking with a decentralized loyalty point system.
This SDK can be used in the following places.
1. It can be used when implementing the function of delivering purchase information paid by a KIOSK or POS.
2. It can be used when implementing the ability to purchase products using loyalty points.
3. It can be used when implementing a method in which partners deposit tokens and then provide points to users.
4. Provides information on shops registered with the settlement manager and the ability to withdraw settlement money into tokens.

---

## 3) How to save purchase data

See [API Docs - https://save.test.acccoin.io/docs/](https://save.test.acccoin.io/docs/)  
See Sample Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/java/sample/src/main/java/org/example/Main.java


This is a function used by partners that support the payment system.  
The test net of the loyalty system is ready.  
You can proceed with the development using the test net and switch to the main net at the time the development is completed.  
Please create a wallet to be used for this feature, and forward the address of the wallet to the operations team of the loyalty system.  
The private key of the wallet, which can be used on testnet, is "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276".  
Please make the wallet of the main net yourself and deliver only its address to the operation team.  
The system adds purchase information received from a trusted partner to the block. Validators verify this. For verified data, the system gives the buyer a percentage of the purchase amount as points.

### 3.1) Create Client Module

```java
String privateKeyOfCollector = "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276";
String addressOfAsset = "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714";
SavePurchaseClient savePurchaseClient = new SavePurchaseClient(
    NetWorkType.testnet,
    privateKeyOfCollector,
    addressOfAsset
);
String shopId = "0x0001be96d74202df38fd21462ffcef10dfe0fcbd7caa3947689a3903e8b6b874";
String userAccount = "0x64D111eA9763c93a003cef491941A011B8df5a49";
String userPhone = "";
```

### 3.2) Save New Purchase Data

```java
ResponseSavePurchase res1 = savePurchaseClient.saveNewPurchase(
    CommonUtils.getSamplePurchaseId(),
    CommonUtils.getTimeStamp(),
    0,
    "10000",
    "10000",
    "php",
    shopId,
    userAccount,
    "",
    new PurchaseDetail[]{new PurchaseDetail("2020051310000000", "10000", 10)}
);
```

### 3.3) Save Cancel Purchase Data
```java
ResponseSavePurchase res3 = savePurchaseClient.saveCancelPurchase(purchaseId, timestamp, 0);
```

---

## 4) How to use loyalty points

See [API Docs - https://relay.test.acccoin.io/docs/](https://relay.test.acccoin.io/docs/#/Payment)  
See Sample Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/java/sample/src/main/java/org/example/Main.java

This is a necessary function to build a point payment system.  
Please create a wallet to be used for payment, and forward the address of the wallet to the operations team of the loyalty system.  
The private key of the wallet, which can be used on testnet, is "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276".  
Please make the wallet of the main net yourself and deliver only its address to the operation team.  

### 4.1) Create Client for Payment

```java
// This is the private key of the wallet to be used for payment.
String privateKeyForPayment = "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276";
PaymentClient client = new PaymentClient(NetWorkType.testnet, privateKeyForPayment);
```

### 4.2) Implement Event Listener

```java
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
```

### 4.3) Create Event Collector

```java
TaskEventListener listener = new TaskEventListener();
TaskEventCollector collector = new TaskEventCollector(client, listener);
```

### 4.4) Start Event Collector
```java
collector.start();
```

### 4.5) Open New Payment
```java
PaymentTaskItem paymentItem = client.openNewPayment(
    CommonUtils.getSamplePurchaseId(),
    temporaryAccount,
    Amount.make("1_000").getValue(),
    "php",
    shopClient.getShopId(),
    terminalID
);

```

### 4.6) Close New Payment
```java
PaymentTaskItem paymentItem = client.closeNewPayment(paymentItem.paymentId, true);
```

### 4.6) Open Cancel Payment
```java
PaymentTaskItem paymentItem = client.openCancelPayment(paymentItem.paymentId, terminalID);
```

### 4.7) Close Cancel Payment
```java
PaymentTaskItem paymentItem = client.closeCancelPayment(paymentItem.paymentId, true);
```

### 4.8) Stop Event Collector
```java
collector.Stop();
```

---

## 5) How to provide loyalty points

See [API Docs - https://relay.test.acccoin.io/docs/](https://relay.test.acccoin.io/docs/#/Loyalty%20Point%20Provider)  
See Test Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/java/sdk/src/test/java/org/acc/service/sdk/client/ProviderClient.java

This is the functionality you need to provide points.  
You first need to deposit more than 100,000 tokens through the app.  
And you have to ask the system operation team for a partner.  
You must register first in your app.  
And if you register the agent's address, you don't have to provide the private key of the wallet with the assets.~~

### 5.1) Create Client for provide without agent

If the agent is not used, the private key of the provider in which the asset is stored should be provided to the development team

```java
ProviderClient providerClient = new ProviderClient(NetWorkType.testnet,
    "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
```

### 5.2) Provide to wallet address without agent

```java
String receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
BigInteger amount = Amount.make("100").getValue();
String txHash = providerClient.provideToAddress(providerClient.getAddress(), receiver, amount);
```

### 5.3) Provide to phone number hash without agent

```java
String phoneNumber = "+82 10-9000-5000";
BigInteger amount = Amount.make("100").getValue();
String txHash = providerClient.ProvideToPhone(providerClient.getAddress(), phoneNumber, amount);
```


### 5.4) Create Client for provide with agent

With agent, you only need to provide the address of the provider to the development team.

```java
String providerAddress = "0x64D111eA9763c93a003cef491941A011B8df5a49";
ProviderClient agentClient = new ProviderClient(NetWorkType.testnet, "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab");
```

### 5.5) Provide to wallet address with agent

```java
String providerAddress = "0x64D111eA9763c93a003cef491941A011B8df5a49";
String receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
BigInteger amount = Amount.make("100").getValue();
String txHash = agentClient.provideToAddress(providerAddress, receiver, amount);
```

### 5.6) Provide to phone number hash with agent

```java
String providerAddress = "0x64D111eA9763c93a003cef491941A011B8df5a49";
String phoneNumber = "+82 10-9000-5000";
BigInteger amount = Amount.make("100").getValue();
String txHash = agentClient.ProvideToPhone(providerAddress, phoneNumber, amount);
```

## 6) How to settlement of shops

See [API Docs - https://relay.test.acccoin.io/docs/](https://relay.test.acccoin.io/docs/#/Shop)  
See Test Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/java/org/acc/service/sdk/client/SettlementClientUsingAgentTest.java  

The shop that acts as an agent for the settlement of shops is the settlement-shop.  
This SDK provides the features you need for this settlement-shop.  
First, the settlement-shop needs to secure the store ID by installing the shop app.  
And register the address of the settlement agent and withdrawal agent on the app.  
The wallet's private key of the settlement agent is managed by the development team,
and the wallet's private key of the withdrawal agent is managed by the accounting team.  
Owners of settlement-shop can set up these two addresses.

### 6.1) Create Client for settlement-shop

```java
var ownerPrivateKey = "0xd72fb7fe49fd18f92481cbee186050816631391b4a25d579b7cff7efdf7099d3";
var managerShopId = "0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86";
var settlementClientForManager = new SettlementClient(network, ownerPrivateKey, managerShopId);
```

### 6.2) Create Client for refund agent
This agent accumulates the settlement of all registered shops into the settlement of the settlement-shop, and exchanges the settlement for tokens.

```java
var refundAgentPrivateKey = "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c";
var managerShopId = "0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86";
var refundAgent = new SettlementClient(network, refundAgentPrivateKey, managerShopId);
```

### 6.3) Create Client for withdrawal agent
This agent is authorized to perform the function of withdrawing tokens to the main chain.

```java
var withdrawalAgentPrivateKey = "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab"
var managerShopId = "0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86"
var withdrawalAgent = new SettlementClient(network, withdrawalAgentPrivateKey, managerShopId);
```

### 6.4) Register the refund agent
This can only be registered by the owner of the settlement-shop.

```java
settlementClientForManager.setAgentOfRefund(refundAgent.getAddress());
```

### 6.5) Register the withdrawal agent
This can only be registered by the owner of the settlement-shop.

```java
settlementClientForManager.setAgentOfWithdrawal(withdrawalAgent.getAddress());
```

### 6.6) Collect Settlement Amount
You have to get the number of stores first, and if the number of stores is too high, you have to do it several times.  
The maximum number of stores that can be processed at once is 10.

```java
var count = refundAgent.getSettlementClientLength();
var clients = refundAgent.getSettlementClientList(0, count);
refundAgent.collectSettlementAmountMultiClient(clients);
```

### 6.7) Refund Settlement Amount
Exchange the settlement amount into tokens.

```java
var refundableData = settlementClient.getRefundable();
refundAgent.refund(refundableData.refundableAmount);
```

### 6.8) Withdrawal token
Withdraw tokens to the main chain.

```java
var accountOfShop = settlementClient.getAccountOfShopOwner();
var res = settlementClient.getBalanceAccount(accountOfShop);
var balanceOfToken = res.token.balance;
withdrawalAgent.withdraw(balanceOfToken);
```

### 6.9) Transfer of tokens
Owners of settlement-shop can transfer tokens withdrawn to the main chain from the app to other addresses
