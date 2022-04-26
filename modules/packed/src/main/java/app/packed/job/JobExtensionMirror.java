package app.packed.job;

import app.packed.container.ExtensionMirror;

public class JobExtensionMirror extends ExtensionMirror<JobExtension> {

    // giver jo kun mening for Application
    public Class<?> resultType() {
        return Object.class;
    }
}
