package app.packed.time;

import app.packed.container.Extension;
import app.packed.inject.Factory;

// Tror den er separat fra Time extension

// Schedule of class/factory/runnable

// schedule(Runnable).atFixedRate();

// Det der ikke er muligt er.
// Single instance, programmatically set timer...
// Schedule prototype();

// Control af existerende tasks...

// Altsaa maaske skal man bruge 

/**
 * An extension that deals with schedulation of tasks.
 */
public class SchedulerExtension extends Extension {
    SchedulerExtension() {}
    // Creates a new instance on every invocation

    // Must have either an runnable or a single @Schedule method
    
    // If the class implements Runnable -> run with be invoked
    // Otherwise will look for exactly 1 @Schedule annotation
    public ScheduledComponentConfiguration schedule(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    // Unlike install this will create a new instance every time
    public ScheduledComponentConfiguration schedule(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    // Will spawn a new fcking component on every xxx
    public ScheduledComponentConfiguration schedulePrototype(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public ScheduledComponentConfiguration schedule(Runnable runnable) {
        throw new UnsupportedOperationException();
    }

    // How do we control
    /// Threads
    /// Retry policies
    /// Error handling

    // We most likely want to have a
    // standard container (namespace inherited way).
    // And then an explicit way...

    // Altsaa hvad hvis vi reler paa defaulten...
    // Og ikke vil have den overskrives af foraeldren...
}
// ScheduledComponentConfiguration
// Was (below). Men giver god mening at splitte den op i 2...

//
//public ComponentConfiguration scheduleAtFixedRate(Class<?> clazz, long initialDelay, long period, TimeUnit unit) {
//
//  throw new UnsupportedOperationException();
//}
//
//public ComponentConfiguration scheduleAtFixedRate(Class<?> clazz, Duration duration) {
//  throw new UnsupportedOperationException();
//}
//
//public ComponentConfiguration scheduleWithFixedDelay(Class<?> clazz, long duration, TimeUnit unit) {
//  throw new UnsupportedOperationException();
//}
//
//public ComponentConfiguration scheduleWithFixedDelay(Class<?> clazz, Duration duration) {
//  throw new UnsupportedOperationException();
//}