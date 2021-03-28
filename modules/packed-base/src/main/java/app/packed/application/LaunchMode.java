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

public enum LaunchMode {
    
    /** Instantiates the application, but does not start it. */
    INSTANTIATE,
    
    STARTING,
    
    RUNNING,
    
    /** Runs the application to completion (Shutdown or Termianted???) */
    COMPLETED;
}
