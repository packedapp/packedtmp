package app.packed.application;

//Or stereotype
public enum ApplicationFamily {

    Doo,

    //
    // Bootstrap a single other Application after initialization (via an entry point)
    BOOTSTRAPPER,

    // Has Runtime
    // Has result, possible Void
    // Control: Process - Start - Cancel -
    JOB,

    // Has Runtime
    // May be Restartable, Suspendable
    // No result
    // No entry 
    // Control: Process - start/stop
    DAEMON;
}
