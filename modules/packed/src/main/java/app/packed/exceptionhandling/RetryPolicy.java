package app.packed.exceptionhandling;

@FunctionalInterface
public interface RetryPolicy {

    boolean retry(RetryableFailureContext t);

}
