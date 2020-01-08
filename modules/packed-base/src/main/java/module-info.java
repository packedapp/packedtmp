module app.packed.base {
    exports app.packed.api; // then have @Preview @DeprecatedWithReason
    exports app.packed.artifact;
    exports app.packed.component;
    exports app.packed.component.feature;
    exports app.packed.container;
    exports app.packed.config;
    exports app.packed.errorhandling;
    exports app.packed.lang;
    exports app.packed.hook;
    exports app.packed.lang.reflect;
    exports app.packed.lang.invoke;
    exports app.packed.lifecycle;
    exports app.packed.service;

    // requires static org.graalvm.sdk;

    // Temporary...
    exports packed.internal.reflect to app.packed.banana;
    // uses app.packed.util.ModuleEnv;
    // provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;
}