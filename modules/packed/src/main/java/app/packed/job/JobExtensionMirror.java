package app.packed.job;

import app.packed.extension.ExtensionMember;
import app.packed.extension.ExtensionMirror;

@ExtensionMember(JobExtension.class)
public class JobExtensionMirror extends ExtensionMirror {

    // giver jo kun mening for Application
    public Class<?> resultType() {
        return Object.class;
    }
}
