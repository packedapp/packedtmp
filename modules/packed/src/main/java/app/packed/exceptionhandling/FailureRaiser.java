package app.packed.exceptionhandling;

public interface FailureRaiser {
    void raiseFailure();
    
    void raiseFailureWithRetry();
}
