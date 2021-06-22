package app.packed.job;

import app.packed.extension.ExtensionMirror;

public class JobExtensionMirror extends ExtensionMirror<JobExtension> {

    public Class<?> resultType() {
        return Object.class;
    }
}
