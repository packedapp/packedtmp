package app.packed.bean;

import static java.util.Objects.requireNonNull;

import app.packed.extension.ExtensionMirror;

public final class BeanExtensionMirror extends ExtensionMirror<BeanExtension> {

    /** The service manager */
    private final BeanExtension extension; // ved ikke om vi skal have en <E> extension() fra ExtensionMirror?

    BeanExtensionMirror(BeanExtension extension) {
        this.extension = requireNonNull(extension);
    }

    public int foo() {
        return extension.extension.hashCode();
    }
}
