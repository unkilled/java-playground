package test.java.com.lxz.common.tools.singleflight;

import main.java.com.lxz.common.tools.singleflight.SingleFlight;
import main.java.com.lxz.common.tools.singleflight.SingleFlightResult;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class MockSingleFlightClientException {
    private final static SingleFlight<String> group = SingleFlight.newSingleFlightGroup();

    static SingleFlightResult<String> runWithSingleFlight(
            int num, Function<Integer, String> keyBuilder) {
        String key = keyBuilder.apply(num);

        SingleFlightResult<String> res = group.run(key, () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // throw an exception randomly
            if (ThreadLocalRandom.current().nextInt() % 2 == 0) {
                throw new RuntimeException("some exp");
            }

            return "haha" + key;
        });

        System.out.println(res);
        return res;
    }
}
