package app.packed.lifecycle;

// Ved ikke om vi kan dele den mellem bean og application. 
public enum ApplicationShutdownCause {

    /** The application is shutting down normally */
    NORMAL,

    // Some
    EXECUTION_FAILED,  // Would like to know if are restarting, when o

    PERSISTING,

    SPAWN, // We are spawning a new instance of Application somewhere else. Serialize what you need to

    // Was Cancelled
    CANCELLED, // Should never be

    // The application is being upgraded
    UPGRADING; // Redeploy???  I think it will always restart

    public boolean isFailed() {
        return this == EXECUTION_FAILED || this == CANCELLED;
    }
}

enum NextAction {
    NOTHING,

    RESTART;
}