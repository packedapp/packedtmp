package app.packed.container;

// used by, uses...
public interface ExtensionMirror {

    /** {@return a descriptor for the extension that is being modeled.} */
    default ExtensionDescriptor descriptor() {
        return ExtensionDescriptor.of(type());
    }

    /** {@return the type of extension being modeled.} */
    Class<? extends Extension> type();
}
