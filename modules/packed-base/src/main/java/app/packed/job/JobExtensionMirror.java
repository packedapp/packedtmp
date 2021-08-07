package app.packed.job;

import app.packed.extension.ExtensionMirror;

public class JobExtensionMirror extends ExtensionMirror<JobExtension> {

    // giver jo kun mening for Application
    public Class<?> resultType() {
        return Object.class;
    }
}
