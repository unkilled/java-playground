package com.lxz.bank.account;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankAccountTest {
    private BankAccount systemUnderTest;

    @Before
    public void setUp() {
        systemUnderTest = new BankAccount();
    }

    @Test
    public void testConcurrency() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        int concurrency = 200;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < concurrency; i++) {
            int amount = i;
            CompletableFuture<Void> f1 = CompletableFuture.runAsync(() -> systemUnderTest.deposit(amount), threadPool);
            CompletableFuture<Void> f2 = CompletableFuture.runAsync(() -> systemUnderTest.withdraw(amount), threadPool);
            CompletableFuture<Long> f3 = CompletableFuture.supplyAsync(() -> systemUnderTest.getBalanceOptimistically(), threadPool);

            f3.thenAccept(System.out::println);

            futures.add(f1);
            futures.add(f2);
        }

        // wait for all threads to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

        Assert.assertEquals(0, systemUnderTest.getBalance());
    }
}