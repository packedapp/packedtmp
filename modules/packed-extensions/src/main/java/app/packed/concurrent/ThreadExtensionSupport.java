package app.packed.concurrent;

import static java.util.Objects.requireNonNull;

import app.packed.extension.ExtensionSupport;

public class ThreadExtensionSupport extends ExtensionSupport {
    final ThreadExtension extension;

    ThreadExtensionSupport(ThreadExtension extension) {
        this.extension = requireNonNull(extension);
    }
}
