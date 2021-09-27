package app.packed.lifecycle;

// Running har aldrig modes...
public enum LifecycleHint {
    
    FAILED,

    TIMEOUT, // failed.reason = TimeoutException Something timed out. Always used together with Failed

    CANCELLED; // if failed.reason = CancellationException
}
