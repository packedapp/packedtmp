package app.packed.container;

import app.packed.component.Wirelet;

// I think it is fair to say than an extension can only have its own extension wirelets injected...

// HMM, syntes vi skal have Wirelet'en. Men ved ikke omkring onBuild();
public abstract class ExtensionWirelet<E extends Extension> extends Wirelet {

    // Invoked by the runtime.. Whenever
    
    // Man skal naesten have onWireletsWired() callback saa 
    // Skal invokeres efter extensionen er blevet initialiseret, men foer
    // onInitialize()
    protected void onBuild(E extension) {}

    // Kommer an paa om vi skal have en intern model vi kan spoerge paa istedet for
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

    // Unless otherwise configured... An extension Wirelet

    
    // Metoden kan extendes med den
    protected @interface onRuntime {
        boolean noOnBuildInvoke() default false;
    }
}

class LaunchableExtensionWirelet<E extends Extension, R extends RuntimeExtension<E>> extends ExtensionWirelet<E> {
    protected void onInitialize(E extension) {} // maa det vaere
}

class UserWirelet extends Wirelet {

}