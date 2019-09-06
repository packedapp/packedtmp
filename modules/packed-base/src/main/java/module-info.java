module app.packed.base {
    exports app.packed.app;
    exports app.packed.artifact;
    exports app.packed.component;
    exports app.packed.contract;
    exports app.packed.config;
    exports app.packed.container;
    exports app.packed.container.extension;
    exports app.packed.container.extension.feature;
    exports app.packed.errorhandling;
    exports app.packed.host;
    exports app.packed.inject;
    exports app.packed.lifecycle;
    exports app.packed.reflect;
    exports app.packed.util;

    // uses app.packed.util.ModuleEnv;
    // provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;
}