package org.kios.service.sdk.client;

import org.kios.service.sdk.data.NetWorkType;
import org.kios.service.sdk.data.UserBalance;
import org.kios.service.sdk.utils.Amount;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.Address;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProviderClientTest {
    ProviderClient providerClient = new ProviderClient(NetWorkType.acc_testnet, "0x70438bc3ed02b5e4b76d496625cb7c06d6b7bf4362295b16fdfe91a046d4586c");
    ProviderClient agentClient = new ProviderClient(NetWorkType.acc_testnet, "0x44868157d6d3524beb64c6ae41ee6c879d03c19a357dadb038fefea30e23cbab");

    @Test
    void ProviderClient() {
        try {
            // isProvider
            {
                System.out.println("[ isProvider ]");
                boolean value = providerClient.isProvider(providerClient.getAddress());
                assertTrue(value);
            }

            // getBalanceAccount
            {
                System.out.println("[ getBalanceAccount ]");
                UserBalance value = providerClient.getBalanceAccount(providerClient.getAddress());
                assertTrue(value.point.balance.compareTo(BigInteger.ZERO) > 0);
                assertTrue(value.token.balance.compareTo(BigInteger.ZERO) > 0);
            }

            // getBalancePhone
            {
                System.out.println("[ getBalancePhone ]");
                UserBalance value = providerClient.getBalancePhone("+82 10-1000-2000");
                assertTrue(value.point.balance.compareTo(BigInteger.ZERO) > 0);
            }

            // ClearAgent
            {
                System.out.println("[ ClearAgent ]");
                providerClient.setAgent(Address.DEFAULT.toString());
                assertEquals(providerClient.getAgent(), Address.DEFAULT.toString());
            }

            // ProviderToAddress
            {
                System.out.println("[ ProviderToAddress ]");
                Boolean isProvider = providerClient.isProvider(providerClient.getAddress());
                if (isProvider) {
                    String receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
                    UserBalance res1 = providerClient.getBalanceAccount(receiver);
                    BigInteger oldBalance = res1.point.balance;

                    BigInteger amount = Amount.make("100").getValue();
                    providerClient.provideToAddress(providerClient.getAddress(), receiver, amount);
                    UserBalance res2 = providerClient.getBalanceAccount(receiver);

                    assertEquals(res2.point.balance, oldBalance.add(amount));
                }
            }

            // ProviderToPhone
            {
                System.out.println("[ ProviderToPhone ]");
                Boolean isProvider = providerClient.isProvider(providerClient.getAddress());
                if (isProvider) {
                    String phoneNumber = "+82 10-9000-5000";
                    UserBalance res1 = providerClient.getBalancePhone(phoneNumber);
                    BigInteger oldBalance = res1.point.balance;

                    BigInteger amount = Amount.make("100").getValue();
                    providerClient.provideToPhone(providerClient.getAddress(), phoneNumber, amount);
                    UserBalance res2 = providerClient.getBalancePhone(phoneNumber);

                    assertEquals(res2.point.balance, oldBalance.add(amount));
                }
            }

            // SetAgent
            {
                System.out.println("[ SetAgent ]");
                providerClient.setAgent(agentClient.getAddress());
                assertEquals(providerClient.getAgent().toLowerCase(), agentClient.getAddress().toLowerCase());
            }


            // ProviderToAddressByAgent
            {
                System.out.println("[ ProviderToAddressByAgent ]");
                Boolean isProvider = providerClient.isProvider(providerClient.getAddress());
                if (isProvider) {
                    String receiver = "0xB6f69F0e9e70034ba0578C542476cC13eF739269";
                    UserBalance res1 = providerClient.getBalanceAccount(receiver);
                    BigInteger oldBalance = res1.point.balance;

                    BigInteger amount = Amount.make("100").getValue();
                    agentClient.provideToAddress(providerClient.getAddress(), receiver, amount);
                    UserBalance res2 = providerClient.getBalanceAccount(receiver);

                    assertEquals(res2.point.balance, oldBalance.add(amount));
                }
            }

            // ProviderToPhoneByAgent
            {
                System.out.println("[ ProviderToPhoneByAgent ]");
                Boolean isProvider = providerClient.isProvider(providerClient.getAddress());
                if (isProvider) {
                    String phoneNumber = "+82 10-9000-5000";
                    UserBalance res1 = providerClient.getBalancePhone(phoneNumber);
                    BigInteger oldBalance = res1.point.balance;

                    BigInteger amount = Amount.make("100").getValue();
                    agentClient.provideToPhone(providerClient.getAddress(), phoneNumber, amount);
                    UserBalance res2 = providerClient.getBalancePhone(phoneNumber);

                    assertEquals(res2.point.balance, oldBalance.add(amount));
                }
            }
        } catch (Exception e) {
            assertEquals("some exception message...", e.getMessage());
        }
    }

}
