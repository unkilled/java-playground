package com.lxz.common.tools.singleflight;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.*;

public class SingleFlightTest {

    @Test
    public void testSingleFlightNoShare() {
        int concurrency = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        List<CompletableFuture<SingleFlightResult<String>>> futures = new ArrayList<>();

        // 构造key的逻辑，每个num一个key，没有任何共享
        Function<Integer, String> keyBuilder = String::valueOf;

        // 启动N个线程，模拟高并发
        for (int i = 0; i < concurrency; i++) {
            int finalInt = i;
            CompletableFuture<SingleFlightResult<String>> c = CompletableFuture.supplyAsync(() ->
                    MockSingleFlightClient.runWithSingleFlight(finalInt, keyBuilder), pool);
            futures.add(c);
        }

        // 等所有线程跑完
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

        Map<String, SingleFlightResult<String>> results = futures
                .stream()
                .map(CompletableFuture::join)
                .collect(toMap(SingleFlightResult::getResult, Function.identity()));

        assertEquals(concurrency, results.size());

        for (int i = 0; i < concurrency; i++) {
            String key = "haha" + i;
            assertTrue(results.containsKey(key));
            assertFalse(results.get(key).isShared());
        }
    }

    @Test
    public void testSingleFlightShareMod5() {
        int concurrency = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        List<CompletableFuture<SingleFlightResult<String>>> futures = new ArrayList<>();

        // 构造key的逻辑，对num取模，共享结果。
        Function<Integer, String> keyBuilder = e -> String.valueOf(e % 5);

        Map<Integer, String> resultMap = new ConcurrentHashMap<>(concurrency);

        // 启动N个线程，模拟高并发
        for (int i = 0; i < concurrency; i++) {
            int finalInt = i;
            CompletableFuture<SingleFlightResult<String>> c
                    = CompletableFuture.supplyAsync(() -> MockSingleFlightClient.runWithSingleFlight(finalInt, keyBuilder), pool);

            c.thenAccept(r -> resultMap.put(finalInt, r.getResult()));
            futures.add(c);
        }

        // 等所有线程跑完
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

        System.out.println(resultMap);
        assertEquals(concurrency, resultMap.size());

        for (int i = 0; i < concurrency; i++) {
            String str = resultMap.get(i);
            String strExample = resultMap.get(i % 5);

            assertNotNull(str);
            assertEquals(strExample, str); // 实际结果应该等于共享的结果
        }
    }

    @Test
    public void testSingleFlightMixedShare() {
        int concurrency = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        List<CompletableFuture<SingleFlightResult<String>>> futures = new ArrayList<>();

        // 构造key的逻辑，对num取模，共享结果。
        Function<Integer, String> keyBuilder = e -> {
            if (e % 100 == 0) {
                return String.valueOf(e);
            } else {
                return String.valueOf(e % 10);
            }
        };

        Map<Integer, String> resultMap = new ConcurrentHashMap<>(concurrency);

        // 启动N个线程，模拟高并发
        for (int i = 0; i < concurrency; i++) {
            int finalInt = i;
            CompletableFuture<SingleFlightResult<String>> c
                    = CompletableFuture.supplyAsync(() -> MockSingleFlightClient.runWithSingleFlight(finalInt, keyBuilder), pool);

            c.thenAccept(r -> resultMap.put(finalInt, r.getResult()));
            futures.add(c);
        }

        // 等所有线程跑完
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

        System.out.println(resultMap);
        assertEquals(concurrency, resultMap.size());

        for (int i = 0; i < concurrency; i++) {
            String str = resultMap.get(i);
            String strExample = resultMap.get(i % 10);

            assertNotNull(str);

            if (i % 100 == 0) {
                assertEquals("haha" + i, str);
            } else {
                assertEquals(strExample, str); // 实际结果应该等于共享的结果
            }
        }
    }

    @Test
    public void testSingleFlightException() {
        int concurrency = 100;
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);

        List<CompletableFuture<SingleFlightResult<String>>> futures = new ArrayList<>();

        // 构造key的逻辑，对num取模，共享结果。
        Function<Integer, String> keyBuilder = e -> String.valueOf(e % 10);

        Map<Integer, SingleFlightResult<String>> resultMap = new ConcurrentHashMap<>(concurrency);

        // 启动N个线程，模拟高并发
        for (int i = 0; i < concurrency; i++) {
            int finalInt = i;
            CompletableFuture<SingleFlightResult<String>> c
                    = CompletableFuture.supplyAsync(() -> MockSingleFlightClientException.runWithSingleFlight(finalInt, keyBuilder), pool);

            c.thenAccept(r -> resultMap.put(finalInt, r));
            futures.add(c);
        }

        // wait for all threads to finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();

        System.out.println(resultMap);
        assertEquals(concurrency, resultMap.size());

        for (int i = 0; i < concurrency; i++) {
            SingleFlightResult<String> res = resultMap.get(i);
            SingleFlightResult<String> resExample = resultMap.get(i % 10);

            // the actual result should be equal to the shared result
            assertEquals(resExample.getException(), res.getException());
            assertEquals(resExample.getResult(), res.getResult());
        }
    }
}
