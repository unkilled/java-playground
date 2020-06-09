package main.java.com.lxz.common.tools.singleflight;

public class SingleFlightResult<T> {
    private final T result;
    private final boolean shared;

    public SingleFlightResult(T result, boolean shared) {
        this.result = result;
        this.shared = shared;
    }

    public T getResult() {
        return result;
    }

    public boolean isShared() {
        return shared;
    }

    @Override
    public String toString() {
        return "SingleFlightResult{" +
                "result=" + result +
                ", shared=" + shared +
                '}';
    }
}
