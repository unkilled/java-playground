package main.java.com.lxz.common.tools.singleflight;

public class SingleFlightResult<T> {
    private final T result;
    private final Exception exception;
    private final boolean shared;

    public SingleFlightResult(T result, Exception exception, boolean shared) {
        this.result = result;
        this.exception = exception;
        this.shared = shared;
    }

    public T getResult() {
        return result;
    }

    public boolean isShared() {
        return shared;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "SingleFlightResult{" +
                "result=" + result +
                ", exception=" + exception +
                ", shared=" + shared +
                '}';
    }
}
