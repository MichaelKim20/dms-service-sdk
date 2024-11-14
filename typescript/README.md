# acc-service-sdk for Typescript

## 1) Installation

Use [yarn](https://yarnpkg.com/) to install acc-service-sdk.

```bash
yarn add acc-service-sdk
```

---

## 2) Features

A standard development kit provided for interworking with a decentralized loyalty point system.
This SDK can be used in the following places.

1. It can be used when implementing the function of delivering purchase information paid by a KIOSK or POS.
2. It can be used when implementing the ability to purchase products using loyalty points.
3. It can be used when implementing a method in which partners deposit tokens and then provide points to users.

---

## 3) How to save purchase data

See [API Docs - https://save.test.acccoin.io/docs/](https://save.test.acccoin.io/docs/)  
See Test Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/typescript/tests/SavePurchaseClient.test.ts

This is a function used by partners that support the payment system.  
The test net of the loyalty system is ready.  
You can proceed with the development using the test net and switch to the main net at the time the development is completed.  
Please create a wallet to be used for this feature, and forward the address of the wallet to the operations team of the loyalty system.  
The private key of the wallet, which can be used on testnet, is "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276".  
Please make the wallet of the main net yourself and deliver only its address to the operation team.  
The system adds purchase information received from a trusted partner to the block. Validators verify this. For verified
data, the system gives the buyer a percentage of the purchase amount as points.

### 3.1) Create Client Module

```typescript
// The private key of wallet
const privateKeyOfCollector = "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276";
// The address of wallet, This is the address where token assets are stored
const addressOfAsset = "0x85EeBb1289c0d0C17eFCbadB40AeF0a1c3b46714";
// Create a client
const savePurchaseClient = new SavePurchaseClient(NetWorkType.testnet, privateKeyOfCollector, addressOfAsset);
```

### 3.2) Save New Purchase Data

```typescript
const purchaseId = "P100000000000000";
const timestamp = BigInt(new Date().getTime() / 1000);
await savePurchaseClient.saveNewPurchase(
    purchaseId,
    timestamp,
    0n,
    10_000,
    10_000,
    "php",
    shopId,
    userAccount,
    userPhone,
    [
        {
            productId: "2020051310000000",
            amount: 10_000,
            providePercent: 10,
        },
    ]
);
```

### 3.3) Save Cancel Purchase Data

```typescript
await savePurchaseClient.saveCancelPurchase(purchaseId, timestamp, 60n);
```

---

## 4) How to use loyalty points

See [API Docs - https://relay.test.acccoin.io/docs/](https://relay.test.acccoin.io/docs/#/Payment)  
See Test Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/typescript/tests/PaymentClient.test.ts

This is a necessary function to build a point payment system.  
Please create a wallet to be used for payment, and forward the address of the wallet to the operations team of the loyalty system.  
The private key of the wallet, which can be used on testnet, is "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276".  
Please make the wallet of the main net yourself and deliver only its address to the operation team.

### 4.1) Create Client for Payment

```typescript
// This is the private key of the wallet to be used for payment.
const privateKeyForPayment = "0x8acceea5937a8e4bb07abc93a1374264dd9bd2fc384c979717936efe63367276";
const paymentClient = new PaymentClient(NetWorkType.testnet, privateKeyForPayment);
```

### 4.2) Implement Event Listener

```typescript
class PaymentEventListener implements ITaskEventListener {
    public onNewPaymentEvent(type: string, code: number, message: string, sequence: bigint, data: IPaymentTaskItem) {
        console.log(`type: ${type.toString()}`);
        console.log(`code: ${code.toString()}`);
        console.log(`message: ${message}`);
        console.log(`sequence: ${sequence.toString()}`);
        console.log(`data: ${JSON.stringify(data)}`);
    }
    public onNewShopEvent(type: string, code: number, message: string, sequence: bigint, data: IShopTaskItem) {
        console.log(`type: ${type.toString()}`);
        console.log(`code: ${code.toString()}`);
        console.log(`message: ${message}`);
        console.log(`sequence: ${sequence.toString()}`);
        console.log(`data: ${JSON.stringify(data)}`);
    }
}
```

### 4.3) Create Event Collector

```typescript
const eventCollector = new TaskEventCollector(paymentClient, new PaymentEventListener());
```

### 4.4) Start Event Collector

```typescript
await eventCollector.start();
```

### 4.5) Open New Payment

```typescript
const purchaseId = "P1000000000000";
const paymentItem = await paymentClient.openNewPayment(
    purchaseId,
    temporaryAccount,
    BOACoin.make(1000).value,
    "php",
    shopClient.getShopId(),
    terminalID
);
```

### 4.6) Close New Payment

```typescript
const res = await paymentClient.closeNewPayment(paymentItem.paymentId, true);
```

### 4.7) Open Cancel Payment

```typescript
const res = await paymentClient.openCancelPayment(paymentItem.paymentId, terminalID);
```

### 4.8) Close Cancel Payment

```typescript
const res = await paymentClient.closeCancelPayment(paymentItem.paymentId, true);
```

### 4.9) Stop Event Collector

```typescript
await eventCollector.stop();
```

---

## 5) How to provide loyalty points

See [API Docs - https://relay.test.acccoin.io/docs/](https://relay.test.acccoin.io/docs/#/Loyalty%20Point%20Provider)  
See Test Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/typescript/tests/ProviderClient.test.ts

This is the functionality you need to provide points.  
You first need to deposit more than 100,000 tokens through the app.  
And you have to ask the system operation team for a partner.  
You must register first in your app.  
And if you register the agent's address, you don't have to provide the private key of the wallet with the assets.~~

### 5.1) Create Client for provide without agent

If the agent is not used, the private key of the provider in which the asset is stored should be provided to the development team

```typescript
const providerClient = new ProviderClient(
    NetWorkType.testnet,
    "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c" // address: 0x64D111eA9763c93a003cef491941A011B8df5a49
);
```

### 5.2) Provide to wallet address without agent

```typescript
const receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
const amount = BOACoin.make(100).value;
await providerClient.provideToAddress(providerClient.address, receiver, amount);
```

### 5.3) Provide to phone number hash without agent

```typescript
const phoneNumber = "+82 10-9000-5000";
const amount = BOACoin.make(100).value;
await providerClient.provideToPhone(providerClient.address, phoneNumber, amount);
```

### 5.4) Create Client for provide with agent

With agent, you only need to provide the address of the provider to the development team.

```typescript
const providerAddress = "0x64D111eA9763c93a003cef491941A011B8df5a49";
const agentClient = new ProviderClient(
    NetWorkType.testnet,
    "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab" // address: 0x3FE8D00143bd0eAd2397D48ba0E31E5E1268dBfb
);
```

### 5.5) Provide to wallet address with agent

```typescript
const providerAddress = "0x64D111eA9763c93a003cef491941A011B8df5a49";
const receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
const amount = BOACoin.make(100).value;
await agentClient.provideToAddress(providerAddress, receiver, amount);
```

### 5.6) Provide to phone number hash with agent

```typescript
const providerAddress = "0x64D111eA9763c93a003cef491941A011B8df5a49";
const phoneNumber = "+82 10-9000-5000";
const amount = BOACoin.make(100).value;
await agentClient.provideToPhone(prviderAddress, phoneNumber, amount);
```

## 6) How to settlement of shops

See [API Docs - https://relay.test.acccoin.io/docs/](https://relay.test.acccoin.io/docs/#/Shop)  
See Sample Code https://github.com/acc-coin/acc-service-sdk/blob/v0.x.x/typescript/tests/SettlementClient.test.ts

The shop that acts as an agent for the settlement of shops is the settlement-shop.  
This SDK provides the features you need for this settlement-shop.  
First, the settlement-shop needs to secure the store ID by installing the shop app.  
And register the address of the settlement agent and withdrawal agent on the app.  
The wallet's private key of the settlement agent is managed by the development team,
and the wallet's private key of the withdrawal agent is managed by the accounting team.  
Owners of settlement-shop can set up these two addresses.

### 6.1) Create Client for settlement-shop

```typescript
var ownerPrivateKey = "0xd72fb7fe49fd18f92481cbee186050816631391b4a25d579b7cff7efdf7099d3";
var managerShopId = "0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86";
var settlementClientForManager = new SettlementClient(network, ownerPrivateKey, managerShopId);
```

### 6.2) Create Client for refund agent
This agent accumulates the settlement of all registered shops into the settlement of the settlement-shop, and exchanges the settlement for tokens.

```typescript
var refundAgentPrivateKey = "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c";
var managerShopId = "0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86";
var refundAgent = new SettlementClient(network, refundAgentPrivateKey, managerShopId);
```

### 6.3) Create Client for withdrawal agent
This agent is authorized to perform the function of withdrawing tokens to the main chain.

```typescript
var withdrawalAgentPrivateKey = "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab";
var managerShopId = "0x000108bde9ef98803841f22e8bc577a69fc47913914a8f5fa60e016aaa74bc86";
var withdrawalAgent = new SettlementClient(network, withdrawalAgentPrivateKey, managerShopId);
```

### 6.4) Register the refund agent
This can only be registered by the owner of the settlement-shop.

```typescript
await settlementClientForManager.setAgentOfRefund(refundAgent.address);
```

### 6.5) Register the withdrawal agent
This can only be registered by the owner of the settlement-shop.

```typescript
await settlementClientForManager.setAgentOfWithdrawal(withdrawalAgent.address);
```

### 6.6) Collect Settlement Amount
You have to get the number of stores first, and if the number of stores is too high, you have to do it several times.  
The maximum number of stores that can be processed at once is 10.

```typescript
var count = await refundAgent.getSettlementClientLength();
var clients = await refundAgent.getSettlementClientList(0, count);
await refundAgent.collectSettlementAmountMultiClient(clients);
```

### 6.7) Refund Settlement Amount
Exchange the settlement amount into tokens.

```typescript
var refundableData = await settlementClient.getRefundable();
await refundAgent.refund(refundableData.refundableAmount);
```

### 6.8) Withdrawal token
Withdraw tokens to the main chain.

```typescript
var accountOfShop = await settlementClient.getAccountOfShopOwner();
var res2 = await settlementClient.getBalanceAccount(accountOfShop);
var balanceOfToken = res2.token.balance;
await withdrawalAgent.withdraw(balanceOfToken);
```

### 6.9) Transfer of tokens
Owners of settlement-shop can transfer tokens withdrawn to the main chain from the app to other addresses
