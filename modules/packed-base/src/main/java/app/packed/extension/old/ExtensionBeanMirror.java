package app.packed.extension.old;

import app.packed.component.BeanMirror;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror of an ExtensionBean (component).
 */
// Maaske er extensors ikke components..... IDK
public interface ExtensionBeanMirror extends BeanMirror {

    /** {@return the extension that installed the extensor.} */
    ExtensionMirror<?> extension();
}
// Taenker det skal vaere muligt at faa en application
// Extensor processing order