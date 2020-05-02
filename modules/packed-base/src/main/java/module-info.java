module app.packed.base {
    exports app.packed.analysis;
    exports app.packed.artifact;
    exports app.packed.base; // then have @Preview @DeprecatedWithReason
    exports app.packed.base.invoke;
    exports app.packed.base.reflect;
    exports app.packed.component;
    exports app.packed.config;
    exports app.packed.container;
    exports app.packed.hook;
    exports app.packed.inject;
    exports app.packed.lifecycle;
    exports app.packed.service;
    exports app.packed.sidecar;

    // Temporary...
    exports packed.internal.reflect to app.packed.banana;
    requires java.management;
}

// requires static org.graalvm.sdk;

// uses app.packed.util.ModuleEnv;
// provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;