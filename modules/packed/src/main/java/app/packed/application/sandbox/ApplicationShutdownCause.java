package app.packed.application.sandbox;

// Ved ikke om vi kan dele den mellem bean og application. 
// Er det i virkeligheden noget med lifetime at goere
// Er det jer i virkeligheden en event???

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
    UPGRADING; // Redeploy???  Implies restarting

    public boolean isFailed() {
        return this == EXECUTION_FAILED || this == CANCELLED;
    }
}
// 

enum NextAction {
    NOTHING,

    RESTART;
}