package app.packed.extension;

import app.packed.component.ComponentMirror;

/**
 * A mirror of an {@link Extensor} (component).
 */
// Se nu bliver det svaert fordi en extensor vel ikke noedvendigvis er en component???
// eller er den...  IDK

// Maaske er extensors ikke components..... IDK
public interface ExtensorMirror extends ComponentMirror {

    /** {@return the extension that installed the extensor.} */
    ExtensionMirror<?> extension();

    /** {@return the type (class) of the extensor.} */
    Class<?> extensorType();

    ExtensorScope scope();

    /// Hooks
}
// Taenker det skal vaere muligt at faa en application
// Extensor processing order