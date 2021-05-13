package packed.internal.container;

import app.packed.component.Wirelet;
import app.packed.container.Extension;

// I think it is fair to say than an extension can only have its own extension wirelets injected...
public abstract class ExtensionWirelet<E extends Extension> extends Wirelet {
    
    public final Class<? extends Extension> extension() {
        // Store it in a ClassValue
        throw new UnsupportedOperationException();
    }
    
    

    // Ideen er man ikke kan angives paa rod niveau
    // Tror faktisk kun den giver mening for extension, og ikke user wirelets
    protected static final void $needsRealm() {
        // Wirelet.wireletRealm(Lookup); // <-- all subsequent wirelets
        // Wirelet.wireletRealm(Lookup, Wirelet... wirelets);

        // Tror det er vigtigt at der er forskel på REALM og BUILDTIME
        // Tror faktisk

        // f.x provide(Doo.class);
        // Hvad hvis vi koere composer.lookup()...
        // Saa laver vi jo saadan set en realm...
    }
}
