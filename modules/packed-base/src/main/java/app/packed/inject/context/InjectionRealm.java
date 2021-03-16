package app.packed.inject.context;

import static java.util.Objects.requireNonNull;

// Ideen er maaske lidt at lave en service loader paa denne

// Maaske er det bare en ikke dokumenteret annotering paa klassen...
// @InjectionRealm(bootstrap=...)
// public class Extension()
public abstract class InjectionRealm {
    final Class<?> realm;

    InjectionRealm(Class<?> realm) {
        this.realm = requireNonNull(realm);
    }
}
// Extension <-- stuff that can be injected into extensions...


// @AutoProvide(realm=Extension.class
// WireletHandle<?> provide(ExtensionSetup es)
