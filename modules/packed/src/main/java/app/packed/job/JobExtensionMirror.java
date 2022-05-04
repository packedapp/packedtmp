package app.packed.job;

import app.packed.container.ExtensionMirror;

/** A specialized extension mirror for the {@link JobExtension}. */
public final class JobExtensionMirror extends ExtensionMirror<JobExtension> {

    // giver jo kun mening for Application
    public Class<?> resultType() {
        return Object.class;
    }
}
