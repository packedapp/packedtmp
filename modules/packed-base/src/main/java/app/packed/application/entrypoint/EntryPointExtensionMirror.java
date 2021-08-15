package app.packed.application.entrypoint;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionMirror;

// Altsaa maaske har vi ikke et specifikt mirror, men det bare ligger paa
// ApplicationExtension
public class EntryPointExtensionMirror extends ExtensionMirror<EntryPointExtension> {

    public Class<? extends Extension> managedBy() {
        // Taenker EntryPoint som default, 
        throw new UnsupportedOperationException();
    }
}
