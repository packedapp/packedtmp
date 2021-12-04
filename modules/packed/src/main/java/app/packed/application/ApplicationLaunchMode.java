package app.packed.application;

// Maybe ditch it and call it Application Usage Mode...
// Or ApplicationLaunchTarget
// Or LaunchTargetMode
public enum ApplicationLaunchMode {

    /** Returns an application that is in an initialized state. */
    // LifecycleThread = Calling thread either on image or driver
    // MainThread = Calling thread
    INITIALIZED,

    //
    STARTING,

    RUNNING,

    EXECUTE_UNTIL_SHUTDOWN,

    EXECUTE_UNTIL_TERMINATED
}
