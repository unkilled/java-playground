package com.lxz.common.tools.singleflight;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * SingleFlight
 *
 * @param <T>
 */
public class SingleFlight<T> {
    private final Map<String, Call<T>> callByKey;

    private SingleFlight() {
        // use ConcurrentHashMap for thread safety.
        // eager initialization is used.
        callByKey = new ConcurrentHashMap<>();
    }

    public static <T> SingleFlight<T> newSingleFlightGroup() {
        return new SingleFlight<>();
    }

    /**
     * @param key            how you would like to share the results.
     * @param resultSupplier your business logic
     * @return the result
     */
    public SingleFlightResult<T> run(String key, Supplier<T> resultSupplier) {
        // check if the 'key' is already being processed
        Call<T> c = callByKey.get(key);

        if (c == null) {
            Call<T> call = newCall();
            callByKey.put(key, call);
            doCall(call, key, resultSupplier);

            return new SingleFlightResult<>(call.result, call.e, call.dups.get() > 0);
        } else {
            // increase the dup count
            c.dups.incrementAndGet();

            // hold this thread and wait for the result
            try {
                c.countDownLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // return the shared results
            return new SingleFlightResult<>(c.result, c.e, true);
        }
    }

    private void doCall(Call<T> call, String key, Supplier<T> resultSupplier) {
        try {
            // run the business logic
            call.result = resultSupplier.get();
        } catch (Exception e) {
            // something unexpected happened. We have to share this exception as well.
            call.e = e;
        } finally {
            // release the latch so that the waiting threads can get the shared result
            call.countDownLatch.countDown();

            // remove the key from the map so that future requests will start a new call
            callByKey.remove(key);
        }
    }

    private Call<T> newCall() {
        return new Call<>();
    }

    // inner class for internal use only
    private static class Call<R> {
        // use AtomicInteger for thread safety
        private final AtomicInteger dups;

        // used for blocking other waiting threads
        private final CountDownLatch countDownLatch;

        // results from the business logic
        private R result;
        private Exception e;

        private Call() {
            dups = new AtomicInteger();
            countDownLatch = new CountDownLatch(1);
        }
    }
}
