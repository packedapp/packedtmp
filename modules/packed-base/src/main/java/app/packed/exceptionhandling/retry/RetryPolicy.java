package app.packed.exceptionhandling.retry;

@FunctionalInterface
public interface RetryPolicy {

    boolean retry(RetryableFailureContext t);

}
