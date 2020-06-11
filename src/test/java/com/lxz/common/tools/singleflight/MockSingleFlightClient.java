package com.lxz.common.tools.singleflight;

import java.util.function.Function;

public class MockSingleFlightClient {
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
            return "haha" + key;
        });

        System.out.println(res);
        return res;
    }
}
