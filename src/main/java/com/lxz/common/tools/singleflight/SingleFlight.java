package main.java.com.lxz.common.tools.singleflight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class SingleFlight<T> {
    private final Map<String, Call<T>> callByKey;

    private SingleFlight() {
        callByKey = new ConcurrentHashMap<>();
    }

    public static <T> SingleFlight<T> newSingleFlightGroup() {
        return new SingleFlight<>();
    }

    public SingleFlightResult<T> run(String key, Supplier<T> resultSupplier) {
        Call<T> c = callByKey.get(key);
        if (c == null) {
            Call<T> call = newCall();
            callByKey.put(key, call);
            doCall(call, key, resultSupplier);

            return new SingleFlightResult<>(call.result, call.dups.get() > 0);
        } else {
            c.dups.incrementAndGet();
            try {
                c.countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new SingleFlightResult<>(c.result, true);
        }
    }

    private void doCall(Call<T> call, String key, Supplier<T> resultSupplier) {
        call.result = resultSupplier.get();
        call.countDownLatch.countDown();

        callByKey.remove(key);
    }

    private Call<T> newCall() {
        return new Call<>();
    }

    private static class Call<R> {
        private final AtomicInteger dups;
        private final CountDownLatch countDownLatch;
        private R result;

        private Call() {
            dups = new AtomicInteger();
            countDownLatch = new CountDownLatch(1);
        }
    }
}
