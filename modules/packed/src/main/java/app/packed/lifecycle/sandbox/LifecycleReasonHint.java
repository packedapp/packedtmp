package app.packed.lifecycle.sandbox;

// Running har aldrig modes...
public enum LifecycleReasonHint {
    
    FAILED,

    TIMEOUT, // failed.reason = TimeoutException Something timed out. Always used together with Failed

    CANCELLED; // if failed.reason = CancellationException
}
