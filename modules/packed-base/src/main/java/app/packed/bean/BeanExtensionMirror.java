package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;

/**
 * A mirror for the {@link BeanExtension}.
 */
@ExtensionMember(BeanExtension.class)
public final class BeanExtensionMirror extends ExtensionMirror {

    /** The service manager */
    // ved ikke om vi skal have en <E> extension() fra ExtensionMirror?
    // Saa vi ikke behoever at gemme extensionen hver gang, men bare kan kalde
    // extension().
    // Det virker kun hvis vi dropper mirrors'ene paa runtime
    private final BeanExtension extension;

    BeanExtensionMirror(BeanExtension extension) {
        this.extension = requireNonNull(extension);
    }

    public int foo() {
        return extension.extension.hashCode();
    }
}
