module app.packed.base {
    exports app.packed.app;
    exports app.packed.bundle;
    exports app.packed.config;
    exports app.packed.container;
    exports app.packed.contract;
    exports app.packed.hook;
    exports app.packed.inject;
    exports app.packed.lifecycle;
    exports app.packed.util;

    exports packed.internal.bundle to app.packed.base.devtools;
    exports packed.internal.inject to app.packed.base.devtools;
    exports packed.internal.inject.builder to app.packed.base.devtools;
    exports packed.internal.box to app.packed.base.devtools;
    exports packed.internal.inject.runtime to app.packed.base.devtools;
    exports packed.internal.annotations to app.packed.base.devtools;
    exports packed.internal.classscan to app.packed.base.devtools;
    exports packed.internal.util.descriptor to app.packed.base.devtools;
    exports packed.internal.config.site to app.packed.base.devtools;

    // uses app.packed.util.ModuleEnv;
    // provides app.packed.util.ModuleEnv with packed.internal.bundle.DefaultBS;
}