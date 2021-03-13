package app.packed.inject;

public enum ServiceType {

    /** A service that is available because a hook of some kind provided it. */
    HOOK,
    
    /** A service that is available to all components in the same container. */
    CONTAINER;
}
