package app.packed.thread;

import static java.util.Objects.requireNonNull;

import app.packed.extension.ExtensionSupport;
import app.packed.extension.ExtensionSupportContext;

public class ThreadExtensionSupport extends ExtensionSupport {
    
    final ThreadExtension extension;
    
    final ExtensionSupportContext context;

    ThreadExtensionSupport(ThreadExtension extension, ExtensionSupportContext context) {
        this.extension = requireNonNull(extension);
        this.context = context;
    }
}
