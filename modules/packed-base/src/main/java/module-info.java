module app.packed.base {
    exports app.packed.artifact;
    exports app.packed.base; // then have @Preview @DeprecatedWithReason
    exports app.packed.base.invoke;
    exports app.packed.introspection;
    exports app.packed.component;
    exports app.packed.config;
    exports app.packed.container;
    exports app.packed.hook;
    exports app.packed.inject;
    exports app.packed.lifecycleold;
    exports app.packed.service;
    exports app.packed.sidecar;

    // Temporary...
    exports packed.internal.reflect to app.packed.banana, app.packed.function;
    exports packed.internal.reflect.typevariable to app.packed.banana, app.packed.function;
    exports packed.internal.util to app.packed.configuration;

    exports packed.internal.component to app.packed.errorhandling;
    exports packed.internal.container to app.packed.errorhandling;
    exports packed.internal.hook to app.packed.errorhandling;
    exports packed.internal.artifact to app.packed.errorhandling;
    exports packed.internal.errorhandling to app.packed.errorhandling;

    exports packed.internal.hook.applicator to app.packed.errorhandling;

    requires java.management;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
// provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;