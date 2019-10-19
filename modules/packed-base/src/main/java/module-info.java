module app.packed.base {
    exports app.packed.artifact;
    exports app.packed.component;
    exports app.packed.contract; // Maybe API, and then have @Preview @DeprecatedWithReason
    exports app.packed.config;
    exports app.packed.container;
    exports app.packed.component.feature;
    exports app.packed.errorhandling;
    exports app.packed.hook;
    exports app.packed.service;
    exports app.packed.lifecycle;
    exports app.packed.lang.reflect;
    exports app.packed.lang;

    // uses app.packed.util.ModuleEnv;
    // provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;
}