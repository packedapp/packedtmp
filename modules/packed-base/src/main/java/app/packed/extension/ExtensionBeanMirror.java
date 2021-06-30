package app.packed.extension;

import app.packed.component.BeanMirror;
import app.packed.extension.sandbox.ExtensionBeanScope;

/**
 * A mirror of an {@link ExtensionBean} (component).
 */
// Se nu bliver det svaert fordi en extensor vel ikke noedvendigvis er en component???
// eller er den...  IDK

// Maaske er extensors ikke components..... IDK
public interface ExtensionBeanMirror extends BeanMirror {

    /** {@return the extension that installed the extensor.} */
    ExtensionMirror<?> extension();

    /** {@return the type (class) of the extensor.} */
    Class<?> extensorType();

    @SuppressWarnings("exports")
    ExtensionBeanScope scope();

    /// Hooks
}
// Taenker det skal vaere muligt at faa en application
// Extensor processing order