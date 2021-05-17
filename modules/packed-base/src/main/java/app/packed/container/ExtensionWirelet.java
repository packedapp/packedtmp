package app.packed.container;

import app.packed.component.Wirelet;

/**
 * Extensions that define their own wirelets must do so by extending this class (or a subclass hereof).
 * <p>
 * Extension wirelets must be defined in the same module as the extension itself. Failure to do so will result in an
 * {@link InternalExtensionException} being thrown at runtime.
 */
public abstract class ExtensionWirelet<E extends Extension> extends Wirelet {

    // Invoked by the runtime.. Whenever

    // Man skal naesten have onWireletsWired() callback saa
    // Skal invokeres efter extensionen er blevet initialiseret, men foer
    // onInitialize()
    protected void onBuild(E extension) {}

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

class ExtensionRuntimeWirelet<E extends Extension, R extends ExtensionRuntime<E>> extends ExtensionWirelet<E> {
    protected void onInitialize(E extension) {} // maa det vaere
}
