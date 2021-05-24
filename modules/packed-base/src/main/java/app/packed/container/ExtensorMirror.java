package app.packed.container;

import app.packed.component.ComponentMirror;

/**
 * A mirror of an {@link Extensor} (component).
 */
public interface ExtensorMirror extends ComponentMirror {

    /** {@return the extension that installed the extensor.} */
    ExtensionMirror<?> extension();

    /** {@return the type (class) of the extensor.} */
    Class<?> extensorType();
    
    /// Hooks
}
// Taenker det skal vaere muligt at faa en application
// Extensor processing order