package app.packed.thread;

import static java.util.Objects.requireNonNull;

import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPointContext;

public class ThreadExtensionPoint extends ExtensionPoint<ThreadExtension> {
    
    final ThreadExtension extension;
    
    final ExtensionPointContext context;

    ThreadExtensionPoint(ThreadExtension extension, ExtensionPointContext context) {
        this.extension = requireNonNull(extension);
        this.context = context;
    }
}
