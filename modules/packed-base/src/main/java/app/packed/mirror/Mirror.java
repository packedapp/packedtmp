package app.packed.mirror;

import app.packed.attribute.AttributedElement;

/**
 * Doing program analysis.
 * 
 * Models are typically not retained doing runtime. However
 * use wirelets. and then you can have them injected into components...
 * as any other service
 */

// http://bracha.org/mirrors.pdf

// It's fucking mirrors...
public interface Mirror extends AttributedElement {

    // Giver det ikke mening at have module paa alt...
    // Application.module = Assembly (or composer)
    // Container.module = the same
    // Component.module ????... Hvad hvis det er en 
    // Module module(); 
}

// Packed contains a mirror API for program analysis