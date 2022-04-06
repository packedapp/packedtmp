package app.packed.bean.hooks.sandbox;

import app.packed.extension.Extension;

//WasExtensionMember

// Det gode ved den her er at vi kun loader dem, hvis klassen bliver brugt...
public @interface ExtensionSupportClass {

    // Need this to determind order of teardown
    Class<? extends Extension<?>> extension();

    Scope scope();

    public enum Scope {
        PLATFORM,

        NAMESPACE, // Er maaske mere Lifetime... IDK

        CONTAINER; // Taenker man har afgang til boernene...
    }
}
// @OnBuild

// @PlatformExt ensionSupport(ServiceExtension.class)
// @ContainerExtensionSupport