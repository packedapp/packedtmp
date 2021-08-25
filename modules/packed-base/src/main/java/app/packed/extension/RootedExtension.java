package app.packed.extension;

/**
 *
 */
public abstract class RootedExtension extends Extension {

    /** Creates a new extension. Subclasses should have a single package-protected constructor. */
    protected RootedExtension() {}
}
// The primary reason for making this a subclass is to signal to users that the specific extension must be rooted.
// We could have made it a single configuration switch, but this is clearer

// Den kan ogsaa bruges til at signalere hov, er du sikker på det her er rigtigt.
// Fx kunne ConverterExtension jo godt vaere RootedExtension. Har svaert ved at se man vil have flere roots i en application.

// Use cases
//