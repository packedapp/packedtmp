package app.packed.application;

// Den er taet knyttet til
// stateless always have INSTANTIATE

// RunState???

// The launch mode of an application...

// Kunne ogsaa rigtig godt kunne taenke mig at

// Den her child application = Delayed | Som parent...

// Parent Running, all children Starting

//Parent Running, all children running (Sync????)

// Delayed App = Altsaa det er lidt pause... 

// Altsaa 

public enum ApplicationLaunchMode {

    UNINITIALIZED, // Only child apps???
    
    /** Instantiates the application, but will not attempt to start it. */
    INITIALIZED,
    
    /** Instantiates the application and asynchronously starts the application. */
    STARTING,

    /** Instantiates the application and synchronously starts the application. */
    RUNNING,
    
    /** Runs the application to completion (Terminated) */
    COMPLETED;
}
